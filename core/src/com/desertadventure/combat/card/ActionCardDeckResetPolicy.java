package com.desertadventure.combat.card;

/**
 * Hook for sandstorm / new-game deck resets.
 * Default implementation wipes to the starter deck; a future policy may preserve instances across storms.
 */
public interface ActionCardDeckResetPolicy {
    void resetDeck(ActionCardDeck deck);
}
