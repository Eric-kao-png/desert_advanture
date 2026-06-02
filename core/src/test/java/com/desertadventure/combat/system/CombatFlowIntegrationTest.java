package com.desertadventure.combat.system;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import com.desertadventure.combat.system.support.CombatTestCardDefs;
import com.desertadventure.combat.system.support.SequencedPlanRoller;
import com.desertadventure.player.PlayerStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Integration-style tests that execute the real {@link CombatController} round flow:
 * planning -> resolving slots -> round-end status/cooldown -> next round or combat end.
 *
 * These tests do not use LibGDX runtime; they use an in-memory card repository.
 */
public class CombatFlowIntegrationTest {
    @BeforeEach
    void setUpCards() {
        CardDatabase.initialize(new InMemoryCardRepository(CombatTestCardDefs.combatFlowMinimalDefs()));
    }

    @Test
    void poison_appliesStatus_thenDealsDamageAtRoundEnd_andTicksDuration() {
        PlayerStats stats = new PlayerStats();
        CombatController combat = new CombatController(stats);

        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.POISON_MAGIC);

        // Use boss mode to avoid normal-enemy maxHp clamping (normal enemies roll maxHp 1..3).
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });

        // Ensure enemy survives the round so we can observe round-end tick.
        combat.getEnemies().get(0).setHp(3f);

        int poisonId = findFirstInstanceId(deck, ActionCardType.POISON_MAGIC);
        combat.assignToPlayerSlot(firstPlayerSlot(combat), poisonId);
        combat.confirmPlanning();

        resolveFullRound(combat);

        var enemy = combat.getEnemies().get(0);
        assertEquals(1f, enemy.getHp(), 0.001f, "Poison should tick for 2 damage at round end");
        assertEquals(1, enemy.getNegativeTurnsRemaining(), "Poison duration should tick down at round end");
    }

    @Test
    void playedAttack_withCooldown1_isAvailableNextRound_becauseRoundEndCountsAsCooldownTurn() {
        PlayerStats stats = new PlayerStats();
        CombatController combat = new CombatController(stats);

        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.ATTACK);

        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });

        combat.getEnemies().get(0).setHp(3f);

        int attackId = findFirstInstanceId(deck, ActionCardType.ATTACK);
        combat.assignToPlayerSlot(firstPlayerSlot(combat), attackId);
        combat.confirmPlanning();

        resolveFullRound(combat);

        ActionCardInstance instance = deck.findById(attackId);
        assertEquals(0, instance.getCooldownRemaining(), "CD=1 should tick to 0 at the end of the same round");
    }

    @Test
    void earlyVictory_stillAppliesRoundEndCooldownTick() {
        PlayerStats stats = new PlayerStats();
        CombatController combat = new CombatController(stats);

        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.ATTACK);

        AtomicReference<CombatOutcome> outcome = new AtomicReference<>();
        combat.startCombat(0, true, 800f, 120f, deck, outcome::set);

        // Enemy dies from slot 1 immediately.
        combat.getEnemies().get(0).setHp(1f);

        int attackId = findFirstInstanceId(deck, ActionCardType.ATTACK);
        combat.assignToPlayerSlot(firstPlayerSlot(combat), attackId);
        combat.confirmPlanning();

        // Resolve slots until combat ends (player slot may not be first due to per-round slot plan).
        for (int i = 0; i < 4 && outcome.get() == null; i++) {
            combat.update(999f);
            combat.finalizePendingOutcome();
        }

        assertEquals(CombatOutcome.BOSS_VICTORY, outcome.get(), "Combat should end with boss victory (boss mode test)");
        ActionCardInstance instance = deck.findById(attackId);
        assertEquals(0, instance.getCooldownRemaining(), "Early end should still apply round-end cooldown tick");
    }

    @Test
    void noStatusByDefault() {
        PlayerStats stats = new PlayerStats();
        CombatController combat = new CombatController(stats);
        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.ATTACK);

        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });

        assertNull(combat.getPlayer().getNegativeStatusType());
        assertEquals(0, combat.getPlayer().getNegativeTurnsRemaining());
        assertNull(combat.getEnemies().get(0).getNegativeStatusType());
        assertEquals(0, combat.getEnemies().get(0).getNegativeTurnsRemaining());
    }

    @Test
    void bleed_ticksForRemainingTurnsValue_andDecrementsUntilCleared() {
        PlayerStats stats = new PlayerStats();
        CombatController combat = new CombatController(stats);

        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.BLADE);

        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });

        var enemy = combat.getEnemies().get(0);
        enemy.setHp(10f);

        int bladeId = findFirstInstanceId(deck, ActionCardType.BLADE);
        combat.assignToPlayerSlot(firstPlayerSlot(combat), bladeId);
        combat.confirmPlanning();

        // Round 1: blade deals 3; end-of-round bleed ticks for 2.
        resolveFullRound(combat);
        assertEquals(5f, enemy.getHp(), 0.001f, "bleed should tick for 2 at end of round 1");
        assertEquals(com.desertadventure.combat.model.NegativeStatusType.BLEED, enemy.getNegativeStatusType());
        assertEquals(1, enemy.getNegativeTurnsRemaining(), "bleed turns should decrement at end of round 1");

        // Round 2: no further effects; end-of-round bleed ticks for 1 then clears.
        combat.confirmPlanning();
        resolveFullRound(combat);
        assertEquals(4f, enemy.getHp(), 0.001f, "bleed should tick for 1 at end of round 2");
        assertNull(enemy.getNegativeStatusType(), "bleed should clear after ticking down to 0");
        assertEquals(0, enemy.getNegativeTurnsRemaining());
    }

    @Test
    void fear_addsPlus1Damage_whenHitByOffenseCardDamage() {
        PlayerStats stats = new PlayerStats();
        CombatController combat = new CombatController(stats);

        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.GREAT_BLADE);
        deck.addCard(ActionCardType.ATTACK);

        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });

        var enemy = combat.getEnemies().get(0);
        enemy.setHp(10f);

        int greatBladeId = findFirstInstanceId(deck, ActionCardType.GREAT_BLADE);
        combat.assignToPlayerSlot(firstPlayerSlot(combat), greatBladeId);
        combat.confirmPlanning();
        resolveFullRound(combat);

        assertEquals(7f, enemy.getHp(), 0.001f, "great blade should deal 3");
        assertEquals(com.desertadventure.combat.model.NegativeStatusType.FEAR, enemy.getNegativeStatusType());
        assertEquals(1, enemy.getNegativeTurnsRemaining(), "duration should tick at round end");

        int attackId = findFirstInstanceId(deck, ActionCardType.ATTACK);
        combat.assignToPlayerSlot(firstPlayerSlot(combat), attackId);
        combat.confirmPlanning();
        resolveFullRound(combat);

        // ATTACK normally deals 2; FEAR adds +1 for offense-card damage => 3
        assertEquals(4f, enemy.getHp(), 0.001f, "fear should add +1 damage when hit by offense card");
    }

    @Test
    void chargedSlash_slot4Condition_deals6WhenResolvedInSlotIndex3() {
        PlayerStats stats = new PlayerStats();
        // Force slot index 3 (slot 4) to be a player slot for this round.
        SequencedPlanRoller roller = new SequencedPlanRoller(
                new com.desertadventure.combat.system.slots.PlayerSlotPlan(3, 0));
        CombatController combat = new CombatController(stats, roller);

        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.CHARGED_SLASH);

        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });

        var enemy = combat.getEnemies().get(0);
        enemy.setHp(10f);

        int id = findFirstInstanceId(deck, ActionCardType.CHARGED_SLASH);
        combat.assignToPlayerSlot(3, id);
        combat.confirmPlanning();
        resolveFullRound(combat);

        assertEquals(4f, enemy.getHp(), 0.001f, "charged slash in slot 4 should deal 6");
    }

    private static void resolveFullRound(CombatController combat) {
        // Each update resolves at most one slot; run enough updates for 4 slots.
        for (int i = 0; i < 4; i++) {
            combat.update(999f);
            combat.finalizePendingOutcome();
        }
    }

    private static int findFirstInstanceId(ActionCardDeck deck, ActionCardType type) {
        for (ActionCardInstance instance : deck.getInstances()) {
            if (instance.getType() == type) {
                return instance.getInstanceId();
            }
        }
        throw new IllegalStateException("Missing card instance: " + type);
    }

    private static int firstPlayerSlot(CombatController combat) {
        for (int i = 0; i < 4; i++) {
            if (combat.isPlayerSlot(i)) {
                return i;
            }
        }
        throw new IllegalStateException("No player slot available");
    }

}

