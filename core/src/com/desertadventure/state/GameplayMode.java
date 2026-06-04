package com.desertadventure.state;

public enum GameplayMode {
    HUB,
    COMBAT;

    public boolean isCombat() {
        return this == COMBAT;
    }

    public boolean isHub() {
        return this == HUB;
    }
}
