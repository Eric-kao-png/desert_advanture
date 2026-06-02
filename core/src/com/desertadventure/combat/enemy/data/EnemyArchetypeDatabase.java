package com.desertadventure.combat.enemy.data;

import com.desertadventure.combat.enemy.EnemyArchetypeDef;
import com.desertadventure.combat.enemy.EnemyArchetypeId;

import java.util.List;

/** Global enemy archetype access for the current run (initialized at session start). */
public final class EnemyArchetypeDatabase {
    private static volatile EnemyArchetypeRepository repository;

    private EnemyArchetypeDatabase() {
    }

    public static void initialize(EnemyArchetypeRepository repo) {
        if (repo == null) {
            throw new IllegalArgumentException("repo must not be null");
        }
        repository = repo;
    }

    public static boolean isInitialized() {
        return repository != null;
    }

    public static EnemyArchetypeRepository getRequired() {
        EnemyArchetypeRepository repo = repository;
        if (repo == null) {
            throw new IllegalStateException("EnemyArchetypeDatabase not initialized");
        }
        return repo;
    }

    public static EnemyArchetypeDef getRequired(EnemyArchetypeId id) {
        return getRequired().getRequired(id);
    }

    public static List<EnemyArchetypeId> normalEncounterPool() {
        return getRequired().normalEncounterPool();
    }
}
