package com.desertadventure.screen;

import com.desertadventure.config.GameConfig;
import com.desertadventure.presentation.GameplayRenderer;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

final class GameplayModeUpdater {
    private final GameSession session;
    private final GameplayInputHandler input;
    private final GameplayRenderer renderer;
    private final CombatSessionState combatState;

    GameplayModeUpdater(
            GameSession session,
            GameplayInputHandler input,
            GameplayRenderer renderer,
            CombatSessionState combatState) {
        this.session = session;
        this.input = input;
        this.renderer = renderer;
        this.combatState = combatState;
    }

    void update(float delta) {
        GameplayMode mode = session.getMode();
        detectModeTransitions(mode);
        ensureCombatInitialized(mode);
        session.getMessageFeed().update(delta);

        switch (mode) {
            case RUNNING -> {
                session.getPathRunner().update(delta);
                session.updateRunning(delta);
            }
            case STORM -> {
                session.updateStorm(delta);
                if (session.getStormTimer() >= GameConfig.STORM_FADE_SECONDS) {
                    session.completeStorm();
                    combatState.combatInitialized = false;
                }
            }
            case COMBAT, BOSS_COMBAT -> input.updateCombatInput(delta);
            default -> {
            }
        }

        if (mode.isCombat() && combatState.combatInitialized && !session.getMode().isCombat()) {
            combatState.combatInitialized = false;
        }
        combatState.lastMode = mode;
    }

    private void detectModeTransitions(GameplayMode mode) {
        if (mode.isCombat() && !combatState.lastMode.isCombat()) {
            combatState.combatInitialized = false;
        }
        if (mode == GameplayMode.STORM && combatState.lastMode != GameplayMode.STORM) {
            renderer.repopulateHouseProps(GameConfig.VIEW_WIDTH);
        }
    }

    private void ensureCombatInitialized(GameplayMode mode) {
        if (mode == GameplayMode.COMBAT && !combatState.combatInitialized) {
            beginCombat(false);
        } else if (mode == GameplayMode.BOSS_COMBAT && !combatState.combatInitialized) {
            beginCombat(true);
        }
    }

    void ensureCombatInitializedForDraw(GameplayMode mode) {
        ensureCombatInitialized(mode);
    }

    private void beginCombat(boolean boss) {
        session.getCombatController().startCombat(
                session.getCurrentDistanceBand(),
                boss,
                GameConfig.VIEW_WIDTH,
                GameConfig.EXPLORE_GROUND_Y,
                session::onCombatEnd
        );
        combatState.combatInitialized = true;
    }
}
