package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.system.support.CombatCardTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Integration tests for PURIFY, MAGIC_BOLT, POISON_BOLT, MAGIC_MIRROR, MAGIC_ARROW
 * through the real {@link CombatController} round flow.
 */
class NewMagicCardsIntegrationTest {
    @BeforeEach
    void setUpCards() throws Exception {
        CombatCardTestSupport.initializeProductionCardDatabase();
    }

    @Test
    void purify_clearsPlayerPoison_whenDebuffed() {
        CombatController combat = CombatTestFactories.passiveEnemyCombat();
        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.PURIFY);
        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });
        combat.getEnemies().get(0).setHp(999f);
        combat.getPlayer().setNegativeStatus(NegativeStatusType.POISON, 2);
        float hpBefore = combat.getPlayer().getHp();

        CombatCardTestSupport.assignToFirstPlayerSlotAndResolveRound(combat, deck, ActionCardType.PURIFY);

        assertFalse(combat.getPlayer().hasNegativeStatus());
        assertEquals(hpBefore, combat.getPlayer().getHp(), 0.001f, "purify with debuff should not heal");
    }

    @Test
    void purify_heals2_whenNoDebuff() {
        CombatController combat = CombatTestFactories.passiveEnemyCombat();
        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.PURIFY);
        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });
        combat.getEnemies().get(0).setHp(999f);
        combat.getPlayer().setHp(combat.getPlayer().getHp() - 3f);
        float hpBefore = combat.getPlayer().getHp();

        CombatCardTestSupport.assignToFirstPlayerSlotAndResolveRound(combat, deck, ActionCardType.PURIFY);

        assertEquals(hpBefore + 2f, combat.getPlayer().getHp(), 0.001f);
    }

    @Test
    void magicBolt_ignoresEnemyShield() {
        CombatController combat = CombatTestFactories.passiveEnemyCombat();
        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.MAGIC_BOLT);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        var enemy = combat.getEnemies().get(0);
        enemy.setHp(10f);
        enemy.addShield(5);

        CombatCardTestSupport.assignToFirstPlayerSlotAndResolveRound(combat, deck, ActionCardType.MAGIC_BOLT);

        assertEquals(7f, enemy.getHp(), 0.001f, "3 damage should bypass 5 shield");
        assertEquals(5, enemy.getShield(), "shield should remain untouched");
    }

    @Test
    void poisonBolt_appliesPoisonOnDeterministicRoll() {
        CombatController combat = CombatTestFactories.passiveEnemyCombat();
        combat.setCardEffectRngForTests(bound -> 0);
        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.POISON_BOLT);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        combat.getEnemies().get(0).setHp(10f);

        CombatCardTestSupport.assignToFirstPlayerSlotAndResolveRound(combat, deck, ActionCardType.POISON_BOLT);

        assertEquals(8f, combat.getEnemies().get(0).getHp(), 0.001f);
        assertEquals(NegativeStatusType.POISON, combat.getEnemies().get(0).getNegativeStatusType());
        assertEquals(1, combat.getEnemies().get(0).getNegativeTurnsRemaining());
    }

    @Test
    void magicMirror_movesPlayerDebuffToEnemy() {
        CombatController combat = CombatTestFactories.passiveEnemyCombat();
        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.MAGIC_MIRROR);
        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });
        combat.getEnemies().get(0).setHp(999f);
        combat.getPlayer().setNegativeStatus(NegativeStatusType.BLEED, 2);
        var enemy = combat.getEnemies().get(0);

        CombatCardTestSupport.assignToFirstPlayerSlotAndResolveRound(combat, deck, ActionCardType.MAGIC_MIRROR);

        assertFalse(combat.getPlayer().hasNegativeStatus());
        assertEquals(NegativeStatusType.BLEED, enemy.getNegativeStatusType());
        assertEquals(2, enemy.getNegativeTurnsRemaining());
    }

    @Test
    void magicArrow_deals4AgainstDebuffedEnemy() {
        CombatController combat = CombatTestFactories.passiveEnemyCombat();
        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.MAGIC_ARROW);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        var enemy = combat.getEnemies().get(0);
        enemy.setHp(10f);
        enemy.setNegativeStatus(NegativeStatusType.POISON, 1);

        CombatCardTestSupport.assignToFirstPlayerSlotAndResolveRound(combat, deck, ActionCardType.MAGIC_ARROW);

        assertEquals(6f, enemy.getHp(), 0.001f);
    }
}
