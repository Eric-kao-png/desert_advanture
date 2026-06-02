package com.desertadventure.combat.card.data;

/**
 * One effect step inside a card definition.
 * Fields are public for LibGDX Json.
 */
public final class CardEffectStepDef {
    public CardEffectConditionDef when;
    public com.desertadventure.combat.system.effects.EffectTemplateId template;

    public Integer amount;
    public com.desertadventure.combat.model.NegativeStatusType status;
    public Integer turns;
    /** Success threshold for {@code APPLY_CHANCE_POISON} (roll 0–99 &lt; chancePercent). */
    public Integer chancePercent;
}

