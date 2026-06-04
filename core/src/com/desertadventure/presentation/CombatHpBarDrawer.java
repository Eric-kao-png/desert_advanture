package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.UiColors;

/** HP and shield bars drawn below the entity sprite, plus numeric labels. */
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
        return layout(entity.getKind(), entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
    }

    public static BarBounds layout(float centerX, float bottomY, float entityWidth, float entityHeight) {
        return layout(null, centerX, bottomY, entityWidth, entityHeight);
    }

    private static BarBounds layout(
            CombatEntity.Kind kind,
            float centerX,
            float bottomY,
            float entityWidth,
            float entityHeight) {
        float barW = hpBarWidth(kind, entityWidth);
        float barH = GameConfig.COMBAT_HP_BAR_HEIGHT;
        float barX = centerX - barW / 2f;
        float barY = bottomY - GameConfig.COMBAT_HP_BAR_GAP - barH;
        return new BarBounds(barX, barY, barW, barH);
    }

    private static float hpBarWidth(CombatEntity.Kind kind, float entityWidth) {
        if (kind == CombatEntity.Kind.PLAYER || kind == CombatEntity.Kind.ENEMY) {
            return fighterHpBarWidth();
        }
        return Math.max(entityWidth * GameConfig.COMBAT_HP_BAR_WIDTH_SCALE,
                GameConfig.COMBAT_HP_BAR_MIN_WIDTH);
    }

    private static float fighterHpBarWidth() {
        return Math.max(GameConfig.ENEMY_WIDTH * GameConfig.COMBAT_HP_BAR_WIDTH_SCALE,
                GameConfig.COMBAT_HP_BAR_MIN_WIDTH);
    }

    /** Bottom-most combat bar for an entity (shield if present, otherwise HP). */
    public static BarBounds lowestBarBounds(CombatEntity entity) {
        BarBounds hpBar = layout(entity);
        if (entity.getShield() > 0) {
            return layoutShieldBar(hpBar);
        }
        return hpBar;
    }

    /** Shield bar directly below the HP bar (lower on screen). */
    public static BarBounds layoutShieldBar(BarBounds hpBar) {
        float barH = GameConfig.COMBAT_SHIELD_BAR_HEIGHT;
        float barY = hpBar.y - GameConfig.COMBAT_SHIELD_BAR_GAP - barH;
        return new BarBounds(hpBar.x, barY, hpBar.width, barH);
    }

    public static void draw(ShapeRenderer shapes, CombatEntity entity) {
        draw(shapes, layout(entity), entity.getHp(), entity.getMaxHp(), entity.getShield());
    }

    public static void draw(
            ShapeRenderer shapes,
            float centerX,
            float bottomY,
            float entityWidth,
            float entityHeight,
            float hp,
            float maxHp) {
        draw(shapes, layout(centerX, bottomY, entityWidth, entityHeight), hp, maxHp, 0);
    }

    public static void draw(ShapeRenderer shapes, BarBounds hpBar, float hp, float maxHp, int shield) {
        float hpRatio = maxHp > 0f ? hp / maxHp : 0f;
        StatBarDrawer.draw(shapes, hpBar.x, hpBar.y, hpBar.width, hpBar.height, hpRatio,
                UiColors.COMBAT_HP_BAR_BG, UiColors.COMBAT_HP_BAR_FILL, UiColors.COMBAT_HP_BAR_BORDER,
                GameConfig.COMBAT_HP_BAR_BORDER_WIDTH);
        if (shield > 0) {
            BarBounds shieldBar = layoutShieldBar(hpBar);
            float ratio = Math.min(1f, shield / GameConfig.COMBAT_SHIELD_BAR_DISPLAY_MAX);
            StatBarDrawer.draw(shapes, shieldBar.x, shieldBar.y, shieldBar.width, shieldBar.height, ratio,
                    UiColors.COMBAT_SHIELD_BAR_BG, UiColors.COMBAT_SHIELD_BAR_FILL,
                    UiColors.COMBAT_SHIELD_BAR_BORDER, GameConfig.COMBAT_HP_BAR_BORDER_WIDTH);
        }
    }

    public static void drawHpText(SpriteBatch batch, BitmapFont font, CombatEntity entity) {
        BarBounds hpBar = layout(entity);
        drawHpText(batch, font, hpBar, entity.getHp(), entity.getMaxHp());
        if (entity.getShield() > 0) {
            drawShieldText(batch, font, layoutShieldBar(hpBar), entity.getShield());
        }
    }

    public static void drawHpText(SpriteBatch batch, BitmapFont font, BarBounds bar, float hp, float maxHp) {
        String text = formatHpText(hp, maxHp);
        drawValueTextLeftOfBar(batch, font, bar, text);
    }

    public static void drawShieldText(SpriteBatch batch, BitmapFont font, BarBounds shieldBar, int shield) {
        drawValueTextLeftOfBar(batch, font, shieldBar, Integer.toString(shield));
    }

    /** Centered label above the top of the entity sprite. */
    public static void drawOpponentNameAboveBar(
            SpriteBatch batch, BitmapFont font, CombatEntity entity, String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return;
        }
        GLYPH.setText(font, displayName);
        float textX = entity.getX() - GLYPH.width / 2f;
        float textY = entity.getY() + entity.getHeight() + GameConfig.COMBAT_OPPONENT_NAME_GAP;
        font.setColor(UiColors.SECTION_LABEL);
        font.draw(batch, displayName, textX, textY);
        font.setColor(Color.WHITE);
    }

    private static void drawValueTextLeftOfBar(
            SpriteBatch batch, BitmapFont font, BarBounds bar, String text) {
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
