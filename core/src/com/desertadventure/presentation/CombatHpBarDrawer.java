package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.UiColors;

/** Overhead HP bar and numeric label for a combat entity (player, enemy, or boss). */
public final class CombatHpBarDrawer {
    private static final GlyphLayout GLYPH = new GlyphLayout();

    private CombatHpBarDrawer() {
    }

    /** Screen-space bounds of the bar after {@link #draw}. */
    public static final class BarBounds {
        public final float x;
        public final float y;
        public final float width;
        public final float height;

        BarBounds(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static BarBounds layout(CombatEntity entity) {
        return layout(entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
    }

    public static BarBounds layout(float centerX, float bottomY, float entityWidth, float entityHeight) {
        float barW = Math.max(entityWidth * GameConfig.COMBAT_HP_BAR_WIDTH_SCALE,
                GameConfig.COMBAT_HP_BAR_MIN_WIDTH);
        float barH = GameConfig.COMBAT_HP_BAR_HEIGHT;
        float barX = centerX - barW / 2f;
        float barY = bottomY + entityHeight + GameConfig.COMBAT_HP_BAR_GAP;
        return new BarBounds(barX, barY, barW, barH);
    }

    public static void draw(ShapeRenderer shapes, CombatEntity entity) {
        draw(shapes, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight(),
                entity.getHp(), entity.getMaxHp());
    }

    public static void draw(
            ShapeRenderer shapes,
            float centerX,
            float bottomY,
            float entityWidth,
            float entityHeight,
            float hp,
            float maxHp) {
        BarBounds bar = layout(centerX, bottomY, entityWidth, entityHeight);
        float ratio = maxHp > 0f ? hp / maxHp : 0f;
        StatBarDrawer.draw(shapes, bar.x, bar.y, bar.width, bar.height, ratio,
                UiColors.COMBAT_HP_BAR_BG, UiColors.COMBAT_HP_BAR_FILL, UiColors.COMBAT_HP_BAR_BORDER,
                GameConfig.COMBAT_HP_BAR_BORDER_WIDTH);
    }

    public static void drawHpText(SpriteBatch batch, BitmapFont font, CombatEntity entity) {
        drawHpText(batch, font, layout(entity), entity.getHp(), entity.getMaxHp());
    }

    public static void drawHpText(SpriteBatch batch, BitmapFont font, BarBounds bar, float hp, float maxHp) {
        String text = formatHpText(hp, maxHp);
        GLYPH.setText(font, text);
        float textX = bar.x - GameConfig.COMBAT_HP_BAR_TEXT_GAP - GLYPH.width;
        float textY = bar.y + (bar.height + GLYPH.height) / 2f;
        font.setColor(UiColors.BAR_VALUE_TEXT);
        font.draw(batch, text, textX, textY);
    }

    static String formatHpText(float hp, float maxHp) {
        int current = MathUtils.ceil(hp);
        int max = MathUtils.ceil(maxHp);
        if (max <= MathUtils.ceil(GameConfig.COMBAT_HP_BAR_COMPACT_MAX_HP)) {
            return Integer.toString(current);
        }
        return current + "/" + max;
    }
}
