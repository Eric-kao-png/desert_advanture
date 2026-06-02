package com.desertadventure.combat.card.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardDefTest {
    @Test
    void amountHelpers_ignoreNullStepsAndReturnExpectedValues() {
        CardDef def = new CardDef();
        // List.of(...) disallows null elements; use a mutable list to include null.
        def.effects = new java.util.ArrayList<>();
        def.effects.add(null);
        def.effects.add(stepWithAmount(6));
        def.effects.add(stepWithAmount(3));
        def.effects.add(new CardEffectStepDef());

        assertEquals(6, def.firstAmount());
        assertEquals(3, def.minAmount());
        assertEquals(6, def.maxAmount());
    }

    @Test
    void firstTurns_returns0WhenAbsent() {
        CardDef def = new CardDef();
        def.effects = List.of(stepWithAmount(1));
        assertEquals(0, def.firstTurns());
    }

    @Test
    void firstTurns_returnsFirstTurnsWhenPresent() {
        CardDef def = new CardDef();
        def.effects = List.of(stepWithTurns(2), stepWithTurns(9));
        assertEquals(2, def.firstTurns());
    }

    private static CardEffectStepDef stepWithAmount(int amount) {
        CardEffectStepDef step = new CardEffectStepDef();
        step.amount = amount;
        return step;
    }

    private static CardEffectStepDef stepWithTurns(int turns) {
        CardEffectStepDef step = new CardEffectStepDef();
        step.turns = turns;
        return step;
    }
}

