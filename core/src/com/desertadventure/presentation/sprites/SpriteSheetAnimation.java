package com.desertadventure.presentation.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/** Horizontal strip sprite sheet; one row of equal-sized frames. */
public final class SpriteSheetAnimation implements Disposable {
    private final Texture texture;
    private final TextureRegion[] frames;
    private final float frameDuration;

    public SpriteSheetAnimation(String internalPath, int frameWidth, int frameHeight, float framesPerSecond) {
        texture = TextureLoader.loadNearest(internalPath);
        int frameCount = texture.getWidth() / frameWidth;
        if (frameCount <= 0 || texture.getHeight() < frameHeight) {
            throw new IllegalArgumentException("Invalid sprite sheet: " + internalPath);
        }
        Array<TextureRegion> built = new Array<>(frameCount);
        for (int i = 0; i < frameCount; i++) {
            built.add(new TextureRegion(texture, i * frameWidth, 0, frameWidth, frameHeight));
        }
        frames = built.toArray(TextureRegion.class);
        frameDuration = 1f / Math.max(framesPerSecond, 1f);
    }

    public TextureRegion getFrame(float stateTime) {
        int index = (int) (stateTime / frameDuration) % frames.length;
        return frames[index];
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
