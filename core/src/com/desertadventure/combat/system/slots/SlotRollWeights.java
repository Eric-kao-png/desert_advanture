package com.desertadventure.combat.system.slots;

/** Weight configuration for the four allowed player slot pairs. */
public final class SlotRollWeights {
    public final int weight13;
    public final int weight24;
    public final int weight12;
    public final int weight34;

    public SlotRollWeights(int weight13, int weight24, int weight12, int weight34) {
        this.weight13 = Math.max(0, weight13);
        this.weight24 = Math.max(0, weight24);
        this.weight12 = Math.max(0, weight12);
        this.weight34 = Math.max(0, weight34);
    }

    public int totalWeight() {
        return weight13 + weight24 + weight12 + weight34;
    }
}

