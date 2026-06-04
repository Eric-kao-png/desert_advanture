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
    POISON_BLADE,
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

    private CardDef def() {
        return CardDatabase.getRequired().getRequired(name());
    }
}
