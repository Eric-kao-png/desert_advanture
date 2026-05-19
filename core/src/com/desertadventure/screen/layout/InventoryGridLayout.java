package com.desertadventure.screen.layout;

import com.desertadventure.config.GameConfig;
import com.desertadventure.util.RectHitTest;

/** Slot positions for the backpack grid in the left column. */
public final class InventoryGridLayout {
    public final float gridLeft;
    public final float gridTop;
    public final float slotSize;
    public final float slotGap;
    public final int cols;
    public final int slotCount;

    public InventoryGridLayout(float leftColX, float columnW, float inventoryTitleY) {
        slotSize = GameConfig.INVENTORY_SLOT_SIZE;
        slotGap = GameConfig.INVENTORY_SLOT_GAP;
        cols = GameConfig.INVENTORY_GRID_COLS;
        slotCount = GameConfig.INVENTORY_SLOT_COUNT;

        float gridW = cols * slotSize + (cols - 1) * slotGap;
        gridLeft = leftColX + (columnW - gridW) / 2f;
        gridTop = inventoryTitleY - GameConfig.CHARACTER_PANEL_LINE_HEIGHT
                * GameConfig.CHARACTER_INVENTORY_LABEL_GAP_MULT;
    }

    public float slotLeft(int index) {
        int col = index % cols;
        return gridLeft + col * (slotSize + slotGap);
    }

    public float slotBottom(int index) {
        int row = index / cols;
        return gridTop - (row + 1) * slotSize - row * slotGap;
    }

    public int slotAt(float worldX, float worldY) {
        for (int i = 0; i < slotCount; i++) {
            float left = slotLeft(i);
            float bottom = slotBottom(i);
            if (RectHitTest.contains(worldX, worldY, left, bottom, slotSize, slotSize)) {
                return i;
            }
        }
        return -1;
    }
}
