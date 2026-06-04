package com.desertadventure.run;

import com.desertadventure.state.GameplayMode;

@FunctionalInterface
public interface RunModeSetter {
    void setMode(GameplayMode mode);
}
