package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
/** Labels for combat action cards (compact name vs hover detail). */
public final class ActionCardUiText {
    private static final GlyphLayout GLYPH = new GlyphLayout();
    private static final float TOOLTIP_LINE_HEIGHT = 18f;
    private static final float TOOLTIP_PAD = 8f;

    private ActionCardUiText() {
    }

    public static void drawNameCentered(
            SpriteBatch batch,
            BitmapFont font,
            String name,
            float cardX,
            float cardY,
            float cardW,
            float cardH,
            Color color) {
        GLYPH.setText(font, name);
        font.setColor(color);
        float textX = cardX + (cardW - GLYPH.width) / 2f;
        float textY = cardY + (cardH + GLYPH.height) / 2f;
        font.draw(batch, name, textX, textY);
    }

    public static String[] detailLines(ActionCardInstance card) {
        ActionCardType type = card.getType();
        return new String[] {
                type.getDisplayName(),
                effectLabel(type),
                cooldownLabel(card),
        };
    }

    public static String[] enemyAttackDetailLines() {
        ActionCardType attack = ActionCardType.ATTACK;
        return new String[] {
                attack.getDisplayName(),
                "Damage: " + attack.getPrimaryValue(),
                "Cooldown: " + attack.getCooldownTurns(),
        };
    }

    public static TooltipBounds measureTooltip(BitmapFont font, String[] lines) {
        float maxW = 0f;
        for (String line : lines) {
            GLYPH.setText(font, line);
            maxW = Math.max(maxW, GLYPH.width);
        }
        float w = maxW + TOOLTIP_PAD * 2f;
        float h = lines.length * TOOLTIP_LINE_HEIGHT + TOOLTIP_PAD * 2f;
        return new TooltipBounds(w, h);
    }

    public static void drawTooltipText(
            SpriteBatch batch,
            BitmapFont font,
            String[] lines,
            float panelX,
            float panelY,
            Color textColor) {
        font.setColor(textColor);
        float textY = panelY + TOOLTIP_PAD + (lines.length - 1) * TOOLTIP_LINE_HEIGHT + 14f;
        for (String line : lines) {
            font.draw(batch, line, panelX + TOOLTIP_PAD, textY);
            textY -= TOOLTIP_LINE_HEIGHT;
        }
    }

    public static float tooltipPanelYAbove(float cardY, float cardH, float panelH) {
        return cardY + cardH + 6f;
    }

    public static float tooltipPanelXCentered(float cardX, float cardW, float panelW) {
        return cardX + (cardW - panelW) / 2f;
    }

    private static String effectLabel(ActionCardType type) {
        return switch (type.getTarget()) {
            case ENEMY -> "Damage: " + type.getPrimaryValue();
            case SELF -> "Heal: " + type.getPrimaryValue();
        };
    }

    private static String cooldownLabel(ActionCardInstance card) {
        ActionCardType type = card.getType();
        if (card.isOnCooldown()) {
            return "Cooldown: " + card.getCooldownRemaining() + " left";
        }
        return "Cooldown: " + type.getCooldownTurns();
    }

    public record TooltipBounds(float width, float height) {
    }
}
