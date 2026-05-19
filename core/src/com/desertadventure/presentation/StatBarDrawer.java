package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/** Filled progress bar for character HUD. */
public final class StatBarDrawer {
    private StatBarDrawer() {
    }

    public static void draw(
            ShapeRenderer shapes,
            float x,
            float y,
            float width,
            float height,
            float fillRatio,
            Color background,
            Color fill,
            Color border) {
        float ratio = MathUtils.clamp(fillRatio, 0f, 1f);
        ShapeDrawer.fillRect(shapes, x, y, width, height, background);
        if (ratio > 0f) {
            ShapeDrawer.fillRect(shapes, x, y, width * ratio, height, fill);
        }
        ShapeDrawer.strokeRect(shapes, x, y, width, height, border, 1.5f);
    }
}
