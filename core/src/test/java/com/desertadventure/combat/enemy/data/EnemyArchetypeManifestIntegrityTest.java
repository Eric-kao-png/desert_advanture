package com.desertadventure.combat.enemy.data;

import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnemyArchetypeManifestIntegrityTest {
    @Test
    void enemyManifest_loadsAndCoversAllArchetypeIds() throws Exception {
        var path = EnemyArchetypeManifestLoader.resolveManifestPath();
        assertTrue(Files.exists(path), "Missing manifest: " + path);

        String json = Files.readString(path, StandardCharsets.UTF_8);
        EnemyArchetypeRepository repo = EnemyArchetypeManifestLoader.loadFromJson(json, path.toString());
        assertNotNull(repo);

        for (EnemyArchetypeId id : EnemyArchetypeId.values()) {
            assertNotNull(repo.getRequired(id), "missing archetype: " + id);
        }

        assertTrue(repo.normalEncounterPool().size() >= 1);
        for (EnemyArchetypeId id : repo.normalEncounterPool()) {
            assertNotNull(repo.getRequired(id));
        }

        for (var def : EnemyArchetypeId.values()) {
            for (ActionCardType card : repo.getRequired(def).deckCardTypes()) {
                assertNotNull(card);
            }
            for (ActionCardType card : repo.getRequired(def).lootPool()) {
                assertNotNull(card);
            }
        }
    }
}
