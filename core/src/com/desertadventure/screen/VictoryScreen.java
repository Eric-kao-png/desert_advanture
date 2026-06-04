package com.desertadventure.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.DesertAdventure;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameInputBindings;
import com.desertadventure.config.UiColors;

public class VictoryScreen extends ScreenAdapter {
    private final DesertAdventure game;
    private final ShapeRenderer shapes = new ShapeRenderer();

    public VictoryScreen(DesertAdventure game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(UiColors.VICTORY_CLEAR.r, UiColors.VICTORY_CLEAR.g, UiColors.VICTORY_CLEAR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(UiColors.VICTORY_BADGE);
        shapes.circle(GameConfig.VIEW_WIDTH / 2f,
                GameConfig.VIEW_HEIGHT * GameConfig.VICTORY_BADGE_Y_RATIO, GameConfig.VICTORY_BADGE_RADIUS);
        shapes.end();

        if (GameInputBindings.justConfirmed()) {
            game.setScreen(new GameplayScreen(game));
        }
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
