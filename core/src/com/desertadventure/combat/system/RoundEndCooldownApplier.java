package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;

import java.util.HashSet;
import java.util.Set;

/** Applies end-of-round cooldown: tick existing CD, then full CD for cards played this round. */
final class RoundEndCooldownApplier {
    private RoundEndCooldownApplier() {
    }

    static void apply(ActionCardDeck deck, Set<Integer> playedThisRound) {
        if (deck == null) {
            playedThisRound.clear();
            return;
        }
        for (ActionCardInstance instance : deck.getInstances()) {
            instance.tickCooldown();
        }
        for (int instanceId : new HashSet<>(playedThisRound)) {
            ActionCardInstance card = deck.findById(instanceId);
            if (card != null) {
                card.setCooldownRemaining(card.getType().getCooldownTurns());
            }
        }
        playedThisRound.clear();
    }
}
