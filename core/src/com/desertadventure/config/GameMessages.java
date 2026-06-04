package com.desertadventure.config;

import com.desertadventure.combat.card.ActionCardCategory;

/** Player-visible feedback strings. */
public final class GameMessages {
    public static final String CARD_CATEGORY_ATTACK = "Offense";
    public static final String CARD_CATEGORY_CHANGE = "Utility";
    public static final String BATTLE_WON = "Battle won!";
    public static final String RUN_DEFEATED = "Defeated. The run rewinds to stage 1.";
    public static final String RUN_REWOUND = "All card cooldowns cleared.";

    public static String cardGained(String cardName) {
        return "Gained: " + cardName;
    }

    public static String cardCategoryTooltip(ActionCardCategory category) {
        return "Type: " + category.getDisplayName();
    }

    public static final String HUD_STORM_TITLE = "Sandstorm!";

    private GameMessages() {
    }

    public static String runStageHeal(float amount) {
        return String.format("Recovered %.0f HP before the next fight.", amount);
    }

    public static String runStageAdvanced(int stageNumber, String stageLabel) {
        return String.format("Next: Stage %d — %s", stageNumber, stageLabel);
    }
}
