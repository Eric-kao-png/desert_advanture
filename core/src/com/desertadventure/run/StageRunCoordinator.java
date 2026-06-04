package com.desertadventure.run;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.state.GameplayMode;

/** Applies combat outcomes to stage index, deck cooldowns, and screen mode. */
public final class StageRunCoordinator {
    private final RunProgress runProgress;
    private final PlayerStats playerStats;
    private final ActionCardDeck actionCardDeck;
    private final RunModeSetter mode;

    public StageRunCoordinator(
            RunProgress runProgress,
            PlayerStats playerStats,
            ActionCardDeck actionCardDeck,
            RunModeSetter mode) {
        this.runProgress = runProgress;
        this.playerStats = playerStats;
        this.actionCardDeck = actionCardDeck;
        this.mode = mode;
    }

    public void apply(CombatOutcome outcome) {
        if (!playerStats.isAlive()) {
            applyRewind();
            return;
        }
        switch (outcome) {
            case VICTORY, BOSS_VICTORY -> applyVictory();
            case DEFEAT -> applyRewind();
        }
    }

    private void applyVictory() {
        if (!runProgress.isOnBossStage()) {
            runProgress.advanceAfterVictory();
        }
        mode.setMode(GameplayMode.HUB);
    }

    private void applyRewind() {
        runProgress.rewindToStart();
        actionCardDeck.clearAllCooldowns();
        playerStats.healFull();
        mode.setMode(GameplayMode.HUB);
    }
}
