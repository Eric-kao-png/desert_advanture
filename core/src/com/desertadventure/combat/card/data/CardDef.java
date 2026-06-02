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

    /** First step with an {@code amount} parameter, or 0 if none. */
    public int firstAmount() {
        if (effects == null) {
            return 0;
        }
        for (CardEffectStepDef step : effects) {
            if (step != null && step.amount != null) {
                return step.amount;
            }
        }
        return 0;
    }

    /** First step with a {@code turns} parameter, or 0 if none. */
    public int firstTurns() {
        if (effects == null) {
            return 0;
        }
        for (CardEffectStepDef step : effects) {
            if (step != null && step.turns != null) {
                return step.turns;
            }
        }
        return 0;
    }

    /** Minimum {@code amount} across steps, or 0 if none. */
    public int minAmount() {
        if (effects == null) {
            return 0;
        }
        int min = Integer.MAX_VALUE;
        boolean found = false;
        for (CardEffectStepDef step : effects) {
            if (step != null && step.amount != null) {
                min = Math.min(min, step.amount);
                found = true;
            }
        }
        return found ? min : 0;
    }

    /** Maximum {@code amount} across steps, or 0 if none. */
    public int maxAmount() {
        if (effects == null) {
            return 0;
        }
        int max = Integer.MIN_VALUE;
        boolean found = false;
        for (CardEffectStepDef step : effects) {
            if (step != null && step.amount != null) {
                max = Math.max(max, step.amount);
                found = true;
            }
        }
        return found ? max : 0;
    }
}

