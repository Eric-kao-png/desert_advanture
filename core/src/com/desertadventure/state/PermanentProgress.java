package com.desertadventure.state;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Gdx;

public class PermanentProgress {
    private static final String PREFS_NAME = "desert_adventure_save";

    private boolean gameWon;

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public void save() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putBoolean("gameWon", gameWon);
        prefs.flush();
    }

    public void load() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        gameWon = prefs.getBoolean("gameWon", false);
    }

    public void resetForNewGame() {
        gameWon = false;
        save();
    }
}
