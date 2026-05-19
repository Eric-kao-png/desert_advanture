package com.desertadventure.map.model;

import com.desertadventure.config.GameConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Square grid map with world coordinates centered at (0, 0).
 * For size 501, valid coordinates are -250..+250 on each axis.
 */
public class GameMap {
    private static final int[][] REVEAL_NEIGHBOR_OFFSETS = {
            {-1, -1}, {0, -1}, {1, -1},
            {-1, 0}, {1, 0},
            {-1, 1}, {0, 1}, {1, 1}
    };

    private final int size;
    private final int minCoord;
    private final int maxCoord;
    private final Tile[][] tiles;
    private final Set<GridPos> permanentlyExplored = new HashSet<>();
    private final Set<GridPos> cycleModifiedTiles = new HashSet<>();

    public GameMap(int size) {
        this.size = size;
        this.minCoord = -(size / 2);
        this.maxCoord = minCoord + size - 1;
        this.tiles = new Tile[size][size];
        for (int wx = minCoord; wx <= maxCoord; wx++) {
            for (int wy = minCoord; wy <= maxCoord; wy++) {
                int ix = toIndex(wx);
                int iy = toIndex(wy);
                tiles[ix][iy] = new Tile(new GridPos(wx, wy), TileType.EMPTY);
            }
        }
        getTile(0, 0).setType(TileType.SPAWN);
    }

    public int getSize() {
        return size;
    }

    public int getMinCoord() {
        return minCoord;
    }

    public int getMaxCoord() {
        return maxCoord;
    }

    public int getCenterX() {
        return 0;
    }

    public int getCenterY() {
        return 0;
    }

    /** Maximum map X for the top-left corner of the map overlay viewport. */
    public int getMaxViewOriginX() {
        return maxCoord - GameConfig.MAP_VIEW_TILES + 1;
    }

    public int getMaxViewOriginY() {
        return maxCoord - GameConfig.MAP_VIEW_TILES + 1;
    }

    public GridPos getSpawnPosition() {
        return new GridPos(0, 0);
    }

    public Tile getTile(int worldX, int worldY) {
        return tiles[toIndex(worldX)][toIndex(worldY)];
    }

    public Tile getTile(GridPos pos) {
        return getTile(pos.x, pos.y);
    }

    public boolean isInside(GridPos pos) {
        return pos.x >= minCoord && pos.x <= maxCoord && pos.y >= minCoord && pos.y <= maxCoord;
    }

    public boolean isBlockedForPath(GridPos pos) {
        return getTile(pos).blocksMovement();
    }

    public boolean canEnter(GridPos pos) {
        if (!isInside(pos)) {
            return false;
        }
        return !getTile(pos).blocksMovement();
    }

    public void markExplored(GridPos pos) {
        permanentlyExplored.add(pos);
    }

    public void revealAround(GridPos center) {
        markExplored(center);
        for (int[] offset : REVEAL_NEIGHBOR_OFFSETS) {
            GridPos neighbor = new GridPos(center.x + offset[0], center.y + offset[1]);
            if (isInside(neighbor)) {
                markExplored(neighbor);
            }
        }
    }

    public boolean isPermanentlyExplored(GridPos pos) {
        return permanentlyExplored.contains(pos);
    }

    public void markCycleModified(GridPos pos) {
        cycleModifiedTiles.add(pos);
    }

    public void resetCycleState() {
        for (GridPos pos : cycleModifiedTiles) {
            if (!isInside(pos)) {
                continue;
            }
            Tile tile = getTile(pos);
            tile.setCycleCleared(false);
            tile.setItemCollectedThisCycle(false);
        }
        cycleModifiedTiles.clear();
    }

    public int distanceBand(GridPos pos) {
        return (Math.abs(pos.x) + Math.abs(pos.y)) / GameConfig.MAP_DISTANCE_BAND_DIVISOR;
    }

    private int toIndex(int worldCoord) {
        return worldCoord - minCoord;
    }
}
