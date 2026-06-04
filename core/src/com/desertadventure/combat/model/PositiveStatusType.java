package com.desertadventure.combat.model;

import com.desertadventure.combat.status.data.StatusEffectDatabase;

/** Positive combat status (one slot per entity). Add buff types here as needed. */
public enum PositiveStatusType {
    ;

    public String getDisplayLabel() {
        return StatusEffectDatabase.displayName(this);
    }
}
