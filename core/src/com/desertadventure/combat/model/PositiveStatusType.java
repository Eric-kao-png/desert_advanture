package com.desertadventure.combat.model;

import com.desertadventure.combat.status.data.StatusEffectDatabase;

/** Positive combat status (one slot per entity). */
public enum PositiveStatusType {
    SPIKE_SHIELD,
    SCALE_ARMOR,
    FOCUS,
    VAMPIRE_FANG,
    DODGE;

    public String getDisplayLabel() {
        return StatusEffectDatabase.displayName(this);
    }
}
