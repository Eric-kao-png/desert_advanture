package com.desertadventure;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.screen.MainMenuScreen;

public class DesertAdventure extends Game {
    private SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new MainMenuScreen(this));
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        super.dispose();
    }
}
