package com.desertadventure.exploration;

import com.desertadventure.config.GameConfig;

/**
 * Animates straight-line travel between two world positions (cell centers use integers).
 */
public class PathRunner {
    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private float totalDistance;
    private float duration;
    private float elapsed;
    private boolean running;
    private boolean paused;
    private Runnable onPathComplete;

    public void startStraightMove(float fromX, float fromY, float toX, float toY, float distance,
                                    Runnable onComplete) {
        this.startX = fromX;
        this.startY = fromY;
        this.endX = toX;
        this.endY = toY;
        this.totalDistance = distance;
        this.duration = Math.max(0.15f, distance * GameConfig.TILE_TRAVEL_SECONDS);
        this.elapsed = 0f;
        this.running = true;
        this.paused = false;
        this.onPathComplete = onComplete;
    }

    /** Stops animation but keeps position and progress for {@link #resume()}. */
    public void pause() {
        if (!running) {
            return;
        }
        running = false;
        paused = true;
    }

    public void resume() {
        if (!paused) {
            return;
        }
        paused = false;
        running = true;
    }

    public boolean isPaused() {
        return paused;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public void cancel() {
        running = false;
        paused = false;
        elapsed = 0f;
    }

    public boolean isRunning() {
        return running;
    }

    public float getProgress() {
        if (duration <= 0f) {
            return 1f;
        }
        return Math.min(1f, elapsed / duration);
    }

    public float getCurrentX() {
        float t = getProgress();
        return startX + (endX - startX) * t;
    }

    public float getCurrentY() {
        float t = getProgress();
        return startY + (endY - startY) * t;
    }

    public void update(float delta) {
        if (!running) {
            return;
        }
        elapsed += delta;
        if (elapsed >= duration) {
            running = false;
            if (onPathComplete != null) {
                onPathComplete.run();
            }
        }
    }

    public float getScrollOffset(float delta) {
        if (!running) {
            return 0f;
        }
        return GameConfig.SCROLL_SPEED * delta;
    }
}
