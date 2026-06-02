package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardCategory;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardCategoryId;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardEffectConditionDef;
import com.desertadventure.combat.card.data.CardEffectStepDef;
import com.desertadventure.combat.card.data.CardTargetingId;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import com.desertadventure.player.PlayerStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardEffectResolverTest {
    private CardEffectResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CardEffectResolver();
        CardDatabase.initialize(new InMemoryCardRepository(minimalDefs()));
    }

    @Test
    void attack_deals2Damage() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, Set.of()), ActionCardType.ATTACK);

        assertEquals(2f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void strongAttack_deals3Damage() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, Set.of()), ActionCardType.STRONG_ATTACK);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void healSelf_heals4() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, Set.of()), ActionCardType.HEAL);

        assertEquals(4f, combat.healPlayerAmount, 0.001f);
    }

    @Test
    void shield_adds4Shield() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, Set.of()), ActionCardType.SHIELD);

        assertEquals(4, combat.playerEntity.getShield());
    }

    @Test
    void poison_appliesPoisonFor2Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, Set.of()), ActionCardType.POISON);

        assertEquals(com.desertadventure.combat.model.NegativeStatusType.POISON, combat.appliedNegativeStatus);
        assertEquals(2, combat.appliedNegativeStatusTurns);
    }

    @Test
    void lifeMagic_halvesEnemyHp() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, Set.of()), ActionCardType.LIFE_MAGIC);

        assertEquals(1, combat.halveEnemyHpCalls);
    }

    @Test
    void fullPower_noUtilityResolved_executesConditional6Damage() {
        FakeCombatController combat = new FakeCombatController();
        CombatContext ctx = new CombatContext(combat, 1, Set.of());

        resolver.resolve(ctx, ActionCardType.FULL_POWER_ATTACK);

        assertEquals(6f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void fullPower_utilityResolved_executesFallback3Damage() {
        FakeCombatController combat = new FakeCombatController();
        combat.cardsByInstanceId.put(10, new ActionCardInstance(10, ActionCardType.HEAL));
        CombatContext ctx = new CombatContext(combat, 1, Set.of(10));

        resolver.resolve(ctx, ActionCardType.FULL_POWER_ATTACK);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void thrust_round1_executes6Damage_otherRoundsExecutes3Damage() {
        FakeCombatController combat = new FakeCombatController();
        resolver.resolve(new CombatContext(combat, 1, Set.of()), ActionCardType.THRUST);
        assertEquals(6f, combat.damageToEnemies, 0.001f);

        combat.damageToEnemies = 0f;
        resolver.resolve(new CombatContext(combat, 2, Set.of()), ActionCardType.THRUST);
        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    private static Map<String, CardDef> minimalDefs() {
        Map<String, CardDef> defs = new HashMap<>();
        defs.put("ATTACK", simpleDamageDef("ATTACK", "Attack", 1, 2));
        defs.put("STRONG_ATTACK", simpleDamageDef("STRONG_ATTACK", "Strong Attack", 2, 3));
        defs.put("HEAL", simpleHealDef());
        defs.put("SHIELD", simpleShieldDef());
        defs.put("FULL_POWER_ATTACK", fullPowerDef());
        defs.put("THRUST", thrustDef());
        defs.put("LIFE_MAGIC", lifeMagicDef());
        defs.put("POISON", poisonDef());
        return defs;
    }

    private static CardDef simpleDamageDef(String id, String name, int cooldown, int damage) {
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

    private static CardDef simpleHealDef() {
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

    private static CardDef simpleShieldDef() {
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

    private static CardDef fullPowerDef() {
        CardDef def = new CardDef();
        def.id = "FULL_POWER_ATTACK";
        def.name = "Full Power";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectConditionDef cond = new CardEffectConditionDef();
        cond.type = com.desertadventure.combat.system.effects.ConditionType.TURN_HAS_RESOLVED_CATEGORY;
        cond.category = CardCategoryId.UTILITY;
        cond.negate = true;

        CardEffectStepDef conditional = new CardEffectStepDef();
        conditional.when = cond;
        conditional.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        conditional.amount = 6;

        CardEffectStepDef fallback = new CardEffectStepDef();
        fallback.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        fallback.amount = 3;

        def.effects = List.of(conditional, fallback);
        return def;
    }

    private static CardDef lifeMagicDef() {
        CardDef def = new CardDef();
        def.id = "LIFE_MAGIC";
        def.name = "Life Magic";
        def.category = CardCategoryId.UTILITY;
        def.cooldown = 5;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectStepDef step = new CardEffectStepDef();
        step.template = com.desertadventure.combat.system.effects.EffectTemplateId.HALVE_ENEMY_HP;
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
        step.template = com.desertadventure.combat.system.effects.EffectTemplateId.APPLY_NEGATIVE_STATUS;
        step.status = com.desertadventure.combat.model.NegativeStatusType.POISON;
        step.turns = 2;
        def.effects = List.of(step);
        return def;
    }

    private static CardDef thrustDef() {
        CardDef def = new CardDef();
        def.id = "THRUST";
        def.name = "Thrust";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectConditionDef cond = new CardEffectConditionDef();
        cond.type = com.desertadventure.combat.system.effects.ConditionType.ROUND_EQUALS;
        cond.round = 1;

        CardEffectStepDef conditional = new CardEffectStepDef();
        conditional.when = cond;
        conditional.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        conditional.amount = 6;

        CardEffectStepDef fallback = new CardEffectStepDef();
        fallback.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        fallback.amount = 3;

        def.effects = List.of(conditional, fallback);
        return def;
    }

    /**
     * Minimal combat controller that records template effects.
     * Lives in the same package so it can override package-private effect methods.
     */
    static final class FakeCombatController extends CombatController {
        float damageToEnemies;
        float healPlayerAmount;
        com.desertadventure.combat.model.NegativeStatusType appliedNegativeStatus;
        int appliedNegativeStatusTurns;
        int halveEnemyHpCalls;
        final Map<Integer, ActionCardInstance> cardsByInstanceId = new HashMap<>();
        final com.desertadventure.combat.model.CombatEntity playerEntity =
                new com.desertadventure.combat.model.CombatEntity(
                        com.desertadventure.combat.model.CombatEntity.Kind.PLAYER,
                        0f, 0f, 10f, 0, 0f);

        FakeCombatController() {
            super(new PlayerStats());
            playerEntity.clearCombatStatus();
        }

        @Override
        public ActionCardInstance findCard(int instanceId) {
            return cardsByInstanceId.get(instanceId);
        }

        @Override
        void dealDamageToEnemy(float amount) {
            damageToEnemies += amount;
        }

        @Override
        void healPlayer(float amount) {
            healPlayerAmount += amount;
        }

        @Override
        void applyNegativeStatusToEnemies(com.desertadventure.combat.model.NegativeStatusType type, int turns) {
            appliedNegativeStatus = type;
            appliedNegativeStatusTurns = turns;
        }

        @Override
        void halveEnemyHp() {
            halveEnemyHpCalls++;
        }

        @Override
        public com.desertadventure.combat.model.CombatEntity getPlayer() {
            return playerEntity;
        }

        @Override
        public java.util.List<com.desertadventure.combat.model.CombatEntity> getEnemies() {
            return java.util.List.of();
        }
    }
}

