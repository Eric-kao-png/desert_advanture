package com.desertadventure.config;

import com.desertadventure.combat.card.ActionCardCategory;

/** Display names for card categories (internal labels). */
public final class GameMessages {
    public static final String CARD_CATEGORY_ATTACK = "Offense";
    public static final String CARD_CATEGORY_CHANGE = "Utility";

    private GameMessages() {
    }

    public static String cardCategoryTooltip(ActionCardCategory category) {
        return "Type: " + category.getDisplayName();
    }
}
