package com.desertadventure.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.DesertAdventure;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameInputBindings;
import com.desertadventure.config.UiColors;
import com.desertadventure.screen.ui.CenteredTextDrawer;

public class VictoryScreen extends ScreenAdapter {
    private final DesertAdventure game;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private BitmapFont font;

    public VictoryScreen(DesertAdventure game) {
        this.game = game;
    }

    @Override
    public void show() {
        font = new BitmapFont();
        font.getData().setScale(GameConfig.VICTORY_TITLE_FONT_SCALE);
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

        SpriteBatch batch = game.getBatch();
        batch.begin();
        font.setColor(Color.WHITE);
        CenteredTextDrawer.draw(batch, font, "Victory!", GameConfig.VIEW_HEIGHT * GameConfig.VICTORY_TITLE_Y_RATIO);
        font.getData().setScale(GameConfig.VICTORY_BODY_FONT_SCALE);
        CenteredTextDrawer.draw(batch, font, "You defeated the desert guardian",
                GameConfig.VIEW_HEIGHT * GameConfig.VICTORY_SUBTITLE_Y_RATIO);
        CenteredTextDrawer.draw(batch, font, "Press Enter to return to main menu",
                GameConfig.VIEW_HEIGHT * GameConfig.VICTORY_PROMPT_Y_RATIO);
        batch.end();

        if (GameInputBindings.justConfirmed()) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override
    public void dispose() {
        shapes.dispose();
        if (font != null) {
            font.dispose();
        }
    }
}
