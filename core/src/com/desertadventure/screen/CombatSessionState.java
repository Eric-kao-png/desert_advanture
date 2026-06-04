package com.desertadventure.screen;

import com.desertadventure.combat.system.CombatController;
import com.desertadventure.state.GameplayMode;

/** Tracks whether the current combat encounter has been started. */
final class CombatSessionState {
    boolean combatInitialized;
    GameplayMode lastMode = GameplayMode.HUB;

    void updateCombatPresentation(float delta, CombatController combat) {
        if (combat != null && combat.hasPendingOutcome()) {
            combat.finalizePendingOutcome();
        }
    }

    void resetForCombatStart() {
    }
}
