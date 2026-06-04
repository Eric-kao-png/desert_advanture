package com.desertadventure.screen;

import com.desertadventure.combat.system.CombatController;
import com.desertadventure.combat.system.presentation.PlayerAttackAnimation;
import com.desertadventure.config.GameConfig;
import com.desertadventure.state.GameplayMode;

/** Mutable combat-init flags owned by {@link GameplayScreen}. */
final class CombatSessionState {
    boolean combatInitialized;
    GameplayMode lastMode = GameplayMode.HUB;
    /** 0 = hub layout, 1 = combat layout. */
    float layoutBlend;

    final AttackAnimationTimer attackAnimation = new AttackAnimationTimer();

    void updateLayoutBlend(float delta, GameplayMode mode) {
        float target = mode.isCombat() ? 1f : 0f;
        float step = delta / GameConfig.COMBAT_LAYOUT_BLEND_SECONDS;
        if (layoutBlend < target) {
            layoutBlend = Math.min(target, layoutBlend + step);
        } else if (layoutBlend > target) {
            layoutBlend = Math.max(target, layoutBlend - step);
        }
    }

    void updateCombatPresentation(float delta, CombatController combat) {
        attackAnimation.update(delta);
        if (combat != null && combat.hasPendingOutcome() && !attackAnimation.isAttacking()) {
            combat.finalizePendingOutcome();
        }
    }

    void resetForCombatStart(CombatController combat) {
        attackAnimation.reset();
        if (combat != null) {
            combat.setPlayerAttackAnimation(attackAnimation);
        }
    }

    /**
     * Presentation-owned timer for the player's attack animation.
     * Lives here to keep {@link CombatController} free of time ownership.
     */
    static final class AttackAnimationTimer implements PlayerAttackAnimation {
        private float remainingSeconds;

        void update(float delta) {
            if (remainingSeconds <= 0f) {
                return;
            }
            remainingSeconds = Math.max(0f, remainingSeconds - delta);
        }

        void reset() {
            remainingSeconds = 0f;
        }

        @Override
        public void triggerAttack(float attackAnimSeconds) {
            remainingSeconds = Math.max(remainingSeconds, attackAnimSeconds);
        }

        @Override
        public boolean isAttacking() {
            return remainingSeconds > 0f;
        }

        @Override
        public float getAttackProgress(float attackDurationSeconds) {
            if (attackDurationSeconds <= 0f) {
                return 1f;
            }
            float remaining = Math.max(0f, Math.min(attackDurationSeconds, remainingSeconds));
            return 1f - (remaining / attackDurationSeconds);
        }
    }
}
