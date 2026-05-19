package com.desertadventure.item;

import com.desertadventure.exploration.StepBudgetService;
import com.desertadventure.player.PlayerStats;

/** Adds and consumes inventory items. */
public final class InventoryService {
    private final Inventory inventory;

    public InventoryService(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean useSlot(int slotIndex, PlayerStats stats, StepBudgetService stepBudget) {
        ItemType type = inventory.takeFromSlot(slotIndex);
        if (type == null) {
            return false;
        }
        type.apply(stats, stepBudget);
        return true;
    }
}
