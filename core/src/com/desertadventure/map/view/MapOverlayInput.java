package com.desertadventure.map.view;

import com.desertadventure.map.model.GridPos;
import com.desertadventure.presentation.OverlayCloseButton;

public class MapOverlayInput {
    private GridPos hoveredCell;
    private boolean hoveredDismiss;

    public void updateHover(MapOverlayLayout layout, float worldX, float worldY) {
        hoveredDismiss = OverlayCloseButton.contains(worldX, worldY);
        if (hoveredDismiss) {
            hoveredCell = null;
            return;
        }
        hoveredCell = layout.screenToGrid(worldX, worldY);
    }

    public GridPos getHoveredGridPos() {
        return hoveredCell;
    }

    public boolean isHoveredDismiss() {
        return hoveredDismiss;
    }
}
