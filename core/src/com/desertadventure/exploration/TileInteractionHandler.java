package com.desertadventure.exploration;

import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameMessages;
import com.desertadventure.map.model.Tile;
import com.desertadventure.map.model.TileType;
import com.desertadventure.state.GameplayMode;

public final class TileInteractionHandler {
    private TileInteractionHandler() {
    }

    public static void handle(Tile tile, boolean duringMove, TileInteractionContext ctx) {
        switch (tile.getType()) {
            case ITEM -> handleItem(tile, duringMove, ctx);
            case EVENT -> handleEvent(tile, duringMove, ctx);
            case COMBAT -> ctx.callbacks.setMode(GameplayMode.COMBAT);
            case BOSS_SUMMON -> handleBossSummon(tile, duringMove, ctx);
            default -> ctx.finish(duringMove);
        }
    }

    private static void handleItem(Tile tile, boolean duringMove, TileInteractionContext ctx) {
        if (!tile.isItemCollectedThisCycle()) {
            tile.setItemCollectedThisCycle(true);
            ctx.map.markCycleModified(tile.getPosition());
            ctx.playerStats.applyItemBonus();
            ctx.playerStats.addExperience(GameConfig.ITEM_EXPERIENCE);
            ctx.callbacks.setPendingMessage(GameMessages.ITEM_COLLECTED);
        }
        ctx.finish(duringMove);
    }

    private static void handleEvent(Tile tile, boolean duringMove, TileInteractionContext ctx) {
        String eventId = tile.getEventId();
        if (eventId != null && ctx.permanentProgress.isEventCompleted(eventId)) {
            ctx.callbacks.setPendingMessage(GameMessages.RUINS_ALREADY_DONE);
        } else if (eventId != null) {
            ctx.eventTracker.completeEvent(eventId);
            ctx.permanentProgress.save();
            ctx.callbacks.setPendingMessage(GameMessages.requiredEventCompleted(eventId));
            tile.setCycleCleared(true);
            ctx.map.markCycleModified(tile.getPosition());
        }
        ctx.finish(duringMove);
    }

    private static void handleBossSummon(Tile tile, boolean duringMove, TileInteractionContext ctx) {
        if (ctx.eventTracker.allRequiredEventsComplete()) {
            ctx.markBossAvailable();
            ctx.callbacks.setMode(GameplayMode.BOSS_COMBAT);
        } else {
            ctx.callbacks.setPendingMessage(GameMessages.requiredEventsIncomplete(
                    ctx.eventTracker.getCompletedCount(), ctx.eventTracker.getRequiredCount()));
            ctx.finish(duringMove);
        }
    }
}
