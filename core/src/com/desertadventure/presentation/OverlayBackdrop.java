package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.config.GameConfig;

/** Full-screen dim layer behind map / character overlays. */
public final class OverlayBackdrop {
    private static final Color DIM = new Color(0f, 0f, 0f, GameConfig.MAP_OVERLAY_DIM_ALPHA);

    private OverlayBackdrop() {
    }

    public static void drawDim(ShapeRenderer shapes) {
        ShapeDrawer.fillRect(shapes, 0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT, DIM);
    }
}
