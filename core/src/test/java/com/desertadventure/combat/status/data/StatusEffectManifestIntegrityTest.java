package com.desertadventure.combat.status.data;

import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.model.PositiveStatusType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusEffectManifestIntegrityTest {
    @Test
    void statusManifest_loadsAndCoversAllStatusEnumIds() throws Exception {
        var path = StatusEffectManifestLoader.resolveManifestPath();
        assertTrue(Files.exists(path), "Missing manifest: " + path);

        String json = Files.readString(path, StandardCharsets.UTF_8);
        StatusEffectRepository repo = StatusEffectManifestLoader.loadFromJson(json, path.toString());
        assertNotNull(repo);

        for (NegativeStatusType type : NegativeStatusType.values()) {
            assertNotNull(repo.getNegativeRequired(type), "missing negative status: " + type);
        }
        for (PositiveStatusType type : PositiveStatusType.values()) {
            assertNotNull(repo.getPositiveRequired(type), "missing positive status: " + type);
        }

        assertEquals(2, repo.getNegativeRequired(NegativeStatusType.POISON).roundEnd.amount);
        assertEquals(RoundEndDamageKind.FIXED, repo.getNegativeRequired(NegativeStatusType.POISON).roundEnd.kind);
        assertEquals(RoundEndDamageKind.REMAINING_TURNS,
                repo.getNegativeRequired(NegativeStatusType.BLEED).roundEnd.kind);
        assertEquals("中毒", repo.getNegativeRequired(NegativeStatusType.POISON).name);
    }
}
