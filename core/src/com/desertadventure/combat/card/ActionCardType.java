package com.desertadventure.combat.card;

import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;

public enum ActionCardType {
    ATTACK,
    SWIFT_STRIKE,
    HEAL,
    SHIELD,
    ASSAULT,
    LIFE_MAGIC,
    AMBUSH,
    POISON_MAGIC,
    CLAW,
    CHARGED_SLASH,
    BLADE,
    GREAT_BLADE,
    VAMPIRISM;

    public String getDisplayName() {
        return def().name;
    }

    public int getPrimaryValue() {
        CardDef def = def();
        return switch (this) {
            case ATTACK, SWIFT_STRIKE, HEAL, SHIELD, CLAW, CHARGED_SLASH, BLADE, GREAT_BLADE, VAMPIRISM -> def.firstAmount();
            case ASSAULT -> def.minAmount();
            case AMBUSH -> def.minAmount();
            case POISON_MAGIC -> def.firstTurns();
            case LIFE_MAGIC -> 0;
        };
    }

    public int getSecondaryValue() {
        CardDef def = def();
        return switch (this) {
            case ASSAULT -> def.maxAmount();
            case AMBUSH -> def.maxAmount();
            case POISON_MAGIC -> def.firstAmount();
            case BLADE, GREAT_BLADE -> def.firstTurns();
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
            case ATTACK, SWIFT_STRIKE, CLAW, CHARGED_SLASH, BLADE, GREAT_BLADE, VAMPIRISM -> ActionCardMechanic.DAMAGE;
            case HEAL -> ActionCardMechanic.HEAL;
            case SHIELD -> ActionCardMechanic.SHIELD;
            case ASSAULT -> ActionCardMechanic.FULL_POWER_ATTACK;
            case LIFE_MAGIC -> ActionCardMechanic.HALVE_ENEMY_HP;
            case AMBUSH -> ActionCardMechanic.THRUST;
            case POISON_MAGIC -> ActionCardMechanic.POISON;
        };
    }

    private CardDef def() {
        return CardDatabase.getRequired().getRequired(name());
    }
}
