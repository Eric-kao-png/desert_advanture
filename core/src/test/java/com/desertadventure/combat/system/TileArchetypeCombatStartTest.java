package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.state.GameplayMode;
import com.desertadventure.state.GameSession;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileArchetypeCombatStartTest {
    @Test
    void combatTile_wanderingWizard_startsCombatWithWizardArchetype() {
        GameSession session = new GameSession();
        var tile = session.getMap().getTile(3, 0);
        session.handleTileInteraction(tile, false);

        assertEquals(GameplayMode.COMBAT, session.getMode());
        assertEquals(EnemyArchetypeId.WANDERING_WIZARD, session.consumePendingCombatArchetype());

        CombatController combat = session.getCombatController();
        combat.startCombat(
                session.getCurrentDistanceBand(),
                false,
                EnemyArchetypeId.WANDERING_WIZARD,
                800f,
                120f,
                new ActionCardDeck(),
                ignored -> {
                });

        assertEquals(EnemyArchetypeId.WANDERING_WIZARD, combat.getCurrentEnemyArchetype());
        assertEquals("Wandering Wizard", combat.getOpponentDisplayName());
        assertEquals(6f, combat.getEnemies().get(0).getMaxHp(), 0.001f);
    }

    @Test
    void combatTile_desertZombie_startsCombatWithZombieDeck() {
        GameSession session = new GameSession();
        var tile = session.getMap().getTile(-3, 0);
        session.handleTileInteraction(tile, false);

        assertEquals(EnemyArchetypeId.DESERT_ZOMBIE, session.consumePendingCombatArchetype());

        CombatController combat = session.getCombatController();
        combat.startCombat(
                0,
                false,
                EnemyArchetypeId.DESERT_ZOMBIE,
                800f,
                120f,
                new ActionCardDeck(),
                ignored -> {
                });

        assertEquals(EnemyArchetypeId.DESERT_ZOMBIE, combat.getCurrentEnemyArchetype());
        assertEquals("Desert Zombie", combat.getOpponentDisplayName());
        assertTrue(combat.enemyDeckInstancesForTests().stream()
                .anyMatch(i -> i.getType() == ActionCardType.CLAW));
    }

    @Test
    void bossFight_ignoresTileArchetypeAndShowsBossLabel() {
        PlayerStats stats = new PlayerStats();
        CombatController combat = new CombatController(stats);
        combat.startCombat(
                0,
                true,
                EnemyArchetypeId.WANDERING_WIZARD,
                800f,
                120f,
                new ActionCardDeck(),
                ignored -> {
                });

        assertNull(combat.getCurrentEnemyArchetype());
        assertEquals("Boss", combat.getOpponentDisplayName());
    }

}
