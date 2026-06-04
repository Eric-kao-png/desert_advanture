package com.desertadventure.combat.card;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionCardDeckTest {
    @Test
    void resetToDefault_hasNineCardsWithExpectedTypes() {
        ActionCardDeck deck = new ActionCardDeck();
        deck.resetToDefault();

        assertEquals(9, deck.getInstances().size());
        Map<ActionCardType, Long> counts = deck.getInstances().stream()
                .collect(Collectors.groupingBy(ActionCardInstance::getType, Collectors.counting()));
        assertEquals(2L, counts.get(ActionCardType.ATTACK));
        assertEquals(1L, counts.get(ActionCardType.STRIKE));
        assertEquals(1L, counts.get(ActionCardType.SWIFT_STRIKE));
        assertEquals(1L, counts.get(ActionCardType.CHARGED_SLASH));
        assertEquals(1L, counts.get(ActionCardType.GREAT_BLADE));
        assertEquals(1L, counts.get(ActionCardType.AMBUSH));
        assertEquals(1L, counts.get(ActionCardType.SHIELD));
        assertEquals(1L, counts.get(ActionCardType.HEAL));
    }

    @Test
    void clearAllCooldowns_zerosEveryInstance() {
        ActionCardDeck deck = ActionCardDeck.fromCardTypes(List.of(ActionCardType.ATTACK, ActionCardType.HEAL));
        deck.findById(1).setCooldownRemaining(4);
        deck.findById(2).setCooldownRemaining(1);

        deck.clearAllCooldowns();

        assertEquals(0, deck.findById(1).getCooldownRemaining());
        assertEquals(0, deck.findById(2).getCooldownRemaining());
        assertEquals(2, deck.getInstances().size());
    }
}
