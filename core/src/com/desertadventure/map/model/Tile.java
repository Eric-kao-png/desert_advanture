package com.desertadventure.map.model;

public class Tile {
    private final GridPos position;
    private TileType type;
    private String eventId;
    private boolean cycleCleared;
    private boolean itemCollectedThisCycle;

    public Tile(GridPos position, TileType type) {
        this.position = position;
        this.type = type;
    }

    public GridPos getPosition() {
        return position;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public boolean isCycleCleared() {
        return cycleCleared;
    }

    public void setCycleCleared(boolean cycleCleared) {
        this.cycleCleared = cycleCleared;
    }

    public boolean isItemCollectedThisCycle() {
        return itemCollectedThisCycle;
    }

    public void setItemCollectedThisCycle(boolean itemCollectedThisCycle) {
        this.itemCollectedThisCycle = itemCollectedThisCycle;
    }

    public int distanceFromCenter(int centerX, int centerY) {
        return Math.abs(position.x - centerX) + Math.abs(position.y - centerY);
    }

    public boolean blocksMovement() {
        if (type == TileType.BLOCKED) {
            return true;
        }
        if (type == TileType.ITEM && itemCollectedThisCycle) {
            return false;
        }
        if (type == TileType.COMBAT && cycleCleared) {
            return false;
        }
        return !type.isWalkable();
    }

    public boolean needsInteractionOnArrival() {
        if (type == TileType.EMPTY || type == TileType.SPAWN) {
            return false;
        }
        if (type == TileType.ITEM && itemCollectedThisCycle) {
            return false;
        }
        if (type == TileType.COMBAT && cycleCleared) {
            return false;
        }
        if (type == TileType.EVENT && cycleCleared) {
            return false;
        }
        if (type == TileType.BOSS_SUMMON) {
            return true;
        }
        return type.isInteractable();
    }
}
