package com.desertadventure.combat.card;

import java.util.concurrent.ThreadLocalRandom;

/** Rolls action-card rewards (victory loot, future tables). */
public final class ActionCardRewards {
    private ActionCardRewards() {
    }

    /** Random card granted on normal combat victory (50/50 Heal vs Strong Attack). */
    public static ActionCardType rollVictoryCard() {
        return ThreadLocalRandom.current().nextBoolean()
                ? ActionCardType.HEAL
                : ActionCardType.STRONG_ATTACK;
    }
}
