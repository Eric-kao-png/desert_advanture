package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.model.Tile;
import com.desertadventure.map.model.TileType;
import com.desertadventure.map.view.MapOverlayLayout;
import com.desertadventure.state.GameSession;

/** Map grid overlay while selecting a destination. */
public class MapOverlayRenderer {
    private final ShapeRenderer shapes = new ShapeRenderer();

    public void setProjection(Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
    }

    public void render(GameSession session, MapOverlayLayout layout, GridPos hoverCell) {
        GameMap map = session.getMap();
        float cellSize = layout.getCellSize();
        int viewTiles = layout.getViewTiles();
        int originX = layout.getViewOriginX();
        int originY = layout.getViewOriginY();

        fillRect(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT, new Color(0f, 0f, 0f, 0.65f));

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        GridPos player = session.getPlayerGridPos();
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
                boolean isPlayer = mapX == player.x && mapY == player.y;
                shapes.setColor(!explored && !isPlayer ? new Color(0.15f, 0.15f, 0.15f, 0.9f) : colorFor(tile.getType()));
                float px = layout.cellLeftForMapX(mapX);
                float py = layout.cellBottomForMapY(mapY);
                shapes.rect(px, py, cellSize - 1f, cellSize - 1f);
                if (isPlayer) {
                    shapes.setColor(Color.WHITE);
                    shapes.rect(px + 2, py + 2, cellSize - 5, cellSize - 5);
                }
            }
        }
        if (hoverCell != null && layout.isOnScreen(hoverCell) && map.canEnter(hoverCell)) {
            shapes.setColor(Color.YELLOW);
            shapes.rect(
                    layout.cellLeftForMapX(hoverCell.x),
                    layout.cellBottomForMapY(hoverCell.y),
                    cellSize - 1f, cellSize - 1f);
        }
        shapes.end();
    }

    public void dispose() {
        shapes.dispose();
    }

    private void fillRect(float x, float y, float w, float h, Color color) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(color);
        shapes.rect(x, y, w, h);
        shapes.end();
    }

    private static Color colorFor(TileType type) {
        return switch (type) {
            case EMPTY, SPAWN -> new Color(0.85f, 0.75f, 0.5f, 1f);
            case BLOCKED -> new Color(0.4f, 0.35f, 0.3f, 1f);
            case ITEM -> new Color(0.3f, 0.85f, 0.4f, 1f);
            case EVENT -> new Color(0.95f, 0.85f, 0.2f, 1f);
            case COMBAT -> new Color(0.9f, 0.3f, 0.3f, 1f);
            case BOSS_SUMMON -> new Color(0.6f, 0.2f, 0.9f, 1f);
        };
    }
}
