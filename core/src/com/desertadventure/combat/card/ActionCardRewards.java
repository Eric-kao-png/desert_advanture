package com.desertadventure.combat.card;

import com.desertadventure.combat.enemy.EnemyArchetypeDef;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.combat.enemy.EnemyArchetypeRegistry;
import com.desertadventure.combat.system.slots.RandomIntSource;

import java.util.concurrent.ThreadLocalRandom;

/** Rolls action-card rewards (victory loot, future tables). */
public final class ActionCardRewards {
    private static final RandomIntSource DEFAULT_RNG = bound -> ThreadLocalRandom.current().nextInt(bound);

    private ActionCardRewards() {
    }

    /** Random card granted on normal combat victory from the defeated enemy's loot pool. */
    public static ActionCardType rollVictoryCard(EnemyArchetypeId defeatedArchetype) {
        if (defeatedArchetype == null) {
            throw new IllegalArgumentException("defeatedArchetype must be non-null");
        }
        return rollFromPool(EnemyArchetypeRegistry.getRequired(defeatedArchetype), DEFAULT_RNG);
    }

    static ActionCardType rollFromPool(EnemyArchetypeDef archetype, RandomIntSource rng) {
        var pool = archetype.lootPool();
        return pool.get(rng.nextInt(pool.size()));
    }
}
