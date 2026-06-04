package com.desertadventure.combat.system.support;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.system.CombatController;

/** Shared helpers for {@link CombatController} integration tests. */
public final class CombatIntegrationTestSupport {
    private CombatIntegrationTestSupport() {
    }

    public static void resolveFullRound(CombatController combat) {
        for (int i = 0; i < 4; i++) {
            combat.update(999f);
            combat.finalizePendingOutcome();
        }
    }

    public static int findFirstInstanceId(ActionCardDeck deck, ActionCardType type) {
        for (ActionCardInstance instance : deck.getInstances()) {
            if (instance.getType() == type) {
                return instance.getInstanceId();
            }
        }
        throw new IllegalStateException("Missing card instance: " + type);
    }

    public static int firstPlayerSlot(CombatController combat) {
        for (int slot = 0; slot < 4; slot++) {
            if (combat.isPlayerSlot(slot)) {
                return slot;
            }
        }
        throw new IllegalStateException("No player slot available");
    }
}
