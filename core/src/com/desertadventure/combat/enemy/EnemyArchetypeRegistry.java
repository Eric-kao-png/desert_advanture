package com.desertadventure.combat.enemy;

import com.desertadventure.combat.card.ActionCardType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Static registry of enemy archetypes. Add new entries here for additional enemy types. */
public final class EnemyArchetypeRegistry {
    private static final Map<EnemyArchetypeId, EnemyArchetypeDef> DEFS = new EnumMap<>(EnemyArchetypeId.class);

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

    private static void register(EnemyArchetypeDef def) {
        DEFS.put(def.id(), def);
    }
}
