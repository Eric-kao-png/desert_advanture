package com.desertadventure.combat.system.support;

import com.badlogic.gdx.utils.Json;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardManifest;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import com.desertadventure.combat.enemy.EnemyArchetypeTestSupport;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Loads production card/enemy manifests for integration tests (no LibGDX). */
public final class CombatTestDataBootstrap {
    private CombatTestDataBootstrap() {
    }

    public static void ensureProductionDatabases() throws Exception {
        if (!CardDatabase.isInitialized()) {
            Path manifestPath = resolveCardManifestPath();
            String jsonText = Files.readString(manifestPath, StandardCharsets.UTF_8);
            CardManifest manifest = new Json().fromJson(CardManifest.class, jsonText);
            Map<String, CardDef> defs = new HashMap<>();
            for (CardDef def : manifest.cards) {
                defs.put(def.id, def);
            }
            CardDatabase.initialize(new InMemoryCardRepository(defs));
        }
        EnemyArchetypeTestSupport.ensureLoaded();
    }

    private static Path resolveCardManifestPath() {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path fromRoot = cwd.resolve("core/assets/cards/action_cards.json");
        if (Files.exists(fromRoot)) {
            return fromRoot;
        }
        return cwd.resolve("assets/cards/action_cards.json");
    }
}
