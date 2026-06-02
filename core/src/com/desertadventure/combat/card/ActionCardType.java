package com.desertadventure.combat.card;

import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;

public enum ActionCardType {
    ATTACK,
    STRONG_ATTACK,
    HEAL,
    SHIELD,
    FULL_POWER_ATTACK,
    LIFE_MAGIC,
    THRUST,
    POISON;

    public String getDisplayName() {
        return def().name;
    }

    public int getPrimaryValue() {
        CardDef def = def();
        return switch (this) {
            case ATTACK, STRONG_ATTACK, HEAL, SHIELD -> def.firstAmount();
            case FULL_POWER_ATTACK -> def.minAmount();
            case THRUST -> def.minAmount();
            case POISON -> def.firstTurns();
            case LIFE_MAGIC -> 0;
        };
    }

    public int getSecondaryValue() {
        CardDef def = def();
        return switch (this) {
            case FULL_POWER_ATTACK -> def.maxAmount();
            case THRUST -> def.maxAmount();
            case POISON -> def.firstAmount();
            default -> 0;
        };
    }

    public ActionCardTarget getTarget() {
        return switch (def().targeting) {
            case SELF -> ActionCardTarget.SELF;
            case ENEMY -> ActionCardTarget.ENEMY;
        };
    }

    public int getCooldownTurns() {
        return def().cooldown;
    }

    public ActionCardCategory getCategory() {
        return ActionCardCategory.fromData(def().category);
    }

    public ActionCardMechanic getMechanic() {
        // Legacy field kept for UI and any switch-based fallbacks; resolver uses JSON templates.
        return switch (this) {
            case ATTACK, STRONG_ATTACK -> ActionCardMechanic.DAMAGE;
            case HEAL -> ActionCardMechanic.HEAL;
            case SHIELD -> ActionCardMechanic.SHIELD;
            case FULL_POWER_ATTACK -> ActionCardMechanic.FULL_POWER_ATTACK;
            case LIFE_MAGIC -> ActionCardMechanic.HALVE_ENEMY_HP;
            case THRUST -> ActionCardMechanic.THRUST;
            case POISON -> ActionCardMechanic.POISON;
        };
    }

    private CardDef def() {
        return CardDatabase.getRequired().getRequired(name());
    }
}
