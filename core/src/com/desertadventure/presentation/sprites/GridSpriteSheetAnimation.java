package com.desertadventure.presentation.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Grid sprite sheet animation built from a rectangular range of frames.
 * <p>
 * Frame coordinates use a top-left origin: (0,0) is the top-left cell, x increases right, y increases down.
 * Frames are ordered row-major from (startX,startY) to (endX,endY).
 */
public final class GridSpriteSheetAnimation implements Disposable {
    private final Texture texture;
    private final TextureRegion[] frames;
    private final float frameDuration;

    public GridSpriteSheetAnimation(
            String internalPath,
            int frameWidth,
            int frameHeight,
            int startX,
            int startY,
            int endX,
            int endY,
            float framesPerSecond) {
        texture = TextureLoader.loadNearest(internalPath);
        int cols = Math.max(0, endX - startX + 1);
        int rows = Math.max(0, endY - startY + 1);
        if (cols <= 0 || rows <= 0) {
            throw new IllegalArgumentException("Invalid frame range: (" + startX + "," + startY + ")..(" + endX + "," + endY + ")");
        }
        if (frameWidth <= 0 || frameHeight <= 0) {
            throw new IllegalArgumentException("Invalid frame size: " + frameWidth + "x" + frameHeight);
        }
        if (texture.getWidth() < (endX + 1) * frameWidth || texture.getHeight() < (endY + 1) * frameHeight) {
            throw new IllegalArgumentException("Sprite sheet too small for range: " + internalPath);
        }

        Array<TextureRegion> built = new Array<>(cols * rows);
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                built.add(new TextureRegion(texture, x * frameWidth, y * frameHeight, frameWidth, frameHeight));
            }
        }
        frames = built.toArray(TextureRegion.class);
        frameDuration = 1f / Math.max(framesPerSecond, 1f);
    }

    public int getFrameCount() {
        return frames.length;
    }

    public TextureRegion getLoopFrame(float stateTimeSeconds) {
        int index = (int) (stateTimeSeconds / frameDuration) % frames.length;
        return frames[index];
    }

    /**
     * Returns a non-looping frame for a normalized progress in [0,1].
     * Progress values outside [0,1] are clamped.
     */
    public TextureRegion getOnceFrame(float normalizedProgress) {
        float p = Math.max(0f, Math.min(1f, normalizedProgress));
        int index = (int) Math.floor(p * frames.length);
        if (index >= frames.length) {
            index = frames.length - 1;
        }
        return frames[index];
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}

