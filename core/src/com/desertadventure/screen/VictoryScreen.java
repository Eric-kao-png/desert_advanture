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
        font.getData().setScale(1.8f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.15f, 0.35f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.95f, 0.85f, 0.3f, 1f);
        shapes.circle(GameConfig.VIEW_WIDTH / 2f, GameConfig.VIEW_HEIGHT * 0.55f, 80f);
        shapes.end();

        SpriteBatch batch = game.getBatch();
        batch.begin();
        font.setColor(Color.WHITE);
        drawCentered("Victory!", GameConfig.VIEW_HEIGHT * 0.7f);
        font.getData().setScale(1.1f);
        drawCentered("You defeated the desert guardian", GameConfig.VIEW_HEIGHT * 0.45f);
        drawCentered("Press Enter to return to main menu", GameConfig.VIEW_HEIGHT * 0.3f);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    private void drawCentered(String text, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(game.getBatch(), text, (GameConfig.VIEW_WIDTH - layout.width) / 2f, y);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        if (font != null) {
            font.dispose();
        }
    }
}
