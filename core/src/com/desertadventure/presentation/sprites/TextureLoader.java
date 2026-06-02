package com.desertadventure.presentation.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.desertadventure.infrastructure.gdx.AssetFiles;

/** Internal texture loading with consistent filtering. */
public final class TextureLoader {
    private TextureLoader() {
    }

    public static Texture loadNearest(String internalPath) {
        Texture texture = new Texture(AssetFiles.internal(internalPath));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    public static Texture loadLinear(String internalPath) {
        Texture texture = new Texture(AssetFiles.internal(internalPath));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }
}
