package com.desertadventure.map.model;

import com.desertadventure.config.GameConfig;

import java.util.Random;

/**
 * Procedurally builds the world map (too large for hand-authored JSON).
 * World origin (0, 0) is the map center and player spawn.
 */
public final class MapGenerator {
    private static final int BLOCKED_DENSITY_PERCENT = 4;

    private MapGenerator() {
    }

    public static GameMap createWorld() {
        GameMap map = new GameMap(GameConfig.MAP_SIZE);
        Random random = new Random(42L);

        scatterBlocked(map, random);
        placeKeyLocations(map);
        scatterInteractables(map, random);
        return map;
    }

    private static void scatterBlocked(GameMap map, Random random) {
        for (int wx = map.getMinCoord(); wx <= map.getMaxCoord(); wx++) {
            for (int wy = map.getMinCoord(); wy <= map.getMaxCoord(); wy++) {
                if (wx == 0 && wy == 0) {
                    continue;
                }
                if (Math.abs(wx) <= 2 && Math.abs(wy) <= 2) {
                    continue;
                }
                if (random.nextInt(100) < BLOCKED_DENSITY_PERCENT) {
                    map.getTile(wx, wy).setType(TileType.BLOCKED);
                }
            }
        }
    }

    private static void placeKeyLocations(GameMap map) {
        // Pulled in near spawn for easier testing (was ±55 / -70).
        setEvent(map, -12, 0, "event_1");
        setEvent(map, 0, 12, "event_2");
        setEvent(map, 12, 0, "event_3");
        setTile(map, 0, -15, TileType.BOSS_SUMMON);
    }

    private static void setEvent(GameMap map, int x, int y, String eventId) {
        setTile(map, x, y, TileType.EVENT);
        if (map.isInside(new GridPos(x, y))) {
            map.getTile(x, y).setEventId(eventId);
        }
    }

    private static void setTile(GameMap map, int x, int y, TileType type) {
        if (!map.isInside(new GridPos(x, y))) {
            return;
        }
        map.getTile(x, y).setType(type);
    }

    private static void scatterInteractables(GameMap map, Random random) {
        for (int wx = map.getMinCoord(); wx <= map.getMaxCoord(); wx++) {
            for (int wy = map.getMinCoord(); wy <= map.getMaxCoord(); wy++) {
                Tile tile = map.getTile(wx, wy);
                if (tile.getType() != TileType.EMPTY) {
                    continue;
                }
                int distance = Math.abs(wx) + Math.abs(wy);
                if (distance < 8) {
                    continue;
                }
                int roll = random.nextInt(1000);
                if (roll < 12) {
                    tile.setType(TileType.COMBAT);
                } else if (roll < 22) {
                    tile.setType(TileType.ITEM);
                }
            }
        }
    }
}
