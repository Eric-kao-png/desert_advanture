package com.desertadventure.combat.card.data;

import com.badlogic.gdx.utils.Json;
import com.desertadventure.combat.card.ActionCardType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JSON-driven data integrity checks for action cards.
 *
 * <p>Reads the manifest from the repository (without using Gdx.files / LibGDX runtime init).</p>
 */
public class CardManifestIntegrityTest {
    @Test
    void actionCardsManifest_hasUniqueNonBlankIds_andCoversAllActionCardTypes() throws Exception {
        Path manifestPath = resolveManifestPath();
        assertTrue(Files.exists(manifestPath), "Missing manifest file: " + manifestPath);

        String jsonText = Files.readString(manifestPath, StandardCharsets.UTF_8);
        CardManifest manifest = new Json().fromJson(CardManifest.class, jsonText);
        assertNotNull(manifest);
        assertNotNull(manifest.cards);

        Set<String> ids = new HashSet<>();
        for (CardDef def : manifest.cards) {
            assertNotNull(def, "manifest contains null card");
            assertNotNull(def.id, "card id must not be null");
            assertTrue(!def.id.isBlank(), "card id must not be blank");
            assertTrue(ids.add(def.id), "duplicate card id: " + def.id);
            assertNotNull(def.description, "card description must not be null: " + def.id);
            assertTrue(!def.description.isBlank(), "card description must not be blank: " + def.id);
        }

        for (ActionCardType type : ActionCardType.values()) {
            assertTrue(ids.contains(type.name()), "manifest missing card id for ActionCardType: " + type.name());
        }
    }

    private static Path resolveManifestPath() {
        // When running via Gradle, user.dir is typically the repo root.
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path p1 = cwd.resolve("core/assets/cards/action_cards.json");
        if (Files.exists(p1)) {
            return p1;
        }
        // Fallback for IDE runs started from core/ module.
        Path p2 = cwd.resolve("assets/cards/action_cards.json");
        if (Files.exists(p2)) {
            return p2;
        }
        return p1;
    }
}

