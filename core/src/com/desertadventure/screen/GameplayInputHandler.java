package com.desertadventure.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.desertadventure.DesertAdventure;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.view.MapOverlayInput;
import com.desertadventure.map.view.MapOverlayLayout;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.presentation.OverlayCloseButton;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

/** Keyboard and pointer input for gameplay modes. */
public class GameplayInputHandler {
    private final DesertAdventure game;
    private final GameSession session;
    private final GameViewport viewport;
    private final MapOverlayInput mapInput;
    private final CharacterOverlayInput characterInput;
    private final CharacterOverlayLayout characterLayout;
    private boolean characterPointerWasDown;

    public GameplayInputHandler(
            DesertAdventure game,
            GameSession session,
            GameViewport viewport,
            MapOverlayInput mapInput,
            CharacterOverlayInput characterInput,
            CharacterOverlayLayout characterLayout) {
        this.game = game;
        this.session = session;
        this.viewport = viewport;
        this.mapInput = mapInput;
        this.characterInput = characterInput;
        this.characterLayout = characterLayout;
    }

    public void handle() {
        GameplayMode mode = session.getMode();

        if (mode == GameplayMode.MAP_OVERLAY) {
            handleMapOverlay();
            return;
        }

        if (mode == GameplayMode.CHARACTER_OVERLAY) {
            handleCharacterOverlay();
            return;
        }

        characterPointerWasDown = false;

        if (Gdx.input.isKeyJustPressed(Input.Keys.M) && mode.canOpenMap()) {
            session.openMapOverlay();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.N) && mode.canOpenCharacter()) {
            session.openCharacterOverlay();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    private void handleCharacterOverlay() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            characterInput.clearSelection();
            characterInput.resetPointer();
            session.closeCharacterOverlay();
            return;
        }

        float worldX = viewport.pointerWorldX();
        float worldY = viewport.pointerWorldY();
        boolean pointerDown = Gdx.input.isTouched();

        updateCharacterHover();

        if (pointerDown) {
            if (Gdx.input.justTouched()) {
                handleCharacterTouchDown(worldX, worldY);
            } else {
                characterInput.onTouchDrag(worldX, worldY);
            }
        } else if (characterPointerWasDown) {
            handleCharacterTouchUp(worldX, worldY);
        }

        characterPointerWasDown = pointerDown;
    }

    private void handleCharacterTouchDown(float worldX, float worldY) {
        if (OverlayCloseButton.contains(worldX, worldY)) {
            characterInput.clearSelection();
            characterInput.resetPointer();
            session.closeCharacterOverlay();
            return;
        }
        characterInput.resetPointer();
        if (characterInput.hasSelection()) {
            if (characterLayout.closeButtonContains(worldX, worldY)) {
                characterInput.clearSelection();
                return;
            }
            if (characterLayout.useButtonContains(worldX, worldY)) {
                int slot = characterInput.getSelectedSlot();
                session.tryUseInventoryItemAtSlot(slot);
                if (isSlotEmpty(slot)) {
                    characterInput.clearSelection();
                }
                return;
            }
            if (characterLayout.detailContains(worldX, worldY)) {
                return;
            }
        }
        characterInput.onTouchDown(worldX, worldY, characterLayout, session.getInventory());
    }

    private void handleCharacterTouchUp(float worldX, float worldY) {
        if (characterInput.isDragging()) {
            characterInput.onTouchUp(worldX, worldY, characterLayout, session.getInventory());
            return;
        }

        int tapSlot = characterInput.onTouchUp(worldX, worldY, characterLayout, session.getInventory());
        if (tapSlot >= 0) {
            if (!session.getInventory().isSlotEmpty(tapSlot)) {
                characterInput.selectSlot(tapSlot);
            } else {
                characterInput.clearSelection();
            }
            return;
        }

        if (characterInput.hasSelection() && !characterLayout.detailContains(worldX, worldY)) {
            characterInput.clearSelection();
        }
    }

    private boolean isSlotEmpty(int slot) {
        return session.getInventory().isSlotEmpty(slot);
    }

    public void updateCharacterHover() {
        if (session.getMode() != GameplayMode.CHARACTER_OVERLAY) {
            return;
        }
        characterInput.updateHover(characterLayout, viewport.pointerWorldX(), viewport.pointerWorldY());
    }

    public CharacterOverlayInput getCharacterInput() {
        return characterInput;
    }

    public CharacterOverlayLayout getCharacterLayout() {
        return characterLayout;
    }

    public void updateMapHover() {
        if (session.getMode() != GameplayMode.MAP_OVERLAY) {
            return;
        }
        float worldX = viewport.pointerWorldX();
        float worldY = viewport.pointerWorldY();
        MapOverlayLayout layout = session.createMapOverlayLayout();
        mapInput.updateHover(layout, worldX, worldY);
    }

    public GridPos getHoveredGridPos() {
        return mapInput.getHoveredGridPos();
    }

    public void updateCombatInput(float delta) {
        if (!session.getMode().isCombat()) {
            return;
        }
        float move = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            move -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            move += 1f;
        }
        session.getCombatController().update(delta, move);
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            session.getCombatController().tryBasicAttack();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            session.getCombatController().trySkill();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            session.getCombatController().tryUltimate();
        }
    }

    private void handleMapOverlay() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            session.closeMapOverlay();
            return;
        }
        float worldX = viewport.pointerWorldX();
        float worldY = viewport.pointerWorldY();
        panMapView();
        updateMapHover();
        if (Gdx.input.justTouched()) {
            if (OverlayCloseButton.contains(worldX, worldY)) {
                session.closeMapOverlay();
                return;
            }
            if (mapInput.getHoveredGridPos() != null) {
                session.trySelectDestination(mapInput.getHoveredGridPos());
            }
        }
    }

    public boolean isMapDismissHovered() {
        return mapInput.isHoveredDismiss();
    }

    public boolean isMapDismissPressed() {
        return Gdx.input.isTouched() && mapInput.isHoveredDismiss();
    }

    public boolean isCharacterDismissHovered() {
        return characterInput.isHoveredOverlayDismiss();
    }

    public boolean isCharacterDismissPressed() {
        return Gdx.input.isTouched() && characterInput.isHoveredOverlayDismiss();
    }

    private void panMapView() {
        int step = GameConfig.MAP_PAN_STEP;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            session.panMapView(-step, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            session.panMapView(step, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            session.panMapView(0, step);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            session.panMapView(0, -step);
        }
    }
}
