package com.desertadventure.state;

/** Read/write access to the current {@link GameplayMode}. */
public interface SessionModeAccess {
    GameplayMode get();

    void set(GameplayMode mode);
}
