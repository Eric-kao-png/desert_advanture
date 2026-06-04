package com.desertadventure.screen;

import com.desertadventure.DesertAdventure;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.screen.input.CombatCardInput;
import com.desertadventure.screen.input.HubKeyboardInput;
import com.badlogic.gdx.Gdx;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

/** Keyboard and pointer input for gameplay modes. */
public class GameplayInputHandler {
    private final DesertAdventure game;
    private final GameSession session;
    private final GameViewport viewport;
    private final HubKeyboardInput hubKeys = new HubKeyboardInput();
    private final HubScreenDrawer hubDrawer = new HubScreenDrawer();
    private final CombatCardInput combatCardInput = new CombatCardInput();

    public GameplayInputHandler(DesertAdventure game, GameSession session, GameViewport viewport) {
        this.game = game;
        this.session = session;
        this.viewport = viewport;
    }

    public void handle() {
        GameplayMode mode = session.getMode();

        if (mode == GameplayMode.HUB) {
            hubKeys.handle(game, session);
            if (Gdx.input.justTouched()) {
                hubDrawer.handlePointerTap(session, viewport.pointerWorldX(), viewport.pointerWorldY());
            }
            return;
        }

        if (mode.isCombat()) {
            return;
        }
    }

    public HubScreenDrawer getHubDrawer() {
        return hubDrawer;
    }

    public void updateCombatInput(float delta) {
        combatCardInput.handle(session, viewport, delta);
    }

    public CombatCardInput getCombatCardInput() {
        return combatCardInput;
    }
}
