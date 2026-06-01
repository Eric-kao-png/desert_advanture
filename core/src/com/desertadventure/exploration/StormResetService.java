package com.desertadventure.exploration;

import com.desertadventure.map.model.GameMap;
import com.desertadventure.player.PlayerStats;

public class StormResetService {

    public void applyCycleReset(GameMap map, PlayerStats stats, StepBudgetService stepBudget) {
        map.resetCycleState();
        stats.healFull();
        stepBudget.resetForCycle(stats);
    }
}
