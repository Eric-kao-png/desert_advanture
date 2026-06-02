package com.desertadventure.combat.card.data;

/**
 * One effect step inside a card definition.
 * Fields are public for LibGDX Json.
 */
public final class CardEffectStepDef {
    public CardEffectConditionDef when;
    public String template;

    public Integer amount;
    public String status;
    public Integer turns;
}

