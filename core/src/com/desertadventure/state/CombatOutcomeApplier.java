package com.desertadventure.state;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardRewards;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
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
    private final ActionCardDeck actionCardDeck;
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
            ActionCardDeck actionCardDeck,
            SessionModeAccess mode,
            Runnable triggerStorm,
            PlayerPosition player) {
        this.map = map;
        this.playerStats = playerStats;
        this.permanentProgress = permanentProgress;
        this.travel = travel;
        this.messages = messages;
        this.actionCardDeck = actionCardDeck;
        this.mode = mode;
        this.triggerStorm = triggerStorm;
        this.player = player;
    }

    public void apply(CombatOutcome outcome, EnemyArchetypeId defeatedEnemyArchetype) {
        Tile tile = map.getTile(player.get());
        switch (outcome) {
            case VICTORY -> applyVictory(tile, defeatedEnemyArchetype);
            // Camp-clear boss uses BOSS_VICTORY, not VICTORY — no random card loot here.
            case BOSS_VICTORY -> applyBossVictory();
            case DEFEAT -> triggerStorm.run();
        }
    }

    private void applyVictory(Tile tile, EnemyArchetypeId defeatedEnemyArchetype) {
        tile.setCycleCleared(true);
        map.markCycleModified(tile.getPosition());
        playerStats.addExperience(GameConfig.VICTORY_EXPERIENCE);
        messages.push(GameMessages.BATTLE_WON);
        if (defeatedEnemyArchetype == null) {
            throw new IllegalStateException("VICTORY requires a defeated enemy archetype");
        }
        ActionCardType reward = ActionCardRewards.rollVictoryCard(defeatedEnemyArchetype);
        actionCardDeck.addCard(reward);
        messages.push(GameMessages.cardGained(reward.getDisplayName()));
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
