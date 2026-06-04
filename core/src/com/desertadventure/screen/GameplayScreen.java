package com.desertadventure.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.desertadventure.DesertAdventure;
import com.desertadventure.config.GameConfig;
import com.desertadventure.infrastructure.gdx.UiFontFactory;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.presentation.GameplayRenderer;
import com.desertadventure.state.GameSession;

public class GameplayScreen extends ScreenAdapter {
    private final DesertAdventure game;
    private final GameSession session = new GameSession();
    private final GameplayRenderer renderer = new GameplayRenderer();
    private final GameViewport gameViewport = new GameViewport();
    private final CombatSessionState combatState = new CombatSessionState();

    private GameplayInputHandler input;
    private GameplayModeUpdater modeUpdater;
    private GameplaySceneDrawer sceneDrawer;
    private BitmapFont font;

    public GameplayScreen(DesertAdventure game) {
        this.game = game;
    }

    @Override
    public void show() {
        font = UiFontFactory.createHudFont();
        font.getData().setScale(GameConfig.HUD_FONT_SCALE);
        input = new GameplayInputHandler(game, session, gameViewport);
        modeUpdater = new GameplayModeUpdater(session, input, combatState);
        sceneDrawer = new GameplaySceneDrawer(game, session, renderer, gameViewport, input, font, modeUpdater);
        gameViewport.update();
        session.startNewGame();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(GameConfig.SKY_BASE_COLOR.r, GameConfig.SKY_BASE_COLOR.g,
                GameConfig.SKY_BASE_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        input.handle();
        modeUpdater.update(delta);
        sceneDrawer.draw(delta);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        if (font != null) {
            font.dispose();
        }
    }
}
