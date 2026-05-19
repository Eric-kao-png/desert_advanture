package com.desertadventure.presentation.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads sprite rectangles from {@code data/desert_sprite_sheet.json} and provides {@link TextureRegion}s.
 * <p>
 * JSON rects use {@code bottom-left} image origin: {@code left}, {@code bottom}, {@code right}, {@code top}.
 * Cropping uses Pixmap (file top-left) so regions match image editors.
 */
public class DesertSpriteAtlas implements Disposable {
    public static final String FLOOR_TILE = "floor_tile";

    private static final String DATA_PATH = "data/desert_sprite_sheet.json";

    private final Map<String, TextureRegion> regions = new HashMap<>();
    private final Array<Texture> textures = new Array<>();

    public DesertSpriteAtlas() {
        DesertSpriteSheetData data = new Json().fromJson(DesertSpriteSheetData.class, Gdx.files.internal(DATA_PATH));
        if (data == null || data.sprites == null) {
            throw new IllegalStateException("Missing or empty sprite sheet data: " + DATA_PATH);
        }
        boolean bottomLeft = data.origin == null || "bottom-left".equalsIgnoreCase(data.origin);
        if (!bottomLeft) {
            throw new IllegalStateException("Only bottom-left origin is supported: " + data.origin);
        }
        for (DesertSpriteSheetData.SpriteEntry entry : data.sprites) {
            if (entry.name == null || entry.name.isEmpty()) {
                continue;
            }
            Texture texture = extractSprite(data.texture, entry);
            textures.add(texture);
            regions.put(entry.name, new TextureRegion(texture));
        }
    }

    public TextureRegion get(String name) {
        TextureRegion region = regions.get(name);
        if (region == null) {
            throw new IllegalArgumentException("Unknown desert sprite: " + name);
        }
        return region;
    }

    public boolean has(String name) {
        return regions.containsKey(name);
    }

    private static Texture extractSprite(String atlasPath, DesertSpriteSheetData.SpriteEntry entry) {
        int w = entry.width();
        int h = entry.height();
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Invalid sprite size for " + entry.name);
        }
        Pixmap atlas = new Pixmap(Gdx.files.internal(atlasPath));
        int srcX = entry.left;
        int srcY = atlas.getHeight() - entry.top;
        Pixmap slice = new Pixmap(w, h, atlas.getFormat());
        slice.drawPixmap(atlas, 0, 0, srcX, srcY, w, h);
        atlas.dispose();
        Texture texture = new Texture(slice);
        slice.dispose();
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
        textures.clear();
        regions.clear();
    }
}
