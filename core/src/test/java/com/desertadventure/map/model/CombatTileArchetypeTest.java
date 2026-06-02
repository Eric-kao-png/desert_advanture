package com.desertadventure.map.model;

import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.combat.enemy.EnemyArchetypeRegistry;
import com.desertadventure.config.GameConfig;
import com.desertadventure.combat.enemy.EnemyArchetypeTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CombatTileArchetypeTest {
    @BeforeAll
    static void loadEnemies() {
        EnemyArchetypeTestSupport.ensureLoaded();
    }

    @Test
    void pickForMapCoordinate_cyclesZombieWizardAndSkeletonArcher() {
        assertEquals(EnemyArchetypeId.DESERT_ZOMBIE, EnemyArchetypeRegistry.pickForMapCoordinate(3, 0));
        assertEquals(EnemyArchetypeId.WANDERING_WIZARD, EnemyArchetypeRegistry.pickForMapCoordinate(4, 0));
        assertEquals(EnemyArchetypeId.SKELETON_ARCHER, EnemyArchetypeRegistry.pickForMapCoordinate(5, 0));
    }

    @Test
    void generatedWorld_assignsArchetypesToCombatTiles() {
        GameMap map = MapGenerator.createWorld();

        if (GameConfig.MAP_NEAR_SPAWN_TEST_COMBATS) {
            Tile east = map.getTile(new GridPos(3, 0));
            assertEquals(TileType.COMBAT, east.getType());
            assertEquals(EnemyArchetypeId.DESERT_ZOMBIE, east.getEnemyArchetype());

            Tile skeleton = map.getTile(new GridPos(-3, -1));
            assertEquals(TileType.COMBAT, skeleton.getType());
            assertEquals(EnemyArchetypeId.SKELETON_ARCHER, skeleton.getEnemyArchetype());
        }

        Tile scattered = findScatteredCombatTile(map);
        assertNotNull(scattered);
        assertEquals(
                EnemyArchetypeRegistry.pickForMapCoordinate(
                        scattered.getPosition().x, scattered.getPosition().y),
                scattered.getEnemyArchetype());
    }

    private static Tile findScatteredCombatTile(GameMap map) {
        for (int wx = map.getMinCoord(); wx <= map.getMaxCoord(); wx++) {
            for (int wy = map.getMinCoord(); wy <= map.getMaxCoord(); wy++) {
                if (Math.abs(wx) + Math.abs(wy) < GameConfig.MAP_MIN_INTERACTABLE_DISTANCE) {
                    continue;
                }
                Tile tile = map.getTile(wx, wy);
                if (tile.getType() == TileType.COMBAT
                        && tile.getEnemyArchetype() != null
                        && !isNearSpawnTestOffset(wx, wy)) {
                    return tile;
                }
            }
        }
        return null;
    }

    private static boolean isNearSpawnTestOffset(int x, int y) {
        int[][] offsets = {
                {3, 0}, {-3, 0}, {0, 3}, {0, -3},
                {4, 0}, {-4, 0}, {3, 1}, {-3, -1},
        };
        for (int[] offset : offsets) {
            if (offset[0] == x && offset[1] == y) {
                return true;
            }
        }
        return false;
    }
}
