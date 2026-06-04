package com.desertadventure.combat.status.data;

/** When a status modifier applies during combat resolution. */
public enum StatusModifierTriggerId {
    /** Increases damage dealt to this entity by an enemy offense card (e.g. FEAR on enemy). */
    INCOMING_OFFENSE_CARD_DAMAGE,
    /** Modifies offense-card damage dealt to the bearer of this buff. */
    INCOMING_OFFENSE_CARD_DAMAGE_TO_SELF,
    /** Side effects when the bearer deals offense-card damage. */
    OUTGOING_OFFENSE_CARD_DAMAGE
}
