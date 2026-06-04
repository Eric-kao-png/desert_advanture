package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.UiColors;
import com.desertadventure.screen.layout.CombatCardLayout;

import java.util.ArrayList;
import java.util.List;

/** Renders card details inside the combat info panel. */
public final class CombatInfoPanelDrawer {
    private static final GlyphLayout GLYPH = new GlyphLayout();
    private static final String EMPTY_HINT = "Click a card in hand or a slot.";

    private CombatInfoPanelDrawer() {
    }

    public static void draw(
            SpriteBatch batch,
            BitmapFont font,
            CombatCardLayout layout,
            ActionCardType inspectedType,
            ActionCardInstance inspectedInstance) {
        float pad = GameConfig.COMBAT_INFO_PANEL_PADDING;
        float innerX = layout.infoPanelX + pad;
        float innerW = layout.infoPanelW - 2f * pad;
        float lineStep = GameConfig.COMBAT_INFO_LINE_HEIGHT;
        float y = layout.infoPanelY + layout.infoPanelH - pad;

        float prevScale = font.getData().scaleX;
        font.getData().setScale(GameConfig.COMBAT_INFO_FONT_SCALE);

        List<String> displayLines = inspectedType == null
                ? List.of(EMPTY_HINT)
                : flattenLines(font, CombatCardInfoFormatter.lines(inspectedType, inspectedInstance), innerW);

        for (String line : displayLines) {
            y -= lineStep;
            Color color = inspectedType == null ? UiColors.MUTED_TEXT : Color.WHITE;
            font.setColor(color);
            font.draw(batch, line, innerX, y);
        }

        font.getData().setScale(prevScale);
        font.setColor(Color.WHITE);
    }

    private static List<String> flattenLines(BitmapFont font, List<String> lines, float maxWidth) {
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            out.addAll(wrapLine(font, line, maxWidth));
        }
        return out;
    }

    private static List<String> wrapLine(BitmapFont font, String text, float maxWidth) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return out;
        }
        GLYPH.setText(font, text);
        if (GLYPH.width <= maxWidth) {
            out.add(text);
            return out;
        }
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            GLYPH.setText(font, candidate);
            if (GLYPH.width <= maxWidth) {
                current = new StringBuilder(candidate);
            } else {
                if (!current.isEmpty()) {
                    out.add(current.toString());
                }
                current = new StringBuilder(word);
            }
        }
        if (!current.isEmpty()) {
            out.add(current.toString());
        }
        return out;
    }
}
