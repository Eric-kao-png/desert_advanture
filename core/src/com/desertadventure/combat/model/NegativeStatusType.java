package com.desertadventure.combat.model;

import com.desertadventure.combat.status.data.StatusEffectDatabase;

/** Negative combat status (one slot per entity). */
public enum NegativeStatusType {
    POISON,
    BLEED,
    FEAR;

    public String getDisplayLabel() {
        return StatusEffectDatabase.displayName(this);
    }
}
