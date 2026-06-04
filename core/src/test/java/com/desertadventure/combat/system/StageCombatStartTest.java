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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageCombatStartTest {
    @BeforeEach
    void setUpCards() throws Exception {
        CombatTestDataBootstrap.ensureProductionDatabases();
    }

    @Test
    void hubStage1_startsCombatWithDesertZombieArchetype() {
        GameSession session = new GameSession();
        assertTrue(session.tryStartCurrentStageCombat());

        assertEquals(GameplayMode.COMBAT, session.getMode());
        assertEquals(EnemyArchetypeId.DESERT_ZOMBIE, session.consumePendingEnemyArchetype());

        CombatController combat = session.getCombatController();
        combat.startCombat(
                session.getRunProgress().getCurrentStageIndex(),
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
    void hubStage2_startsCombatWithWanderingWizardArchetype() {
        GameSession session = new GameSession();
        session.getRunProgress().advanceAfterVictory();
        assertTrue(session.tryStartCurrentStageCombat());

        assertEquals(EnemyArchetypeId.WANDERING_WIZARD, session.consumePendingEnemyArchetype());

        CombatController combat = session.getCombatController();
        combat.startCombat(
                session.getRunProgress().getCurrentStageIndex(),
                false,
                EnemyArchetypeId.WANDERING_WIZARD,
                800f,
                120f,
                new ActionCardDeck(),
                ignored -> {
                });

        assertEquals(EnemyArchetypeId.WANDERING_WIZARD, combat.getCurrentEnemyArchetype());
        var wizardDef = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.WANDERING_WIZARD);
        float maxHp = combat.getEnemies().get(0).getMaxHp();
        assertTrue(maxHp >= wizardDef.hpMin() && maxHp <= wizardDef.hpMax());
    }

    @Test
    void bossFight_ignoresStageArchetypeAndShowsBossLabel() {
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
