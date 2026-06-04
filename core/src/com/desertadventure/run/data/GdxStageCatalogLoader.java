package com.desertadventure.run.data;

import com.badlogic.gdx.files.FileHandle;
import com.desertadventure.infrastructure.gdx.AssetFiles;
import com.desertadventure.run.StageCatalog;

/** Loads stage catalog from LibGDX assets. */
public final class GdxStageCatalogLoader {
    private static final String MANIFEST_PATH = "stages/stages.json";

    private GdxStageCatalogLoader() {
    }

    public static StageCatalog loadDefault() {
        FileHandle handle = AssetFiles.internal(MANIFEST_PATH);
        if (!handle.exists()) {
            throw new IllegalStateException("Stage manifest not found: " + MANIFEST_PATH);
        }
        return StageManifestLoader.loadFromJson(handle.readString(), MANIFEST_PATH);
    }
}
