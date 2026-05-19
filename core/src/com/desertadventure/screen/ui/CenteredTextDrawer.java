package com.desertadventure.screen.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.config.GameConfig;

/** Horizontally centered HUD/menu text. */
public final class CenteredTextDrawer {
    private static final GlyphLayout GLYPH = new GlyphLayout();

    private CenteredTextDrawer() {
    }

    public static void draw(SpriteBatch batch, BitmapFont font, String text, float y) {
        GLYPH.setText(font, text);
        font.draw(batch, text, (GameConfig.VIEW_WIDTH - GLYPH.width) / 2f, y);
    }
}
