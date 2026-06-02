package com.desertadventure.combat.enemy;

import com.desertadventure.combat.card.ActionCardType;

import com.desertadventure.combat.system.slots.RandomIntSource;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Static registry of enemy archetypes. Add new entries here for additional enemy types. */
public final class EnemyArchetypeRegistry {
    private static final Map<EnemyArchetypeId, EnemyArchetypeDef> DEFS = new EnumMap<>(EnemyArchetypeId.class);

    private static final List<EnemyArchetypeId> NORMAL_ENCOUNTER_POOL = List.of(
            EnemyArchetypeId.DESERT_ZOMBIE,
            EnemyArchetypeId.WANDERING_WIZARD);

    static {
        register(new EnemyArchetypeDef(
                EnemyArchetypeId.DESERT_ZOMBIE,
                "Desert Zombie",
                7,
                8,
                List.of(
                        ActionCardType.CLAW,
                        ActionCardType.CLAW,
                        ActionCardType.ATTACK,
                        ActionCardType.HEAL,
                        ActionCardType.SHIELD,
                        ActionCardType.VAMPIRISM,
                        ActionCardType.VAMPIRISM),
                List.of(
                        ActionCardType.HEAL,
                        ActionCardType.SHIELD,
                        ActionCardType.VAMPIRISM)));
        register(new EnemyArchetypeDef(
                EnemyArchetypeId.WANDERING_WIZARD,
                "Wandering Wizard",
                6,
                7,
                List.of(
                        ActionCardType.MAGIC_BOLT,
                        ActionCardType.MAGIC_BOLT,
                        ActionCardType.MAGIC_ARROW,
                        ActionCardType.POISON_BOLT,
                        ActionCardType.POISON_MAGIC,
                        ActionCardType.PURIFY,
                        ActionCardType.MAGIC_MIRROR),
                List.of(
                        ActionCardType.MAGIC_BOLT,
                        ActionCardType.MAGIC_ARROW,
                        ActionCardType.PURIFY)));
    }

    private EnemyArchetypeRegistry() {
    }

    public static EnemyArchetypeDef getRequired(EnemyArchetypeId id) {
        EnemyArchetypeDef def = DEFS.get(id);
        if (def == null) {
            throw new IllegalArgumentException("Unknown enemy archetype: " + id);
        }
        return def;
    }

    /** Deterministic mix for map combat tiles without an explicit archetype. */
    public static EnemyArchetypeId pickForMapCoordinate(int worldX, int worldY) {
        return (worldX + worldY) % 2 == 0
                ? EnemyArchetypeId.WANDERING_WIZARD
                : EnemyArchetypeId.DESERT_ZOMBIE;
    }

    /** Uses tile archetype when set; otherwise rolls from the normal encounter pool. */
    public static EnemyArchetypeId resolveNormalEncounter(EnemyArchetypeId tileArchetype, RandomIntSource rng) {
        return tileArchetype != null ? tileArchetype : rollNormalEncounter(rng);
    }

    /** Picks a normal (non-boss) encounter archetype uniformly from the pool. */
    public static EnemyArchetypeId rollNormalEncounter(RandomIntSource rng) {
        if (NORMAL_ENCOUNTER_POOL.isEmpty()) {
            throw new IllegalStateException("No normal enemy archetypes registered");
        }
        return NORMAL_ENCOUNTER_POOL.get(rng.nextInt(NORMAL_ENCOUNTER_POOL.size()));
    }

    private static void register(EnemyArchetypeDef def) {
        DEFS.put(def.id(), def);
    }
}
