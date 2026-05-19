package com.desertadventure.presentation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.desertadventure.config.GameConfig;

/** Fixed sky fill and sun; drawn before scrolling parallax layers. */
public class SkyBackdrop implements com.badlogic.gdx.utils.Disposable {
    private final Texture pixel;
    private final Texture sunTexture;
    private final TextureRegion sun;

    public SkyBackdrop() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();
        pixel.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        sunTexture = new Texture(Gdx.files.internal(GameConfig.SUN_TEXTURE_PATH));
        sunTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sun = new TextureRegion(sunTexture);
    }

    public void draw(SpriteBatch batch, float screenW, float screenH) {
        Color previous = batch.getColor().cpy();
        batch.setColor(GameConfig.SKY_BASE_COLOR);
        batch.draw(pixel, 0f, 0f, screenW, screenH);
        batch.setColor(previous);

        float size = GameConfig.SUN_DISPLAY_SIZE;
        batch.draw(sun, GameConfig.SUN_X, GameConfig.SUN_Y, size, size);
    }

    @Override
    public void dispose() {
        pixel.dispose();
        sunTexture.dispose();
    }
}
