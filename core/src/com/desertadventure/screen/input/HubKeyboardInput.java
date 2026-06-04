package com.desertadventure.screen.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.desertadventure.state.GameSession;

/** Hub hotkey: start current stage battle. */
public final class HubKeyboardInput {
    public void handle(GameSession session) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
            session.tryStartCurrentStageCombat();
        }
    }
}
