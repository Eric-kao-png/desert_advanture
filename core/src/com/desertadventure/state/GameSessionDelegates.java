package com.desertadventure.state;

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
}
