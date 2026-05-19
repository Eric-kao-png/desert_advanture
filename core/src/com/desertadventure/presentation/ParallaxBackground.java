package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.desertadventure.config.GameConfig;
import com.desertadventure.presentation.sprites.TextureLoader;

/** Three-layer parallax background plus tiled floor. */
public class ParallaxBackground implements com.badlogic.gdx.utils.Disposable {
    private static final String PATH_BACK = "backgrounds/parallax_back.png";
    private static final String PATH_MIDDLE = "backgrounds/parallax_middle.png";
    private static final String PATH_FORWARD = "backgrounds/parallax_forward.png";

    private final Texture back;
    private final Texture middle;
    private final Texture forward;
    private final TextureRegion floorTile;
    private float scrollBack;
    private float scrollMiddle;
    private float scrollForward;

    public ParallaxBackground(TextureRegion floorTile) {
        this.floorTile = floorTile;
        back = load(PATH_BACK);
        middle = load(PATH_MIDDLE);
        forward = load(PATH_FORWARD);
    }

    private static Texture load(String path) {
        return TextureLoader.loadLinear(path);
    }

    public void scroll(float delta, boolean running) {
        if (!running) {
            return;
        }
        float base = GameConfig.SCROLL_SPEED * delta;
        scrollForward += base * GameConfig.PARALLAX_FORWARD_MULT;
        scrollMiddle += base * GameConfig.PARALLAX_MIDDLE_MULT;
        scrollBack += base * GameConfig.PARALLAX_BACK_MULT;
    }

    public float getForwardScroll() {
        return scrollForward;
    }

    public void drawBack(SpriteBatch batch, float screenW, float screenH) {
        float drawY = GameConfig.PARALLAX_VERTICAL_OFFSET + GameConfig.PARALLAX_BACK_EXTRA_OFFSET;
        drawTiled(batch, back, scrollBack, screenW, screenH, drawY);
    }

    public void drawMiddle(SpriteBatch batch, float screenW, float screenH) {
        float drawY = GameConfig.PARALLAX_VERTICAL_OFFSET + GameConfig.PARALLAX_MIDDLE_EXTRA_OFFSET;
        drawTiled(batch, middle, scrollMiddle, screenW, screenH, drawY);
    }

    public void drawForward(SpriteBatch batch, float screenW, float screenH) {
        float drawY = GameConfig.PARALLAX_VERTICAL_OFFSET + GameConfig.PARALLAX_FORWARD_EXTRA_OFFSET;
        drawTiled(batch, forward, scrollForward, screenW, screenH, drawY);
    }

    public void drawFloor(SpriteBatch batch, float screenW, float groundY) {
        float tileH = groundY;
        float tileW = tileH * (floorTile.getRegionWidth() / (float) floorTile.getRegionHeight());
        float offset = scrollForward % tileW;
        if (offset < 0f) {
            offset += tileW;
        }
        for (float x = -offset; x < screenW; x += tileW) {
            batch.draw(floorTile, x, 0f, tileW, tileH);
        }
    }

    private static void drawTiled(SpriteBatch batch, Texture texture, float scroll,
                                  float screenW, float screenH, float drawY) {
        float scale = screenH / texture.getHeight();
        float tileW = texture.getWidth() * scale;
        float offset = scroll % tileW;
        if (offset < 0f) {
            offset += tileW;
        }
        for (float x = -offset; x < screenW; x += tileW) {
            batch.draw(texture, x, drawY, tileW, screenH);
        }
    }

    @Override
    public void dispose() {
        back.dispose();
        middle.dispose();
        forward.dispose();
    }
}
