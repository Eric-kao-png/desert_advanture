package com.desertadventure.combat.card.data;

/**
 * Optional condition for a single effect step.
 * Kept intentionally small; add new condition types as needed.
 */
public final class CardEffectConditionDef {
    public com.desertadventure.combat.system.effects.ConditionType type;
    public Integer round;
    public CardCategoryId category;
    /** 0-based slot index (0..3) for slot-index conditions. */
    public Integer slotIndex;
    public Boolean negate;
}

