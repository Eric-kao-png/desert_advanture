package com.desertadventure.presentation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.model.Tile;
import com.desertadventure.map.model.TileType;
import com.desertadventure.map.view.MapOverlayLayout;
import com.desertadventure.presentation.sprites.DesertSpriteAtlas;
import com.desertadventure.state.GameSession;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Renders gameplay visuals: parallax backgrounds, tiled floor, map overlay, combat entities.
 */
public class GameplayRenderer {
    private static final String PARALLAX_BACK = "backgrounds/parallax_back.png";
    private static final String PARALLAX_MIDDLE = "backgrounds/parallax_middle.png";
    private static final String PARALLAX_FORWARD = "backgrounds/parallax_forward.png";
    private static final float EXPLORE_GROUND_Y = 120f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Matrix4 screenProjection = new Matrix4();
    private final Texture bgBack;
    private final Texture bgMiddle;
    private final Texture bgForward;
    private final DesertSpriteAtlas desertSprites;
    private final TextureRegion floorTile;
    private final TextureRegion[] houseSprites;
    private final List<BackgroundHouseProp> houseProps = new ArrayList<>();
    private float scrollBack;
    private float scrollMiddle;
    private float scrollForward;
    private float scrollProps;
    private float distanceSinceLastSpawn;
    private float nextSpawnGap;

    public GameplayRenderer() {
        screenProjection.setToOrtho2D(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT);
        bgBack = loadBackground(PARALLAX_BACK);
        bgMiddle = loadBackground(PARALLAX_MIDDLE);
        bgForward = loadBackground(PARALLAX_FORWARD);
        desertSprites = new DesertSpriteAtlas();
        floorTile = desertSprites.get(DesertSpriteAtlas.FLOOR_TILE);
        houseSprites = new TextureRegion[DesertSpriteAtlas.DESERT_HOUSES.length];
        for (int i = 0; i < DesertSpriteAtlas.DESERT_HOUSES.length; i++) {
            houseSprites[i] = desertSprites.get(DesertSpriteAtlas.DESERT_HOUSES[i]);
        }
        resetSpawnPacing();
    }

    private static final class BackgroundHouseProp {
        final float scrollX;
        final int houseIndex;
        final float baseY;

        BackgroundHouseProp(float scrollX, int houseIndex, float baseY) {
            this.scrollX = scrollX;
            this.houseIndex = houseIndex;
            this.baseY = baseY;
        }
    }

    private static Texture loadBackground(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    public void dispose() {
        shapes.dispose();
        bgBack.dispose();
        bgMiddle.dispose();
        bgForward.dispose();
        desertSprites.dispose();
    }

    public void setProjectionMatrix(Matrix4 projection) {
        screenProjection.set(projection);
    }

    /**
     * Clears houses and places a few random spawns (game start / sandstorm).
     */
    public void repopulateHouseProps(float screenW) {
        houseProps.clear();
        scrollProps = 0f;
        resetSpawnPacing();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        float margin = 180f;
        float maxX = Math.max(margin, screenW - margin);
        for (int i = 0; i < GameConfig.HOUSE_REPOPULATE_ATTEMPTS; i++) {
            float scrollX = margin + rng.nextFloat() * (maxX - margin);
            trySpawnHouse(scrollX);
        }
    }

    private void resetSpawnPacing() {
        distanceSinceLastSpawn = 0f;
        nextSpawnGap = randomSpawnGap();
    }

    private static float randomSpawnGap() {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return GameConfig.HOUSE_SPAWN_GAP_MIN
                + rng.nextFloat() * (GameConfig.HOUSE_SPAWN_GAP_MAX - GameConfig.HOUSE_SPAWN_GAP_MIN);
    }

    private void beginShapes() {
        shapes.setProjectionMatrix(screenProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
    }

    /**
     * Draws parallax layers: back → houses → middle → forward → floor.
     * Call with {@link SpriteBatch#begin()} active.
     */
    public void drawParallaxBackground(SpriteBatch batch, boolean running, float delta) {
        if (running) {
            float base = GameConfig.SCROLL_SPEED * delta;
            scrollForward += base * GameConfig.PARALLAX_FORWARD_MULT;
            scrollMiddle += base * GameConfig.PARALLAX_MIDDLE_MULT;
            scrollBack += base * GameConfig.PARALLAX_BACK_MULT;
            scrollProps += base * GameConfig.HOUSE_PROP_PARALLAX_MULT;
        }

        float w = GameConfig.VIEW_WIDTH;
        float h = GameConfig.VIEW_HEIGHT;
        if (running) {
            updateHouseSpawnsWhileRunning(delta);
        }
        batch.setProjectionMatrix(screenProjection);
        drawTiledLayer(batch, bgBack, scrollBack, w, h);
        drawHouseProps(batch, w);
        drawTiledLayer(batch, bgMiddle, scrollMiddle, w, h);
        drawTiledLayer(batch, bgForward, scrollForward, w, h);
        drawTiledFloor(batch, scrollForward, w, EXPLORE_GROUND_Y);
    }

    private void updateHouseSpawnsWhileRunning(float delta) {
        float scrollDelta = GameConfig.SCROLL_SPEED * delta * GameConfig.HOUSE_PROP_PARALLAX_MULT;
        distanceSinceLastSpawn += scrollDelta;
        if (distanceSinceLastSpawn < nextSpawnGap) {
            return;
        }
        distanceSinceLastSpawn = 0f;
        nextSpawnGap = randomSpawnGap();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        float scrollX = scrollProps + rng.nextFloat() * GameConfig.HOUSE_SPAWN_JITTER;
        trySpawnHouse(scrollX);
    }

    /**
     * @return true if a house was added
     */
    private boolean trySpawnHouse(float scrollX) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        if (rng.nextFloat() > GameConfig.HOUSE_SPAWN_CHANCE) {
            return false;
        }
        int houseIndex = rng.nextInt(houseSprites.length);
        float drawW = houseDrawWidth(houseIndex);
        float screenX = scrollX - scrollProps;
        if (countOverlapping(screenX, drawW) >= GameConfig.HOUSE_OVERLAP_MAX) {
            return false;
        }
        float baseY = GameConfig.HOUSE_PROP_MIN_Y
                + rng.nextFloat() * (GameConfig.HOUSE_PROP_MAX_Y - GameConfig.HOUSE_PROP_MIN_Y);
        houseProps.add(new BackgroundHouseProp(scrollX, houseIndex, baseY));
        return true;
    }

    private float houseDrawWidth(int houseIndex) {
        TextureRegion region = houseSprites[houseIndex];
        float drawH = GameConfig.HOUSE_PROP_DISPLAY_HEIGHT;
        return drawH * (region.getRegionWidth() / (float) region.getRegionHeight());
    }

    private int countOverlapping(float screenLeft, float screenRight) {
        int count = 0;
        for (BackgroundHouseProp prop : houseProps) {
            float otherLeft = prop.scrollX - scrollProps;
            float otherRight = otherLeft + houseDrawWidth(prop.houseIndex);
            if (screenRight > otherLeft && screenLeft < otherRight) {
                count++;
            }
        }
        return count;
    }

    private void drawHouseProps(SpriteBatch batch, float screenW) {
        float maxWidth = GameConfig.HOUSE_PROP_DISPLAY_HEIGHT * 2f;
        Iterator<BackgroundHouseProp> it = houseProps.iterator();
        while (it.hasNext()) {
            BackgroundHouseProp prop = it.next();
            float screenX = prop.scrollX - scrollProps;
            float drawW = houseDrawWidth(prop.houseIndex);
            if (screenX < -maxWidth) {
                it.remove();
                continue;
            }
            if (screenX > screenW + maxWidth) {
                continue;
            }
            TextureRegion region = houseSprites[prop.houseIndex];
            float drawH = GameConfig.HOUSE_PROP_DISPLAY_HEIGHT;
            batch.draw(region, screenX, prop.baseY, drawW, drawH);
        }
    }

    private void drawTiledFloor(SpriteBatch batch, float scroll, float screenW, float groundY) {
        float tileH = groundY;
        float tileW = tileH * (floorTile.getRegionWidth() / (float) floorTile.getRegionHeight());
        float offset = scroll % tileW;
        if (offset < 0f) {
            offset += tileW;
        }
        float x = -offset;
        while (x < screenW) {
            batch.draw(floorTile, x, 0f, tileW, tileH);
            x += tileW;
        }
    }

    private static void drawTiledLayer(SpriteBatch batch, Texture texture, float scroll, float screenW, float screenH) {
        float scale = screenH / texture.getHeight();
        float tileW = texture.getWidth() * scale;
        float offset = scroll % tileW;
        if (offset < 0f) {
            offset += tileW;
        }
        float x = -offset;
        while (x < screenW) {
            batch.draw(texture, x, 0f, tileW, screenH);
            x += tileW;
        }
    }

    public void renderExploreForeground(GameSession session, boolean running) {
        float w = GameConfig.VIEW_WIDTH;
        float h = GameConfig.VIEW_HEIGHT;
        float groundY = EXPLORE_GROUND_Y;

        float playerX = w * 0.35f;
        float playerY = groundY;
        drawPlayer(playerX, playerY, running, true);

        drawHudPanel(0, h - 60, w, 60, new Color(0f, 0f, 0f, 0.35f));
    }

    /** Draws combat entities only; parallax background must be drawn separately. */
    public void renderCombatEntities(List<CombatEntity> entities) {
        beginShapes();
        for (CombatEntity entity : entities) {
            if (!entity.isAlive() && entity.getKind() != CombatEntity.Kind.PLAYER) {
                continue;
            }
            Color color = colorForEntity(entity, entity.getHurtFlash() > 0f);
            shapes.setColor(color);
            shapes.rect(entity.getX() - entity.getWidth() / 2f, entity.getY(),
                    entity.getWidth(), entity.getHeight());
        }
        shapes.end();
    }

    public void renderMapOverlay(GameSession session, MapOverlayLayout layout, GridPos hoverCell) {
        GameMap map = session.getMap();
        float cellSize = layout.getCellSize();
        int viewTiles = layout.getViewTiles();
        int originX = layout.getViewOriginX();
        int originY = layout.getViewOriginY();

        drawHudPanel(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT, new Color(0f, 0f, 0f, 0.65f));

        beginShapes();
        for (int vx = 0; vx < viewTiles; vx++) {
            for (int vy = 0; vy < viewTiles; vy++) {
                int mapX = originX + vx;
                int mapY = originY + vy;
                GridPos pos = new GridPos(mapX, mapY);
                if (!map.isInside(pos)) {
                    continue;
                }
                Tile tile = map.getTile(pos);
                boolean explored = map.isPermanentlyExplored(pos);
                boolean isPlayer = mapX == session.getPlayerGridPos().x && mapY == session.getPlayerGridPos().y;

                if (!explored && !isPlayer) {
                    shapes.setColor(0.15f, 0.15f, 0.15f, 0.9f);
                } else {
                    shapes.setColor(colorForTile(tile.getType()));
                }
                float px = layout.cellLeftForMapX(mapX);
                float py = layout.cellBottomForMapY(mapY);
                shapes.rect(px, py, cellSize - 1f, cellSize - 1f);

                if (isPlayer) {
                    shapes.setColor(Color.WHITE);
                    shapes.rect(px + 2, py + 2, cellSize - 5, cellSize - 5);
                }
            }
        }

        if (hoverCell != null && layout.isOnScreen(hoverCell)) {
            Tile hoverTile = map.getTile(hoverCell);
            if (map.canEnter(hoverCell)) {
                shapes.setColor(Color.YELLOW);
                float px = layout.cellLeftForMapX(hoverCell.x);
                float py = layout.cellBottomForMapY(hoverCell.y);
                shapes.rect(px, py, cellSize - 1f, cellSize - 1f);
            }
        }
        shapes.end();
    }

    public void renderStorm(float progress) {
        float alpha = Math.min(1f, progress);
        drawHudPanel(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT,
                new Color(0.9f, 0.75f, 0.35f, alpha * 0.85f));
    }

    private void drawPlayer(float x, float y, boolean running, boolean idle) {
        beginShapes();
        shapes.setColor(0.2f, 0.5f, 0.95f, 1f);
        float bob = running ? (float) Math.sin(System.currentTimeMillis() * 0.02) * 4f : 0f;
        shapes.rect(x - GameConfig.PLAYER_WIDTH / 2f, y + bob,
                GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT);
        shapes.end();
    }

    private Color colorForEntity(CombatEntity entity, boolean hurt) {
        if (hurt) {
            return Color.WHITE;
        }
        return switch (entity.getKind()) {
            case PLAYER -> new Color(0.2f, 0.5f, 0.95f, 1f);
            case ENEMY -> new Color(0.9f, 0.25f, 0.2f, 1f);
            case BOSS -> new Color(0.55f, 0.2f, 0.85f, 1f);
        };
    }

    private Color colorForTile(TileType type) {
        return switch (type) {
            case EMPTY, SPAWN -> new Color(0.85f, 0.75f, 0.5f, 1f);
            case BLOCKED -> new Color(0.4f, 0.35f, 0.3f, 1f);
            case ITEM -> new Color(0.3f, 0.85f, 0.4f, 1f);
            case EVENT -> new Color(0.95f, 0.85f, 0.2f, 1f);
            case COMBAT -> new Color(0.9f, 0.3f, 0.3f, 1f);
            case BOSS_SUMMON -> new Color(0.6f, 0.2f, 0.9f, 1f);
        };
    }

    private void drawHudPanel(float x, float y, float w, float h, Color color) {
        beginShapes();
        shapes.setColor(color);
        shapes.rect(x, y, w, h);
        shapes.end();
    }
}
