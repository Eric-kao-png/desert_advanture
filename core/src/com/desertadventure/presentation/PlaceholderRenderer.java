package com.desertadventure.presentation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.desertadventure.state.GameSession;

import java.util.List;

public class PlaceholderRenderer {
    private static final String BG_BACK = "backgrounds/DesBg_Back.png";
    private static final String BG_MIDDLE = "backgrounds/DesBg_Middle.png";
    private static final String BG_FORWARD = "backgrounds/DesBg_Forward.png";
    private static final String DESERT_ATLAS = "backgrounds/Desert.png";
    /**
     * Floor tile in Desert.png (image origin bottom-left).
     * Rect from (0, 128) to (32, 64) → x=0..32, y=64..128, size 32×64.
     */
    private static final int FLOOR_BL_LEFT = 0;
    private static final int FLOOR_BL_BOTTOM = 64;
    private static final int FLOOR_BL_RIGHT = 32;
    private static final int FLOOR_BL_TOP = 128;
    private static final float EXPLORE_GROUND_Y = 120f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Matrix4 screenProjection = new Matrix4();
    private final Texture bgBack;
    private final Texture bgMiddle;
    private final Texture bgForward;
    private final Texture desertAtlas;
    private final Texture floorTexture;
    private final TextureRegion floorTile;
    private float scrollBack;
    private float scrollMiddle;
    private float scrollForward;

    public PlaceholderRenderer() {
        screenProjection.setToOrtho2D(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT);
        bgBack = loadBackground(BG_BACK);
        bgMiddle = loadBackground(BG_MIDDLE);
        bgForward = loadBackground(BG_FORWARD);
        desertAtlas = loadPixelArt(DESERT_ATLAS);
        floorTexture = extractFloorTileTexture();
        floorTile = new TextureRegion(floorTexture);
    }

    /**
     * Crops using bottom-left image coords; Pixmap uses top-left, so we convert y.
     */
    private static Texture extractFloorTileTexture() {
        Pixmap atlas = new Pixmap(Gdx.files.internal(DESERT_ATLAS));
        int w = FLOOR_BL_RIGHT - FLOOR_BL_LEFT;
        int h = FLOOR_BL_TOP - FLOOR_BL_BOTTOM;
        int srcX = FLOOR_BL_LEFT;
        int srcY = atlas.getHeight() - FLOOR_BL_TOP;
        Pixmap slice = new Pixmap(w, h, atlas.getFormat());
        slice.drawPixmap(atlas, 0, 0, srcX, srcY, w, h);
        atlas.dispose();
        Texture texture = new Texture(slice);
        slice.dispose();
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    private static Texture loadBackground(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private static Texture loadPixelArt(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    public void dispose() {
        shapes.dispose();
        bgBack.dispose();
        bgMiddle.dispose();
        bgForward.dispose();
        desertAtlas.dispose();
        floorTexture.dispose();
    }

    public void setProjectionMatrix(Matrix4 projection) {
        screenProjection.set(projection);
    }

    private void beginShapes() {
        shapes.setProjectionMatrix(screenProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
    }

    /**
     * Draws tiled parallax desert layers (back → middle → forward). Call with {@link SpriteBatch#begin()} active.
     */
    public void drawParallaxBackground(SpriteBatch batch, boolean running, float delta) {
        if (running) {
            float base = GameConfig.SCROLL_SPEED * delta;
            scrollForward += base * GameConfig.PARALLAX_FORWARD_MULT;
            scrollMiddle += base * GameConfig.PARALLAX_MIDDLE_MULT;
            scrollBack += base * GameConfig.PARALLAX_BACK_MULT;
        }

        float w = GameConfig.VIEW_WIDTH;
        float h = GameConfig.VIEW_HEIGHT;
        batch.setProjectionMatrix(screenProjection);
        drawTiledLayer(batch, bgBack, scrollBack, w, h);
        drawTiledLayer(batch, bgMiddle, scrollMiddle, w, h);
        drawTiledLayer(batch, bgForward, scrollForward, w, h);
        drawTiledFloor(batch, scrollForward, w, EXPLORE_GROUND_Y);
    }

    private void drawTiledFloor(SpriteBatch batch, float scroll, float screenW, float groundY) {
        float tileH = groundY;
        int floorW = FLOOR_BL_RIGHT - FLOOR_BL_LEFT;
        int floorH = FLOOR_BL_TOP - FLOOR_BL_BOTTOM;
        float tileW = tileH * (floorW / (float) floorH);
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
