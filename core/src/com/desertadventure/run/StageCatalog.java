package com.desertadventure.run;

import java.util.Collections;
import java.util.List;

/** Immutable ordered list of stages for a card run. */
public final class StageCatalog {
    private final List<StageDef> stages;

    public StageCatalog(List<StageDef> stages) {
        if (stages == null || stages.isEmpty()) {
            throw new IllegalArgumentException("stages must not be empty");
        }
        this.stages = List.copyOf(stages);
    }

    public int size() {
        return stages.size();
    }

    public StageDef getStage(int index) {
        if (index < 0 || index >= stages.size()) {
            throw new IndexOutOfBoundsException("stage index " + index + " (size " + stages.size() + ")");
        }
        return stages.get(index);
    }

    public List<StageDef> getStages() {
        return Collections.unmodifiableList(stages);
    }
}
