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
}
