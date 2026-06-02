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
        Json json = new Json();
        CardManifest manifest = json.fromJson(CardManifest.class, handle);
        Map<String, CardDef> defs = new HashMap<>();
        for (CardDef def : manifest.cards) {
            defs.put(def.id, def);
        }
        return new InMemoryCardRepository(defs);
    }
}

