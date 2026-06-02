package com.desertadventure.combat.system.slots;

/**
 * Default slot rolling rule:
 * (1,3) most likely, (2,4) next, (1,2) and (3,4) least (weights provided externally).
 */
public final class WeightedPlayerSlotRoller implements PlayerSlotRoller {
    private final SlotRollWeights weights;
    private final RandomIntSource rng;

    public WeightedPlayerSlotRoller(SlotRollWeights weights, RandomIntSource rng) {
        this.weights = weights;
        this.rng = rng;
    }

    @Override
    public PlayerSlotPlan rollPlan() {
        int total = Math.max(1, weights.totalWeight());
        int roll = Math.floorMod(rng.nextInt(total), total);

        int w13 = weights.weight13;
        int w24 = weights.weight24;
        int w12 = weights.weight12;

        if (roll < w13) {
            return new PlayerSlotPlan(0, 2); // 1 & 3
        }
        if (roll < w13 + w24) {
            return new PlayerSlotPlan(1, 3); // 2 & 4
        }
        if (roll < w13 + w24 + w12) {
            return new PlayerSlotPlan(0, 1); // 1 & 2
        }
        return new PlayerSlotPlan(2, 3); // 3 & 4
    }
}

