package com.desertadventure.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/** Keyboard bindings shared across screens. */
public final class GameInputBindings {
    public static final int BACK = Input.Keys.ESCAPE;
    public static final int CONFIRM = Input.Keys.ENTER;
    public static final int CONFIRM_ALT = Input.Keys.SPACE;

    private GameInputBindings() {
    }

    public static boolean justConfirmed() {
        return Gdx.input.isKeyJustPressed(CONFIRM) || Gdx.input.isKeyJustPressed(CONFIRM_ALT);
    }
}
