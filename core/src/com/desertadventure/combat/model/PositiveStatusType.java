package com.desertadventure.combat.model;

/** Positive combat status (one slot per entity). Add buff types here as needed. */
public enum PositiveStatusType {
    ;

    public String getDisplayLabel() {
        String name = name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
