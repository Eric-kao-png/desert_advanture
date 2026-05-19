package com.desertadventure.exploration;

import com.desertadventure.event.RequiredEventTracker;
import com.desertadventure.item.Inventory;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.state.ExplorationCallbacks;
import com.desertadventure.state.GameplayMode;
import com.desertadventure.state.PermanentProgress;

/** Dependencies for {@link TileInteractionHandler}. */
public final class TileInteractionContext {
    final ExplorationCallbacks callbacks;
    final GameMap map;
    final PlayerStats playerStats;
    final PermanentProgress permanentProgress;
    final RequiredEventTracker eventTracker;
    final Inventory inventory;
    final StepBudgetService stepBudget;
    private final Runnable resumeTravel;
    private final java.util.function.Consumer<Boolean> setBossAvailable;

    public TileInteractionContext(
            ExplorationCallbacks callbacks,
            GameMap map,
            PlayerStats playerStats,
            PermanentProgress permanentProgress,
            RequiredEventTracker eventTracker,
            Inventory inventory,
            StepBudgetService stepBudget,
            Runnable resumeTravel,
            java.util.function.Consumer<Boolean> setBossAvailable) {
        this.callbacks = callbacks;
        this.map = map;
        this.playerStats = playerStats;
        this.permanentProgress = permanentProgress;
        this.eventTracker = eventTracker;
        this.inventory = inventory;
        this.stepBudget = stepBudget;
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
