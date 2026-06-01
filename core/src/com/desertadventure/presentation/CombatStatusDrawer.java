package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.model.PositiveStatusType;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.UiColors;

/** Positive (left) and negative (right) status labels centered under combat entity feet. */
public final class CombatStatusDrawer {
    private static final GlyphLayout GLYPH = new GlyphLayout();

    private CombatStatusDrawer() {
    }

    public static final class PanelBounds {
        public final float x;
        public final float y;
        public final float width;
        public final float height;

        PanelBounds(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static void drawPanels(ShapeRenderer shapes, CombatEntity entity, BitmapFont font) {
        PanelBounds positive = layoutPositivePanel(entity, font);
        if (positive != null) {
            drawPanel(shapes, positive);
        }
        PanelBounds negative = layoutNegativePanel(entity, font);
        if (negative != null) {
            drawPanel(shapes, negative);
        }
    }

    public static void drawText(SpriteBatch batch, BitmapFont font, CombatEntity entity) {
        drawStatusColumnText(batch, font, entity, true);
        drawStatusColumnText(batch, font, entity, false);
    }

    static String formatPositive(CombatEntity entity) {
        return formatStatusLine(entity.getPositiveStatusType(), entity.getPositiveTurnsRemaining());
    }

    static String formatNegative(CombatEntity entity) {
        return formatStatusLine(entity.getNegativeStatusType(), entity.getNegativeTurnsRemaining());
    }

    static String formatLine(String label, int turns) {
        return label + " " + turns;
    }

    static PanelBounds layoutPositivePanel(CombatEntity entity, BitmapFont font) {
        return layoutColumnPanel(entity, font, formatPositive(entity), true);
    }

    static PanelBounds layoutNegativePanel(CombatEntity entity, BitmapFont font) {
        return layoutColumnPanel(entity, font, formatNegative(entity), false);
    }

    private static PanelBounds layoutColumnPanel(
            CombatEntity entity, BitmapFont font, String text, boolean leftColumn) {
        if (text.isEmpty()) {
            return null;
        }
        GLYPH.setText(font, text);
        float padH = GameConfig.COMBAT_STATUS_PANEL_PADDING_H;
        float padV = GameConfig.COMBAT_STATUS_PANEL_PADDING_V;
        float panelW = GLYPH.width + padH * 2f;
        float panelH = GLYPH.height + padV * 2f;
        float centerX = entity.getX();
        float feetY = entity.getY();
        float panelY = feetY - GameConfig.COMBAT_STATUS_BELOW_FEET_GAP - panelH;
        float columnOffset = GameConfig.COMBAT_STATUS_COLUMN_OFFSET;
        float panelX = leftColumn
                ? centerX - columnOffset - panelW
                : centerX + columnOffset;
        return new PanelBounds(panelX, panelY, panelW, panelH);
    }

    private static void drawPanel(ShapeRenderer shapes, PanelBounds panel) {
        ShapeDrawer.fillRect(shapes, panel.x, panel.y, panel.width, panel.height,
                UiColors.COMBAT_STATUS_PANEL_FILL);
        ShapeDrawer.strokeRect(shapes, panel.x, panel.y, panel.width, panel.height,
                UiColors.COMBAT_STATUS_PANEL_BORDER, GameConfig.COMBAT_STATUS_BORDER_WIDTH);
    }

    private static void drawColumnText(
            SpriteBatch batch, BitmapFont font, PanelBounds panel, String text, Color color) {
        if (panel == null || text.isEmpty()) {
            return;
        }
        GLYPH.setText(font, text);
        float padH = GameConfig.COMBAT_STATUS_PANEL_PADDING_H;
        float padV = GameConfig.COMBAT_STATUS_PANEL_PADDING_V;
        float textX = panel.x + padH + (panel.width - padH * 2f - GLYPH.width) / 2f;
        float textY = panel.y + padV + GLYPH.height;
        font.setColor(color);
        font.draw(batch, text, textX, textY);
    }

    private static String formatStatusLine(PositiveStatusType statusType, int turns) {
        if (statusType == null || turns <= 0) {
            return "";
        }
        return formatLine(statusType.getDisplayLabel(), turns);
    }

    private static String formatStatusLine(NegativeStatusType statusType, int turns) {
        if (statusType == null || turns <= 0) {
            return "";
        }
        return formatLine(statusType.getDisplayLabel(), turns);
    }

    private static void drawStatusColumnText(
            SpriteBatch batch, BitmapFont font, CombatEntity entity, boolean positiveColumn) {
        PanelBounds panel = positiveColumn ? layoutPositivePanel(entity, font) : layoutNegativePanel(entity, font);
        String text = positiveColumn ? formatPositive(entity) : formatNegative(entity);
        Color color = positiveColumn ? UiColors.COMBAT_STATUS_POSITIVE_TEXT : UiColors.COMBAT_STATUS_NEGATIVE_TEXT;
        drawColumnText(batch, font, panel, text, color);
    }

}
