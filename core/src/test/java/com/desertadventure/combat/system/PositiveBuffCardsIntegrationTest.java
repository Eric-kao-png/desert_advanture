package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.enemy.EnemyAi;
import com.desertadventure.combat.enemy.EnemyArchetypeTestSupport;
import com.desertadventure.combat.model.PositiveStatusType;
import com.desertadventure.combat.system.slots.PlayerSlotPlan;
import com.desertadventure.combat.system.support.CombatCardTestSupport;
import com.desertadventure.combat.system.support.CombatIntegrationTestSupport;
import com.desertadventure.combat.system.support.SequencedPlanRoller;
import com.desertadventure.player.PlayerStats;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.desertadventure.combat.system.support.CombatIntegrationTestSupport.resolveFullRound;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Integration tests for JSON-driven positive buff change cards. */
class PositiveBuffCardsIntegrationTest {
    @BeforeAll
    static void loadProductionData() throws Exception {
        CombatCardTestSupport.initializeProductionCardDatabase();
        EnemyArchetypeTestSupport.ensureLoaded();
    }

    @Test
    void changeCards_applyPositiveStatusFromManifest() {
        CombatController combat = passiveCombat();
        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.SPIKE_SHIELD);
        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });

        CombatCardTestSupport.assignToFirstPlayerSlotAndResolveRound(combat, deck, ActionCardType.SPIKE_SHIELD);

        assertEquals(PositiveStatusType.SPIKE_SHIELD, combat.getPlayer().getPositiveStatusType());
        assertEquals(2, combat.getPlayer().getPositiveTurnsRemaining());
    }

    @Test
    void scaleArmor_reducesIncomingOffenseDamageByOne() {
        CombatController combat = passiveCombat();
        combat.startCombat(0, false, 800f, 120f, new ActionCardDeck(), ignored -> {
        });
        combat.getPlayer().setPositiveStatus(PositiveStatusType.SCALE_ARMOR, 2);

        combat.dealDamageToPlayer(3f, CombatController.DamageSource.OFFENSE_CARD);

        assertEquals(18f, combat.getPlayer().getHp(), 0.001f);
    }

    @Test
    void dodge_blocksFirstEnemyAttackAndClearsBuff() {
        CombatController combat = passiveCombat();
        combat.startCombat(0, false, 800f, 120f, new ActionCardDeck(), ignored -> {
        });
        combat.getPlayer().setPositiveStatus(PositiveStatusType.DODGE, 3);

        combat.dealDamageToPlayer(5f, CombatController.DamageSource.OFFENSE_CARD);

        assertEquals(20f, combat.getPlayer().getHp(), 0.001f);
        assertFalse(combat.getPlayer().hasPositiveStatus());

        combat.dealDamageToPlayer(4f, CombatController.DamageSource.OFFENSE_CARD);
        assertEquals(16f, combat.getPlayer().getHp(), 0.001f);
    }

    @Test
    void focus_playerAttackIgnoresEnemyShield() {
        CombatController combat = passiveCombat();
        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.FOCUS);
        deck.addCard(ActionCardType.ATTACK);
        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });

        combat.getEnemies().get(0).addShield(4);
        float enemyHpBefore = combat.getEnemies().get(0).getHp();

        int focusId = deck.getInstances().stream()
                .filter(i -> i.getType() == ActionCardType.FOCUS)
                .findFirst().orElseThrow().getInstanceId();
        int attackId = deck.getInstances().stream()
                .filter(i -> i.getType() == ActionCardType.ATTACK)
                .findFirst().orElseThrow().getInstanceId();

        int slot0 = CombatIntegrationTestSupport.firstPlayerSlot(combat);
        combat.assignToPlayerSlot(slot0, focusId);
        combat.assignToPlayerSlot(slot0 == 0 ? 2 : 0, attackId);
        combat.confirmPlanning();
        resolveFullRound(combat);

        assertEquals(enemyHpBefore - 2f, combat.getEnemies().get(0).getHp(), 0.001f);
        assertEquals(4, combat.getEnemies().get(0).getShield(), "focus ignores shield but does not remove it");
    }

    @Test
    void vampireFang_healsTwoWhenPlayerAttacksAndEnemyHadNoShield() {
        PlayerStats stats = new PlayerStats();
        stats.setHp(15f);
        CombatController combat = new CombatController(
                stats, new SequencedPlanRoller(new PlayerSlotPlan(0, 2)), candidates -> null, bound -> 0);

        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.VAMPIRE_FANG);
        deck.addCard(ActionCardType.ATTACK);
        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });

        int fangId = deck.getInstances().stream()
                .filter(i -> i.getType() == ActionCardType.VAMPIRE_FANG)
                .findFirst().orElseThrow().getInstanceId();
        int attackId = deck.getInstances().stream()
                .filter(i -> i.getType() == ActionCardType.ATTACK)
                .findFirst().orElseThrow().getInstanceId();

        int slot0 = CombatIntegrationTestSupport.firstPlayerSlot(combat);
        combat.assignToPlayerSlot(slot0, fangId);
        combat.assignToPlayerSlot(slot0 == 0 ? 2 : 0, attackId);
        combat.confirmPlanning();
        resolveFullRound(combat);

        assertEquals(17f, combat.getPlayer().getHp(), 0.001f);
    }

    @Test
    void vampireFang_noHealWhenEnemyHadShieldBeforeAttack() {
        PlayerStats stats = new PlayerStats();
        stats.setHp(15f);
        CombatController combat = new CombatController(
                stats, new SequencedPlanRoller(new PlayerSlotPlan(0, 2)), candidates -> null, bound -> 0);

        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.VAMPIRE_FANG);
        deck.addCard(ActionCardType.ATTACK);
        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });
        combat.getEnemies().get(0).addShield(4);

        int fangId = deck.getInstances().stream()
                .filter(i -> i.getType() == ActionCardType.VAMPIRE_FANG)
                .findFirst().orElseThrow().getInstanceId();
        int attackId = deck.getInstances().stream()
                .filter(i -> i.getType() == ActionCardType.ATTACK)
                .findFirst().orElseThrow().getInstanceId();

        int slot0 = CombatIntegrationTestSupport.firstPlayerSlot(combat);
        combat.assignToPlayerSlot(slot0, fangId);
        combat.assignToPlayerSlot(slot0 == 0 ? 2 : 0, attackId);
        combat.confirmPlanning();
        resolveFullRound(combat);

        assertEquals(15f, combat.getPlayer().getHp(), 0.001f);
    }

    @Test
    void spikeShield_enemyTakesRetaliationWhenAttacking() {
        CombatController combat = passiveCombat();
        combat.startCombat(0, false, 800f, 120f, new ActionCardDeck(), ignored -> {
        });
        combat.getPlayer().setPositiveStatus(PositiveStatusType.SPIKE_SHIELD, 2);

        float enemyHpBefore = combat.getEnemies().get(0).getHp();
        combat.dealDamageToPlayer(2f, CombatController.DamageSource.OFFENSE_CARD);

        assertEquals(enemyHpBefore - 2f, combat.getEnemies().get(0).getHp(), 0.001f);
        assertEquals(18f, combat.getPlayer().getHp(), 0.001f);
    }

    private static CombatController passiveCombat() {
        PlayerStats stats = new PlayerStats();
        stats.setHp(20f);
        EnemyAi passive = candidates -> null;
        return new CombatController(
                stats, new SequencedPlanRoller(new PlayerSlotPlan(0, 2)), passive, bound -> 0);
    }
}
