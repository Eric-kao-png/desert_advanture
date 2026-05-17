package com.desertadventure.event;

import com.desertadventure.config.GameConfig;
import com.desertadventure.state.PermanentProgress;

public class RequiredEventTracker {
    private final PermanentProgress progress;

    public RequiredEventTracker(PermanentProgress progress) {
        this.progress = progress;
    }

    public boolean isEventRequired(String eventId) {
        return eventId != null && eventId.startsWith("event_");
    }

    public void completeEvent(String eventId) {
        if (isEventRequired(eventId)) {
            progress.completeEvent(eventId);
        }
    }

    public boolean allRequiredEventsComplete() {
        return progress.getCompletedEventCount() >= GameConfig.REQUIRED_EVENT_COUNT;
    }

    public int getCompletedCount() {
        return progress.getCompletedEventCount();
    }

    public int getRequiredCount() {
        return GameConfig.REQUIRED_EVENT_COUNT;
    }
}
