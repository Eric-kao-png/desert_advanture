package com.desertadventure.item;

import com.desertadventure.config.GameConfig;
import com.desertadventure.util.WeightedPicker;

import java.util.concurrent.ThreadLocalRandom;

/** Rolls item drops from map ITEM tiles. */
public final class ItemLoot {
    private static final ItemType[] POOL = ItemType.values();

    private ItemLoot() {
    }

    /**
     * @return dropped item, or null if nothing was found
     */
    public static ItemType rollDrop() {
        if (ThreadLocalRandom.current().nextFloat() > GameConfig.ITEM_TILE_DROP_CHANCE) {
            return null;
        }
        int index = WeightedPicker.pickIndex(GameConfig.ITEM_DROP_WEIGHTS);
        if (index < 0 || index >= POOL.length) {
            return null;
        }
        return POOL[index];
    }
}
