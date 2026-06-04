package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.combat.card.ActionCardType;

/** Card name labels on combat cards. */
public final class ActionCardUiText {
    private static final GlyphLayout GLYPH = new GlyphLayout();

    private ActionCardUiText() {
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
        String name = type.getDisplayName();
        GLYPH.setText(font, name);
        font.setColor(nameColor);
        float nameX = cardX + (cardW - GLYPH.width) / 2f;
        float nameY = cardY + (cardH + GLYPH.height) / 2f;
        font.draw(batch, name, nameX, nameY);
    }
}
