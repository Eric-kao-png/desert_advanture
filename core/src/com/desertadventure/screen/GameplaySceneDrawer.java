package com.desertadventure.screen;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.DesertAdventure;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.presentation.GameplayRenderer;
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

        switch (mode) {
            case HUB -> drawHub(batch, delta);
            case COMBAT, BOSS_COMBAT -> drawCombat(batch, delta, mode);
            case VICTORY -> game.setScreen(new VictoryScreen(game));
            default -> drawHub(batch, delta);
        }
    }

    private void drawCombat(SpriteBatch batch, float delta, GameplayMode mode) {
        modeUpdater.ensureCombatInitializedForDraw(mode);
        float blend = modeUpdater.getLayoutBlend();
        CombatCardLayout layout = input.getCombatCardInput().getLayout();
        layout.applyBlend(blend);

        drawBackgroundAndFloor(batch, blend);

        CombatController combat = session.getCombatController();
        CombatEntity player = combat.getPlayer();
        if (player == null) {
            return;
        }
        List<CombatEntity> entities = buildCombatEntities(combat, player);
        renderer.renderCombatEntities(combat, entities, batch, uiFont);
        renderer.renderCombatHand(combat, layout, batch, uiFont);
        renderer.renderCombatSlotsAndControls(combat, layout, batch, uiFont);
    }

    private void drawHub(SpriteBatch batch, float delta) {
        float blend = modeUpdater.getLayoutBlend();
        drawBackgroundAndFloor(batch, blend);
        batch.begin();
        input.getHubDrawer().draw(batch, uiFont, session);
        batch.end();
    }

    private void drawBackgroundAndFloor(SpriteBatch batch, float blend) {
        batch.begin();
        renderer.drawSceneBackground(batch, blend);
        batch.end();
    }

    private static List<CombatEntity> buildCombatEntities(CombatController combat, CombatEntity player) {
        List<CombatEntity> entities = new ArrayList<>();
        entities.add(player);
        entities.addAll(combat.getEnemies());
        return entities;
    }
}
