package com.desertadventure.combat.system.support;

import com.desertadventure.combat.system.slots.PlayerSlotPlan;
import com.desertadventure.combat.system.slots.PlayerSlotRoller;

/** Deterministic slot plan sequence for integration tests. */
public final class SequencedPlanRoller implements PlayerSlotRoller {
    private final PlayerSlotPlan[] plans;
    private int idx;

    public SequencedPlanRoller(PlayerSlotPlan... plans) {
        this.plans = plans;
    }

    @Override
    public PlayerSlotPlan rollPlan() {
        int i = Math.min(idx, plans.length - 1);
        idx++;
        return plans[i];
    }
}
