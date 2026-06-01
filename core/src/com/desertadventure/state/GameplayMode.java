package com.desertadventure.state;

public enum GameplayMode {
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

    /** Parallax explore scene with overhead player HP bar (not combat or victory). */
    public boolean isExploreScene() {
        return !isCombat() && this != VICTORY;
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
