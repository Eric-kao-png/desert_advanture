package com.desertadventure;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.screen.GameplayScreen;

public class DesertAdventure extends Game {
    private SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new GameplayScreen(this));
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
