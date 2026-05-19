package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.desertadventure.map.model.TileType;

/** Map overlay tile fill colors. */
public final class MapTileColors {
    public static final Color UNEXPLORED = new Color(0.15f, 0.15f, 0.15f, 0.9f);
    public static final Color HOVER_VALID = Color.YELLOW;
    public static final Color PLAYER_MARKER = Color.WHITE;

    private MapTileColors() {
    }

    public static Color forTileType(TileType type) {
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
