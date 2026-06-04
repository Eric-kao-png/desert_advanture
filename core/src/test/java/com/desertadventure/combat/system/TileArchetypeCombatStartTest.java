package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.combat.enemy.EnemyArchetypeRegistry;
import com.desertadventure.combat.system.support.CombatTestDataBootstrap;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.state.GameplayMode;
import com.desertadventure.state.GameSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileArchetypeCombatStartTest {
    @BeforeEach
    void setUpCards() throws Exception {
        CombatTestDataBootstrap.ensureProductionDatabases();
    }

    private static GameSession newExplorationSession() {
        GameSession session = new GameSession();
        session.setMode(GameplayMode.EXPLORE_IDLE);
        return session;
    }

    @Test
    void combatTile_wanderingWizard_startsCombatWithWizardArchetype() {
        GameSession session = newExplorationSession();
        var tile = session.getMap().getTile(4, 0);
        assertNotNull(tile);
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
        var wizardDef = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.WANDERING_WIZARD);
        float maxHp = combat.getEnemies().get(0).getMaxHp();
        assertTrue(maxHp >= wizardDef.hpMin() && maxHp <= wizardDef.hpMax());
    }

    @Test
    void combatTile_desertZombie_startsCombatWithZombieDeck() {
        GameSession session = newExplorationSession();
        var tile = session.getMap().getTile(-3, 0);
        assertNotNull(tile);
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
