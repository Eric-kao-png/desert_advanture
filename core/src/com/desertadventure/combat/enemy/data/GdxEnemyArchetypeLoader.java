package com.desertadventure.combat.enemy.data;

import com.badlogic.gdx.files.FileHandle;
import com.desertadventure.infrastructure.gdx.AssetFiles;

/** Loads enemy archetypes from LibGDX assets. */
public final class GdxEnemyArchetypeLoader {
    private static final String MANIFEST_PATH = "enemies/enemy_archetypes.json";

    private GdxEnemyArchetypeLoader() {
    }

    public static EnemyArchetypeRepository loadDefault() {
        FileHandle handle = AssetFiles.internal(MANIFEST_PATH);
        if (!handle.exists()) {
            throw new IllegalStateException("Enemy manifest not found: " + MANIFEST_PATH);
        }
        return EnemyArchetypeManifestLoader.loadFromJson(handle.readString(), MANIFEST_PATH);
    }
}
