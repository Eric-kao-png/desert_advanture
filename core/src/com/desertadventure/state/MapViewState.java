package com.desertadventure.state;

import com.desertadventure.config.GameConfig;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.GridPos;

public class MapViewState {
    private int originX;
    private int originY;

    public void centerOn(GridPos player, GameMap map) {
        int half = GameConfig.MAP_VIEW_TILES / 2;
        originX = clamp(player.x - half, map.getMinCoord(), map.getMaxViewOriginX());
        originY = clamp(player.y - half, map.getMinCoord(), map.getMaxViewOriginY());
    }

    public void pan(int dx, int dy, GameMap map) {
        originX = clamp(originX + dx, map.getMinCoord(), map.getMaxViewOriginX());
        originY = clamp(originY + dy, map.getMinCoord(), map.getMaxViewOriginY());
    }

    public int getOriginX() {
        return originX;
    }

    public int getOriginY() {
        return originY;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
