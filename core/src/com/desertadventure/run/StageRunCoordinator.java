package com.desertadventure.run;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardRewards;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.config.CombatConfig;
import com.desertadventure.config.RunConfig;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.state.GameplayMode;
import com.desertadventure.state.PermanentProgress;
import com.desertadventure.state.SessionModeAccess;

/** Applies combat results for hub-based linear stage runs (no map tiles). */
public final class StageRunCoordinator {
    private final RunProgress runProgress;
    private final PlayerStats playerStats;
    private final PermanentProgress permanentProgress;
    private final ActionCardDeck actionCardDeck;
    private final SessionModeAccess mode;

    public StageRunCoordinator(
            RunProgress runProgress,
            PlayerStats playerStats,
            PermanentProgress permanentProgress,
            ActionCardDeck actionCardDeck,
            SessionModeAccess mode) {
        this.runProgress = runProgress;
        this.playerStats = playerStats;
        this.permanentProgress = permanentProgress;
        this.actionCardDeck = actionCardDeck;
        this.mode = mode;
    }

    public void apply(CombatOutcome outcome, EnemyArchetypeId defeatedEnemyArchetype) {
        if (!playerStats.isAlive()) {
            applyRewind();
            return;
        }
        switch (outcome) {
            case VICTORY -> applyNormalVictory(defeatedEnemyArchetype);
            case BOSS_VICTORY -> applyBossVictory();
            case DEFEAT -> applyRewind();
        }
    }

    private void applyNormalVictory(EnemyArchetypeId defeatedEnemyArchetype) {
        if (runProgress.isOnBossStage()) {
            throw new IllegalStateException("Non-boss VICTORY on boss stage");
        }
        playerStats.addExperience(CombatConfig.VICTORY_EXPERIENCE);
        if (defeatedEnemyArchetype == null) {
            throw new IllegalStateException("VICTORY requires a defeated enemy archetype");
        }
        ActionCardType reward = ActionCardRewards.rollVictoryCard(defeatedEnemyArchetype);
        actionCardDeck.addCard(reward);
        if (RunConfig.STAGE_VICTORY_HEAL > 0f) {
            playerStats.restoreHp(RunConfig.STAGE_VICTORY_HEAL);
        }
        runProgress.advanceAfterVictory();
        mode.set(GameplayMode.HUB);
    }

    private void applyBossVictory() {
        permanentProgress.setGameWon(true);
        permanentProgress.save();
        mode.set(GameplayMode.VICTORY);
    }

    private void applyRewind() {
        runProgress.rewindToStart();
        actionCardDeck.clearAllCooldowns();
        playerStats.healFull();
        mode.set(GameplayMode.HUB);
    }
}
