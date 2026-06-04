package com.desertadventure.presentation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.desertadventure.config.GameConfig;

/** Dismiss control on the top-right of a selected slotted player card. */
public final class SlotCardDismissButton implements Disposable {
    private final Array<Texture> textures = new Array<>(3);
    private final TextureRegion normal;
    private final TextureRegion hovered;
    private final TextureRegion pressed;

    public SlotCardDismissButton() {
        normal = loadRegion(GameConfig.COMBAT_SLOT_DISMISS_TEXTURE);
        hovered = loadRegion(GameConfig.COMBAT_SLOT_DISMISS_TEXTURE_HOVERED);
        pressed = loadRegion(GameConfig.COMBAT_SLOT_DISMISS_TEXTURE_PRESSED);
    }

    public static boolean contains(float worldX, float worldY, float buttonX, float buttonY) {
        float size = GameConfig.COMBAT_SLOT_DISMISS_SIZE;
        return worldX >= buttonX && worldX <= buttonX + size
                && worldY >= buttonY && worldY <= buttonY + size;
    }

    public void draw(SpriteBatch batch, float buttonX, float buttonY, boolean hovered, boolean pressed) {
        float size = GameConfig.COMBAT_SLOT_DISMISS_SIZE;
        TextureRegion region = pressed ? this.pressed : (hovered ? this.hovered : normal);
        batch.draw(region, buttonX, buttonY, size, size);
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
