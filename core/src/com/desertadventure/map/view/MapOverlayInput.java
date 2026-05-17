package com.desertadventure.map.view;

import com.desertadventure.map.model.GridPos;

public class MapOverlayInput {
    private GridPos hoveredCell;

    public void updateHover(MapOverlayLayout layout, float worldX, float worldY) {
        hoveredCell = layout.screenToGrid(worldX, worldY);
    }

    public GridPos getHoveredGridPos() {
        return hoveredCell;
    }
}
