package com.desertadventure.map.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Straight-line movement between grid cell centers (integer coordinates).
 * Movement is blocked if any grid cell intersected by the line blocks passage.
 */
public final class StraightLinePath {
    private StraightLinePath() {
    }

    public static final class Plan {
        private final GridPos destination;
        private final float distance;
        private final List<GridPos> cellsOnLine;

        public Plan(GridPos destination, float distance, List<GridPos> cellsOnLine) {
            this.destination = destination;
            this.distance = distance;
            this.cellsOnLine = cellsOnLine;
        }

        public GridPos getDestination() {
            return destination;
        }

        public float getDistance() {
            return distance;
        }

        public List<GridPos> getCellsOnLine() {
            return cellsOnLine;
        }
    }

    public static Plan plan(GameMap map, GridPos start, GridPos end) {
        if (!map.isInside(end) || !map.canEnter(end)) {
            return null;
        }
        if (start.equals(end)) {
            return null;
        }

        List<GridPos> line = cellsOnLine(start, end);
        for (GridPos cell : line) {
            if (!map.isInside(cell)) {
                return null;
            }
            if (cell.equals(start)) {
                continue;
            }
            if (map.isBlockedForPath(cell)) {
                return null;
            }
        }

        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float distance = (float) Math.hypot(dx, dy);
        return new Plan(end, distance, line);
    }

    /** Bresenham line through integer cell centers. */
    static List<GridPos> cellsOnLine(GridPos from, GridPos to) {
        List<GridPos> points = new ArrayList<>();
        int x0 = from.x;
        int y0 = from.y;
        int x1 = to.x;
        int y1 = to.y;
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int x = x0;
        int y = y0;

        while (true) {
            points.add(new GridPos(x, y));
            if (x == x1 && y == y1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
        return points;
    }
}
