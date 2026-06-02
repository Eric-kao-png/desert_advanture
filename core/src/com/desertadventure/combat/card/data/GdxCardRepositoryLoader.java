package com.desertadventure.combat.card.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import java.util.HashMap;
import java.util.Map;

/** Loads card definitions from LibGDX assets. */
public final class GdxCardRepositoryLoader {
    private static final String MANIFEST_PATH = "cards/action_cards.json";

    private GdxCardRepositoryLoader() {
    }

    public static CardRepository loadDefault() {
        FileHandle handle = Gdx.files.internal(MANIFEST_PATH);
        if (!handle.exists()) {
            throw new IllegalStateException("Card manifest not found: " + MANIFEST_PATH);
        }
        Json json = new Json();
        CardManifest manifest = json.fromJson(CardManifest.class, handle);
        if (manifest == null || manifest.cards == null) {
            throw new IllegalStateException("Invalid card manifest: " + MANIFEST_PATH);
        }
        Map<String, CardDef> defs = new HashMap<>();
        for (CardDef def : manifest.cards) {
            if (def == null || def.id == null || def.id.isBlank()) {
                throw new IllegalStateException("Card with missing id in manifest: " + MANIFEST_PATH);
            }
            if (defs.put(def.id, def) != null) {
                throw new IllegalStateException("Duplicate card id in manifest: " + def.id);
            }
        }
        return new InMemoryCardRepository(defs);
    }
}

