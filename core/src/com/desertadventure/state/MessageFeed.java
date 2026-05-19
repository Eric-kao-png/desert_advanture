package com.desertadventure.state;

import com.desertadventure.config.GameConfig;

import java.util.ArrayDeque;
import java.util.Iterator;

/** Bottom-anchored log: newest line at slot 0, older lines pushed upward; lines fade out. */
public final class MessageFeed {
    public static final class Line {
        private final String text;
        private float timeRemaining;

        Line(String text) {
            this.text = text;
            this.timeRemaining = GameConfig.MESSAGE_FEED_DISPLAY_SECONDS + GameConfig.MESSAGE_FEED_FADE_SECONDS;
        }

        public String getText() {
            return text;
        }

        /** 1 while fully visible; ramps down during the fade window. */
        public float getAlpha() {
            float fade = GameConfig.MESSAGE_FEED_FADE_SECONDS;
            if (timeRemaining <= fade) {
                return Math.max(0f, timeRemaining / fade);
            }
            return 1f;
        }
    }

    private final ArrayDeque<Line> lines = new ArrayDeque<>();

    public void push(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        lines.addFirst(new Line(message));
        while (lines.size() > GameConfig.MESSAGE_FEED_MAX_LINES) {
            lines.removeLast();
        }
    }

    public void update(float delta) {
        Iterator<Line> it = lines.iterator();
        while (it.hasNext()) {
            Line line = it.next();
            line.timeRemaining -= delta;
            if (line.timeRemaining <= 0f) {
                it.remove();
            }
        }
    }

    public void clear() {
        lines.clear();
    }

    /** Newest message first (maps to the lowest on-screen line). */
    public Iterator<Line> newestFirst() {
        return lines.iterator();
    }

    public int size() {
        return lines.size();
    }
}
