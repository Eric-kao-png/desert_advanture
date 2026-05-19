package com.desertadventure.presentation.character;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.UiColors;
import com.desertadventure.presentation.OverlayBackdrop;
import com.desertadventure.presentation.ShapeDrawer;
import com.desertadventure.screen.CharacterOverlayLayout;

/** Panel background, column dividers, and portrait frame. */
public final class CharacterPanelBackdrop {
    private CharacterPanelBackdrop() {
    }

    public static void draw(ShapeRenderer shapes, CharacterOverlayLayout layout) {
        OverlayBackdrop.drawDim(shapes);
        ShapeDrawer.fillRect(shapes, layout.getPanelX(), layout.getPanelY(),
                layout.getPanelW(), layout.getPanelH(), UiColors.PANEL_FILL);
        drawColumnDividers(shapes, layout);
        ShapeDrawer.fillRect(shapes, layout.getPortraitX(), layout.getPortraitY(),
                layout.getPortraitW(), layout.getPortraitH(), UiColors.PORTRAIT_FILL);
        ShapeDrawer.strokeRect(shapes, layout.getPortraitX(), layout.getPortraitY(),
                layout.getPortraitW(), layout.getPortraitH(), UiColors.PORTRAIT_BORDER,
                GameConfig.CHARACTER_SLOT_BORDER_WIDTH);
    }

    private static void drawColumnDividers(ShapeRenderer shapes, CharacterOverlayLayout layout) {
        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        float top = layout.getHeaderBottom() - lineH * GameConfig.CHARACTER_COLUMN_DIVIDER_TOP_MULT;
        float bottom = layout.getContentBottom();
        float h = top - bottom;
        float w = GameConfig.CHARACTER_COLUMN_DIVIDER_WIDTH;
        float colGap = GameConfig.CHARACTER_PANEL_COLUMN_GAP;
        float x1 = layout.getCenterColX() - colGap / 2f - w / 2f;
        float x2 = layout.getRightColX() - colGap / 2f - w / 2f;
        ShapeDrawer.fillRect(shapes, x1, bottom, w, h, UiColors.COLUMN_DIVIDER);
        ShapeDrawer.fillRect(shapes, x2, bottom, w, h, UiColors.COLUMN_DIVIDER);
    }
}
