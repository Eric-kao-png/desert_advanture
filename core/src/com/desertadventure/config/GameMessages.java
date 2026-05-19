package com.desertadventure.config;

/** Player-visible feedback strings. */
public final class GameMessages {
    public static final String CANNOT_ENTER_TILE = "Cannot enter this tile.";
    public static final String PATH_BLOCKED = "Straight path is blocked.";
    public static final String RUINS_ALREADY_DONE = "Ruins already investigated.";
    public static final String BATTLE_WON = "Battle won!";
    public static final String SANDSTORM_RETURN = "Sandstorm... You returned to camp center.";

    public static final String CHARACTER_PANEL_TITLE = "Character";
    public static final String CHARACTER_PORTRAIT_PLACEHOLDER = "Portrait";
    public static final String STAT_SECTION_HEALTH = "Health";
    public static final String STAT_SECTION_COMBAT = "Combat";
    public static final String STAT_SECTION_EXPLORATION = "Exploration";
    public static final String STAT_HP = "  Current / Max HP";
    public static final String STAT_ATTACK = "  Attack";
    public static final String STAT_STAMINA = "  Stamina (remaining / max)";

    public static final String INVENTORY_SECTION = "Backpack";
    public static final String INVENTORY_USE = "Use";
    public static final String INVENTORY_CLOSE = "Close";
    public static final String ITEM_HEALTH_POTION = "Health Potion";
    public static final String ITEM_STAMINA_POTION = "Stamina Potion";
    public static final String ITEM_HEALTH_GEM = "Health Gem";
    public static final String ITEM_STAMINA_GEM = "Stamina Gem";
    public static final String ITEM_HEALTH_POTION_DESC = "Restores 50 HP.";
    public static final String ITEM_STAMINA_POTION_DESC = "Restores 5 stamina.";
    public static final String ITEM_HEALTH_GEM_DESC = "Permanently raises max HP by 50.";
    public static final String ITEM_STAMINA_GEM_DESC = "Permanently raises max stamina by 5.";

    public static String inventoryFull() {
        return "Backpack is full.";
    }

    public static String itemObtained(String itemName) {
        return "Found: " + itemName;
    }

    public static String itemUsed(String itemName) {
        return "Used: " + itemName;
    }

    public static String itemEmpty(String itemName) {
        return "No " + itemName + " in inventory.";
    }

    public static String itemTileEmpty() {
        return "Found nothing on this spot.";
    }

    public static final String HUD_EXPLORE_IDLE = "[M] Map  [N] Inventory";
    public static final String HUD_MAP_OVERLAY = "Click destination | Arrows: pan | [X] or [M] or [Esc] Close";
    public static final String HUD_CHARACTER_OVERLAY = "Drag items to reorder | Click for details | [X] or [N] or [Esc] Close";
    public static final String HUD_RUNNING = "Moving... | [M] Map  [N] Inventory";
    public static final String HUD_COMBAT = "WASD: Move | J: Attack K: Skill L: Ultimate";
    public static final String HUD_STORM_TITLE = "Sandstorm!";

    private GameMessages() {
    }

    public static String requiredEventCompleted(String eventId) {
        return "Required event completed: " + eventId;
    }

    public static String requiredEventsIncomplete(int completed, int required) {
        return "Required events incomplete (" + completed + "/" + required + ")";
    }
}
