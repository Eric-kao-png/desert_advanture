package com.desertadventure.presentation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.desertadventure.config.GameConfig;

/**
 * Maps window pointer coordinates to fixed game world space (1280x720).
 */
public final class GameViewport {
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(
            GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT, camera);
    private final Vector3 pointer = new Vector3();

    public GameViewport() {
        camera.position.set(GameConfig.VIEW_WIDTH / 2f, GameConfig.VIEW_HEIGHT / 2f, 0f);
        update();
    }

    public void update() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public void toWorld(float screenX, float screenY, Vector3 out) {
        pointer.set(screenX, screenY, 0f);
        viewport.unproject(pointer);
        out.set(pointer.x, pointer.y, 0f);
    }

    public float toWorldX(float screenX, float screenY) {
        toWorld(screenX, screenY, pointer);
        return pointer.x;
    }

    public float toWorldY(float screenX, float screenY) {
        toWorld(screenX, screenY, pointer);
        return pointer.y;
    }

    public com.badlogic.gdx.math.Matrix4 getProjectionMatrix() {
        return camera.combined;
    }
}
