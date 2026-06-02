package com.desertadventure.combat.system;

import com.desertadventure.combat.CombatOutcome;

/**
 * Holds a produced combat outcome that must be finalized later by presentation.
 *
 * <p>This keeps {@link CombatController} fields cohesive while preserving existing external behavior.</p>
 */
final class CombatOutcomeFinalization {
    private CombatOutcome pendingOutcome;
    private boolean needsRoundCleanup;

    CombatOutcome getPendingOutcome() {
        return pendingOutcome;
    }

    boolean hasPendingOutcome() {
        return pendingOutcome != null;
    }

    void setPendingOutcome(CombatOutcome outcome, boolean needsRoundCleanup) {
        this.pendingOutcome = outcome;
        this.needsRoundCleanup = needsRoundCleanup;
    }

    /**
     * Consumes the current pending outcome (if any) and clears internal state.
     */
    ConsumedOutcome consume() {
        if (pendingOutcome == null) {
            return null;
        }
        ConsumedOutcome consumed = new ConsumedOutcome(pendingOutcome, needsRoundCleanup);
        pendingOutcome = null;
        needsRoundCleanup = false;
        return consumed;
    }

    record ConsumedOutcome(CombatOutcome outcome, boolean needsRoundCleanup) {
    }
}

