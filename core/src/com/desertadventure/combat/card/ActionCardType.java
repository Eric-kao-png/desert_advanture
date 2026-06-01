package com.desertadventure.combat.card;

import com.desertadventure.config.GameConfig;

public enum ActionCardType {
    ATTACK(
            "Attack",
            GameConfig.CARD_ATTACK_DAMAGE,
            0,
            ActionCardTarget.ENEMY,
            GameConfig.CARD_ATTACK_COOLDOWN_TURNS,
            ActionCardCategory.ATTACK,
            ActionCardMechanic.DAMAGE),
    STRONG_ATTACK(
            "Strong Attack",
            GameConfig.CARD_STRONG_ATTACK_DAMAGE,
            0,
            ActionCardTarget.ENEMY,
            GameConfig.CARD_STRONG_ATTACK_COOLDOWN_TURNS,
            ActionCardCategory.ATTACK,
            ActionCardMechanic.DAMAGE),
    HEAL(
            "Heal",
            GameConfig.CARD_HEAL_AMOUNT,
            0,
            ActionCardTarget.SELF,
            GameConfig.CARD_HEAL_COOLDOWN_TURNS,
            ActionCardCategory.CHANGE,
            ActionCardMechanic.HEAL),
    SHIELD(
            "Shield",
            GameConfig.CARD_SHIELD_AMOUNT,
            0,
            ActionCardTarget.SELF,
            GameConfig.CARD_SHIELD_COOLDOWN_TURNS,
            ActionCardCategory.CHANGE,
            ActionCardMechanic.SHIELD),
    FULL_POWER_ATTACK(
            "Full Power",
            GameConfig.CARD_FULL_POWER_DAMAGE_LOW,
            GameConfig.CARD_FULL_POWER_DAMAGE_HIGH,
            ActionCardTarget.ENEMY,
            GameConfig.CARD_FULL_POWER_COOLDOWN_TURNS,
            ActionCardCategory.ATTACK,
            ActionCardMechanic.FULL_POWER_ATTACK),
    LIFE_MAGIC(
            "Life Magic",
            0,
            0,
            ActionCardTarget.ENEMY,
            GameConfig.CARD_LIFE_MAGIC_COOLDOWN_TURNS,
            ActionCardCategory.CHANGE,
            ActionCardMechanic.HALVE_ENEMY_HP),
    THRUST(
            "Thrust",
            GameConfig.CARD_THRUST_DAMAGE_OTHER,
            GameConfig.CARD_THRUST_DAMAGE_ROUND_ONE,
            ActionCardTarget.ENEMY,
            GameConfig.CARD_THRUST_COOLDOWN_TURNS,
            ActionCardCategory.ATTACK,
            ActionCardMechanic.THRUST),
    POISON(
            "Poison",
            GameConfig.CARD_POISON_DURATION_TURNS,
            GameConfig.CARD_POISON_DAMAGE_PER_ROUND,
            ActionCardTarget.ENEMY,
            GameConfig.CARD_POISON_COOLDOWN_TURNS,
            ActionCardCategory.CHANGE,
            ActionCardMechanic.POISON);

    private final String displayName;
    private final int primaryValue;
    private final int secondaryValue;
    private final ActionCardTarget target;
    private final int cooldownTurns;
    private final ActionCardCategory category;
    private final ActionCardMechanic mechanic;

    ActionCardType(
            String displayName,
            int primaryValue,
            int secondaryValue,
            ActionCardTarget target,
            int cooldownTurns,
            ActionCardCategory category,
            ActionCardMechanic mechanic) {
        this.displayName = displayName;
        this.primaryValue = primaryValue;
        this.secondaryValue = secondaryValue;
        this.target = target;
        this.cooldownTurns = cooldownTurns;
        this.category = category;
        this.mechanic = mechanic;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPrimaryValue() {
        return primaryValue;
    }

    public int getSecondaryValue() {
        return secondaryValue;
    }

    public ActionCardTarget getTarget() {
        return target;
    }

    public int getCooldownTurns() {
        return cooldownTurns;
    }

    public ActionCardCategory getCategory() {
        return category;
    }

    public ActionCardMechanic getMechanic() {
        return mechanic;
    }
}
