package com.desertadventure.state;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Gdx;

import java.util.HashSet;
import java.util.Set;

public class PermanentProgress {
    private static final String PREFS_NAME = "desert_adventure_save";

    private final Set<String> completedEvents = new HashSet<>();
    private boolean outerZoneUnlocked;
    private boolean gameWon;

    public Set<String> getCompletedEvents() {
        return completedEvents;
    }

    public boolean isEventCompleted(String eventId) {
        return completedEvents.contains(eventId);
    }

    public void completeEvent(String eventId) {
        completedEvents.add(eventId);
    }

    public boolean isOuterZoneUnlocked() {
        return outerZoneUnlocked;
    }

    public void unlockOuterZone() {
        outerZoneUnlocked = true;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public int getCompletedEventCount() {
        return completedEvents.size();
    }

    public void save() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger("eventCount", completedEvents.size());
        int index = 0;
        for (String eventId : completedEvents) {
            prefs.putString("event_" + index, eventId);
            index++;
        }
        prefs.putBoolean("outerUnlocked", outerZoneUnlocked);
        prefs.putBoolean("gameWon", gameWon);
        prefs.flush();
    }

    public void load() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        completedEvents.clear();
        int count = prefs.getInteger("eventCount", 0);
        for (int i = 0; i < count; i++) {
            completedEvents.add(prefs.getString("event_" + i));
        }
        outerZoneUnlocked = prefs.getBoolean("outerUnlocked", false);
        gameWon = prefs.getBoolean("gameWon", false);
    }

    public void resetForNewGame() {
        completedEvents.clear();
        outerZoneUnlocked = false;
        gameWon = false;
        save();
    }
}
