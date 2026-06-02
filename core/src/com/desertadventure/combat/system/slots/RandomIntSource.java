package com.desertadventure.combat.system.slots;

/** Small indirection to make slot rolling deterministic in tests. */
public interface RandomIntSource {
    int nextInt(int boundExclusive);
}

