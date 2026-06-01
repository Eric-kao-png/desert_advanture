package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardMechanic;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.config.GameMessages;
import com.desertadventure.config.UiColors;

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

    public static void drawCardFaceCentered(
            SpriteBatch batch,
            BitmapFont font,
            ActionCardType type,
            float cardX,
            float cardY,
            float cardW,
            float cardH,
            Color nameColor) {
        String category = type.getCategory().getDisplayName();
        GLYPH.setText(font, category);
        font.setColor(UiColors.MUTED_TEXT);
        float categoryX = cardX + (cardW - GLYPH.width) / 2f;
        float categoryY = cardY + cardH * 0.62f;
        font.draw(batch, category, categoryX, categoryY);

        String name = type.getDisplayName();
        GLYPH.setText(font, name);
        font.setColor(nameColor);
        float nameX = cardX + (cardW - GLYPH.width) / 2f;
        float nameY = cardY + cardH * 0.36f;
        font.draw(batch, name, nameX, nameY);
    }

    public static String[] detailLines(ActionCardInstance card) {
        ActionCardType type = card.getType();
        return new String[] {
                type.getDisplayName(),
                GameMessages.cardCategoryTooltip(type.getCategory()),
                effectLabel(type),
                cooldownLabel(card),
        };
    }

    public static String[] enemyAttackDetailLines() {
        ActionCardType attack = ActionCardType.ATTACK;
        return new String[] {
                attack.getDisplayName(),
                GameMessages.cardCategoryTooltip(attack.getCategory()),
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
        return switch (type.getMechanic()) {
            case DAMAGE -> "Damage: " + type.getPrimaryValue();
            case HEAL -> "Heal: " + type.getPrimaryValue();
            case SHIELD -> "Shield: +" + type.getPrimaryValue();
            case FULL_POWER_ATTACK -> "Damage: " + type.getPrimaryValue()
                    + " (or " + type.getSecondaryValue() + " if no Utility)";
            case HALVE_ENEMY_HP -> "Enemy HP halved";
            case THRUST -> "Damage: " + type.getSecondaryValue() + " round 1, else "
                    + type.getPrimaryValue();
            case POISON -> "Poison: " + type.getPrimaryValue() + " rds, "
                    + type.getSecondaryValue() + " dmg/round";
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
