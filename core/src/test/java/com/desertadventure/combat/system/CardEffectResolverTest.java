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

        resolver.resolve(new CombatContext(combat, 1, Set.of()), ActionCardType.SWIFT_STRIKE);

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

        resolver.resolve(new CombatContext(combat, 1, Set.of()), ActionCardType.POISON_MAGIC);

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

        resolver.resolve(ctx, ActionCardType.ASSAULT);

        assertEquals(6f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void fullPower_utilityResolved_executesFallback3Damage() {
        FakeCombatController combat = new FakeCombatController();
        combat.cardsByInstanceId.put(10, new ActionCardInstance(10, ActionCardType.HEAL));
        CombatContext ctx = new CombatContext(combat, 1, Set.of(10));

        resolver.resolve(ctx, ActionCardType.ASSAULT);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void thrust_round1_executes6Damage_otherRoundsExecutes3Damage() {
        FakeCombatController combat = new FakeCombatController();
        resolver.resolve(new CombatContext(combat, 1, Set.of()), ActionCardType.AMBUSH);
        assertEquals(6f, combat.damageToEnemies, 0.001f);

        combat.damageToEnemies = 0f;
        resolver.resolve(new CombatContext(combat, 2, Set.of()), ActionCardType.AMBUSH);
        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void claw_deals3Damage() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, Set.of(), 0), ActionCardType.CLAW);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void chargedSlash_slot4_deals6Damage_otherSlotsDeal3() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, Set.of(), 3), ActionCardType.CHARGED_SLASH);
        assertEquals(6f, combat.damageToEnemies, 0.001f);

        combat.damageToEnemies = 0f;
        resolver.resolve(new CombatContext(combat, 1, Set.of(), 2), ActionCardType.CHARGED_SLASH);
        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void blade_appliesBleedFor2Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, Set.of(), 0), ActionCardType.BLADE);

        assertEquals(com.desertadventure.combat.model.NegativeStatusType.BLEED, combat.appliedNegativeStatus);
        assertEquals(2, combat.appliedNegativeStatusTurns);
    }

    @Test
    void greatBlade_appliesFearFor2Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, Set.of(), 0), ActionCardType.GREAT_BLADE);

        assertEquals(com.desertadventure.combat.model.NegativeStatusType.FEAR, combat.appliedNegativeStatus);
        assertEquals(2, combat.appliedNegativeStatusTurns);
    }

    @Test
    void vampirism_deals3_andHeals3() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, Set.of(), 0), ActionCardType.VAMPIRISM);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
        assertEquals(3f, combat.healPlayerAmount, 0.001f);
    }

    private static Map<String, CardDef> minimalDefs() {
        Map<String, CardDef> defs = new HashMap<>();
        defs.put("ATTACK", simpleDamageDef("ATTACK", "Attack", 1, 2));
        defs.put("SWIFT_STRIKE", simpleDamageDef("SWIFT_STRIKE", "Swift Strike", 2, 3));
        defs.put("HEAL", simpleHealDef());
        defs.put("SHIELD", simpleShieldDef());
        defs.put("ASSAULT", fullPowerDef());
        defs.put("AMBUSH", thrustDef());
        defs.put("LIFE_MAGIC", lifeMagicDef());
        defs.put("POISON_MAGIC", poisonDef());
        defs.put("CLAW", simpleDamageDef("CLAW", "Claw", 2, 3));
        defs.put("CHARGED_SLASH", chargedSlashDef());
        defs.put("BLADE", bladeDef());
        defs.put("GREAT_BLADE", greatBladeDef());
        defs.put("VAMPIRISM", vampirismDef());
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
        def.id = "ASSAULT";
        def.name = "Assault";
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
        def.id = "POISON_MAGIC";
        def.name = "Poison Magic";
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
        def.id = "AMBUSH";
        def.name = "Ambush";
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

    private static CardDef chargedSlashDef() {
        CardDef def = new CardDef();
        def.id = "CHARGED_SLASH";
        def.name = "Charged Slash";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectConditionDef cond = new CardEffectConditionDef();
        cond.type = com.desertadventure.combat.system.effects.ConditionType.SLOT_INDEX_EQUALS;
        cond.slotIndex = 3;

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

    private static CardDef bladeDef() {
        CardDef def = new CardDef();
        def.id = "BLADE";
        def.name = "Blade";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectStepDef damage = new CardEffectStepDef();
        damage.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        damage.amount = 3;

        CardEffectStepDef status = new CardEffectStepDef();
        status.template = com.desertadventure.combat.system.effects.EffectTemplateId.APPLY_NEGATIVE_STATUS;
        status.status = com.desertadventure.combat.model.NegativeStatusType.BLEED;
        status.turns = 2;

        def.effects = List.of(damage, status);
        return def;
    }

    private static CardDef greatBladeDef() {
        CardDef def = new CardDef();
        def.id = "GREAT_BLADE";
        def.name = "Great Blade";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectStepDef damage = new CardEffectStepDef();
        damage.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        damage.amount = 3;

        CardEffectStepDef status = new CardEffectStepDef();
        status.template = com.desertadventure.combat.system.effects.EffectTemplateId.APPLY_NEGATIVE_STATUS;
        status.status = com.desertadventure.combat.model.NegativeStatusType.FEAR;
        status.turns = 2;

        def.effects = List.of(damage, status);
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
        void dealDamageToEnemy(float amount, DamageSource source) {
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

