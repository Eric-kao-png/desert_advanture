package com.desertadventure.screen;

import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.screen.layout.CombatSceneLayout;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

final class GameplayModeUpdater {
    private final GameSession session;
    private final GameplayInputHandler input;
    private final CombatSessionState combatState;

    GameplayModeUpdater(
            GameSession session,
            GameplayInputHandler input,
            CombatSessionState combatState) {
        this.session = session;
        this.input = input;
        this.combatState = combatState;
    }

    void update(float delta) {
        GameplayMode mode = session.getMode();
        if (mode.isCombat() && !combatState.lastMode.isCombat()) {
            combatState.combatInitialized = false;
        }

        syncCombatEntityGround();
        ensureCombatInitialized(mode);

        if (mode.isCombat()) {
            updateCombat(delta);
        }

        if (mode.isCombat() && combatState.combatInitialized && !session.getMode().isCombat()) {
            combatState.combatInitialized = false;
        }
        combatState.lastMode = mode;
    }

    private void updateCombat(float delta) {
        combatState.updateCombatPresentation(delta, session.getCombatController());
        session.getCombatController().update(delta);
        combatState.updateCombatPresentation(0f, session.getCombatController());
        input.updateCombatInput(delta);
    }

    void ensureCombatInitializedForDraw(GameplayMode mode) {
        ensureCombatInitialized(mode);
    }

    private void ensureCombatInitialized(GameplayMode mode) {
        if (mode == GameplayMode.COMBAT && !combatState.combatInitialized) {
            beginCombat();
        }
    }

    private void syncCombatEntityGround() {
        if (!session.getCombatController().isActive()) {
            return;
        }
        float groundY = CombatSceneLayout.entityGroundY();
        CombatEntity player = session.getCombatController().getPlayer();
        if (player != null) {
            player.setPosition(player.getX(), groundY);
        }
        for (CombatEntity enemy : session.getCombatController().getEnemies()) {
            enemy.setPosition(enemy.getX(), groundY);
        }
    }

    private void beginCombat() {
        int stageIndex = session.getRunProgress().getCurrentStageIndex();
        session.getCombatController().startCombat(
                stageIndex,
                session.isPendingBossFight(),
                session.consumePendingEnemyArchetype(),
                GameConfig.VIEW_WIDTH,
                GameConfig.COMBAT_GROUND_Y,
                session.getActionCardDeck(),
                session::onCombatEnd);
        combatState.resetForCombatStart();
        combatState.combatInitialized = true;
    }
}
