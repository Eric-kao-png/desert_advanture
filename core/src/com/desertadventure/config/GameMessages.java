package com.desertadventure.config;

/** Player-visible feedback strings. */
public final class GameMessages {
    public static final String CANNOT_ENTER_TILE = "Cannot enter this tile.";
    public static final String PATH_BLOCKED = "Straight path is blocked.";
    public static final String ITEM_COLLECTED = "Item collected! ATK +2, step limit +1";
    public static final String RUINS_ALREADY_DONE = "Ruins already investigated.";
    public static final String BATTLE_WON = "Battle won!";
    public static final String SANDSTORM_RETURN = "Sandstorm... You returned to camp center.";

    public static final String CHARACTER_PANEL_TITLE = "Character";
    public static final String STAT_SECTION_HEALTH = "Health";
    public static final String STAT_SECTION_COMBAT = "Combat";
    public static final String STAT_SECTION_EXPLORATION = "Exploration";
    public static final String STAT_HP = "  Current / Max HP";
    public static final String STAT_ATTACK = "  Attack";
    public static final String STAT_STAMINA = "  Stamina (remaining / max)";

    private GameMessages() {
    }

    public static String requiredEventCompleted(String eventId) {
        return "Required event completed: " + eventId;
    }

    public static String requiredEventsIncomplete(int completed, int required) {
        return "Required events incomplete (" + completed + "/" + required + ")";
    }
}
