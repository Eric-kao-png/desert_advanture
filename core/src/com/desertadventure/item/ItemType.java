package com.desertadventure.item;

import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameMessages;
import com.desertadventure.exploration.StepBudgetService;
import com.desertadventure.player.PlayerStats;

/** Consumable / permanent boost items found on the map. */
public enum ItemType {
    HEALTH_POTION,
    STAMINA_POTION,
    HEALTH_GEM,
    STAMINA_GEM;

    public String getDisplayName() {
        return switch (this) {
            case HEALTH_POTION -> GameMessages.ITEM_HEALTH_POTION;
            case STAMINA_POTION -> GameMessages.ITEM_STAMINA_POTION;
            case HEALTH_GEM -> GameMessages.ITEM_HEALTH_GEM;
            case STAMINA_GEM -> GameMessages.ITEM_STAMINA_GEM;
        };
    }

    public String getDescription() {
        return switch (this) {
            case HEALTH_POTION -> GameMessages.ITEM_HEALTH_POTION_DESC;
            case STAMINA_POTION -> GameMessages.ITEM_STAMINA_POTION_DESC;
            case HEALTH_GEM -> GameMessages.ITEM_HEALTH_GEM_DESC;
            case STAMINA_GEM -> GameMessages.ITEM_STAMINA_GEM_DESC;
        };
    }

    public String getSpritePath() {
        return switch (this) {
            case HEALTH_POTION -> "sprites/item_health_potion.png";
            case STAMINA_POTION -> "sprites/item_stamina_potion.png";
            case HEALTH_GEM -> "sprites/item_health_gem.png";
            case STAMINA_GEM -> "sprites/item_stamina_gem.png";
        };
    }

    public void apply(PlayerStats stats, StepBudgetService stepBudget) {
        switch (this) {
            case HEALTH_POTION -> stats.restoreHp(GameConfig.ITEM_HEALTH_POTION_RESTORE);
            case STAMINA_POTION -> stepBudget.restoreStamina(GameConfig.ITEM_STAMINA_POTION_RESTORE);
            case HEALTH_GEM -> stats.increaseMaxHp(GameConfig.ITEM_HEALTH_GEM_BONUS);
            case STAMINA_GEM -> stepBudget.increaseMaxStamina(GameConfig.ITEM_STAMINA_GEM_BONUS);
        }
    }
}
