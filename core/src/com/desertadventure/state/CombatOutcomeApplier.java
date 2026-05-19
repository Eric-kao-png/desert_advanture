package com.desertadventure.state;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameMessages;
import com.desertadventure.exploration.TravelMovement;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.model.Tile;
import com.desertadventure.player.PlayerStats;

/** Applies combat end results to exploration state. */
public final class CombatOutcomeApplier {
    private final GameMap map;
    private final PlayerStats playerStats;
    private final PermanentProgress permanentProgress;
    private final TravelMovement travel;
    private final MessageFeed messages;
    private final SessionModeAccess mode;
    private final Runnable triggerStorm;
    private final PlayerPosition player;

    public interface PlayerPosition {
        GridPos get();
    }

    public CombatOutcomeApplier(
            GameMap map,
            PlayerStats playerStats,
            PermanentProgress permanentProgress,
            TravelMovement travel,
            MessageFeed messages,
            SessionModeAccess mode,
            Runnable triggerStorm,
            PlayerPosition player) {
        this.map = map;
        this.playerStats = playerStats;
        this.permanentProgress = permanentProgress;
        this.travel = travel;
        this.messages = messages;
        this.mode = mode;
        this.triggerStorm = triggerStorm;
        this.player = player;
    }

    public void apply(CombatOutcome outcome) {
        Tile tile = map.getTile(player.get());
        switch (outcome) {
            case VICTORY -> applyVictory(tile);
            case BOSS_VICTORY -> applyBossVictory();
            case DEFEAT -> triggerStorm.run();
        }
    }

    private void applyVictory(Tile tile) {
        tile.setCycleCleared(true);
        map.markCycleModified(tile.getPosition());
        playerStats.addExperience(GameConfig.VICTORY_EXPERIENCE);
        messages.push(GameMessages.BATTLE_WON);
        if (travel.hasActivePlan()) {
            travel.resume();
        } else {
            mode.set(GameplayMode.EXPLORE_IDLE);
        }
    }

    private void applyBossVictory() {
        permanentProgress.setGameWon(true);
        permanentProgress.save();
        mode.set(GameplayMode.VICTORY);
    }
}
