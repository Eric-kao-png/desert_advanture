package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardCategoryId;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardEffectStepDef;
import com.desertadventure.combat.card.data.CardTargetingId;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import com.desertadventure.combat.enemy.EnemyAi;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.combat.enemy.EnemyArchetypeTestSupport;
import com.desertadventure.combat.system.slots.PlayerSlotPlan;
import com.desertadventure.combat.system.slots.PlayerSlotRoller;
import com.desertadventure.combat.system.slots.RandomIntSource;
import com.desertadventure.player.PlayerStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies normal-enemy slots resolve through {@link CardEffectResolver} with enemy targeting,
 * not the legacy fixed ATTACK-only damage.
 */
class EnemyCardResolveIntegrationTest {
    @BeforeEach
    void setUpCards() {
        CardDatabase.initialize(new InMemoryCardRepository(minimalDefs()));
        EnemyArchetypeTestSupport.ensureLoaded();
    }

    @Test
    void enemyHealSlot_healsEnemyNotOnlyFixedAttackDamage() {
        PlayerStats stats = new PlayerStats();
        stats.setHp(20f);

        EnemyAi alwaysHeal = candidates -> pickFirstOfType(candidates, ActionCardType.HEAL);
        RandomIntSource hpRng = bound -> 0;
        SequencedPlanRoller roller = new SequencedPlanRoller(new PlayerSlotPlan(0, 2));
        CombatController combat = new CombatController(stats, roller, alwaysHeal, hpRng);

        ActionCardDeck deck = new ActionCardDeck();
        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });

        assertEquals(EnemyArchetypeId.DESERT_ZOMBIE, combat.getCurrentEnemyArchetype());
        var enemy = combat.getEnemies().get(0);
        float maxHp = enemy.getMaxHp();
        assertTrueHpInRange(maxHp);
        enemy.setHp(maxHp - 2f);
        float playerHpBefore = combat.getPlayer().getHp();

        combat.confirmPlanning();
        resolveFullRound(combat);

        assertEquals(maxHp, enemy.getHp(), 0.001f, "enemy HEAL should restore enemy HP via resolver");
        // Second enemy slot may play a different card when only one HEAL exists in the deck.
        assertTrue(combat.getPlayer().getHp() <= playerHpBefore, "enemy HEAL must not increase player HP");
    }

    @Test
    void enemyAttackSlot_dealsDamageToPlayerViaResolver() {
        PlayerStats stats = new PlayerStats();
        stats.setHp(20f);

        EnemyAi alwaysAttack = candidates -> pickFirstOfType(candidates, ActionCardType.ATTACK);
        RandomIntSource hpRng = bound -> 0;
        SequencedPlanRoller roller = new SequencedPlanRoller(new PlayerSlotPlan(0, 2));
        CombatController combat = new CombatController(stats, roller, alwaysAttack, hpRng);

        combat.startCombat(0, false, 800f, 120f, new ActionCardDeck(), ignored -> {
        });

        float playerHpBefore = combat.getPlayer().getHp();
        combat.confirmPlanning();
        resolveFullRound(combat);

        assertEquals(ActionCardType.ATTACK, combat.getResolvedEnemyCardForSlot(1), "slot 1 should resolve ATTACK");
        assertTrue(combat.getPlayer().getHp() <= playerHpBefore - 2f, "enemy ATTACK(2) should damage the player");
    }

    @Test
    void plannedEnemyCards_matchResolvedEnemyCards_andNotAlwaysAttack() {
        PlayerStats stats = new PlayerStats();
        stats.setHp(20f);

        EnemyAi sequenced = new EnemyAi() {
            private int i;

            @Override
            public ActionCardInstance pickCard(List<ActionCardInstance> candidates) {
                // Desert zombie deck has two of each; distinct instances per enemy slot.
                ActionCardType want = (i++ % 2 == 0) ? ActionCardType.CLAW : ActionCardType.VAMPIRISM;
                return pickFirstOfType(candidates, want);
            }
        };
        RandomIntSource hpRng = bound -> 0;
        SequencedPlanRoller roller = new SequencedPlanRoller(new PlayerSlotPlan(0, 2));
        CombatController combat = new CombatController(stats, roller, sequenced, hpRng);
        combat.startCombat(0, false, 800f, 120f, new ActionCardDeck(), ignored -> {
        });

        // Player slots 0 & 2 => enemy slots are 1 & 3.
        ActionCardType planned1 = combat.getPlannedEnemyCardForSlot(1);
        ActionCardType planned3 = combat.getPlannedEnemyCardForSlot(3);
        assertNotNull(planned1);
        assertNotNull(planned3);
        assertNotEquals(ActionCardType.ATTACK, planned1, "planned enemy card must not be fixed ATTACK");
        assertNotEquals(ActionCardType.ATTACK, planned3, "planned enemy card must not be fixed ATTACK");
        assertNotEquals(planned1, planned3, "enemy planned cards should reflect AI picks per slot");

        combat.confirmPlanning();
        resolveFullRound(combat);

        assertEquals(planned1, combat.getResolvedEnemyCardForSlot(1), "resolved enemy card must match planned");
        assertEquals(planned3, combat.getResolvedEnemyCardForSlot(3), "resolved enemy card must match planned");
    }

    @Test
    void playedEnemyCard_entersCooldownAfterRoundEnd() {
        PlayerStats stats = new PlayerStats();
        EnemyAi alwaysClaw = candidates -> pickFirstOfType(candidates, ActionCardType.CLAW);
        SequencedPlanRoller roller = new SequencedPlanRoller(new PlayerSlotPlan(0, 2));
        CombatController combat = new CombatController(stats, roller, alwaysClaw, bound -> 0);

        combat.startCombat(0, false, 800f, 120f, new ActionCardDeck(), ignored -> {
        });

        combat.getEnemies().get(0).setHp(999f);
        combat.confirmPlanning();
        resolveFullRound(combat);

        int clawCooldownTurns = ActionCardType.CLAW.getCooldownTurns();
        boolean playedClawCoolingDown = combat.enemyDeckInstancesForTests().stream()
                .filter(i -> i.getType() == ActionCardType.CLAW)
                .anyMatch(i -> i.getCooldownRemaining() == clawCooldownTurns - 1);
        assertTrue(playedClawCoolingDown,
                "played CLAW should be mid-cooldown after round-end set+tick (CD=" + clawCooldownTurns + ")");
    }

    private static ActionCardInstance pickFirstOfType(List<ActionCardInstance> candidates, ActionCardType type) {
        for (ActionCardInstance instance : candidates) {
            if (instance.getType() == type) {
                return instance;
            }
        }
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private static void assertTrueHpInRange(float maxHp) {
        assertEquals(true, maxHp >= 7f && maxHp <= 8f);
    }

    private static void resolveFullRound(CombatController combat) {
        for (int i = 0; i < 4; i++) {
            combat.update(999f);
            combat.finalizePendingOutcome();
        }
    }

    private static Map<String, CardDef> minimalDefs() {
        Map<String, CardDef> defs = new HashMap<>();
        defs.put("ATTACK", damageDef("ATTACK", 2));
        defs.put("HEAL", healDef());
        defs.put("SHIELD", shieldDef());
        defs.put("CLAW", damageDef("CLAW", 2, 3));
        defs.put("VAMPIRISM", vampirismDef());
        return defs;
    }

    private static CardDef damageDef(String id, int damage) {
        return damageDef(id, 1, damage);
    }

    private static CardDef damageDef(String id, int cooldown, int damage) {
        CardDef def = new CardDef();
        def.id = id;
        def.name = id;
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = cooldown;
        def.targeting = CardTargetingId.ENEMY;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        step.amount = damage;
        def.effects = List.of(step);
        return def;
    }

    private static CardDef healDef() {
        CardDef def = new CardDef();
        def.id = "HEAL";
        def.name = "Heal";
        def.category = CardCategoryId.UTILITY;
        def.cooldown = 4;
        def.targeting = CardTargetingId.SELF;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = com.desertadventure.combat.system.effects.EffectTemplateId.HEAL_SELF;
        step.amount = 4;
        def.effects = List.of(step);
        return def;
    }

    private static CardDef shieldDef() {
        CardDef def = new CardDef();
        def.id = "SHIELD";
        def.name = "Shield";
        def.category = CardCategoryId.UTILITY;
        def.cooldown = 3;
        def.targeting = CardTargetingId.SELF;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = com.desertadventure.combat.system.effects.EffectTemplateId.ADD_SHIELD;
        step.amount = 4;
        def.effects = List.of(step);
        return def;
    }

    private static CardDef vampirismDef() {
        CardDef def = new CardDef();
        def.id = "VAMPIRISM";
        def.name = "Vampirism";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectStepDef damage = new CardEffectStepDef();
        damage.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        damage.amount = 3;

        CardEffectStepDef heal = new CardEffectStepDef();
        heal.template = com.desertadventure.combat.system.effects.EffectTemplateId.HEAL_SELF;
        heal.amount = 3;

        def.effects = List.of(damage, heal);
        return def;
    }

    private static final class SequencedPlanRoller implements PlayerSlotRoller {
        private final PlayerSlotPlan[] plans;
        private int idx;

        SequencedPlanRoller(PlayerSlotPlan... plans) {
            this.plans = plans;
        }

        @Override
        public PlayerSlotPlan rollPlan() {
            int i = Math.min(idx, plans.length - 1);
            idx++;
            return plans[i];
        }
    }
}
