package com.desertadventure.state;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.run.RunProgress;
import com.desertadventure.run.StageCatalog;
import com.desertadventure.run.StageDef;
import com.desertadventure.run.StageRunCoordinator;
import com.desertadventure.run.data.StageCatalogDatabase;

/** Owns player, deck, combat, and linear stage progress for one play session. */
public final class GameSession {
    private final PlayerStats playerStats = new PlayerStats();
    private final CombatController combatController;
    private final ActionCardDeck actionCardDeck = new ActionCardDeck();
    private final RunProgress runProgress;
    private final StageRunCoordinator stageRunCoordinator;

    private GameplayMode mode = GameplayMode.HUB;
    private EnemyArchetypeId pendingEnemyArchetype;
    private boolean pendingBossFight;

    public GameSession() {
        GameDataBootstrap.initializeIfNeeded();
        combatController = new CombatController(playerStats);
        StageCatalog stageCatalog = StageCatalogDatabase.getRequired();
        runProgress = new RunProgress(stageCatalog);
        stageRunCoordinator = new StageRunCoordinator(
                runProgress, playerStats, actionCardDeck, this::setMode);
        actionCardDeck.resetToDefault();
    }

    public void startNewGame() {
        playerStats.resetForNewGame();
        mode = GameplayMode.HUB;
        runProgress.resetForNewRun();
        actionCardDeck.resetToDefault();
        pendingEnemyArchetype = null;
        pendingBossFight = false;
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }

    public CombatController getCombatController() {
        return combatController;
    }

    public ActionCardDeck getActionCardDeck() {
        return actionCardDeck;
    }

    public RunProgress getRunProgress() {
        return runProgress;
    }

    public boolean tryStartCurrentStageCombat() {
        if (mode != GameplayMode.HUB) {
            return false;
        }
        StageDef stage = runProgress.getCurrentStage();
        pendingBossFight = stage.boss();
        pendingEnemyArchetype = stage.boss() ? null : stage.enemyArchetype();
        mode = GameplayMode.COMBAT;
        return true;
    }

    public GameplayMode getMode() {
        return mode;
    }

    void setMode(GameplayMode mode) {
        this.mode = mode;
    }

    public boolean isPendingBossFight() {
        return pendingBossFight;
    }

    public EnemyArchetypeId consumePendingEnemyArchetype() {
        EnemyArchetypeId archetype = pendingEnemyArchetype;
        pendingEnemyArchetype = null;
        return archetype;
    }

    public void onCombatEnd(CombatOutcome outcome) {
        stageRunCoordinator.apply(outcome);
    }
}
