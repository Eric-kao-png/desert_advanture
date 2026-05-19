package com.desertadventure.screen;

import com.desertadventure.config.GameConfig;
import com.desertadventure.screen.layout.CharacterPanelGeometry;
import com.desertadventure.screen.layout.CharacterStatsLayout;
import com.desertadventure.screen.layout.InventoryGridLayout;
import com.desertadventure.screen.layout.ItemDetailLayout;
import com.desertadventure.util.RectHitTest;

/** Hit areas and layout metrics for the character overlay. */
public class CharacterOverlayLayout {
    private final CharacterPanelGeometry panel;
    private final InventoryGridLayout grid;
    private final CharacterStatsLayout stats;
    private final ItemDetailLayout detail;
    public final float inventoryTitleX;
    public final float inventoryTitleY;

    public CharacterOverlayLayout() {
        panel = new CharacterPanelGeometry();
        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        inventoryTitleX = panel.leftColX;
        inventoryTitleY = panel.headerBottom - lineH * GameConfig.CHARACTER_SECTION_LABEL_OFFSET_MULT;
        grid = new InventoryGridLayout(panel.leftColX, panel.columnW, inventoryTitleY);
        stats = new CharacterStatsLayout(panel.rightColX, panel.columnW, panel.headerBottom);
        detail = new ItemDetailLayout(panel.panelX, panel.panelY, panel.panelW, panel.panelH);
    }

    public CharacterPanelGeometry panel() {
        return panel;
    }

    public InventoryGridLayout grid() {
        return grid;
    }

    public CharacterStatsLayout stats() {
        return stats;
    }

    public ItemDetailLayout detail() {
        return detail;
    }

    public boolean containsPanel(float worldX, float worldY) {
        return RectHitTest.contains(worldX, worldY, panel.panelX, panel.panelY, panel.panelW, panel.panelH);
    }

    public int slotAt(float worldX, float worldY) {
        if (!containsPanel(worldX, worldY)) {
            return -1;
        }
        return grid.slotAt(worldX, worldY);
    }
}
