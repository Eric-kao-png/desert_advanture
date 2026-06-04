package com.desertadventure.run.data;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageManifestIntegrityTest {
    @Test
    void stageManifest_loadsWithThreeFightsAndOneBoss() throws Exception {
        var path = StageManifestLoader.resolveManifestPath();
        assertTrue(Files.exists(path), "Missing manifest: " + path);

        String json = Files.readString(path, StandardCharsets.UTF_8);
        var catalog = StageManifestLoader.loadFromJson(json, path.toString());

        assertEquals(4, catalog.size());
        assertTrue(catalog.getStage(3).boss());
        assertEquals("DESERT_ZOMBIE", catalog.getStage(0).enemyArchetype().name());
    }
}
