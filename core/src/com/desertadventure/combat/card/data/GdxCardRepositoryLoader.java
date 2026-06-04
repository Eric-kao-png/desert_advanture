package com.desertadventure.combat.card.data;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.desertadventure.infrastructure.gdx.AssetFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Loads card definitions from LibGDX assets (offense + change manifests). */
public final class GdxCardRepositoryLoader {
    private GdxCardRepositoryLoader() {
    }

    public static CardRepository loadDefault() {
        List<String> paths = CardManifestLoader.defaultManifestPaths();
        List<String> texts = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (String path : paths) {
            FileHandle handle = AssetFiles.internal(path);
            if (!handle.exists()) {
                throw new IllegalStateException("Card manifest not found: " + path);
            }
            texts.add(handle.readString());
            labels.add(path);
        }
        Map<String, CardDef> defs = CardManifestLoader.loadMergedFromJsonTexts(texts, labels);
        return new InMemoryCardRepository(defs);
    }
}
