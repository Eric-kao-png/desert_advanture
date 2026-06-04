package com.desertadventure.state;

public enum GameplayMode {
    HUB,
    COMBAT,
    BOSS_COMBAT,
    VICTORY;

    public boolean isCombat() {
        return this == COMBAT || this == BOSS_COMBAT;
    }

    public boolean isHub() {
        return this == HUB;
    }
}
