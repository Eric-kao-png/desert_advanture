package com.desertadventure.combat.card.data;

/**
 * Optional condition for a single effect step.
 * Kept intentionally small; add new condition types as needed.
 */
public final class CardEffectConditionDef {
    public String type;
    public Integer round;
    public CardCategoryId category;
    public Boolean negate;
}

