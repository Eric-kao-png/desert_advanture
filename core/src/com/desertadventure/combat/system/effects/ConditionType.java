package com.desertadventure.combat.system.effects;

/** Stable identifiers for conditions referenced by card JSON. */
public enum ConditionType {
    ROUND_EQUALS,
    TURN_HAS_USED_CATEGORY,
    SLOT_INDEX_EQUALS,
    /** Prior slot in resolve order (index - 1) holds a player offense card. */
    PREVIOUS_SLOT_PLAYER_OFFENSE,
    CASTER_HAS_NEGATIVE_STATUS,
    OPPONENT_HAS_NEGATIVE_STATUS,
    /** Opponent had no shield when this card began resolving (lifesteal gate). */
    OPPONENT_HAD_NO_SHIELD
}

