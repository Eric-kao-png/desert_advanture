package com.desertadventure.combat.enemy;

import com.desertadventure.combat.enemy.data.EnemyArchetypeDatabase;
import com.desertadventure.combat.system.slots.RandomIntSource;

import java.util.List;

/** Facade for enemy archetype lookup and encounter selection (data loaded from JSON). */
public final class EnemyArchetypeRegistry {
    private EnemyArchetypeRegistry() {
    }

    public static EnemyArchetypeDef getRequired(EnemyArchetypeId id) {
        return EnemyArchetypeDatabase.getRequired(id);
    }

    /** Deterministic mix for map combat tiles: cycles through {@code normalEncounterPool} order. */
    public static EnemyArchetypeId pickForMapCoordinate(int worldX, int worldY) {
        List<EnemyArchetypeId> pool = EnemyArchetypeDatabase.normalEncounterPool();
        int index = Math.floorMod(worldX + worldY, pool.size());
        return pool.get(index);
    }

    /** Uses tile archetype when set; otherwise rolls from the normal encounter pool. */
    public static EnemyArchetypeId resolveNormalEncounter(EnemyArchetypeId tileArchetype, RandomIntSource rng) {
        return tileArchetype != null ? tileArchetype : rollNormalEncounter(rng);
    }

    /** Picks a normal (non-boss) encounter archetype uniformly from the pool. */
    public static EnemyArchetypeId rollNormalEncounter(RandomIntSource rng) {
        List<EnemyArchetypeId> pool = EnemyArchetypeDatabase.normalEncounterPool();
        if (pool.isEmpty()) {
            throw new IllegalStateException("No normal enemy archetypes in encounter pool");
        }
        return pool.get(rng.nextInt(pool.size()));
    }
}
