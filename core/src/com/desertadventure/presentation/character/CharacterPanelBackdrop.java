package com.desertadventure.presentation.character;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.UiColors;
import com.desertadventure.presentation.OverlayBackdrop;
import com.desertadventure.presentation.ShapeDrawer;
import com.desertadventure.screen.CharacterOverlayLayout;
import com.desertadventure.screen.layout.CharacterPanelGeometry;

/** Panel background, column dividers, and portrait frame. */
public final class CharacterPanelBackdrop {
    private CharacterPanelBackdrop() {
    }

    public static void draw(ShapeRenderer shapes, CharacterOverlayLayout layout) {
        CharacterPanelGeometry panel = layout.panel();
        OverlayBackdrop.drawDim(shapes);
        ShapeDrawer.fillRect(shapes, panel.panelX, panel.panelY, panel.panelW, panel.panelH, UiColors.PANEL_FILL);
        drawColumnDividers(shapes, panel);
        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        float portraitY = panel.contentBottom;
        float portraitH = panel.headerBottom - lineH * GameConfig.CHARACTER_PORTRAIT_TOP_MULT - panel.contentBottom;
        ShapeDrawer.fillRect(shapes, panel.centerColX, portraitY, panel.columnW, portraitH, UiColors.PORTRAIT_FILL);
        ShapeDrawer.strokeRect(shapes, panel.centerColX, portraitY, panel.columnW, portraitH, UiColors.PORTRAIT_BORDER,
                GameConfig.CHARACTER_SLOT_BORDER_WIDTH);
    }

    private static void drawColumnDividers(ShapeRenderer shapes, CharacterPanelGeometry panel) {
        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        float top = panel.headerBottom - lineH * GameConfig.CHARACTER_COLUMN_DIVIDER_TOP_MULT;
        float bottom = panel.contentBottom;
        float h = top - bottom;
        float w = GameConfig.CHARACTER_COLUMN_DIVIDER_WIDTH;
        float colGap = GameConfig.CHARACTER_PANEL_COLUMN_GAP;
        float x1 = panel.centerColX - colGap / 2f - w / 2f;
        float x2 = panel.rightColX - colGap / 2f - w / 2f;
        ShapeDrawer.fillRect(shapes, x1, bottom, w, h, UiColors.COLUMN_DIVIDER);
        ShapeDrawer.fillRect(shapes, x2, bottom, w, h, UiColors.COLUMN_DIVIDER);
    }
}
