package com.desertadventure.combat.system;

import com.desertadventure.combat.CombatOutcome;

/**
 * Encapsulates the "delay combat end until player attack animation finishes" rule.
 *
 * <p>This keeps {@link CombatController} focused on combat flow, while the delay policy
 * remains a small, swappable unit (later we can move the timing ownership to presentation).</p>
 */
final class CombatEndDelayGate {
    private float playerAttackTimerSeconds;
    private CombatOutcome pendingCombatEndOutcome;
    private boolean pendingCombatEndNeedsRoundCleanup;

    void update(float deltaSeconds) {
        if (playerAttackTimerSeconds <= 0f) {
            return;
        }
        playerAttackTimerSeconds = Math.max(0f, playerAttackTimerSeconds - deltaSeconds);
    }

    boolean hasPendingCombatEnd() {
        return pendingCombatEndOutcome != null;
    }

    boolean shouldEndCombatNow() {
        return pendingCombatEndOutcome != null && playerAttackTimerSeconds <= 0f;
    }

    CombatOutcome consumePendingOutcome() {
        CombatOutcome outcome = pendingCombatEndOutcome;
        pendingCombatEndOutcome = null;
        pendingCombatEndNeedsRoundCleanup = false;
        return outcome;
    }

    boolean consumeNeedsRoundCleanupFlag() {
        boolean needs = pendingCombatEndNeedsRoundCleanup;
        pendingCombatEndNeedsRoundCleanup = false;
        return needs;
    }

    boolean shouldDelayCombatEndForPlayerAttack() {
        return playerAttackTimerSeconds > 0f;
    }

    void scheduleCombatEnd(CombatOutcome outcome, boolean needsRoundCleanup) {
        pendingCombatEndOutcome = outcome;
        pendingCombatEndNeedsRoundCleanup = needsRoundCleanup;
    }

    void triggerPlayerAttack(float attackAnimSeconds) {
        playerAttackTimerSeconds = Math.max(playerAttackTimerSeconds, attackAnimSeconds);
    }

    boolean isPlayerAttacking() {
        return playerAttackTimerSeconds > 0f;
    }

    float getPlayerAttackProgress(float attackDurationSeconds) {
        if (attackDurationSeconds <= 0f) {
            return 1f;
        }
        float remaining = Math.max(0f, Math.min(attackDurationSeconds, playerAttackTimerSeconds));
        return 1f - (remaining / attackDurationSeconds);
    }
}

