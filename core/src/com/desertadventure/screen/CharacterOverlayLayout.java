package com.desertadventure.screen;

import com.desertadventure.config.GameConfig;

/** Three-column character panel: backpack (left), portrait (center), stats (right). */
public class CharacterOverlayLayout {
    private final float panelX;
    private final float panelY;
    private final float panelW;
    private final float panelH;
    private final float leftColX;
    private final float centerColX;
    private final float rightColX;
    private final float columnW;
    private final float contentBottom;
    private final float headerBottom;
    private final float gridLeft;
    private final float gridTop;
    private final float slotSize;
    private final float slotGap;
    private final int cols;
    private final int slotCount;
    private final float portraitX;
    private final float portraitY;
    private final float portraitW;
    private final float portraitH;
    private final float statsTextX;
    private final float statsTopY;
    private final float statBarX;
    private final float statBarW;
    private final float statBarH;
    private final float hpBarBottom;
    private final float combatSectionY;
    private final float explorationSectionY;
    private final float staminaBarBottom;
    private final float inventoryTitleX;
    private final float inventoryTitleY;
    private final float titleY;
    private final float detailX;
    private final float detailY;
    private final float detailW;
    private final float detailH;
    private final float useButtonX;
    private final float useButtonY;
    private final float closeButtonX;
    private final float closeButtonY;
    private final float buttonW;
    private final float buttonH;

    public CharacterOverlayLayout() {
        panelW = GameConfig.CHARACTER_PANEL_WIDTH;
        panelH = GameConfig.CHARACTER_PANEL_HEIGHT;
        panelX = (GameConfig.VIEW_WIDTH - panelW) / 2f;
        panelY = (GameConfig.VIEW_HEIGHT - panelH) / 2f;

        float pad = GameConfig.CHARACTER_PANEL_PADDING;
        float colGap = GameConfig.CHARACTER_PANEL_COLUMN_GAP;
        float contentW = panelW - pad * 2f;
        columnW = (contentW - colGap * 2f) / 3f;

        leftColX = panelX + pad;
        centerColX = leftColX + columnW + colGap;
        rightColX = centerColX + columnW + colGap;
        contentBottom = panelY + pad;

        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        titleY = panelY + panelH - pad;
        headerBottom = titleY - lineH * 1.15f;
        inventoryTitleX = leftColX;
        inventoryTitleY = headerBottom - lineH * 0.25f;
        statsTextX = rightColX;
        statsTopY = headerBottom - lineH * 0.25f;

        statBarH = GameConfig.CHARACTER_STAT_BAR_HEIGHT;
        statBarW = columnW - 12f;
        statBarX = statsTextX;
        hpBarBottom = statsTopY - lineH * 0.85f - statBarH;
        combatSectionY = hpBarBottom - GameConfig.CHARACTER_STAT_BAR_SECTION_GAP;
        explorationSectionY = combatSectionY - lineH * 1.95f;
        staminaBarBottom = explorationSectionY - lineH * 0.85f - statBarH;

        slotSize = GameConfig.INVENTORY_SLOT_SIZE;
        slotGap = GameConfig.INVENTORY_SLOT_GAP;
        cols = GameConfig.INVENTORY_GRID_COLS;
        slotCount = GameConfig.INVENTORY_SLOT_COUNT;

        float gridW = cols * slotSize + (cols - 1) * slotGap;
        gridLeft = leftColX + (columnW - gridW) / 2f;
        gridTop = inventoryTitleY - lineH * 1.05f;

        portraitX = centerColX;
        portraitY = contentBottom;
        portraitW = columnW;
        portraitH = headerBottom - lineH * 0.35f - contentBottom;

        detailW = GameConfig.INVENTORY_DETAIL_WIDTH;
        detailH = GameConfig.INVENTORY_DETAIL_HEIGHT;
        detailX = panelX + (panelW - detailW) / 2f;
        detailY = panelY + (panelH - detailH) / 2f;

        buttonW = GameConfig.INVENTORY_BUTTON_WIDTH;
        buttonH = GameConfig.INVENTORY_BUTTON_HEIGHT;
        float buttonGap = 16f;
        float buttonsY = detailY + 24f;
        float buttonsW = buttonW * 2f + buttonGap;
        float buttonsLeft = detailX + (detailW - buttonsW) / 2f;
        useButtonX = buttonsLeft;
        useButtonY = buttonsY;
        closeButtonX = buttonsLeft + buttonW + buttonGap;
        closeButtonY = buttonsY;
    }

    public float getPanelX() {
        return panelX;
    }

    public float getPanelY() {
        return panelY;
    }

    public float getPanelW() {
        return panelW;
    }

    public float getPanelH() {
        return panelH;
    }

    public float getTitleY() {
        return titleY;
    }

    public float getLeftColX() {
        return leftColX;
    }

    public float getCenterColX() {
        return centerColX;
    }

    public float getRightColX() {
        return rightColX;
    }

    public float getColumnW() {
        return columnW;
    }

    public float getContentBottom() {
        return contentBottom;
    }

    public float getHeaderBottom() {
        return headerBottom;
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
        return statsTextX;
    }

    public float getStatsTopY() {
        return statsTopY;
    }

    public float getStatBarX() {
        return statBarX;
    }

    public float getStatBarW() {
        return statBarW;
    }

    public float getStatBarH() {
        return statBarH;
    }

    public float getHpBarBottom() {
        return hpBarBottom;
    }

    public float getCombatSectionY() {
        return combatSectionY;
    }

    public float getExplorationSectionY() {
        return explorationSectionY;
    }

    public float getStaminaBarBottom() {
        return staminaBarBottom;
    }

    public float getInventoryTitleX() {
        return inventoryTitleX;
    }

    public float getInventoryTitleY() {
        return inventoryTitleY;
    }

    public int getSlotCount() {
        return slotCount;
    }

    public boolean containsPanel(float worldX, float worldY) {
        return worldX >= panelX && worldX <= panelX + panelW
                && worldY >= panelY && worldY <= panelY + panelH;
    }

    public int slotAt(float worldX, float worldY) {
        if (!containsPanel(worldX, worldY)) {
            return -1;
        }
        for (int i = 0; i < slotCount; i++) {
            if (containsSlot(worldX, worldY, i)) {
                return i;
            }
        }
        return -1;
    }

    public boolean containsSlot(float worldX, float worldY, int index) {
        float left = slotLeft(index);
        float bottom = slotBottom(index);
        return worldX >= left && worldX <= left + slotSize
                && worldY >= bottom && worldY <= bottom + slotSize;
    }

    public float slotLeft(int index) {
        int col = index % cols;
        return gridLeft + col * (slotSize + slotGap);
    }

    public float slotBottom(int index) {
        int row = index / cols;
        return gridTop - (row + 1) * slotSize - row * slotGap;
    }

    public float slotSize() {
        return slotSize;
    }

    public boolean detailContains(float worldX, float worldY) {
        return worldX >= detailX && worldX <= detailX + detailW
                && worldY >= detailY && worldY <= detailY + detailH;
    }

    public float getDetailX() {
        return detailX;
    }

    public float getDetailY() {
        return detailY;
    }

    public float getDetailW() {
        return detailW;
    }

    public float getDetailH() {
        return detailH;
    }

    public float detailSpriteCenterX() {
        return detailX + detailW / 2f;
    }

    public float detailSpriteBottom() {
        return detailY + detailH - 56f;
    }

    public float detailNameY() {
        return detailY + detailH - 132f;
    }

    public float detailDescY() {
        return detailY + detailH - 168f;
    }

    public boolean useButtonContains(float worldX, float worldY) {
        return containsButton(worldX, worldY, useButtonX, useButtonY);
    }

    public boolean closeButtonContains(float worldX, float worldY) {
        return containsButton(worldX, worldY, closeButtonX, closeButtonY);
    }

    public float getUseButtonX() {
        return useButtonX;
    }

    public float getUseButtonY() {
        return useButtonY;
    }

    public float getCloseButtonX() {
        return closeButtonX;
    }

    public float getCloseButtonY() {
        return closeButtonY;
    }

    public float getButtonW() {
        return buttonW;
    }

    public float getButtonH() {
        return buttonH;
    }

    private boolean containsButton(float worldX, float worldY, float x, float y) {
        return worldX >= x && worldX <= x + buttonW && worldY >= y && worldY <= y + buttonH;
    }
}
