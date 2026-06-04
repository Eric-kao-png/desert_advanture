package com.desertadventure.combat.card.data;

import com.desertadventure.combat.card.ActionCardType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JSON-driven data integrity checks for action cards.
 *
 * <p>Reads offense + change manifests from the repository (without LibGDX init).</p>
 */
public class CardManifestIntegrityTest {
    @Test
    void cardManifests_haveUniqueNonBlankIds_andCoversAllActionCardTypes() throws Exception {
        Path cwd = Path.of(System.getProperty("user.dir"));
        var files = CardManifestLoader.resolveDefaultManifestFiles(cwd);
        for (Path file : files) {
            assertTrue(java.nio.file.Files.exists(file), "Missing manifest file: " + file);
        }

        Map<String, CardDef> defs = CardManifestLoader.loadMergedFromFiles(files);

        Set<String> ids = new HashSet<>();
        for (CardDef def : defs.values()) {
            assertNotNull(def, "manifest contains null card");
            assertNotNull(def.id, "card id must not be null");
            assertTrue(!def.id.isBlank(), "card id must not be blank");
            assertTrue(ids.add(def.id), "duplicate card id: " + def.id);
            assertNotNull(def.description, "card description must not be null: " + def.id);
            assertTrue(!def.description.isBlank(), "card description must not be blank: " + def.id);
        }

        assertEquals(ActionCardType.values().length, ids.size(),
                "manifest card count should match ActionCardType enum");

        for (ActionCardType type : ActionCardType.values()) {
            assertTrue(ids.contains(type.name()), "manifest missing card id for ActionCardType: " + type.name());
        }
    }

    @Test
    void offenseManifest_containsOnlyOffenseCategory() throws Exception {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path offense = CardManifestLoader.resolveDefaultManifestFiles(cwd).get(0);
        Map<String, CardDef> defs = CardManifestLoader.loadMergedFromFiles(java.util.List.of(offense));
        for (CardDef def : defs.values()) {
            assertEquals(CardCategoryId.OFFENSE, def.category,
                    "offense_cards.json must only contain OFFENSE: " + def.id);
        }
    }

    @Test
    void changeManifest_containsOnlyUtilityCategory() throws Exception {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path change = CardManifestLoader.resolveDefaultManifestFiles(cwd).get(1);
        Map<String, CardDef> defs = CardManifestLoader.loadMergedFromFiles(java.util.List.of(change));
        for (CardDef def : defs.values()) {
            assertEquals(CardCategoryId.UTILITY, def.category,
                    "change_cards.json must only contain UTILITY: " + def.id);
        }
    }
}
