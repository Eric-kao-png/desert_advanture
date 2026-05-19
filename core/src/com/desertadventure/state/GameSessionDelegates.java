package com.desertadventure.state;

import com.desertadventure.map.model.GridPos;

/** Named delegates wired into {@link GameSession} collaborators. */
final class GameSessionDelegates {
    private GameSessionDelegates() {
    }

    static SessionModeAccess modeAccess(GameSession session) {
        return new SessionModeAccess() {
            @Override
            public GameplayMode get() {
                return session.getMode();
            }

            @Override
            public void set(GameplayMode value) {
                session.setMode(value);
            }
        };
    }

    static MapTravelActions.PlayerPosition playerPosition(GameSession session) {
        return new MapTravelActions.PlayerPosition() {
            @Override
            public GridPos get() {
                return session.getPlayerGridPos();
            }

            @Override
            public void set(int x, int y) {
                session.setPlayerGridPos(x, y);
            }
        };
    }
}
