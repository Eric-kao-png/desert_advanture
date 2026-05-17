package com.desertadventure.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.DesertAdventure;
import com.desertadventure.config.GameConfig;

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
        font.getData().setScale(1.5f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.75f, 0.6f, 0.35f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.45f, 0.65f, 0.95f, 1f);
        shapes.rect(0, GameConfig.VIEW_HEIGHT * 0.4f, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT * 0.6f);
        shapes.setColor(0.85f, 0.72f, 0.42f, 1f);
        shapes.rect(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT * 0.4f);
        shapes.end();

        SpriteBatch batch = game.getBatch();
        batch.begin();
        font.setColor(Color.WHITE);
        drawCentered(batch, "Desert Adventure", GameConfig.VIEW_HEIGHT * 0.68f);
        font.getData().setScale(1f);
        drawCentered(batch, "Survive the sandstorm. Reach the guardian.", GameConfig.VIEW_HEIGHT * 0.58f);
        drawCentered(batch, "Press Enter to Start", GameConfig.VIEW_HEIGHT * 0.35f);
        drawCentered(batch, "M: Map | WASD: Move | J/K/L: Attack/Skill/Ultimate", GameConfig.VIEW_HEIGHT * 0.28f);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameplayScreen(game));
        }
    }

    private void drawCentered(SpriteBatch batch, String text, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, (GameConfig.VIEW_WIDTH - layout.width) / 2f, y);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        if (font != null) {
            font.dispose();
        }
    }
}
