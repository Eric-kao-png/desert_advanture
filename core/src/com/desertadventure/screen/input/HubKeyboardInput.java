package com.desertadventure.screen.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.desertadventure.DesertAdventure;
import com.desertadventure.config.GameInputBindings;
import com.desertadventure.screen.MainMenuScreen;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.HubPanel;

/** Hub menu hotkeys: start battle, deck, back. */
public final class HubKeyboardInput {
    public void handle(DesertAdventure game, GameSession session) {
        HubPanel panel = session.getHubPanel();
        if (panel != HubPanel.MAIN) {
            if (Gdx.input.isKeyJustPressed(GameInputBindings.BACK)
                    || Gdx.input.isKeyJustPressed(Input.Keys.B)) {
                session.returnHubToMainPanel();
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
            session.tryStartCurrentStageCombat();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
            session.setHubPanel(HubPanel.DECK);
        }
        if (Gdx.input.isKeyJustPressed(GameInputBindings.BACK)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }
}
