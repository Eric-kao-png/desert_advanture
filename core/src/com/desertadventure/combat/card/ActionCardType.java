package com.desertadventure.combat.card;

import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;

public enum ActionCardType {
    ATTACK,
    STRIKE,
    HEAVY_STRIKE,
    SWIFT_STRIKE,
    SPELLBLADE,
    CHASE_ATTACK,
    DOUBLE_BLADE,
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
    VAMPIRISM,
    PURIFY,
    MAGIC_BOLT,
    POISON_BOLT,
    MAGIC_MIRROR,
    MAGIC_ARROW,
    ARROW,
    POISON_ARROW;

    public String getDisplayName() {
        return def().name;
    }

    public String getDescription() {
        String text = def().description;
        return text != null ? text : "";
    }

    public int getPrimaryValue() {
        CardDef def = def();
        return switch (this) {
            case ATTACK, STRIKE, HEAVY_STRIKE, HEAL, SHIELD, CLAW, CHARGED_SLASH, BLADE, GREAT_BLADE, VAMPIRISM,
                    MAGIC_BOLT, POISON_BOLT, MAGIC_ARROW, ARROW, POISON_ARROW -> def.firstAmount();
            case ASSAULT, SWIFT_STRIKE, SPELLBLADE, CHASE_ATTACK -> def.minAmount();
            case DOUBLE_BLADE -> 2;
            case AMBUSH -> def.minAmount();
            case POISON_MAGIC -> def.firstTurns();
            case LIFE_MAGIC, PURIFY, MAGIC_MIRROR -> 0;
        };
    }

    public int getSecondaryValue() {
        CardDef def = def();
        return switch (this) {
            case ASSAULT, SWIFT_STRIKE, SPELLBLADE, CHASE_ATTACK -> def.maxAmount();
            case AMBUSH -> def.maxAmount();
            case POISON_MAGIC -> def.firstAmount();
            case BLADE, GREAT_BLADE -> def.firstTurns();
            case PURIFY -> 2;
            case MAGIC_ARROW -> def.maxAmount();
            case POISON_ARROW -> def.firstTurns();
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
            case ATTACK, STRIKE, HEAVY_STRIKE, SWIFT_STRIKE, SPELLBLADE, CHASE_ATTACK, DOUBLE_BLADE, CLAW,
                    CHARGED_SLASH, BLADE, GREAT_BLADE, VAMPIRISM -> ActionCardMechanic.DAMAGE;
            case POISON_BOLT -> ActionCardMechanic.RANDOM_POISON_DAMAGE;
            case POISON_ARROW -> ActionCardMechanic.CHANCE_POISON_DAMAGE;
            case ARROW -> ActionCardMechanic.DAMAGE;
            case HEAL -> ActionCardMechanic.HEAL;
            case SHIELD -> ActionCardMechanic.SHIELD;
            case ASSAULT -> ActionCardMechanic.FULL_POWER_ATTACK;
            case LIFE_MAGIC -> ActionCardMechanic.HALVE_ENEMY_HP;
            case AMBUSH -> ActionCardMechanic.THRUST;
            case POISON_MAGIC -> ActionCardMechanic.POISON;
            case PURIFY -> ActionCardMechanic.PURIFY;
            case MAGIC_BOLT -> ActionCardMechanic.IGNORE_SHIELD_DAMAGE;
            case MAGIC_MIRROR -> ActionCardMechanic.TRANSFER_DEBUFF;
            case MAGIC_ARROW -> ActionCardMechanic.BONUS_DAMAGE_VS_DEBUFFED;
        };
    }

    private CardDef def() {
        return CardDatabase.getRequired().getRequired(name());
    }
}
