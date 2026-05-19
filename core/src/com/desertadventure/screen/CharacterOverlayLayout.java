package com.desertadventure.screen;

import com.desertadventure.config.GameConfig;
import com.desertadventure.screen.layout.CharacterPanelGeometry;
import com.desertadventure.screen.layout.CharacterStatsLayout;
import com.desertadventure.screen.layout.InventoryGridLayout;
import com.desertadventure.screen.layout.ItemDetailLayout;

/** Hit areas and layout metrics for the character overlay (facade over layout helpers). */
public class CharacterOverlayLayout {
    private final CharacterPanelGeometry panel;
    private final InventoryGridLayout grid;
    private final CharacterStatsLayout stats;
    private final ItemDetailLayout detail;
    private final float inventoryTitleX;
    private final float inventoryTitleY;
    private final float portraitX;
    private final float portraitY;
    private final float portraitW;
    private final float portraitH;

    public CharacterOverlayLayout() {
        panel = new CharacterPanelGeometry();
        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        inventoryTitleX = panel.leftColX;
        inventoryTitleY = panel.headerBottom - lineH * GameConfig.CHARACTER_SECTION_LABEL_OFFSET_MULT;
        grid = new InventoryGridLayout(panel.leftColX, panel.columnW, inventoryTitleY);
        stats = new CharacterStatsLayout(panel.rightColX, panel.columnW, panel.headerBottom);
        detail = new ItemDetailLayout(panel.panelX, panel.panelY, panel.panelW, panel.panelH);

        portraitX = panel.centerColX;
        portraitY = panel.contentBottom;
        portraitW = panel.columnW;
        portraitH = panel.headerBottom - lineH * GameConfig.CHARACTER_PORTRAIT_TOP_MULT - panel.contentBottom;
    }

    public float getPanelX() {
        return panel.panelX;
    }

    public float getPanelY() {
        return panel.panelY;
    }

    public float getPanelW() {
        return panel.panelW;
    }

    public float getPanelH() {
        return panel.panelH;
    }

    public float getTitleY() {
        return panel.titleY;
    }

    public float getLeftColX() {
        return panel.leftColX;
    }

    public float getCenterColX() {
        return panel.centerColX;
    }

    public float getRightColX() {
        return panel.rightColX;
    }

    public float getColumnW() {
        return panel.columnW;
    }

    public float getContentBottom() {
        return panel.contentBottom;
    }

    public float getHeaderBottom() {
        return panel.headerBottom;
    }

    public float getPortraitX() {
        return portraitX;
    }

    public float getPortraitY() {
        return portraitY;
    }

    public float getPortraitW() {
        return portraitW;
    }

    public float getPortraitH() {
        return portraitH;
    }

    public float getStatsTextX() {
        return stats.statsTextX;
    }

    public float getStatsTopY() {
        return stats.statsTopY;
    }

    public float getStatBarX() {
        return stats.statBarX;
    }

    public float getStatBarW() {
        return stats.statBarW;
    }

    public float getStatBarH() {
        return stats.statBarH;
    }

    public float getHpBarBottom() {
        return stats.hpBarBottom;
    }

    public float getCombatSectionY() {
        return stats.combatSectionY;
    }

    public float getExplorationSectionY() {
        return stats.explorationSectionY;
    }

    public float getStaminaBarBottom() {
        return stats.staminaBarBottom;
    }

    public float getInventoryTitleX() {
        return inventoryTitleX;
    }

    public float getInventoryTitleY() {
        return inventoryTitleY;
    }

    public int getSlotCount() {
        return grid.slotCount;
    }

    public boolean containsPanel(float worldX, float worldY) {
        return worldX >= panel.panelX && worldX <= panel.panelX + panel.panelW
                && worldY >= panel.panelY && worldY <= panel.panelY + panel.panelH;
    }

    public int slotAt(float worldX, float worldY) {
        if (!containsPanel(worldX, worldY)) {
            return -1;
        }
        return grid.slotAt(worldX, worldY);
    }

    public float slotLeft(int index) {
        return grid.slotLeft(index);
    }

    public float slotBottom(int index) {
        return grid.slotBottom(index);
    }

    public float slotSize() {
        return grid.slotSize;
    }

    public boolean detailContains(float worldX, float worldY) {
        return detail.contains(worldX, worldY);
    }

    public float getDetailX() {
        return detail.detailX;
    }

    public float getDetailY() {
        return detail.detailY;
    }

    public float getDetailW() {
        return detail.detailW;
    }

    public float getDetailH() {
        return detail.detailH;
    }

    public float detailSpriteCenterX() {
        return detail.detailX + detail.detailW / 2f;
    }

    public float detailSpriteBottom() {
        return detail.detailY + detail.detailH - GameConfig.CHARACTER_DETAIL_SPRITE_BOTTOM_OFFSET;
    }

    public float detailNameY() {
        return detail.detailY + detail.detailH - GameConfig.CHARACTER_DETAIL_NAME_Y_OFFSET;
    }

    public float detailDescY() {
        return detail.detailY + detail.detailH - GameConfig.CHARACTER_DETAIL_DESC_Y_OFFSET;
    }

    public boolean useButtonContains(float worldX, float worldY) {
        return detail.useButtonContains(worldX, worldY);
    }

    public boolean closeButtonContains(float worldX, float worldY) {
        return detail.closeButtonContains(worldX, worldY);
    }

    public float getUseButtonX() {
        return detail.useButtonX;
    }

    public float getUseButtonY() {
        return detail.useButtonY;
    }

    public float getCloseButtonX() {
        return detail.closeButtonX;
    }

    public float getCloseButtonY() {
        return detail.closeButtonY;
    }

    public float getButtonW() {
        return detail.buttonW;
    }

    public float getButtonH() {
        return detail.buttonH;
    }
}
