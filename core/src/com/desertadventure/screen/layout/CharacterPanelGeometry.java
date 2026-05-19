package com.desertadventure.screen.layout;

import com.desertadventure.config.GameConfig;

/** Three-column bounds for the character overlay panel. */
public final class CharacterPanelGeometry {
    public final float panelX;
    public final float panelY;
    public final float panelW;
    public final float panelH;
    public final float leftColX;
    public final float centerColX;
    public final float rightColX;
    public final float columnW;
    public final float contentBottom;
    public final float headerBottom;
    public final float titleY;

    public CharacterPanelGeometry() {
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
        headerBottom = titleY - lineH * GameConfig.CHARACTER_TITLE_LINE_MULT;
    }
}
