package com.desertadventure.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.DesertAdventure;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.view.MapOverlayInput;
import com.desertadventure.map.view.MapOverlayLayout;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.presentation.GameplayRenderer;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

import java.util.ArrayList;
import java.util.List;

public class GameplayScreen extends ScreenAdapter {
    private final DesertAdventure game;
    private final GameSession session = new GameSession();
    private final GameplayRenderer renderer = new GameplayRenderer();
    private final GameViewport gameViewport = new GameViewport();
    private final MapOverlayInput mapInput = new MapOverlayInput();
    private GameplayHud hud;
    private GameplayInputHandler input;
    private BitmapFont font;
    private boolean combatInitialized;
    private GameplayMode lastMode = GameplayMode.EXPLORE_IDLE;
    private float messageTimer;

    public GameplayScreen(DesertAdventure game) {
        this.game = game;
    }

    @Override
    public void show() {
        font = new BitmapFont();
        font.getData().setScale(1.1f);
        hud = new GameplayHud(font);
        input = new GameplayInputHandler(game, session, gameViewport, mapInput);
        gameViewport.update();
        session.startNewGame();
        renderer.repopulateHouseProps(GameConfig.VIEW_WIDTH);
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);
        float feedback = input.handle();
        if (feedback > 0f) {
            messageTimer = feedback;
        }
        draw(delta);
    }

    private void update(float delta) {
        GameplayMode mode = session.getMode();
        detectModeTransitions(mode);
        ensureCombatInitialized(mode);

        if (messageTimer > 0f) {
            messageTimer -= delta;
        }
        if (session.getPendingMessage() != null && messageTimer <= 0f) {
            messageTimer = 4f;
        }

        switch (mode) {
            case RUNNING -> {
                session.getPathRunner().update(delta);
                session.updateRunning(delta);
            }
            case STORM -> {
                session.updateStorm(delta);
                if (session.getStormTimer() >= GameConfig.STORM_FADE_SECONDS) {
                    session.completeStorm();
                    combatInitialized = false;
                }
            }
            case COMBAT, BOSS_COMBAT -> input.updateCombatInput(delta);
            default -> {
            }
        }

        if ((mode == GameplayMode.COMBAT || mode == GameplayMode.BOSS_COMBAT) && combatInitialized) {
            GameplayMode after = session.getMode();
            if (after != GameplayMode.COMBAT && after != GameplayMode.BOSS_COMBAT) {
                combatInitialized = false;
            }
        }
        lastMode = mode;
    }

    private void detectModeTransitions(GameplayMode mode) {
        boolean enteringCombat = (mode == GameplayMode.COMBAT || mode == GameplayMode.BOSS_COMBAT)
                && lastMode != GameplayMode.COMBAT && lastMode != GameplayMode.BOSS_COMBAT;
        if (enteringCombat) {
            combatInitialized = false;
        }
        if (mode == GameplayMode.STORM && lastMode != GameplayMode.STORM) {
            renderer.repopulateHouseProps(GameConfig.VIEW_WIDTH);
        }
    }

    private void ensureCombatInitialized(GameplayMode mode) {
        if (mode == GameplayMode.COMBAT && !combatInitialized) {
            beginCombat(false);
        } else if (mode == GameplayMode.BOSS_COMBAT && !combatInitialized) {
            beginCombat(true);
        }
    }

    private void beginCombat(boolean boss) {
        session.getCombatController().startCombat(
                session.getCurrentDistanceBand(),
                boss,
                GameConfig.VIEW_WIDTH,
                GameplayRenderer.EXPLORE_GROUND_Y,
                session::onCombatEnd
        );
        combatInitialized = true;
    }

    private void draw(float delta) {
        GameplayMode mode = session.getMode();
        SpriteBatch batch = game.getBatch();
        renderer.setProjectionMatrix(gameViewport.getProjectionMatrix());

        if (mode == GameplayMode.MAP_OVERLAY) {
            input.updateMapHover();
        }

        switch (mode) {
            case MAP_OVERLAY -> {
                drawExploreScene(batch, false, delta);
                MapOverlayLayout layout = session.createMapOverlayLayout();
                renderer.renderMapOverlay(session, layout, input.getHoveredGridPos());
            }
            case COMBAT, BOSS_COMBAT -> {
                ensureCombatInitialized(mode);
                batch.begin();
                renderer.drawParallaxBackground(batch, false, delta);
                batch.end();
                CombatEntity player = session.getCombatController().getPlayer();
                if (player != null) {
                    List<CombatEntity> entities = new ArrayList<>();
                    entities.add(player);
                    entities.addAll(session.getCombatController().getEnemies());
                    renderer.renderCombatEntities(entities);
                }
            }
            case STORM -> {
                drawExploreScene(batch, false, delta);
                renderer.renderStorm(session.getStormTimer() / GameConfig.STORM_FADE_SECONDS);
            }
            case VICTORY -> {
                game.setScreen(new VictoryScreen(game));
                return;
            }
            default -> drawExploreScene(batch, mode == GameplayMode.RUNNING, delta);
        }

        batch.begin();
        hud.draw(batch, session, mode, messageTimer);
        batch.end();
    }

    private void drawExploreScene(SpriteBatch batch, boolean running, float delta) {
        batch.begin();
        renderer.drawParallaxBackground(batch, running, delta);
        batch.end();
        renderer.renderExploreForeground(session, running);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        if (font != null) {
            font.dispose();
        }
    }
}
