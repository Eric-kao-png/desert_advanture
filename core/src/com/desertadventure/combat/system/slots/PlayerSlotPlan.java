package com.desertadventure.combat.system.slots;

/** Two distinct 0-based slot indices the player may use this round. */
public final class PlayerSlotPlan {
    private final int slotA;
    private final int slotB;

    public PlayerSlotPlan(int slotA, int slotB) {
        if (slotA == slotB) {
            throw new IllegalArgumentException("slotA and slotB must differ");
        }
        this.slotA = slotA;
        this.slotB = slotB;
    }

    public int slotA() {
        return slotA;
    }

    public int slotB() {
        return slotB;
    }
}

