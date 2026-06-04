package com.desertadventure.state;

public enum GameplayMode {
    HUB,
    EXPLORE_IDLE,
    MAP_OVERLAY,
    CHARACTER_OVERLAY,
    RUNNING,
    COMBAT,
    BOSS_COMBAT,
    STORM,
    VICTORY;

    public boolean isCombat() {
        return this == COMBAT || this == BOSS_COMBAT;
    }

    public boolean isHub() {
        return this == HUB;
    }

    /** Parallax explore scene with overhead player HP bar (not combat, hub, or victory). */
    public boolean isExploreScene() {
        return !isCombat() && !isHub() && this != VICTORY;
    }

    public boolean canOpenMap() {
        return canOpenExplorationOverlay();
    }

    public boolean canOpenCharacter() {
        return canOpenExplorationOverlay();
    }

    /** Idle or moving exploration (not combat / storm / menus). */
    public boolean canOpenExplorationOverlay() {
        return this == EXPLORE_IDLE || this == RUNNING;
    }

    public boolean interruptsTravel() {
        return this == STORM || isCombat();
    }
}
