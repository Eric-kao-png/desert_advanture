package com.desertadventure.combat.system.support;

import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardManifestLoader;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import com.desertadventure.combat.enemy.EnemyArchetypeTestSupport;
import com.desertadventure.run.data.StageCatalogDatabase;
import com.desertadventure.run.data.StageManifestLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Loads production card/enemy manifests for integration tests (no LibGDX). */
public final class CombatTestDataBootstrap {
    private CombatTestDataBootstrap() {
    }

    public static void ensureProductionDatabases() throws Exception {
        if (!CardDatabase.isInitialized()) {
            Path cwd = Path.of(System.getProperty("user.dir"));
            Map<String, CardDef> defs = CardManifestLoader.loadMergedFromFiles(
                    CardManifestLoader.resolveDefaultManifestFiles(cwd));
            CardDatabase.initialize(new InMemoryCardRepository(defs));
        }
        EnemyArchetypeTestSupport.ensureLoaded();
        if (!StageCatalogDatabase.isInitialized()) {
            var stagePath = StageManifestLoader.resolveManifestPath();
            String stageJson = Files.readString(stagePath, StandardCharsets.UTF_8);
            StageCatalogDatabase.initialize(StageManifestLoader.loadFromJson(stageJson, stagePath.toString()));
        }
    }
}
