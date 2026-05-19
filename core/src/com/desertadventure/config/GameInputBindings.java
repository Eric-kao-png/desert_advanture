package com.desertadventure.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/** Keyboard bindings shared across screens. */
public final class GameInputBindings {
    public static final int MAP = Input.Keys.M;
    public static final int INVENTORY = Input.Keys.N;
    public static final int BACK = Input.Keys.ESCAPE;
    public static final int CONFIRM = Input.Keys.ENTER;
    public static final int CONFIRM_ALT = Input.Keys.SPACE;
    public static final int ATTACK = Input.Keys.J;
    public static final int SKILL = Input.Keys.K;
    public static final int ULTIMATE = Input.Keys.L;

    private GameInputBindings() {
    }

    public static boolean justConfirmed() {
        return Gdx.input.isKeyJustPressed(CONFIRM) || Gdx.input.isKeyJustPressed(CONFIRM_ALT);
    }

    public static boolean justCloseMap() {
        return Gdx.input.isKeyJustPressed(BACK) || Gdx.input.isKeyJustPressed(MAP);
    }

    public static boolean justCloseInventory() {
        return Gdx.input.isKeyJustPressed(BACK) || Gdx.input.isKeyJustPressed(INVENTORY);
    }

    public static boolean moveLeft() {
        return Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
    }

    public static boolean moveRight() {
        return Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
    }
}
