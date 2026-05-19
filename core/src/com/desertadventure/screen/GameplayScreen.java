package com.desertadventure.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.DesertAdventure;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.view.MapOverlayInput;
import com.desertadventure.map.view.MapOverlayLayout;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.presentation.PlaceholderRenderer;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

import java.util.ArrayList;
import java.util.List;

public class GameplayScreen extends ScreenAdapter {
    private final DesertAdventure game;
    private final GameSession session = new GameSession();
    private final PlaceholderRenderer renderer = new PlaceholderRenderer();
    private final GameViewport gameViewport = new GameViewport();
    private final MapOverlayInput mapInput = new MapOverlayInput();
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
        gameViewport.update();
        session.startNewGame();
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
        handleInput();
        draw(delta);
    }

    private void update(float delta) {
        GameplayMode mode = session.getMode();
        detectCombatEntry(mode);
        ensureCombatInitialized(mode);

        if (messageTimer > 0f) {
            messageTimer -= delta;
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
            case COMBAT, BOSS_COMBAT -> updateCombat(delta);
            default -> {
            }
        }

        if ((mode == GameplayMode.COMBAT || mode == GameplayMode.BOSS_COMBAT) && combatInitialized) {
            GameplayMode after = session.getMode();
            if (after != GameplayMode.COMBAT && after != GameplayMode.BOSS_COMBAT) {
                combatInitialized = false;
            }
        }

        if (session.getPendingMessage() != null && messageTimer <= 0f) {
            messageTimer = 4f;
        }
    }

    private void detectCombatEntry(GameplayMode mode) {
        boolean enteringCombat = (mode == GameplayMode.COMBAT || mode == GameplayMode.BOSS_COMBAT)
                && lastMode != GameplayMode.COMBAT && lastMode != GameplayMode.BOSS_COMBAT;
        if (enteringCombat) {
            combatInitialized = false;
        }
        lastMode = mode;
    }

    private void ensureCombatInitialized(GameplayMode mode) {
        if (mode == GameplayMode.COMBAT && !combatInitialized) {
            beginCombat(false);
        } else if (mode == GameplayMode.BOSS_COMBAT && !combatInitialized) {
            beginCombat(true);
        }
    }

    private void beginCombat(boolean boss) {
        float groundY = 120f;
        session.getCombatController().startCombat(
                session.getCurrentDistanceBand(),
                boss,
                GameConfig.VIEW_WIDTH,
                groundY,
                session::onCombatEnd
        );
        combatInitialized = true;
    }

    private void updateCombat(float delta) {
        if (!combatInitialized) {
            return;
        }
        float move = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            move -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            move += 1f;
        }
        session.getCombatController().update(delta, move);

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            session.getCombatController().tryBasicAttack();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            session.getCombatController().trySkill();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            session.getCombatController().tryUltimate();
        }
    }

    private void draw(float delta) {
        GameplayMode mode = session.getMode();
        float groundY = 120f;
        SpriteBatch batch = game.getBatch();
        renderer.setProjectionMatrix(gameViewport.getProjectionMatrix());

        if (mode == GameplayMode.MAP_OVERLAY) {
            updateMapPointer();
        }

        switch (mode) {
            case MAP_OVERLAY -> {
                drawExploreScene(batch, false, delta);
                MapOverlayLayout layout = session.createMapOverlayLayout();
                renderer.renderMapOverlay(session, layout, mapInput.getHoveredGridPos());
            }
            case COMBAT, BOSS_COMBAT -> {
                ensureCombatInitialized(mode);
                batch.begin();
                renderer.drawParallaxBackground(batch, false, delta);
                batch.end();
                CombatEntity combatPlayer = session.getCombatController().getPlayer();
                if (combatPlayer != null) {
                    List<CombatEntity> entities = new ArrayList<>();
                    entities.add(combatPlayer);
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

        drawHud();
    }

    private void drawExploreScene(SpriteBatch batch, boolean running, float delta) {
        batch.begin();
        renderer.drawParallaxBackground(batch, running, delta);
        batch.end();
        renderer.renderExploreForeground(session, running);
    }

    private void drawHud() {
        SpriteBatch batch = game.getBatch();
        batch.begin();
        font.setColor(Color.WHITE);

        String hp = String.format("HP: %.0f/%.0f", session.getPlayerStats().getHp(),
                session.getPlayerStats().getMaxHp());
        String steps = String.format("Steps: %.1f/%.1f", session.getStepBudget().getRemainingSteps(),
                session.getStepBudget().getStepBudget());
        String events = String.format("Required Events: %d/%d", session.getEventTracker().getCompletedCount(),
                session.getEventTracker().getRequiredCount());
        GridPos currentTile = session.getDisplayGridPos();
        float distanceFromOrigin = session.getDistanceFromOrigin();
        String tileLine = String.format("Tile: %s", currentTile);
        String distanceLine = String.format("Distance from origin: %.1f", distanceFromOrigin);

        font.draw(batch, hp, 16, GameConfig.VIEW_HEIGHT - 16);
        font.draw(batch, steps, 16, GameConfig.VIEW_HEIGHT - 40);
        font.draw(batch, events, 16, GameConfig.VIEW_HEIGHT - 64);
        font.draw(batch, tileLine, 16, GameConfig.VIEW_HEIGHT - 88);
        font.draw(batch, distanceLine, 16, GameConfig.VIEW_HEIGHT - 112);

        GameplayMode mode = session.getMode();
        if (mode == GameplayMode.EXPLORE_IDLE) {
            font.draw(batch, "[M] Map", 16, 40);
        } else if (mode == GameplayMode.MAP_OVERLAY) {
            font.draw(batch, "Click destination | Arrows: pan | [Esc] Cancel", 16, 40);
        } else if (mode == GameplayMode.RUNNING) {
            font.draw(batch, "Moving...", 16, 40);
        } else if (mode == GameplayMode.COMBAT || mode == GameplayMode.BOSS_COMBAT) {
            font.draw(batch, "WASD: Move | J: Attack K: Skill L: Ultimate", 16, 40);
            CombatEntity boss = findBoss();
            if (boss != null) {
                font.draw(batch, String.format("Boss HP: %.0f/%.0f", boss.getHp(), boss.getMaxHp()),
                        GameConfig.VIEW_WIDTH - 280, GameConfig.VIEW_HEIGHT - 16);
            }
        } else if (mode == GameplayMode.STORM) {
            font.setColor(0.2f, 0.15f, 0.05f, 1f);
            drawCentered(batch, "Sandstorm!", GameConfig.VIEW_HEIGHT * 0.55f);
        }

        if (session.getPendingMessage() != null && messageTimer > 0f) {
            font.setColor(Color.YELLOW);
            drawCentered(batch, session.getPendingMessage(), GameConfig.VIEW_HEIGHT * 0.2f);
        } else if (messageTimer <= 0f) {
            session.clearPendingMessage();
        }

        batch.end();
    }

    private CombatEntity findBoss() {
        for (CombatEntity enemy : session.getCombatController().getEnemies()) {
            if (enemy.getKind() == CombatEntity.Kind.BOSS) {
                return enemy;
            }
        }
        return null;
    }

    private void drawCentered(SpriteBatch batch, String text, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, (GameConfig.VIEW_WIDTH - layout.width) / 2f, y);
    }

    private void panMapViewWithArrows() {
        int step = GameConfig.MAP_PAN_STEP;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            session.panMapView(-step, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            session.panMapView(step, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            session.panMapView(0, step);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            session.panMapView(0, -step);
        }
    }

    private void updateMapPointer() {
        float worldX = gameViewport.toWorldX(Gdx.input.getX(), Gdx.input.getY());
        float worldY = gameViewport.toWorldY(Gdx.input.getX(), Gdx.input.getY());
        MapOverlayLayout layout = session.createMapOverlayLayout();
        mapInput.updateHover(layout, worldX, worldY);
    }

    private void handleInput() {
        GameplayMode mode = session.getMode();

        if (mode == GameplayMode.MAP_OVERLAY) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                session.setMode(GameplayMode.EXPLORE_IDLE);
                return;
            }
            panMapViewWithArrows();
            updateMapPointer();
            if (Gdx.input.justTouched()) {
                GridPos hovered = mapInput.getHoveredGridPos();
                if (hovered != null && session.trySelectDestination(hovered)) {
                    messageTimer = 3f;
                } else if (hovered != null) {
                    messageTimer = 3f;
                }
            }
            return;
        }

        if (mode == GameplayMode.EXPLORE_IDLE) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                session.openMapOverlay();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }
}
