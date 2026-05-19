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

public class MainMenuScreen extends ScreenAdapter {
    private final DesertAdventure game;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private BitmapFont font;

    public MainMenuScreen(DesertAdventure game) {
        this.game = game;
    }

    @Override
    public void show() {
        font = new BitmapFont();
        font.getData().setScale(GameConfig.MENU_TITLE_FONT_SCALE);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(UiColors.MENU_SKY_CLEAR.r, UiColors.MENU_SKY_CLEAR.g, UiColors.MENU_SKY_CLEAR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float groundTop = GameConfig.VIEW_HEIGHT * GameConfig.MENU_GROUND_HEIGHT_RATIO;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(UiColors.MENU_SKY_BAND);
        shapes.rect(0, groundTop, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT * GameConfig.MENU_SKY_BAND_HEIGHT_RATIO);
        shapes.setColor(UiColors.MENU_GROUND_BAND);
        shapes.rect(0, 0, GameConfig.VIEW_WIDTH, groundTop);
        shapes.end();

        SpriteBatch batch = game.getBatch();
        batch.begin();
        font.setColor(Color.WHITE);
        CenteredTextDrawer.draw(batch, font, "Desert Adventure", GameConfig.VIEW_HEIGHT * GameConfig.MENU_TITLE_Y_RATIO);
        font.getData().setScale(GameConfig.MENU_BODY_FONT_SCALE);
        CenteredTextDrawer.draw(batch, font, "Survive the sandstorm. Reach the guardian.",
                GameConfig.VIEW_HEIGHT * GameConfig.MENU_SUBTITLE_Y_RATIO);
        CenteredTextDrawer.draw(batch, font, "Press Enter to Start", GameConfig.VIEW_HEIGHT * GameConfig.MENU_PROMPT_Y_RATIO);
        CenteredTextDrawer.draw(batch, font, "M: Map | N: Inventory | WASD: Move | J/K/L: Attack/Skill/Ultimate",
                GameConfig.VIEW_HEIGHT * GameConfig.MENU_HINT_Y_RATIO);
        batch.end();

        if (GameInputBindings.justConfirmed()) {
            game.setScreen(new GameplayScreen(game));
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
