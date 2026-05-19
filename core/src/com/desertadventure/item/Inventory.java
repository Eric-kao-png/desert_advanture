package com.desertadventure.item;

import com.desertadventure.config.GameConfig;

import java.util.Arrays;

/**
 * Fixed backpack: one item per cell, no stacking. New pickups use the lowest-index empty cell.
 */
public class Inventory {
    private final ItemType[] items;

    public Inventory() {
        items = new ItemType[GameConfig.INVENTORY_SLOT_COUNT];
    }

    public int getSlotCount() {
        return items.length;
    }

    public ItemType getItemAt(int index) {
        if (index < 0 || index >= items.length) {
            return null;
        }
        return items[index];
    }

    public boolean isSlotEmpty(int index) {
        return getItemAt(index) == null;
    }

    public int firstEmptySlotIndex() {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public boolean add(ItemType type) {
        if (type == null) {
            return false;
        }
        int index = firstEmptySlotIndex();
        if (index < 0) {
            return false;
        }
        items[index] = type;
        return true;
    }

    /** Removes and returns the item in the slot (one item per cell). */
    public ItemType takeFromSlot(int index) {
        if (index < 0 || index >= items.length) {
            return null;
        }
        ItemType type = items[index];
        items[index] = null;
        return type;
    }

    public void clear() {
        Arrays.fill(items, null);
    }

    public boolean isEmpty() {
        for (ItemType item : items) {
            if (item != null) {
                return false;
            }
        }
        return true;
    }
}
