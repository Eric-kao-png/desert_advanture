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
            case ATTACK, STRONG_ATTACK, HEAL, SHIELD -> firstAmount(def);
            case FULL_POWER_ATTACK -> minAmount(def);
            case THRUST -> minAmount(def);
            case POISON -> firstTurns(def);
            case LIFE_MAGIC -> 0;
        };
    }

    public int getSecondaryValue() {
        CardDef def = def();
        return switch (this) {
            case FULL_POWER_ATTACK -> maxAmount(def);
            case THRUST -> maxAmount(def);
            case POISON -> firstAmount(def);
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

    private static int firstAmount(CardDef def) {
        if (def.effects == null) {
            return 0;
        }
        for (var step : def.effects) {
            if (step != null && step.amount != null) {
                return step.amount;
            }
        }
        return 0;
    }

    private static int firstTurns(CardDef def) {
        if (def.effects == null) {
            return 0;
        }
        for (var step : def.effects) {
            if (step != null && step.turns != null) {
                return step.turns;
            }
        }
        return 0;
    }

    private static int minAmount(CardDef def) {
        int min = Integer.MAX_VALUE;
        boolean found = false;
        if (def.effects == null) {
            return 0;
        }
        for (var step : def.effects) {
            if (step != null && step.amount != null) {
                min = Math.min(min, step.amount);
                found = true;
            }
        }
        return found ? min : 0;
    }

    private static int maxAmount(CardDef def) {
        int max = Integer.MIN_VALUE;
        boolean found = false;
        if (def.effects == null) {
            return 0;
        }
        for (var step : def.effects) {
            if (step != null && step.amount != null) {
                max = Math.max(max, step.amount);
                found = true;
            }
        }
        return found ? max : 0;
    }
}
