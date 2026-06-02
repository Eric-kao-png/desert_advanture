package com.desertadventure.combat.card;

import com.desertadventure.config.GameMessages;
import com.desertadventure.combat.card.data.CardCategoryId;

/** High-level card grouping for UI (attack vs change). */
public enum ActionCardCategory {
    ATTACK,
    CHANGE;

    public String getDisplayName() {
        return switch (this) {
            case ATTACK -> GameMessages.CARD_CATEGORY_ATTACK;
            case CHANGE -> GameMessages.CARD_CATEGORY_CHANGE;
        };
    }

    public static ActionCardCategory fromData(CardCategoryId id) {
        return switch (id) {
            case OFFENSE -> ATTACK;
            case UTILITY -> CHANGE;
        };
    }
}
