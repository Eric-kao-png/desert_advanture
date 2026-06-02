package com.desertadventure.combat.system;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardCategoryId;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardEffectStepDef;
import com.desertadventure.combat.card.data.CardTargetingId;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import com.desertadventure.player.PlayerStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        CardDatabase.initialize(new InMemoryCardRepository(minimalDefs()));
    }

    @Test
    void poison_appliesStatus_thenDealsDamageAtRoundEnd_andTicksDuration() {
        PlayerStats stats = new PlayerStats();
        CombatController combat = new CombatController(stats);

        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.POISON);

        // Use boss mode to avoid normal-enemy maxHp clamping (normal enemies roll maxHp 1..3).
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });

        // Ensure enemy survives the round so we can observe round-end tick.
        combat.getEnemies().get(0).setHp(3f);

        int poisonId = findFirstInstanceId(deck, ActionCardType.POISON);
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

    private static void resolveFullRound(CombatController combat) {
        // Each update resolves at most one slot; run enough updates for 4 slots.
        for (int i = 0; i < 4; i++) {
            combat.update(999f);
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

    private static Map<String, CardDef> minimalDefs() {
        Map<String, CardDef> defs = new HashMap<>();
        defs.put("ATTACK", damageDef("ATTACK", "Attack", 1, 2));
        defs.put("POISON", poisonDef());
        return defs;
    }

    private static CardDef damageDef(String id, String name, int cooldown, int damage) {
        CardDef def = new CardDef();
        def.id = id;
        def.name = name;
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = cooldown;
        def.targeting = CardTargetingId.ENEMY;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = "DealDamage";
        step.amount = damage;
        def.effects = List.of(step);
        return def;
    }

    private static CardDef poisonDef() {
        CardDef def = new CardDef();
        def.id = "POISON";
        def.name = "Poison";
        def.category = CardCategoryId.UTILITY;
        def.cooldown = 2;
        def.targeting = CardTargetingId.ENEMY;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = "ApplyNegativeStatus";
        step.status = "POISON";
        step.turns = 2;
        def.effects = List.of(step);
        return def;
    }
}

