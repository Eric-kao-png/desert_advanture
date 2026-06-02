package com.desertadventure.combat.system.presentation;

/**
 * Presentation-owned attack animation state.
 *
 * <p>Combat core may trigger an attack animation, but does not own time/ticking.</p>
 */
public interface PlayerAttackAnimation {
    void triggerAttack(float attackAnimSeconds);

    boolean isAttacking();

    /** Normalized progress for the current attack animation (0..1). */
    float getAttackProgress(float attackDurationSeconds);
}

