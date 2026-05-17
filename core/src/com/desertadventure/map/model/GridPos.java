package com.desertadventure.map.model;

import java.util.Objects;

public final class GridPos {
    public final int x;
    public final int y;

    public GridPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int manhattanDistance(GridPos other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GridPos)) {
            return false;
        }
        GridPos gridPos = (GridPos) o;
        return x == gridPos.x && y == gridPos.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
