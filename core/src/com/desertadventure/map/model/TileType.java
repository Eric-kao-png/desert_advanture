package com.desertadventure.map.model;

public enum TileType {
    EMPTY(true, false),
    BLOCKED(false, false),
    SPAWN(true, false),
    ITEM(true, true),
    EVENT(true, true),
    COMBAT(true, true),
    BOSS_SUMMON(true, true);

    private final boolean walkable;
    private final boolean interactable;

    TileType(boolean walkable, boolean interactable) {
        this.walkable = walkable;
        this.interactable = interactable;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public boolean isInteractable() {
        return interactable;
    }
}
