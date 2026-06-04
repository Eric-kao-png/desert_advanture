package com.desertadventure.combat.card;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionCardDeckTest {
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
