package com.desertadventure.combat.enemy;

import com.desertadventure.combat.card.ActionCardInstance;

import java.util.List;

/** Chooses which action card an enemy plays from assignable candidates (not on cooldown). */
public interface EnemyAi {
    /** Returns null when {@code candidates} is empty. */
    ActionCardInstance pickCard(List<ActionCardInstance> candidates);
}
