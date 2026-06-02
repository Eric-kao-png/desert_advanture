package com.desertadventure.combat.system.slots;

/** Strategy for picking the player's available slot pair each round. */
public interface PlayerSlotRoller {
    PlayerSlotPlan rollPlan();
}

