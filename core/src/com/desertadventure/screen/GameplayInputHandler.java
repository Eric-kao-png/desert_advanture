package com.desertadventure.screen;

import com.desertadventure.DesertAdventure;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.view.MapOverlayInput;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.screen.input.CharacterOverlayPointerInput;
import com.desertadventure.screen.input.CombatKeyboardInput;
import com.desertadventure.screen.input.ExplorationKeyboardInput;
import com.desertadventure.screen.input.MapOverlayPointerInput;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

/** Keyboard and pointer input for gameplay modes. */
public class GameplayInputHandler {
    private final DesertAdventure game;
    private final GameSession session;
    private final CharacterOverlayInput characterInput;
    private final CharacterOverlayLayout characterLayout;
    private final ExplorationKeyboardInput explorationKeys = new ExplorationKeyboardInput();
    private final CombatKeyboardInput combatKeys = new CombatKeyboardInput();
    private final MapOverlayPointerInput mapOverlayInput;
    private final CharacterOverlayPointerInput characterOverlayInput;

    public GameplayInputHandler(
            DesertAdventure game,
            GameSession session,
            GameViewport viewport,
            MapOverlayInput mapInput,
            CharacterOverlayInput characterInput,
            CharacterOverlayLayout characterLayout) {
        this.game = game;
        this.session = session;
        this.characterInput = characterInput;
        this.characterLayout = characterLayout;
        mapOverlayInput = new MapOverlayPointerInput(session, viewport, mapInput);
        characterOverlayInput = new CharacterOverlayPointerInput(session, viewport, characterInput, characterLayout);
    }

    public void handle() {
        GameplayMode mode = session.getMode();

        if (mode == GameplayMode.MAP_OVERLAY) {
            mapOverlayInput.handle();
            return;
        }

        if (mode == GameplayMode.CHARACTER_OVERLAY) {
            characterOverlayInput.handle();
            return;
        }

        characterOverlayInput.resetPointerTracking();
        explorationKeys.handle(game, session, mode);
    }

    public void updateCharacterHover() {
        characterOverlayInput.updateHover();
    }

    public CharacterOverlayInput getCharacterInput() {
        return characterInput;
    }

    public CharacterOverlayLayout getCharacterLayout() {
        return characterLayout;
    }

    public void updateMapHover() {
        mapOverlayInput.updateHover();
    }

    public GridPos getHoveredGridPos() {
        return mapOverlayInput.hoveredCell();
    }

    public void updateCombatInput(float delta) {
        combatKeys.handle(session, delta);
    }

    public boolean isMapDismissHovered() {
        return mapOverlayInput.isDismissHovered();
    }

    public boolean isMapDismissPressed() {
        return mapOverlayInput.isDismissPressed();
    }

    public boolean isCharacterDismissHovered() {
        return characterOverlayInput.isDismissHovered();
    }

    public boolean isCharacterDismissPressed() {
        return characterOverlayInput.isDismissPressed();
    }
}
