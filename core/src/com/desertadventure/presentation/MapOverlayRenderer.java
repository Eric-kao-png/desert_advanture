package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.model.Tile;
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

        ShapeDrawer.fillRect(shapes, 0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT,
                new Color(0f, 0f, 0f, GameConfig.MAP_OVERLAY_DIM_ALPHA));

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
                shapes.setColor(!explored && !isPlayer ? MapTileColors.UNEXPLORED : MapTileColors.forTileType(tile.getType()));
                float px = layout.cellLeftForMapX(mapX);
                float py = layout.cellBottomForMapY(mapY);
                shapes.rect(px, py, cellSize - GameConfig.MAP_CELL_INSET, cellSize - GameConfig.MAP_CELL_INSET);
                if (isPlayer) {
                    shapes.setColor(MapTileColors.PLAYER_MARKER);
                    shapes.rect(px + GameConfig.MAP_PLAYER_MARKER_INSET, py + GameConfig.MAP_PLAYER_MARKER_INSET,
                            cellSize - GameConfig.MAP_PLAYER_MARKER_SHRINK,
                            cellSize - GameConfig.MAP_PLAYER_MARKER_SHRINK);
                }
            }
        }
        if (hoverCell != null && layout.isOnScreen(hoverCell) && map.canEnter(hoverCell)) {
            shapes.setColor(MapTileColors.HOVER_VALID);
            shapes.rect(
                    layout.cellLeftForMapX(hoverCell.x),
                    layout.cellBottomForMapY(hoverCell.y),
                    cellSize - GameConfig.MAP_CELL_INSET, cellSize - GameConfig.MAP_CELL_INSET);
        }
        shapes.end();
    }

    public void dispose() {
        shapes.dispose();
    }

}
