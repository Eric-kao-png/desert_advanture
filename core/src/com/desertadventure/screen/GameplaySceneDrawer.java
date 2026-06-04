package com.desertadventure.screen;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.DesertAdventure;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.presentation.GameplayRenderer;
import com.desertadventure.screen.input.CombatCardInput;
import com.desertadventure.screen.layout.CombatCardLayout;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

import java.util.ArrayList;
import java.util.List;

final class GameplaySceneDrawer {
    private final DesertAdventure game;
    private final GameSession session;
    private final GameplayRenderer renderer;
    private final GameViewport viewport;
    private final GameplayInputHandler input;
    private final BitmapFont uiFont;
    private final GameplayModeUpdater modeUpdater;

    GameplaySceneDrawer(
            DesertAdventure game,
            GameSession session,
            GameplayRenderer renderer,
            GameViewport viewport,
            GameplayInputHandler input,
            BitmapFont uiFont,
            GameplayModeUpdater modeUpdater) {
        this.game = game;
        this.session = session;
        this.renderer = renderer;
        this.viewport = viewport;
        this.input = input;
        this.uiFont = uiFont;
        this.modeUpdater = modeUpdater;
    }

    void draw(float delta) {
        GameplayMode mode = session.getMode();
        SpriteBatch batch = game.getBatch();
        renderer.setProjectionMatrix(viewport.getProjectionMatrix());

        if (mode == GameplayMode.HUB) {
            drawHub(batch);
        } else if (mode.isCombat()) {
            drawCombat(batch, mode);
        }
    }

    private void drawCombat(SpriteBatch batch, GameplayMode mode) {
        modeUpdater.ensureCombatInitializedForDraw(mode);
        CombatCardInput cardInput = input.getCombatCardInput();
        CombatCardLayout layout = cardInput.getLayout();

        CombatController combat = session.getCombatController();
        CombatEntity player = combat.getPlayer();
        if (player == null) {
            return;
        }
        List<CombatEntity> entities = new ArrayList<>();
        entities.add(player);
        entities.addAll(combat.getEnemies());
        int draggingId = cardInput.isDragging() ? cardInput.getDragInstanceId() : -1;
        renderer.renderCombatHand(
                combat, layout, batch, uiFont, draggingId,
                cardInput.getInspectedCardType(),
                cardInput.getInspectedCardInstance());
        renderer.renderCombatSlotsAndControls(
                combat, layout, batch, uiFont,
                cardInput.getHoveredDismissSlot(),
                cardInput.isPressedDismiss());
        if (cardInput.isDragging()) {
            renderer.renderCombatDraggedCard(
                    combat, layout, batch, uiFont,
                    cardInput.getDragInstanceId(),
                    cardInput.getDragPointerX(),
                    cardInput.getDragPointerY());
        }
        renderer.renderCombatEntities(combat, entities, batch, uiFont);
    }

    private void drawHub(SpriteBatch batch) {
        batch.begin();
        input.getHubDrawer().draw(batch, uiFont, session);
        batch.end();
    }
}
