package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Shared {@link ShapeRenderer} fill helpers. */
public final class ShapeDrawer {
    private ShapeDrawer() {
    }

    public static void fillRect(ShapeRenderer shapes, float x, float y, float w, float h, Color color) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(color);
        shapes.rect(x, y, w, h);
        shapes.end();
    }

    public static void strokeRect(
            ShapeRenderer shapes, float x, float y, float w, float h, Color color, float thickness) {
        fillRect(shapes, x, y, w, thickness, color);
        fillRect(shapes, x, y + h - thickness, w, thickness, color);
        fillRect(shapes, x, y, thickness, h, color);
        fillRect(shapes, x + w - thickness, y, thickness, h, color);
    }
}
