package com.desertadventure.state;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardDeckResetPolicy;
import com.desertadventure.combat.card.DefaultActionCardDeckResetPolicy;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.run.RunProgress;
import com.desertadventure.run.StageCatalog;
import com.desertadventure.run.StageDef;
import com.desertadventure.run.StageRunCoordinator;
import com.desertadventure.run.data.StageCatalogDatabase;

public class GameSession {
    private final PlayerStats playerStats = new PlayerStats();
    private final PermanentProgress permanentProgress = new PermanentProgress();
    private final CombatController combatController;
    private final ActionCardDeck actionCardDeck = new ActionCardDeck();
    private final ActionCardDeckResetPolicy actionCardDeckResetPolicy = new DefaultActionCardDeckResetPolicy();
    private final StageCatalog stageCatalog;
    private final RunProgress runProgress;
    private final StageRunCoordinator stageRunCoordinator;

    private GameplayMode mode = GameplayMode.HUB;
    private EnemyArchetypeId pendingCombatArchetype;

    public GameSession() {
        GameDataBootstrap.initializeIfNeeded();
        combatController = new CombatController(playerStats);
        stageCatalog = StageCatalogDatabase.getRequired();
        runProgress = new RunProgress(stageCatalog);
        stageRunCoordinator = new StageRunCoordinator(
                runProgress, playerStats, permanentProgress, actionCardDeck,
                GameSessionDelegates.modeAccess(this));
        actionCardDeck.resetToDefault();
    }

    public void startNewGame() {
        permanentProgress.resetForNewGame();
        playerStats.resetForNewGame();
        mode = GameplayMode.HUB;
        runProgress.resetForNewRun();
        actionCardDeckResetPolicy.resetDeck(actionCardDeck);
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }

    public PermanentProgress getPermanentProgress() {
        return permanentProgress;
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

    public StageCatalog getStageCatalog() {
        return stageCatalog;
    }

    /** Starts combat for the current linear stage (hub). */
    public boolean tryStartCurrentStageCombat() {
        if (mode != GameplayMode.HUB) {
            return false;
        }
        StageDef stage = runProgress.getCurrentStage();
        if (stage.boss()) {
            mode = GameplayMode.BOSS_COMBAT;
            pendingCombatArchetype = null;
        } else {
            mode = GameplayMode.COMBAT;
            pendingCombatArchetype = stage.enemyArchetype();
        }
        return true;
    }

    public GameplayMode getMode() {
        return mode;
    }

    public void setMode(GameplayMode mode) {
        this.mode = mode;
    }

    /** Archetype from the current stage; cleared after combat session starts. */
    public EnemyArchetypeId consumePendingCombatArchetype() {
        EnemyArchetypeId archetype = pendingCombatArchetype;
        pendingCombatArchetype = null;
        return archetype;
    }

    public void onCombatEnd(CombatOutcome outcome) {
        stageRunCoordinator.apply(outcome, combatController.getLastDefeatedEnemyArchetype());
    }
}
