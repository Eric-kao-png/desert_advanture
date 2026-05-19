package com.desertadventure.screen;

import com.desertadventure.state.GameplayMode;

/** Mutable combat-init flags owned by {@link GameplayScreen}. */
final class CombatSessionState {
    boolean combatInitialized;
    GameplayMode lastMode = GameplayMode.EXPLORE_IDLE;
}
