package com.desertadventure.screen;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.DesertAdventure;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.view.MapOverlayLayout;
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
    private final GameplayHud hud;
    private final BitmapFont uiFont;
    private final GameplayModeUpdater modeUpdater;

    GameplaySceneDrawer(
            DesertAdventure game,
            GameSession session,
            GameplayRenderer renderer,
            GameViewport viewport,
            GameplayInputHandler input,
            GameplayHud hud,
            BitmapFont uiFont,
            GameplayModeUpdater modeUpdater) {
        this.game = game;
        this.session = session;
        this.renderer = renderer;
        this.viewport = viewport;
        this.input = input;
        this.hud = hud;
        this.uiFont = uiFont;
        this.modeUpdater = modeUpdater;
    }

    void draw(float delta) {
        GameplayMode mode = session.getMode();
        SpriteBatch batch = game.getBatch();
        renderer.setProjectionMatrix(viewport.getProjectionMatrix());

        updateHoverStateForMode(mode);

        switch (mode) {
            case HUB -> drawHub(batch, delta);
            case MAP_OVERLAY -> drawMapOverlay(batch, delta);
            case CHARACTER_OVERLAY -> drawCharacterOverlay(batch, delta);
            case COMBAT, BOSS_COMBAT -> drawCombat(batch, delta, mode);
            case STORM -> drawStorm(batch, delta);
            case VICTORY -> game.setScreen(new VictoryScreen(game));
            default -> drawExplore(batch, mode == GameplayMode.RUNNING, delta);
        }

        if (mode == GameplayMode.VICTORY) {
            return;
        }

        batch.begin();
        hud.draw(batch, session, mode);
        batch.end();
    }

    private void updateHoverStateForMode(GameplayMode mode) {
        if (mode == GameplayMode.MAP_OVERLAY) {
            input.updateMapHover();
            return;
        }
        if (mode == GameplayMode.CHARACTER_OVERLAY) {
            input.updateCharacterHover();
        }
    }

    private void drawMapOverlay(SpriteBatch batch, float delta) {
        drawExplore(batch, false, delta);
        MapOverlayLayout layout = session.createMapOverlayLayout();
        renderer.renderMapOverlay(
                session,
                layout,
                input.getHoveredGridPos(),
                batch,
                input.isMapDismissHovered(),
                input.isMapDismissPressed());
    }

    private void drawCharacterOverlay(SpriteBatch batch, float delta) {
        drawExplore(batch, false, delta);
        renderer.renderCharacterOverlay(
                batch,
                session,
                uiFont,
                input.getCharacterLayout(),
                input.getCharacterInput(),
                delta,
                input.isCharacterDismissHovered(),
                input.isCharacterDismissPressed());
    }

    private void drawCombat(SpriteBatch batch, float delta, GameplayMode mode) {
        modeUpdater.ensureCombatInitializedForDraw(mode);
        float blend = modeUpdater.getLayoutBlend();
        CombatCardLayout layout = input.getCombatCardInput().getLayout();
        layout.applyBlend(blend);

        drawBackgroundAndFloor(batch, false, delta, blend);

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
        drawBackgroundAndFloor(batch, false, delta, blend);
        batch.begin();
        input.getHubDrawer().draw(batch, uiFont, session);
        batch.end();
    }

    private void drawStorm(SpriteBatch batch, float delta) {
        drawExplore(batch, false, delta);
        renderer.renderStorm(session.getStormTimer() / GameConfig.STORM_FADE_SECONDS);
    }

    private void drawExplore(SpriteBatch batch, boolean running, float delta) {
        float blend = modeUpdater.getLayoutBlend();
        drawBackgroundAndFloor(batch, running, delta, blend);
        renderer.renderExploreForeground(session, running, blend, batch, uiFont);
    }

    private void drawBackgroundAndFloor(SpriteBatch batch, boolean running, float delta, float blend) {
        batch.begin();
        renderer.drawParallaxBackground(batch, running, delta, blend);
        renderer.drawParallaxFloor(batch, blend);
        batch.end();
    }

    private static List<CombatEntity> buildCombatEntities(CombatController combat, CombatEntity player) {
        List<CombatEntity> entities = new ArrayList<>();
        entities.add(player);
        entities.addAll(combat.getEnemies());
        return entities;
    }
}
