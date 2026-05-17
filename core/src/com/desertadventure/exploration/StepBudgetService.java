package com.desertadventure.exploration;

import com.desertadventure.player.PlayerStats;

public class StepBudgetService {
    private float stepsUsed;
    private float stepBudget;

    public void resetForCycle(PlayerStats stats) {
        stepsUsed = 0f;
        stepBudget = stats.getTotalStepBudget();
    }

    public float getStepsUsed() {
        return stepsUsed;
    }

    public float getStepBudget() {
        return stepBudget;
    }

    public float getRemainingSteps() {
        return Math.max(0f, stepBudget - stepsUsed);
    }

    public boolean canAfford(float cost) {
        return cost <= getRemainingSteps() + 1e-4f;
    }

    public void consumeSteps(float count) {
        stepsUsed += count;
    }

    public boolean isExhausted() {
        return stepsUsed >= stepBudget - 1e-4f;
    }
}
