package com.desertadventure.map.model;

import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.combat.enemy.EnemyArchetypeRegistry;
import com.desertadventure.config.GameConfig;

import java.util.Random;

public final class MapGenerator {
    private MapGenerator() {
    }

    public static GameMap createWorld() {
        GameMap map = new GameMap(GameConfig.MAP_SIZE);
        Random random = new Random(GameConfig.MAP_GENERATOR_SEED);
        scatterBlocked(map, random);
        placeKeyLocations(map);
        scatterInteractables(map, random);
        placeTestCombatsNearSpawn(map);
        return map;
    }

    private static void scatterBlocked(GameMap map, Random random) {
        int clear = GameConfig.MAP_CAMP_CLEAR_RADIUS;
        for (int wx = map.getMinCoord(); wx <= map.getMaxCoord(); wx++) {
            for (int wy = map.getMinCoord(); wy <= map.getMaxCoord(); wy++) {
                if (wx == 0 && wy == 0) {
                    continue;
                }
                if (Math.abs(wx) <= clear && Math.abs(wy) <= clear) {
                    continue;
                }
                if (random.nextInt(100) < GameConfig.MAP_BLOCKED_DENSITY_PERCENT) {
                    map.getTile(wx, wy).setType(TileType.BLOCKED);
                }
            }
        }
    }

    private static void placeKeyLocations(GameMap map) {
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
        int minDist = GameConfig.MAP_MIN_INTERACTABLE_DISTANCE;
        int combatRoll = GameConfig.MAP_COMBAT_ROLL_THRESHOLD;
        int itemRoll = GameConfig.MAP_ITEM_ROLL_THRESHOLD;
        int rollMax = GameConfig.MAP_SCATTER_ROLL_DENOMINATOR;

        for (int wx = map.getMinCoord(); wx <= map.getMaxCoord(); wx++) {
            for (int wy = map.getMinCoord(); wy <= map.getMaxCoord(); wy++) {
                Tile tile = map.getTile(wx, wy);
                if (tile.getType() != TileType.EMPTY) {
                    continue;
                }
                if (Math.abs(wx) + Math.abs(wy) < minDist) {
                    continue;
                }
                int roll = random.nextInt(rollMax);
                if (roll < combatRoll) {
                    setCombatTile(map, wx, wy, EnemyArchetypeRegistry.pickForMapCoordinate(wx, wy));
                } else if (roll < itemRoll) {
                    tile.setType(TileType.ITEM);
                }
            }
        }
    }

    /** Fixed combat tiles within a few steps of spawn (random scatter skips this band). */
    private static void placeTestCombatsNearSpawn(GameMap map) {
        if (!GameConfig.MAP_NEAR_SPAWN_TEST_COMBATS) {
            return;
        }
        int[][] offsets = {
                {3, 0}, {-3, 0}, {0, 3}, {0, -3},
                {4, 0}, {-4, 0}, {3, 1}, {-3, -1},
        };
        for (int[] offset : offsets) {
            setCombatTile(
                    map,
                    offset[0],
                    offset[1],
                    EnemyArchetypeRegistry.pickForMapCoordinate(offset[0], offset[1]));
        }
    }

    private static void setCombatTile(GameMap map, int x, int y, EnemyArchetypeId archetype) {
        setTile(map, x, y, TileType.COMBAT);
        if (map.isInside(new GridPos(x, y))) {
            map.getTile(x, y).setEnemyArchetype(archetype);
        }
    }
}
