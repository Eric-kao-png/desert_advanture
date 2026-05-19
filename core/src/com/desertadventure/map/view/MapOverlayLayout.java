package com.desertadventure.map.view;

import com.desertadventure.config.GameConfig;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.GridPos;

/**
 * Renders and hit-tests a sliding window over a large world map (world coords).
 * +Y points upward on screen; {@code viewOriginY} is the bottom row of the viewport.
 */
public final class MapOverlayLayout {
    private final GameMap map;
    private final int viewTiles;
    private final int viewOriginX;
    private final int viewOriginY;
    private final float cellSize;
    private final float offsetX;
    private final float offsetY;

    public MapOverlayLayout(GameMap map, int viewOriginX, int viewOriginY) {
        this.map = map;
        this.viewTiles = GameConfig.MAP_VIEW_TILES;
        this.viewOriginX = clampOrigin(viewOriginX, map.getMinCoord(), map.getMaxViewOriginX());
        this.viewOriginY = clampOrigin(viewOriginY, map.getMinCoord(), map.getMaxViewOriginY());
        this.cellSize = GameConfig.MAP_OVERLAY_VIEW_PIXEL_SIZE / viewTiles;
        this.offsetX = (GameConfig.VIEW_WIDTH - viewTiles * cellSize) / 2f;
        this.offsetY = (GameConfig.VIEW_HEIGHT - viewTiles * cellSize) / 2f;
    }

    public int getViewOriginX() {
        return viewOriginX;
    }

    public int getViewOriginY() {
        return viewOriginY;
    }

    public int getViewTiles() {
        return viewTiles;
    }

    public float cellLeftForMapX(int mapX) {
        return offsetX + (mapX - viewOriginX) * cellSize;
    }

    public float cellBottomForMapY(int mapY) {
        int viewY = mapY - viewOriginY;
        return offsetY + viewY * cellSize;
    }

    public float getCellSize() {
        return cellSize;
    }

    public boolean isOnScreen(GridPos pos) {
        return pos.x >= viewOriginX && pos.x < viewOriginX + viewTiles
                && pos.y >= viewOriginY && pos.y < viewOriginY + viewTiles;
    }

    public GridPos screenToGrid(float screenX, float screenY) {
        float localX = screenX - offsetX;
        float localY = screenY - offsetY;
        if (localX < 0f || localY < 0f || localX >= viewTiles * cellSize || localY >= viewTiles * cellSize) {
            return null;
        }
        int vx = (int) Math.floor(localX / cellSize);
        int vy = (int) Math.floor(localY / cellSize);
        int mapX = viewOriginX + vx;
        int mapY = viewOriginY + vy;
        GridPos pos = new GridPos(mapX, mapY);
        if (!map.isInside(pos)) {
            return null;
        }
        return pos;
    }

    private static int clampOrigin(int origin, int minCoord, int maxOrigin) {
        return Math.max(minCoord, Math.min(origin, maxOrigin));
    }
}
