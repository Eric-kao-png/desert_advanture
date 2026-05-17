package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
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
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Matrix4 screenProjection = new Matrix4();
    private float parallaxOffset;

    public PlaceholderRenderer() {
        screenProjection.setToOrtho2D(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT);
    }

    public void dispose() {
        shapes.dispose();
    }

    public void setProjectionMatrix(Matrix4 projection) {
        screenProjection.set(projection);
    }

    private void beginShapes() {
        shapes.setProjectionMatrix(screenProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
    }

    public void renderExplore(GameSession session, boolean running) {
        float w = GameConfig.VIEW_WIDTH;
        float h = GameConfig.VIEW_HEIGHT;
        float groundY = 120f;

        Tile tile = session.getCurrentTile();
        drawBackground(tile, w, h, groundY, running);

        float playerX = w * 0.35f;
        float playerY = groundY;
        drawPlayer(playerX, playerY, running, true);

        drawHudPanel(0, h - 60, w, 60, new Color(0f, 0f, 0f, 0.35f));
    }

    public void renderCombat(List<CombatEntity> entities, float arenaWidth, float groundY, boolean bossFight) {
        beginShapes();
        shapes.setColor(0.85f, 0.7f, 0.45f, 1f);
        shapes.rect(0, 0, arenaWidth, groundY);
        shapes.setColor(0.55f, 0.75f, 0.95f, 1f);
        shapes.rect(0, groundY, arenaWidth, GameConfig.VIEW_HEIGHT - groundY);
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

    private void drawBackground(Tile tile, float w, float h, float groundY, boolean running) {
        if (running) {
            parallaxOffset -= GameConfig.SCROLL_SPEED * 0.016f;
            if (parallaxOffset < -200f) {
                parallaxOffset = 0f;
            }
        }

        Color sky = skyForTile(tile.getType());
        Color sand = sandForTile(tile.getType());

        beginShapes();
        shapes.setColor(sky);
        shapes.rect(0, groundY, w, h - groundY);
        shapes.setColor(sand);
        shapes.rect(0, 0, w, groundY);

        shapes.setColor(sand.r * 0.85f, sand.g * 0.85f, sand.b * 0.85f, 1f);
        for (int i = 0; i < 5; i++) {
            float duneX = (i * 250f + parallaxOffset) % (w + 100f) - 50f;
            shapes.triangle(duneX, groundY, duneX + 120f, groundY + 40f, duneX + 240f, groundY);
        }
        shapes.end();
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

    private Color skyForTile(TileType type) {
        return switch (type) {
            case COMBAT, BOSS_SUMMON -> new Color(0.5f, 0.55f, 0.7f, 1f);
            case EVENT -> new Color(0.55f, 0.7f, 0.95f, 1f);
            default -> new Color(0.45f, 0.65f, 0.95f, 1f);
        };
    }

    private Color sandForTile(TileType type) {
        return switch (type) {
            case ITEM -> new Color(0.9f, 0.8f, 0.45f, 1f);
            case BLOCKED -> new Color(0.65f, 0.55f, 0.4f, 1f);
            default -> new Color(0.85f, 0.72f, 0.42f, 1f);
        };
    }

    private void drawHudPanel(float x, float y, float w, float h, Color color) {
        beginShapes();
        shapes.setColor(color);
        shapes.rect(x, y, w, h);
        shapes.end();
    }
}
