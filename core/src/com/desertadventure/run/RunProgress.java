package com.desertadventure.run;

/** Tracks linear stage index for the current card run (0-based). */
public final class RunProgress {
    private final StageCatalog catalog;
    private int currentStageIndex;

    public RunProgress(StageCatalog catalog) {
        this.catalog = catalog;
    }

    public StageCatalog getCatalog() {
        return catalog;
    }

    public int getCurrentStageIndex() {
        return currentStageIndex;
    }

    /** 1-based stage number for UI. */
    public int getDisplayStageNumber() {
        return currentStageIndex + 1;
    }

    public int getTotalStages() {
        return catalog.size();
    }

    public StageDef getCurrentStage() {
        return catalog.getStage(currentStageIndex);
    }

    public void resetForNewRun() {
        currentStageIndex = 0;
    }

    public void rewindToStart() {
        currentStageIndex = 0;
    }

    /** Advances after a non-boss victory; returns false if already on the final boss stage. */
    public boolean advanceAfterVictory() {
        StageDef current = getCurrentStage();
        if (current.boss()) {
            return false;
        }
        if (currentStageIndex >= catalog.size() - 1) {
            return false;
        }
        currentStageIndex++;
        return true;
    }

    public boolean isOnBossStage() {
        return getCurrentStage().boss();
    }
}
