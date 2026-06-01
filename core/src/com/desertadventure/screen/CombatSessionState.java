package com.desertadventure.screen;

import com.desertadventure.config.GameConfig;
import com.desertadventure.state.GameplayMode;

/** Mutable combat-init flags owned by {@link GameplayScreen}. */
final class CombatSessionState {
    boolean combatInitialized;
    GameplayMode lastMode = GameplayMode.EXPLORE_IDLE;
    /** 0 = exploration layout, 1 = combat layout. */
    float layoutBlend;

    void updateLayoutBlend(float delta, GameplayMode mode) {
        float target = mode.isCombat() ? 1f : 0f;
        float step = delta / GameConfig.COMBAT_LAYOUT_BLEND_SECONDS;
        if (layoutBlend < target) {
            layoutBlend = Math.min(target, layoutBlend + step);
        } else if (layoutBlend > target) {
            layoutBlend = Math.max(target, layoutBlend - step);
        }
    }
}
