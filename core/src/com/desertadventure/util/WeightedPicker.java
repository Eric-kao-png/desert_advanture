package com.desertadventure.util;

import java.util.concurrent.ThreadLocalRandom;

/** Weighted random index selection. */
public final class WeightedPicker {
    private WeightedPicker() {
    }

    public static int pickIndex(int[] weights) {
        if (weights == null || weights.length == 0) {
            return -1;
        }
        int total = 0;
        for (int weight : weights) {
            total += weight;
        }
        if (total <= 0) {
            return 0;
        }
        int roll = ThreadLocalRandom.current().nextInt(total);
        int cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) {
                return i;
            }
        }
        return weights.length - 1;
    }
}
