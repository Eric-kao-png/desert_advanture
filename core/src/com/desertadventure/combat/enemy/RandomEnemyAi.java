package com.desertadventure.combat.enemy;

import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.system.slots.RandomIntSource;

import java.util.List;

/** Uniform random pick from assignable enemy cards. */
public final class RandomEnemyAi implements EnemyAi {
    private final RandomIntSource rng;

    public RandomEnemyAi(RandomIntSource rng) {
        if (rng == null) {
            throw new IllegalArgumentException("rng must be non-null");
        }
        this.rng = rng;
    }

    @Override
    public ActionCardInstance pickCard(List<ActionCardInstance> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        return candidates.get(rng.nextInt(candidates.size()));
    }
}
