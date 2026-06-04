package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.system.support.CombatCardTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Integration tests for ARROW and POISON_ARROW through {@link CombatController}. */
class ArrowCardsIntegrationTest {
    @BeforeEach
    void setUpCards() throws Exception {
        CombatCardTestSupport.initializeProductionCardDatabase();
    }

    @Test
    void arrow_deals3DamageToEnemy() {
        CombatController combat = CombatTestFactories.passiveEnemyCombat();
        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.ARROW);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        combat.getEnemies().get(0).setHp(10f);

        CombatCardTestSupport.assignToFirstPlayerSlotAndResolveRound(combat, deck, ActionCardType.ARROW);

        assertEquals(7f, combat.getEnemies().get(0).getHp(), 0.001f);
    }

    @Test
    void poisonArrow_appliesPoisonOnDeterministicRoll() {
        CombatController combat = CombatTestFactories.passiveEnemyCombat();
        combat.setCardEffectRngForTests(bound -> bound == 100 ? 49 : 0);
        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.POISON_ARROW);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        var enemy = combat.getEnemies().get(0);
        enemy.setHp(10f);

        CombatCardTestSupport.assignToFirstPlayerSlotAndResolveRound(combat, deck, ActionCardType.POISON_ARROW);

        assertEquals(8f, enemy.getHp(), 0.001f);
        assertEquals(NegativeStatusType.POISON, enemy.getNegativeStatusType());
        assertEquals(2, enemy.getNegativeTurnsRemaining());
    }

    @Test
    void poisonArrow_noPoisonOnFailedRoll() {
        CombatController combat = CombatTestFactories.passiveEnemyCombat();
        combat.setCardEffectRngForTests(bound -> bound == 100 ? 50 : 0);
        ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(ActionCardType.POISON_ARROW);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        var enemy = combat.getEnemies().get(0);
        enemy.setHp(10f);

        CombatCardTestSupport.assignToFirstPlayerSlotAndResolveRound(combat, deck, ActionCardType.POISON_ARROW);

        assertEquals(8f, enemy.getHp(), 0.001f);
        assertNull(enemy.getNegativeStatusType());
    }
}
