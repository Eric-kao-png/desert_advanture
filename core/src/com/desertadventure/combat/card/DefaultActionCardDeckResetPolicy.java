package com.desertadventure.combat.card;

public final class DefaultActionCardDeckResetPolicy implements ActionCardDeckResetPolicy {
    @Override
    public void resetDeck(ActionCardDeck deck) {
        deck.resetToDefault();
    }
}
