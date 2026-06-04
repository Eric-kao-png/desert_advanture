package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardTargetingId;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.system.support.CombatCardTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Integration smoke: each card type can be assigned and resolved through {@link CombatController}. */
class AllActionCardsIntegrationTest {
    @BeforeAll
    static void loadProductionCards() throws Exception {
        CombatCardTestSupport.initializeProductionCardDatabase();
    }

    @Test
    void everyCardType_runsOneRoundInCombat() {
        for (ActionCardType type : ActionCardType.values()) {
            CombatController combat = CombatTestFactories.passiveEnemyCombat();
            ActionCardDeck deck = CombatCardTestSupport.deckWithSingleCard(type);
            combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
            });

            CombatEntity enemy = combat.getEnemies().get(0);
            enemy.setHp(40f);
            float enemyHpBefore = enemy.getHp();
            float playerHpBefore = combat.getPlayer().getHp();
            int shieldBefore = combat.getPlayer().getShield();
            combat.getPlayer().setNegativeStatus(
                    com.desertadventure.combat.model.NegativeStatusType.POISON, 1);
            combat.getPlayer().setHp(combat.getPlayer().getMaxHp() - 10f);

            CombatCardTestSupport.assignToFirstPlayerSlotAndResolveRound(combat, deck, type);

            CardDef def = CombatCardTestSupport.productionDef(type);
            assertCombatStateChanged(combat, enemy, def, enemyHpBefore, playerHpBefore, shieldBefore, type);
        }
    }

    private static void assertCombatStateChanged(
            CombatController combat,
            CombatEntity enemy,
            CardDef def,
            float enemyHpBefore,
            float playerHpBefore,
            int shieldBefore,
            ActionCardType type) {
        boolean enemyChanged = enemy.getHp() != enemyHpBefore || enemy.hasNegativeStatus();
        boolean playerChanged = combat.getPlayer().getHp() != playerHpBefore
                || combat.getPlayer().getShield() != shieldBefore
                || !combat.getPlayer().hasNegativeStatus();

        if ("LIFE_MAGIC".equals(def.id)) {
            assertTrue(enemy.getHp() < enemyHpBefore, type + " should reduce enemy HP");
            return;
        }
        if (def.targeting == CardTargetingId.ENEMY) {
            assertTrue(enemyChanged, type + " should affect the enemy");
            return;
        }
        assertTrue(playerChanged || enemyChanged, type + " should change player or enemy state");
    }
}
