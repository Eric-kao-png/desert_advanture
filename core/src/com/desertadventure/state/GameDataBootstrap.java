package com.desertadventure.state;

import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.GdxCardRepositoryLoader;
import com.desertadventure.combat.enemy.data.EnemyArchetypeDatabase;
import com.desertadventure.combat.enemy.data.GdxEnemyArchetypeLoader;
import com.desertadventure.run.StageCatalog;
import com.desertadventure.run.data.GdxStageCatalogLoader;
import com.desertadventure.run.data.StageCatalogDatabase;
import com.desertadventure.run.data.StageManifestLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** One-time card, enemy, and stage DB initialization for a game session. */
public final class GameDataBootstrap {
    private GameDataBootstrap() {
    }

    public static void initializeIfNeeded() {
        if (!CardDatabase.isInitialized()) {
            CardDatabase.initialize(GdxCardRepositoryLoader.loadDefault());
        }
        if (!EnemyArchetypeDatabase.isInitialized()) {
            EnemyArchetypeDatabase.initialize(GdxEnemyArchetypeLoader.loadDefault());
        }
        if (!StageCatalogDatabase.isInitialized()) {
            StageCatalogDatabase.initialize(loadStageCatalog());
        }
    }

    private static StageCatalog loadStageCatalog() {
        try {
            if (com.badlogic.gdx.Gdx.files != null) {
                return GdxStageCatalogLoader.loadDefault();
            }
        } catch (Exception ignored) {
            // Fall through to filesystem load (unit tests).
        }
        try {
            var path = StageManifestLoader.resolveManifestPath();
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return StageManifestLoader.loadFromJson(json, path.toString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load stage catalog", e);
        }
    }
}
