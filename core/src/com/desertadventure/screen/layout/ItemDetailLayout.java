package com.desertadventure.screen.layout;

import com.desertadventure.config.GameConfig;

/** Item detail popup and action buttons. */
public final class ItemDetailLayout {
    public final float detailX;
    public final float detailY;
    public final float detailW;
    public final float detailH;
    public final float useButtonX;
    public final float useButtonY;
    public final float closeButtonX;
    public final float closeButtonY;
    public final float buttonW;
    public final float buttonH;

    public ItemDetailLayout(float panelX, float panelY, float panelW, float panelH) {
        detailW = GameConfig.INVENTORY_DETAIL_WIDTH;
        detailH = GameConfig.INVENTORY_DETAIL_HEIGHT;
        detailX = panelX + (panelW - detailW) / 2f;
        detailY = panelY + (panelH - detailH) / 2f;

        buttonW = GameConfig.INVENTORY_BUTTON_WIDTH;
        buttonH = GameConfig.INVENTORY_BUTTON_HEIGHT;
        float buttonGap = GameConfig.INVENTORY_BUTTON_GAP;
        float buttonsY = detailY + GameConfig.CHARACTER_DETAIL_BUTTON_ROW_Y;
        float buttonsW = buttonW * 2f + buttonGap;
        float buttonsLeft = detailX + (detailW - buttonsW) / 2f;
        useButtonX = buttonsLeft;
        useButtonY = buttonsY;
        closeButtonX = buttonsLeft + buttonW + buttonGap;
        closeButtonY = buttonsY;
    }

    public boolean contains(float worldX, float worldY) {
        return worldX >= detailX && worldX <= detailX + detailW
                && worldY >= detailY && worldY <= detailY + detailH;
    }

    public boolean useButtonContains(float worldX, float worldY) {
        return containsButton(worldX, worldY, useButtonX, useButtonY);
    }

    public boolean closeButtonContains(float worldX, float worldY) {
        return containsButton(worldX, worldY, closeButtonX, closeButtonY);
    }

    private boolean containsButton(float worldX, float worldY, float x, float y) {
        return worldX >= x && worldX <= x + buttonW && worldY >= y && worldY <= y + buttonH;
    }
}
