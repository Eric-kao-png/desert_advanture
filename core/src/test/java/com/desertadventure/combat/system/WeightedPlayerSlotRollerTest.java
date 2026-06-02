package com.desertadventure.combat.system;

import com.desertadventure.combat.system.slots.PlayerSlotPlan;
import com.desertadventure.combat.system.slots.RandomIntSource;
import com.desertadventure.combat.system.slots.SlotRollWeights;
import com.desertadventure.combat.system.slots.WeightedPlayerSlotRoller;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeightedPlayerSlotRollerTest {
    @Test
    void rollPlan_mapsRollIntoFourSlotPairs_byProvidedWeights() {
        // total=10; ranges:
        // [0..3] -> (1,3)
        // [4..6] -> (2,4)
        // [7..8] -> (1,2)
        // [9..9] -> (3,4)
        SlotRollWeights weights = new SlotRollWeights(4, 3, 2, 1);

        assertPlan(weights, 0, 0, 2); // (1,3)
        assertPlan(weights, 3, 0, 2); // (1,3)
        assertPlan(weights, 4, 1, 3); // (2,4)
        assertPlan(weights, 6, 1, 3); // (2,4)
        assertPlan(weights, 7, 0, 1); // (1,2)
        assertPlan(weights, 8, 0, 1); // (1,2)
        assertPlan(weights, 9, 2, 3); // (3,4)
    }

    @Test
    void rollPlan_usesFloorMod_toAvoidNegativeOrOverBoundRngValues() {
        SlotRollWeights weights = new SlotRollWeights(1, 1, 1, 1); // total=4

        // -1 mod 4 => 3 (last bucket)
        assertPlan(weights, -1, 2, 3);

        // 5 mod 4 => 1 (second bucket)
        assertPlan(weights, 5, 1, 3);
    }

    private static void assertPlan(SlotRollWeights weights, int rngValue, int expectedA, int expectedB) {
        RandomIntSource rng = boundExclusive -> rngValue;
        WeightedPlayerSlotRoller roller = new WeightedPlayerSlotRoller(weights, rng);
        PlayerSlotPlan plan = roller.rollPlan();
        assertEquals(expectedA, plan.slotA());
        assertEquals(expectedB, plan.slotB());
    }
}

