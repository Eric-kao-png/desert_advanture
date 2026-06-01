package com.desertadventure.combat.model;

/** Negative combat status (one slot per entity). */
public enum NegativeStatusType {
    POISON;

    public String getDisplayLabel() {
        return switch (this) {
            case POISON -> "Poison";
        };
    }
}
