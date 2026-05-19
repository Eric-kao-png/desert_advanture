package com.desertadventure.item;

import com.desertadventure.config.GameConfig;

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
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        if (rng.nextFloat() > GameConfig.ITEM_TILE_DROP_CHANCE) {
            return null;
        }
        int totalWeight = 0;
        for (int weight : GameConfig.ITEM_DROP_WEIGHTS) {
            totalWeight += weight;
        }
        int roll = rng.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < POOL.length; i++) {
            cumulative += GameConfig.ITEM_DROP_WEIGHTS[i];
            if (roll < cumulative) {
                return POOL[i];
            }
        }
        return POOL[POOL.length - 1];
    }
}
