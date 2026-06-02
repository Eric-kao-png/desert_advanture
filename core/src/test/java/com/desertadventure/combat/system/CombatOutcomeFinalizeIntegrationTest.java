package com.desertadventure.combat.system;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardCategoryId;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardEffectStepDef;
import com.desertadventure.combat.card.data.CardTargetingId;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import com.desertadventure.combat.system.presentation.PlayerAttackAnimation;
import com.desertadventure.player.PlayerStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CombatOutcomeFinalizeIntegrationTest {
    @BeforeEach
    void setUpCards() {
        CardDatabase.initialize(new InMemoryCardRepository(minimalDefs()));
    }

    @Test
    void pendingOutcome_doesNotTriggerOnCombatEnd_untilFinalized_afterAttackAnimation() {
        PlayerStats stats = new PlayerStats();
        CombatController combat = new CombatController(stats);

        FakeAttackAnimation anim = new FakeAttackAnimation();
        combat.setPlayerAttackAnimation(anim);

        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.ATTACK);

        AtomicReference<CombatOutcome> ended = new AtomicReference<>();
        combat.startCombat(0, true, 800f, 120f, deck, ended::set);

        // Make sure the first enemy will die from a single ATTACK.
        combat.getEnemies().get(0).setHp(1f);

        int attackId = deck.getInstances().get(0).getInstanceId();
        combat.assignToPlayerSlot(firstPlayerSlot(combat), attackId);
        combat.confirmPlanning();

        // Resolve until the player slot hits and outcome is produced.
        for (int i = 0; i < 4 && !combat.hasPendingOutcome(); i++) {
            combat.update(999f);
        }

        assertTrue(combat.hasPendingOutcome(), "core should produce pending outcome when enemy dies");
        assertNull(ended.get(), "end callback must not fire until finalizePendingOutcome()");
        assertTrue(anim.triggered, "attack animation should be triggered for ATTACK category");

        // Presentation waits for animation to finish, then finalizes.
        anim.finish();
        combat.finalizePendingOutcome();

        assertEquals(CombatOutcome.BOSS_VICTORY, ended.get(), "end callback should fire only after finalize");
    }

    @Test
    void slotPlanChange_clearsPreviouslyAssignedCards_inNowInvalidPlayerSlots() {
        PlayerStats stats = new PlayerStats();

        // First round: player slots (1,3) => indices (0,2)
        // Second round: player slots (2,4) => indices (1,3) which makes (0,2) invalid.
        SequencedPlanRoller roller = new SequencedPlanRoller(
                new com.desertadventure.combat.system.slots.PlayerSlotPlan(0, 2),
                new com.desertadventure.combat.system.slots.PlayerSlotPlan(1, 3)
        );
        CombatController combat = new CombatController(stats, roller);

        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(ActionCardType.ATTACK);
        deck.addCard(ActionCardType.SWIFT_STRIKE);

        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });

        // Assign a card to slot 0 (valid in round 1 plan).
        int attackId = deck.getInstances().get(0).getInstanceId();
        combat.assignToPlayerSlot(0, attackId);
        assertEquals(attackId, combat.getSlotInstanceId(0));

        // Finish the round without killing enemy so finishRound runs and rolls the next plan.
        combat.getEnemies().get(0).setHp(999f);
        combat.confirmPlanning();
        for (int i = 0; i < 4; i++) {
            combat.update(999f);
            combat.finalizePendingOutcome();
        }

        // Now round 2 should be planning with slots (1,3) in 0-based indices (1,3),
        // and any assigned cards in non-player slots (including previous slot 0) must be cleared.
        assertNull(combat.getSlotInstanceId(0), "assigned card in now-invalid slot should be cleared");
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
        defs.put("SWIFT_STRIKE", damageDef("SWIFT_STRIKE", "Swift Strike", 2, 3));
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
        step.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        step.amount = damage;
        def.effects = List.of(step);
        return def;
    }

    private static final class FakeAttackAnimation implements PlayerAttackAnimation {
        boolean triggered;
        private boolean attacking;

        @Override
        public void triggerAttack(float attackAnimSeconds) {
            triggered = true;
            attacking = true;
        }

        @Override
        public boolean isAttacking() {
            return attacking;
        }

        @Override
        public float getAttackProgress(float attackDurationSeconds) {
            return attacking ? 0f : 1f;
        }

        void finish() {
            attacking = false;
        }
    }

    private static final class SequencedPlanRoller implements com.desertadventure.combat.system.slots.PlayerSlotRoller {
        private final com.desertadventure.combat.system.slots.PlayerSlotPlan[] plans;
        private int idx;

        SequencedPlanRoller(com.desertadventure.combat.system.slots.PlayerSlotPlan... plans) {
            this.plans = plans;
        }

        @Override
        public com.desertadventure.combat.system.slots.PlayerSlotPlan rollPlan() {
            int i = Math.min(idx, plans.length - 1);
            idx++;
            return plans[i];
        }
    }
}

