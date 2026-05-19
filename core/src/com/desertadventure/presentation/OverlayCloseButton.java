package com.desertadventure.presentation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.desertadventure.config.GameConfig;

/** Shared top-right dismiss control for fullscreen overlays. */
public final class OverlayCloseButton implements Disposable {
    private final Array<Texture> textures = new Array<>(3);
    private final TextureRegion normal;
    private final TextureRegion hovered;
    private final TextureRegion pressed;

    public OverlayCloseButton() {
        normal = loadRegion(GameConfig.OVERLAY_CLOSE_TEXTURE);
        hovered = loadRegion(GameConfig.OVERLAY_CLOSE_TEXTURE_HOVERED);
        pressed = loadRegion(GameConfig.OVERLAY_CLOSE_TEXTURE_PRESSED);
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
        float bx = x();
        float by = y();
        float s = size();
        return worldX >= bx && worldX <= bx + s && worldY >= by && worldY <= by + s;
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

    private TextureRegion loadRegion(String internalPath) {
        Texture texture = new Texture(Gdx.files.internal(internalPath));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(texture);
        return new TextureRegion(texture);
    }
}
