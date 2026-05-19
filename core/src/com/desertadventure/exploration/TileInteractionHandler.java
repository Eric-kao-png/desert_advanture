package com.desertadventure.exploration;

import com.desertadventure.event.RequiredEventTracker;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.Tile;
import com.desertadventure.map.model.TileType;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.state.ExplorationCallbacks;
import com.desertadventure.state.GameplayMode;
import com.desertadventure.state.PermanentProgress;

/**
 * Applies tile arrival effects (items, events, combat, boss gate).
 */
public final class TileInteractionHandler {
    private TileInteractionHandler() {
    }

    public static void handle(Tile tile, boolean duringMove, InteractionContext ctx) {
        switch (tile.getType()) {
            case ITEM -> handleItem(tile, duringMove, ctx);
            case EVENT -> handleEvent(tile, duringMove, ctx);
            case COMBAT -> ctx.callbacks.setMode(GameplayMode.COMBAT);
            case BOSS_SUMMON -> handleBossSummon(tile, duringMove, ctx);
            default -> ctx.finish(duringMove);
        }
    }

    private static void handleItem(Tile tile, boolean duringMove, InteractionContext ctx) {
        if (!tile.isItemCollectedThisCycle()) {
            tile.setItemCollectedThisCycle(true);
            ctx.map.markCycleModified(tile.getPosition());
            ctx.playerStats.applyItemBonus();
            ctx.playerStats.addExperience(10);
            ctx.callbacks.setPendingMessage("Item collected! ATK +2, step limit +1");
        }
        ctx.finish(duringMove);
    }

    private static void handleEvent(Tile tile, boolean duringMove, InteractionContext ctx) {
        String eventId = tile.getEventId();
        if (eventId != null && ctx.permanentProgress.isEventCompleted(eventId)) {
            ctx.callbacks.setPendingMessage("Ruins already investigated.");
        } else if (eventId != null) {
            ctx.eventTracker.completeEvent(eventId);
            ctx.permanentProgress.save();
            ctx.callbacks.setPendingMessage("Required event completed: " + eventId);
            tile.setCycleCleared(true);
            ctx.map.markCycleModified(tile.getPosition());
        }
        ctx.finish(duringMove);
    }

    private static void handleBossSummon(Tile tile, boolean duringMove, InteractionContext ctx) {
        if (ctx.eventTracker.allRequiredEventsComplete()) {
            ctx.markBossAvailable();
            ctx.callbacks.setMode(GameplayMode.BOSS_COMBAT);
        } else {
            ctx.callbacks.setPendingMessage("Required events incomplete ("
                    + ctx.eventTracker.getCompletedCount() + "/" + ctx.eventTracker.getRequiredCount() + ")");
            ctx.finish(duringMove);
        }
    }

    public static final class InteractionContext {
        private final ExplorationCallbacks callbacks;
        private final GameMap map;
        private final PlayerStats playerStats;
        private final PermanentProgress permanentProgress;
        private final RequiredEventTracker eventTracker;
        private final Runnable resumeTravel;
        private final java.util.function.Consumer<Boolean> setBossAvailable;

        public InteractionContext(
                ExplorationCallbacks callbacks,
                GameMap map,
                PlayerStats playerStats,
                PermanentProgress permanentProgress,
                RequiredEventTracker eventTracker,
                Runnable resumeTravel,
                java.util.function.Consumer<Boolean> setBossAvailable) {
            this.callbacks = callbacks;
            this.map = map;
            this.playerStats = playerStats;
            this.permanentProgress = permanentProgress;
            this.eventTracker = eventTracker;
            this.resumeTravel = resumeTravel;
            this.setBossAvailable = setBossAvailable;
        }

        void markBossAvailable() {
            setBossAvailable.accept(true);
        }

        void finish(boolean duringMove) {
            if (duringMove) {
                resumeTravel.run();
            } else {
                callbacks.setMode(GameplayMode.EXPLORE_IDLE);
            }
        }
    }
}
