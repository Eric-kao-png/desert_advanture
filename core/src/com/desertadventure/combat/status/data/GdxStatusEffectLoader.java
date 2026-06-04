package com.desertadventure.combat.status.data;

import com.badlogic.gdx.files.FileHandle;
import com.desertadventure.infrastructure.gdx.AssetFiles;

/** Loads status effect definitions from LibGDX internal assets. */
public final class GdxStatusEffectLoader {
    private GdxStatusEffectLoader() {
    }

    public static StatusEffectRepository loadDefault() {
        FileHandle handle = AssetFiles.internal(StatusEffectManifestLoader.MANIFEST_PATH);
        if (!handle.exists()) {
            throw new IllegalStateException("Status effect manifest not found: " + StatusEffectManifestLoader.MANIFEST_PATH);
        }
        return StatusEffectManifestLoader.loadFromJson(handle.readString(), StatusEffectManifestLoader.MANIFEST_PATH);
    }
}
