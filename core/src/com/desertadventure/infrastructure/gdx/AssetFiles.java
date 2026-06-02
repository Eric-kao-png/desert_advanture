package com.desertadventure.infrastructure.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Centralizes access to LibGDX internal assets.
 *
 * <p>This is a small dependency boundary helper so asset loading sites don't depend on {@link Gdx} directly.</p>
 */
public final class AssetFiles {
    private AssetFiles() {
    }

    public static FileHandle internal(String internalPath) {
        return Gdx.files.internal(internalPath);
    }
}

