package com.desertadventure.combat.card;

import java.util.concurrent.ThreadLocalRandom;

/** Rolls action-card rewards (victory loot, future tables). */
public final class ActionCardRewards {
    private static final ActionCardType[] VICTORY_LOOT = {
            ActionCardType.HEAL,
            ActionCardType.STRONG_ATTACK,
            ActionCardType.SHIELD,
            ActionCardType.FULL_POWER_ATTACK,
            ActionCardType.LIFE_MAGIC,
            ActionCardType.THRUST,
            ActionCardType.POISON,
    };

    private ActionCardRewards() {
    }

    /** Random card granted on normal combat victory (uniform among loot pool). */
    public static ActionCardType rollVictoryCard() {
        int index = ThreadLocalRandom.current().nextInt(VICTORY_LOOT.length);
        return VICTORY_LOOT[index];
    }
}
