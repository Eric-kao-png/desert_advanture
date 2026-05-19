package com.desertadventure.screen;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.DesertAdventure;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.view.MapOverlayLayout;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.presentation.GameplayRenderer;
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

        if (mode == GameplayMode.MAP_OVERLAY) {
            input.updateMapHover();
        }

        switch (mode) {
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
        input.updateCharacterHover();
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
        batch.begin();
        renderer.drawParallaxBackground(batch, false, delta);
        batch.end();
        CombatEntity player = session.getCombatController().getPlayer();
        if (player == null) {
            return;
        }
        List<CombatEntity> entities = new ArrayList<>();
        entities.add(player);
        entities.addAll(session.getCombatController().getEnemies());
        renderer.renderCombatEntities(entities);
    }

    private void drawStorm(SpriteBatch batch, float delta) {
        drawExplore(batch, false, delta);
        renderer.renderStorm(session.getStormTimer() / GameConfig.STORM_FADE_SECONDS);
    }

    private void drawExplore(SpriteBatch batch, boolean running, float delta) {
        batch.begin();
        renderer.drawParallaxBackground(batch, running, delta);
        batch.end();
        renderer.renderExploreForeground(session, running);
    }
}
