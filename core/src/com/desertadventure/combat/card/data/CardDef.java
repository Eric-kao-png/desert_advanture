package com.desertadventure.combat.card.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data definition for a single card, loaded from JSON.
 * This describes card values and its effect pipeline; behavior is executed by templates.
 */
public final class CardDef {
    public String id;
    public String name;
    public CardCategoryId category;
    public int cooldown;
    public CardTargetingId targeting;
    public List<CardEffectStepDef> effects = new ArrayList<>();
}

