package com.desertadventure.combat.card;

import com.desertadventure.config.GameConfig;

public enum ActionCardType {
    ATTACK(
            "Attack",
            GameConfig.CARD_ATTACK_DAMAGE,
            0,
            ActionCardTarget.ENEMY,
            GameConfig.CARD_ATTACK_COOLDOWN_TURNS),
    STRONG_ATTACK(
            "Strong Attack",
            GameConfig.CARD_STRONG_ATTACK_DAMAGE,
            0,
            ActionCardTarget.ENEMY,
            GameConfig.CARD_STRONG_ATTACK_COOLDOWN_TURNS),
    HEAL(
            "Heal",
            GameConfig.CARD_HEAL_AMOUNT,
            0,
            ActionCardTarget.SELF,
            GameConfig.CARD_HEAL_COOLDOWN_TURNS);

    private final String displayName;
    private final int primaryValue;
    private final int secondaryValue;
    private final ActionCardTarget target;
    private final int cooldownTurns;

    ActionCardType(String displayName, int primaryValue, int secondaryValue, ActionCardTarget target, int cooldownTurns) {
        this.displayName = displayName;
        this.primaryValue = primaryValue;
        this.secondaryValue = secondaryValue;
        this.target = target;
        this.cooldownTurns = cooldownTurns;
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
}
