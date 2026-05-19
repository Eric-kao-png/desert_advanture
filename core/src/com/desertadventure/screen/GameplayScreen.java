package com.desertadventure.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.desertadventure.DesertAdventure;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.view.MapOverlayInput;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.presentation.GameplayRenderer;
import com.desertadventure.state.GameSession;

public class GameplayScreen extends ScreenAdapter {
    private final DesertAdventure game;
    private final GameSession session = new GameSession();
    private final GameplayRenderer renderer = new GameplayRenderer();
    private final GameViewport gameViewport = new GameViewport();
    private final MapOverlayInput mapInput = new MapOverlayInput();
    private final CombatSessionState combatState = new CombatSessionState();

    private GameplayHud hud;
    private GameplayInputHandler input;
    private GameplayModeUpdater modeUpdater;
    private GameplaySceneDrawer sceneDrawer;
    private BitmapFont font;

    public GameplayScreen(DesertAdventure game) {
        this.game = game;
    }

    @Override
    public void show() {
        font = new BitmapFont();
        font.getData().setScale(GameConfig.HUD_FONT_SCALE);
        hud = new GameplayHud(font);
        input = new GameplayInputHandler(game, session, gameViewport, mapInput);
        modeUpdater = new GameplayModeUpdater(session, input, renderer, combatState);
        sceneDrawer = new GameplaySceneDrawer(game, session, renderer, gameViewport, input, hud, font, modeUpdater);
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
