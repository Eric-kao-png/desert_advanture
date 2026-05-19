package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.desertadventure.config.GameConfig;
import com.desertadventure.presentation.sprites.DesertSpriteAtlas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Spawns and draws parallax desert house props between back and middle layers. */
public class BackgroundHouseSpawner {
    private static final class HouseProp {
        final float scrollX;
        final int houseIndex;
        final float baseY;

        HouseProp(float scrollX, int houseIndex, float baseY) {
            this.scrollX = scrollX;
            this.houseIndex = houseIndex;
            this.baseY = baseY;
        }
    }

    private final TextureRegion[] houseSprites;
    private final List<HouseProp> props = new ArrayList<>();
    private float scrollProps;
    private float distanceSinceLastSpawn;
    private float nextSpawnGap;

    public BackgroundHouseSpawner(DesertSpriteAtlas atlas) {
        houseSprites = new TextureRegion[DesertSpriteAtlas.DESERT_HOUSES.length];
        for (int i = 0; i < DesertSpriteAtlas.DESERT_HOUSES.length; i++) {
            houseSprites[i] = atlas.get(DesertSpriteAtlas.DESERT_HOUSES[i]);
        }
        resetPacing();
    }

    public void repopulate(float screenW) {
        props.clear();
        scrollProps = 0f;
        resetPacing();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        float margin = 180f;
        float maxX = Math.max(margin, screenW - margin);
        for (int i = 0; i < GameConfig.HOUSE_REPOPULATE_ATTEMPTS; i++) {
            trySpawn(margin + rng.nextFloat() * (maxX - margin));
        }
    }

    public void scroll(float delta, boolean running) {
        if (running) {
            scrollProps += GameConfig.SCROLL_SPEED * delta * GameConfig.HOUSE_PROP_PARALLAX_MULT;
            float scrollDelta = GameConfig.SCROLL_SPEED * delta * GameConfig.HOUSE_PROP_PARALLAX_MULT;
            distanceSinceLastSpawn += scrollDelta;
            if (distanceSinceLastSpawn >= nextSpawnGap) {
                distanceSinceLastSpawn = 0f;
                nextSpawnGap = randomGap();
                float scrollX = scrollProps + ThreadLocalRandom.current().nextFloat() * GameConfig.HOUSE_SPAWN_JITTER;
                trySpawn(scrollX);
            }
        }
    }

    public void draw(SpriteBatch batch, float screenW) {
        float maxWidth = GameConfig.HOUSE_PROP_DISPLAY_HEIGHT * 2f;
        Iterator<HouseProp> it = props.iterator();
        while (it.hasNext()) {
            HouseProp prop = it.next();
            float screenX = prop.scrollX - scrollProps;
            float drawW = drawWidth(prop.houseIndex);
            if (screenX < -maxWidth) {
                it.remove();
                continue;
            }
            if (screenX > screenW + maxWidth) {
                continue;
            }
            TextureRegion region = houseSprites[prop.houseIndex];
            batch.draw(region, screenX, prop.baseY, drawW, GameConfig.HOUSE_PROP_DISPLAY_HEIGHT);
        }
    }

    private void resetPacing() {
        distanceSinceLastSpawn = 0f;
        nextSpawnGap = randomGap();
    }

    private static float randomGap() {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return GameConfig.HOUSE_SPAWN_GAP_MIN
                + rng.nextFloat() * (GameConfig.HOUSE_SPAWN_GAP_MAX - GameConfig.HOUSE_SPAWN_GAP_MIN);
    }

    private boolean trySpawn(float scrollX) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        if (rng.nextFloat() > GameConfig.HOUSE_SPAWN_CHANCE) {
            return false;
        }
        int houseIndex = rng.nextInt(houseSprites.length);
        float drawW = drawWidth(houseIndex);
        float screenX = scrollX - scrollProps;
        if (countOverlapping(screenX, drawW) >= GameConfig.HOUSE_OVERLAP_MAX) {
            return false;
        }
        float baseY = GameConfig.HOUSE_PROP_MIN_Y
                + rng.nextFloat() * (GameConfig.HOUSE_PROP_MAX_Y - GameConfig.HOUSE_PROP_MIN_Y);
        props.add(new HouseProp(scrollX, houseIndex, baseY));
        return true;
    }

    private float drawWidth(int houseIndex) {
        TextureRegion region = houseSprites[houseIndex];
        return GameConfig.HOUSE_PROP_DISPLAY_HEIGHT
                * (region.getRegionWidth() / (float) region.getRegionHeight());
    }

    private int countOverlapping(float left, float right) {
        int count = 0;
        for (HouseProp prop : props) {
            float otherLeft = prop.scrollX - scrollProps;
            float otherRight = otherLeft + drawWidth(prop.houseIndex);
            if (right > otherLeft && left < otherRight) {
                count++;
            }
        }
        return count;
    }
}
