package com.desertadventure.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.desertadventure.DesertAdventure;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.view.MapOverlayInput;
import com.desertadventure.map.view.MapOverlayLayout;
import com.desertadventure.presentation.GameViewport;
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.M) && mode.canOpenMap()) {
            session.openMapOverlay();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && mode.canOpenCharacter()) {
            session.openCharacterOverlay();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    private void handleCharacterOverlay() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            characterInput.clearSelection();
            session.closeCharacterOverlay();
            return;
        }
        updateCharacterHover();
        if (!Gdx.input.justTouched()) {
            return;
        }
        float worldX = viewport.toWorldX(Gdx.input.getX(), Gdx.input.getY());
        float worldY = viewport.toWorldY(Gdx.input.getX(), Gdx.input.getY());

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

        int slot = characterLayout.slotAt(worldX, worldY);
        if (slot >= 0) {
            if (!session.getInventory().isSlotEmpty(slot)) {
                characterInput.selectSlot(slot);
            } else {
                characterInput.clearSelection();
            }
            return;
        }

        if (characterInput.hasSelection()) {
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
        float worldX = viewport.toWorldX(Gdx.input.getX(), Gdx.input.getY());
        float worldY = viewport.toWorldY(Gdx.input.getX(), Gdx.input.getY());
        characterInput.updateHover(characterLayout, worldX, worldY);
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
        float worldX = viewport.toWorldX(Gdx.input.getX(), Gdx.input.getY());
        float worldY = viewport.toWorldY(Gdx.input.getX(), Gdx.input.getY());
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            session.setMode(GameplayMode.EXPLORE_IDLE);
            return;
        }
        panMapView();
        updateMapHover();
        if (Gdx.input.justTouched() && mapInput.getHoveredGridPos() != null) {
            session.trySelectDestination(mapInput.getHoveredGridPos());
        }
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
