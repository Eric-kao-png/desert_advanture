package com.desertadventure.combat.card;

/** How a card resolves in combat (beyond simple damage/heal constants). */
public enum ActionCardMechanic {
    DAMAGE,
    HEAL,
    SHIELD,
    FULL_POWER_ATTACK,
    HALVE_ENEMY_HP,
    THRUST,
    POISON,
    PURIFY,
    IGNORE_SHIELD_DAMAGE,
    RANDOM_POISON_DAMAGE,
    TRANSFER_DEBUFF,
    BONUS_DAMAGE_VS_DEBUFFED
}
