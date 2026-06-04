package com.desertadventure.combat.system;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import com.desertadventure.combat.system.support.CombatTestCardDefs;
import com.desertadventure.combat.system.support.SequencedPlanRoller;
import com.desertadventure.player.PlayerStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.desertadventure.combat.system.support.CombatIntegrationTestSupport.firstPlayerSlot;

public class CombatOutcomeFinalizeIntegrationTest {
    @BeforeEach
    void setUpCards() {
        CardDatabase.initialize(new InMemoryCardRepository(CombatTestCardDefs.outcomeFinalizeMinimalDefs()));
    }

    @Test
    void pendingOutcome_doesNotTriggerOnCombatEnd_untilFinalized() {
        PlayerStats stats = new PlayerStats();
        CombatController combat = new CombatController(stats);

        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.ATTACK);

        AtomicReference<CombatOutcome> ended = new AtomicReference<>();
        combat.startCombat(0, true, 800f, 120f, deck, ended::set);

        combat.getEnemies().get(0).setHp(1f);

        int attackId = deck.getInstances().get(0).getInstanceId();
        combat.assignToPlayerSlot(firstPlayerSlot(combat), attackId);
        combat.confirmPlanning();

        for (int i = 0; i < 4 && !combat.hasPendingOutcome(); i++) {
            combat.update(999f);
        }

        assertTrue(combat.hasPendingOutcome(), "core should produce pending outcome when enemy dies");
        assertNull(ended.get(), "end callback must not fire until finalizePendingOutcome()");

        combat.finalizePendingOutcome();

        assertEquals(CombatOutcome.BOSS_VICTORY, ended.get(), "end callback should fire only after finalize");
    }

    @Test
    void slotPlanChange_clearsPreviouslyAssignedCards_inNowInvalidPlayerSlots() {
        PlayerStats stats = new PlayerStats();

        SequencedPlanRoller roller = new SequencedPlanRoller(
                new com.desertadventure.combat.system.slots.PlayerSlotPlan(0, 2),
                new com.desertadventure.combat.system.slots.PlayerSlotPlan(1, 3)
        );
        CombatController combat = new CombatController(stats, roller);

        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.ATTACK);
        deck.addCard(ActionCardType.STRIKE);

        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });

        int attackId = deck.getInstances().get(0).getInstanceId();
        combat.assignToPlayerSlot(0, attackId);
        assertEquals(attackId, combat.getSlotInstanceId(0));

        combat.getEnemies().get(0).setHp(999f);
        combat.confirmPlanning();
        for (int i = 0; i < 4; i++) {
            combat.update(999f);
            combat.finalizePendingOutcome();
        }

        assertNull(combat.getSlotInstanceId(0), "assigned card in now-invalid slot should be cleared");
    }
}
