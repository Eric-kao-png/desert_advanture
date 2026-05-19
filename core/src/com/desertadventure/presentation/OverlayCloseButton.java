package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.desertadventure.config.GameConfig;
import com.desertadventure.presentation.sprites.TextureLoader;
import com.desertadventure.util.RectHitTest;

import com.badlogic.gdx.graphics.Texture;

/** Shared top-right dismiss control for fullscreen overlays. */
public final class OverlayCloseButton implements Disposable {
    private final Array<Texture> textures = new Array<>(3);
    private final TextureRegion normal;
    private final TextureRegion hovered;
    private final TextureRegion pressed;

    public OverlayCloseButton() {
        normal = region(GameConfig.OVERLAY_CLOSE_TEXTURE);
        hovered = region(GameConfig.OVERLAY_CLOSE_TEXTURE_HOVERED);
        pressed = region(GameConfig.OVERLAY_CLOSE_TEXTURE_PRESSED);
    }

    public static float x() {
        return GameConfig.VIEW_WIDTH - GameConfig.OVERLAY_CLOSE_MARGIN - GameConfig.OVERLAY_CLOSE_SIZE;
    }

    public static float y() {
        return GameConfig.VIEW_HEIGHT - GameConfig.OVERLAY_CLOSE_MARGIN - GameConfig.OVERLAY_CLOSE_SIZE;
    }

    public static float size() {
        return GameConfig.OVERLAY_CLOSE_SIZE;
    }

    public static boolean contains(float worldX, float worldY) {
        return RectHitTest.contains(worldX, worldY, x(), y(), size(), size());
    }

    public void draw(SpriteBatch batch, boolean hovered, boolean pressed) {
        TextureRegion region = pressed ? this.pressed : (hovered ? this.hovered : normal);
        batch.draw(region, x(), y(), size(), size());
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
        textures.clear();
    }

    private TextureRegion region(String internalPath) {
        Texture texture = TextureLoader.loadNearest(internalPath);
        textures.add(texture);
        return new TextureRegion(texture);
    }
}
