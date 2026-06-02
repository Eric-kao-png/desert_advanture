package com.desertadventure.combat.enemy.data;

import com.badlogic.gdx.utils.Json;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.enemy.EnemyArchetypeDef;
import com.desertadventure.combat.enemy.EnemyArchetypeId;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Parses enemy archetype JSON into an {@link EnemyArchetypeRepository}. */
public final class EnemyArchetypeManifestLoader {
    private EnemyArchetypeManifestLoader() {
    }

    public static EnemyArchetypeRepository loadFromJson(String jsonText, String sourceLabel) {
        EnemyArchetypeManifest manifest = new Json().fromJson(EnemyArchetypeManifest.class, jsonText);
        if (manifest == null || manifest.archetypes == null) {
            throw new IllegalStateException("Invalid enemy manifest: " + sourceLabel);
        }
        Map<EnemyArchetypeId, EnemyArchetypeDef> byId = new EnumMap<>(EnemyArchetypeId.class);
        Set<EnemyArchetypeId> seen = new HashSet<>();
        for (EnemyArchetypeJsonDef jsonDef : manifest.archetypes) {
            if (jsonDef == null || jsonDef.id == null || jsonDef.id.isBlank()) {
                throw new IllegalStateException("Enemy archetype with missing id in: " + sourceLabel);
            }
            EnemyArchetypeId id = parseArchetypeId(jsonDef.id, sourceLabel);
            if (!seen.add(id)) {
                throw new IllegalStateException("Duplicate enemy archetype id: " + jsonDef.id);
            }
            byId.put(id, toDef(id, jsonDef, sourceLabel));
        }
        for (EnemyArchetypeId required : EnemyArchetypeId.values()) {
            if (!byId.containsKey(required)) {
                throw new IllegalStateException(
                        "Manifest missing archetype for EnemyArchetypeId: " + required + " (" + sourceLabel + ")");
            }
        }
        List<EnemyArchetypeId> pool = parsePool(manifest.normalEncounterPool, sourceLabel, byId);
        return new InMemoryEnemyArchetypeRepository(byId, pool);
    }

    public static Path resolveManifestPath() {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path p1 = cwd.resolve("core/assets/enemies/enemy_archetypes.json");
        if (Files.exists(p1)) {
            return p1;
        }
        Path p2 = cwd.resolve("assets/enemies/enemy_archetypes.json");
        if (Files.exists(p2)) {
            return p2;
        }
        return p1;
    }

    public static EnemyArchetypeRepository loadFromProjectAssets() {
        try {
            Path path = resolveManifestPath();
            String jsonText = Files.readString(path, StandardCharsets.UTF_8);
            return loadFromJson(jsonText, path.toString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load enemy manifest from project assets", e);
        }
    }

    private static EnemyArchetypeDef toDef(EnemyArchetypeId id, EnemyArchetypeJsonDef json, String sourceLabel) {
        if (json.name == null || json.name.isBlank()) {
            throw new IllegalStateException("Enemy " + id + " missing name in: " + sourceLabel);
        }
        return new EnemyArchetypeDef(
                id,
                json.name,
                json.hpMin,
                json.hpMax,
                parseCardList(json.deck, "deck", id, sourceLabel),
                parseCardList(json.loot, "loot", id, sourceLabel));
    }

    private static List<EnemyArchetypeId> parsePool(
            List<String> poolIds,
            String sourceLabel,
            Map<EnemyArchetypeId, EnemyArchetypeDef> known) {
        if (poolIds == null || poolIds.isEmpty()) {
            throw new IllegalStateException("normalEncounterPool must be non-empty in: " + sourceLabel);
        }
        List<EnemyArchetypeId> pool = new ArrayList<>();
        for (String rawId : poolIds) {
            EnemyArchetypeId id = parseArchetypeId(rawId, sourceLabel);
            if (!known.containsKey(id)) {
                throw new IllegalStateException("normalEncounterPool references unknown archetype: " + rawId);
            }
            pool.add(id);
        }
        return pool;
    }

    private static EnemyArchetypeId parseArchetypeId(String rawId, String sourceLabel) {
        try {
            return EnemyArchetypeId.valueOf(rawId);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unknown enemy archetype id in " + sourceLabel + ": " + rawId, e);
        }
    }

    private static List<ActionCardType> parseCardList(
            List<String> cardIds,
            String field,
            EnemyArchetypeId archetypeId,
            String sourceLabel) {
        if (cardIds == null || cardIds.isEmpty()) {
            throw new IllegalStateException(
                    "Enemy " + archetypeId + " has empty " + field + " in: " + sourceLabel);
        }
        List<ActionCardType> types = new ArrayList<>();
        for (String cardId : cardIds) {
            if (cardId == null || cardId.isBlank()) {
                throw new IllegalStateException(
                        "Enemy " + archetypeId + " has blank card id in " + field + " (" + sourceLabel + ")");
            }
            try {
                types.add(ActionCardType.valueOf(cardId));
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(
                        "Enemy " + archetypeId + " references unknown card in " + field + ": " + cardId,
                        e);
            }
        }
        return types;
    }
}
