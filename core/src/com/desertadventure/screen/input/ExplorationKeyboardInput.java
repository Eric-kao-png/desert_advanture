package com.desertadventure.screen.input;

import com.badlogic.gdx.Gdx;
import com.desertadventure.DesertAdventure;
import com.desertadventure.config.GameInputBindings;
import com.desertadventure.screen.MainMenuScreen;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

/** Exploration hotkeys: map, inventory, return to main menu. */
public final class ExplorationKeyboardInput {
    public void handle(DesertAdventure game, GameSession session, GameplayMode mode) {
        if (Gdx.input.isKeyJustPressed(GameInputBindings.MAP) && mode.canOpenMap()) {
            session.openMapOverlay();
        }
        if (Gdx.input.isKeyJustPressed(GameInputBindings.INVENTORY) && mode.canOpenCharacter()) {
            session.openCharacterOverlay();
        }
        if (Gdx.input.isKeyJustPressed(GameInputBindings.BACK)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }
}
