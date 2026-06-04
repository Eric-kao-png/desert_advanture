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
import com.desertadventure.combat.system.support.CombatTestCardDefs;
import com.desertadventure.player.PlayerStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardEffectResolverTest {
    private CardEffectResolver resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new CardEffectResolver();
        com.desertadventure.combat.system.support.CombatCardTestSupport.initializeProductionCardDatabase();
    }

    @Test
    void attack_deals2Damage() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.ATTACK);

        assertEquals(2f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void enemyCaster_attack_dealsDamageToPlayer() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(
                new CombatContext(combat, 1, -1, EffectCaster.ENEMY),
                ActionCardType.ATTACK);

        assertEquals(2f, combat.damageToPlayer, 0.001f);
        assertEquals(0f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void enemyCaster_heal_healsEnemy() {
        FakeCombatController combat = new FakeCombatController();
        var enemy = new com.desertadventure.combat.model.CombatEntity(
                com.desertadventure.combat.model.CombatEntity.Kind.ENEMY, 0f, 0f, 10f);
        enemy.setHp(3f);
        combat.enemies.add(enemy);

        resolver.resolve(
                new CombatContext(combat, 1, -1, EffectCaster.ENEMY),
                ActionCardType.HEAL);

        assertEquals(7f, enemy.getHp(), 0.001f);
    }

    @Test
    void strongAttack_deals3Damage() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.STRIKE);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void healSelf_heals4() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.HEAL);

        assertEquals(4f, combat.healPlayerAmount, 0.001f);
    }

    @Test
    void shield_adds4Shield() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.SHIELD);

        assertEquals(4, combat.playerEntity.getShield());
    }

    @Test
    void spikeShield_appliesPositiveFor2Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.SPIKE_SHIELD);

        assertEquals(com.desertadventure.combat.model.PositiveStatusType.SPIKE_SHIELD,
                combat.playerEntity.getPositiveStatusType());
        assertEquals(2, combat.playerEntity.getPositiveTurnsRemaining());
    }

    @Test
    void scaleArmor_appliesPositiveFor2Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.SCALE_ARMOR);

        assertEquals(com.desertadventure.combat.model.PositiveStatusType.SCALE_ARMOR,
                combat.playerEntity.getPositiveStatusType());
        assertEquals(2, combat.playerEntity.getPositiveTurnsRemaining());
    }

    @Test
    void focus_appliesPositiveFor3Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.FOCUS);

        assertEquals(com.desertadventure.combat.model.PositiveStatusType.FOCUS,
                combat.playerEntity.getPositiveStatusType());
        assertEquals(3, combat.playerEntity.getPositiveTurnsRemaining());
    }

    @Test
    void vampireFang_appliesPositiveFor2Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.VAMPIRE_FANG);

        assertEquals(com.desertadventure.combat.model.PositiveStatusType.VAMPIRE_FANG,
                combat.playerEntity.getPositiveStatusType());
        assertEquals(2, combat.playerEntity.getPositiveTurnsRemaining());
    }

    @Test
    void dodge_appliesPositiveFor3Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.DODGE);

        assertEquals(com.desertadventure.combat.model.PositiveStatusType.DODGE,
                combat.playerEntity.getPositiveStatusType());
        assertEquals(3, combat.playerEntity.getPositiveTurnsRemaining());
    }

    @Test
    void poison_appliesPoisonFor2Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.POISON_MAGIC);

        assertEquals(com.desertadventure.combat.model.NegativeStatusType.POISON, combat.appliedNegativeStatus);
        assertEquals(2, combat.appliedNegativeStatusTurns);
    }

    @Test
    void lifeMagic_halvesEnemyHp() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.LIFE_MAGIC);

        assertEquals(1, combat.halveEnemyHpCalls);
    }

    @Test
    void assault_noChangeCardUsed_executes6Damage() {
        FakeCombatController combat = new FakeCombatController();
        CombatContext ctx = new CombatContext(combat, 1);

        resolver.resolve(ctx, ActionCardType.ASSAULT);

        assertEquals(6f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void assault_changeCardAssigned_executesFallback3Damage() {
        FakeCombatController combat = new FakeCombatController();
        combat.cardsByInstanceId.put(10, new ActionCardInstance(10, ActionCardType.HEAL));
        combat.setPlayerSlotInstanceForTest(2, 10);

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.ASSAULT);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void assault_changeCardResolvedEarlier_executesFallback3Damage() {
        FakeCombatController combat = new FakeCombatController();
        combat.cardsByInstanceId.put(10, new ActionCardInstance(10, ActionCardType.HEAL));
        combat.setPlayerSlotInstanceForTest(0, 10);
        CombatContext ctx = new CombatContext(combat, 1);

        resolver.resolve(ctx, ActionCardType.ASSAULT);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void heavyStrike_deals4Damage() {
        FakeCombatController combat = new FakeCombatController();
        resolver.resolve(new CombatContext(combat, 1), ActionCardType.HEAVY_STRIKE);
        assertEquals(4f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void swiftStrike_slot1_executes6Damage_otherSlots3() {
        FakeCombatController combat = new FakeCombatController();
        resolver.resolve(new CombatContext(combat, 1, 0), ActionCardType.SWIFT_STRIKE);
        assertEquals(6f, combat.damageToEnemies, 0.001f);

        combat.damageToEnemies = 0f;
        resolver.resolve(new CombatContext(combat, 1, 2), ActionCardType.SWIFT_STRIKE);
        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void spellblade_changeCardUsed_executes6Damage() {
        FakeCombatController combat = new FakeCombatController();
        combat.cardsByInstanceId.put(10, new ActionCardInstance(10, ActionCardType.HEAL));
        combat.setPlayerSlotInstanceForTest(0, 10);
        resolver.resolve(new CombatContext(combat, 1), ActionCardType.SPELLBLADE);
        assertEquals(6f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void spellblade_noChangeCard_executes3Damage() {
        FakeCombatController combat = new FakeCombatController();
        resolver.resolve(new CombatContext(combat, 1), ActionCardType.SPELLBLADE);
        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void chaseAttack_previousSlotPlayerOffense_executes6Damage() {
        FakeCombatController combat = new FakeCombatController();
        combat.cardsByInstanceId.put(1, new ActionCardInstance(1, ActionCardType.ATTACK));
        combat.setPlayerSlotInstanceForTest(0, 1);
        resolver.resolve(new CombatContext(combat, 1, 1), ActionCardType.CHASE_ATTACK);
        assertEquals(6f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void chaseAttack_noPreviousPlayerOffense_executes3Damage() {
        FakeCombatController combat = new FakeCombatController();
        resolver.resolve(new CombatContext(combat, 1, 2), ActionCardType.CHASE_ATTACK);
        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void doubleBlade_dealsTwoHitsOf2() {
        FakeCombatController combat = new FakeCombatController();
        resolver.resolve(new CombatContext(combat, 1), ActionCardType.DOUBLE_BLADE);
        assertEquals(4f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void thrust_round1_executes6Damage_otherRoundsExecutes3Damage() {
        FakeCombatController combat = new FakeCombatController();
        resolver.resolve(new CombatContext(combat, 1), ActionCardType.AMBUSH);
        assertEquals(6f, combat.damageToEnemies, 0.001f);

        combat.damageToEnemies = 0f;
        resolver.resolve(new CombatContext(combat, 2), ActionCardType.AMBUSH);
        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void claw_deals3Damage() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, 0), ActionCardType.CLAW);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void chargedSlash_slot4_deals6Damage_otherSlotsDeal3() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, 3), ActionCardType.CHARGED_SLASH);
        assertEquals(6f, combat.damageToEnemies, 0.001f);

        combat.damageToEnemies = 0f;
        resolver.resolve(new CombatContext(combat, 1, 2), ActionCardType.CHARGED_SLASH);
        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void blade_appliesBleedFor2Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, 0), ActionCardType.BLADE);

        assertEquals(com.desertadventure.combat.model.NegativeStatusType.BLEED, combat.appliedNegativeStatus);
        assertEquals(2, combat.appliedNegativeStatusTurns);
    }

    @Test
    void greatBlade_appliesFearFor2Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, 0), ActionCardType.GREAT_BLADE);

        assertEquals(com.desertadventure.combat.model.NegativeStatusType.FEAR, combat.appliedNegativeStatus);
        assertEquals(2, combat.appliedNegativeStatusTurns);
    }

    @Test
    void poisonBlade_deals3DamageAndAppliesPoisonFor2Turns() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1, 0), ActionCardType.POISON_BLADE);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
        assertEquals(com.desertadventure.combat.model.NegativeStatusType.POISON, combat.appliedNegativeStatus);
        assertEquals(2, combat.appliedNegativeStatusTurns);
    }

    @Test
    void vampirism_deals3_andHeals2WhenOpponentHadNoShield() {
        FakeCombatController combat = new FakeCombatController();
        combat.opponentShieldAtCardStart = 0;

        resolver.resolve(new CombatContext(combat, 1, 0), ActionCardType.VAMPIRISM);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
        assertEquals(2f, combat.healPlayerAmount, 0.001f);
    }

    @Test
    void vampirism_doesNotHealWhenOpponentHadShield() {
        FakeCombatController combat = new FakeCombatController();
        combat.opponentShieldAtCardStart = 3;

        resolver.resolve(new CombatContext(combat, 1, 0), ActionCardType.VAMPIRISM);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
        assertEquals(0f, combat.healPlayerAmount, 0.001f);
    }

    @Test
    void purify_withDebuff_clearsNegativeStatus() {
        FakeCombatController combat = new FakeCombatController();
        combat.playerEntity.setNegativeStatus(
                com.desertadventure.combat.model.NegativeStatusType.POISON, 2);

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.PURIFY);

        assertEquals(0, combat.playerEntity.getNegativeTurnsRemaining());
        assertEquals(0f, combat.healPlayerAmount, 0.001f);
    }

    @Test
    void purify_withoutDebuff_heals2() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.PURIFY);

        assertEquals(2f, combat.healPlayerAmount, 0.001f);
    }

    @Test
    void magicBolt_deals3IgnoringShield() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.MAGIC_BOLT);

        assertEquals(3f, combat.damageIgnoringShieldToEnemies, 0.001f);
    }

    @Test
    void poisonBolt_deals2_andAppliesPoisonOnLowRoll() {
        FakeCombatController combat = new FakeCombatController();
        combat.setCardEffectRngForTests(bound -> bound == 100 ? 10 : 0);

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.POISON_BOLT);

        assertEquals(2f, combat.damageToEnemies, 0.001f);
        assertEquals(com.desertadventure.combat.model.NegativeStatusType.POISON, combat.appliedNegativeStatus);
        assertEquals(1, combat.appliedNegativeStatusTurns);
    }

    @Test
    void poisonBolt_noPoisonOnHighRoll() {
        FakeCombatController combat = new FakeCombatController();
        combat.setCardEffectRngForTests(bound -> bound == 100 ? 60 : 0);

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.POISON_BOLT);

        assertEquals(2f, combat.damageToEnemies, 0.001f);
        assertEquals(null, combat.appliedNegativeStatus);
    }

    @Test
    void magicMirror_transfersPlayerDebuffToEnemy() {
        FakeCombatController combat = new FakeCombatController();
        var enemy = new com.desertadventure.combat.model.CombatEntity(
                com.desertadventure.combat.model.CombatEntity.Kind.ENEMY, 0f, 0f, 10f);
        combat.enemies.add(enemy);
        combat.playerEntity.setNegativeStatus(
                com.desertadventure.combat.model.NegativeStatusType.BLEED, 2);

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.MAGIC_MIRROR);

        assertEquals(0, combat.playerEntity.getNegativeTurnsRemaining());
        assertEquals(com.desertadventure.combat.model.NegativeStatusType.BLEED, enemy.getNegativeStatusType());
        assertEquals(2, enemy.getNegativeTurnsRemaining());
    }

    @Test
    void enemyCaster_magicMirror_transfersEnemyDebuffToPlayer() {
        FakeCombatController combat = new FakeCombatController();
        var enemy = new com.desertadventure.combat.model.CombatEntity(
                com.desertadventure.combat.model.CombatEntity.Kind.ENEMY, 0f, 0f, 10f);
        enemy.setNegativeStatus(com.desertadventure.combat.model.NegativeStatusType.POISON, 1);
        combat.enemies.add(enemy);

        resolver.resolve(
                new CombatContext(combat, 1, -1, EffectCaster.ENEMY),
                ActionCardType.MAGIC_MIRROR);

        assertEquals(0, enemy.getNegativeTurnsRemaining());
        assertEquals(com.desertadventure.combat.model.NegativeStatusType.POISON,
                combat.playerEntity.getNegativeStatusType());
        assertEquals(1, combat.playerEntity.getNegativeTurnsRemaining());
    }

    @Test
    void arrow_deals3() {
        FakeCombatController combat = new FakeCombatController();

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.ARROW);

        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    @Test
    void poisonArrow_deals2_andAppliesPoisonOnLowRoll() {
        FakeCombatController combat = new FakeCombatController();
        combat.setCardEffectRngForTests(bound -> bound == 100 ? 49 : 0);

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.POISON_ARROW);

        assertEquals(2f, combat.damageToEnemies, 0.001f);
        assertEquals(com.desertadventure.combat.model.NegativeStatusType.POISON, combat.appliedNegativeStatus);
        assertEquals(2, combat.appliedNegativeStatusTurns);
    }

    @Test
    void poisonArrow_noPoisonOnHighRoll() {
        FakeCombatController combat = new FakeCombatController();
        combat.setCardEffectRngForTests(bound -> bound == 100 ? 50 : 0);

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.POISON_ARROW);

        assertEquals(2f, combat.damageToEnemies, 0.001f);
        assertEquals(null, combat.appliedNegativeStatus);
    }

    @Test
    void magicArrow_deals4WhenTargetHasDebuff_else3() {
        FakeCombatController combat = new FakeCombatController();
        var enemy = new com.desertadventure.combat.model.CombatEntity(
                com.desertadventure.combat.model.CombatEntity.Kind.ENEMY, 0f, 0f, 10f);
        enemy.setNegativeStatus(com.desertadventure.combat.model.NegativeStatusType.FEAR, 1);
        combat.enemies.add(enemy);

        resolver.resolve(new CombatContext(combat, 1), ActionCardType.MAGIC_ARROW);
        assertEquals(4f, combat.damageToEnemies, 0.001f);

        combat.damageToEnemies = 0f;
        enemy.clearNegativeStatus();
        resolver.resolve(new CombatContext(combat, 1), ActionCardType.MAGIC_ARROW);
        assertEquals(3f, combat.damageToEnemies, 0.001f);
    }

    private static Map<String, CardDef> minimalDefs() {
        Map<String, CardDef> defs = new HashMap<>();
        defs.put("ATTACK", CombatTestCardDefs.damageDef("ATTACK", "Attack", 1, 2));
        defs.put("STRIKE", CombatTestCardDefs.damageDef("STRIKE", "斬擊", 2, 3));
        defs.put("HEAVY_STRIKE", CombatTestCardDefs.damageDef("HEAVY_STRIKE", "重擊", 3, 4));
        defs.put("SWIFT_STRIKE", swiftStrikeDef());
        defs.put("SPELLBLADE", spellbladeDef());
        defs.put("CHASE_ATTACK", chaseAttackDef());
        defs.put("DOUBLE_BLADE", doubleBladeDef());
        defs.put("HEAL", CombatTestCardDefs.healDef());
        defs.put("SHIELD", CombatTestCardDefs.shieldDef());
        defs.put("ASSAULT", fullPowerDef());
        defs.put("AMBUSH", thrustDef());
        defs.put("LIFE_MAGIC", lifeMagicDef());
        defs.put("POISON_MAGIC", CombatTestCardDefs.poisonDef());
        defs.put("CLAW", CombatTestCardDefs.damageDef("CLAW", "Claw", 2, 3));
        defs.put("CHARGED_SLASH", CombatTestCardDefs.chargedSlashDef());
        defs.put("BLADE", CombatTestCardDefs.bladeDef());
        defs.put("GREAT_BLADE", CombatTestCardDefs.greatBladeDef());
        defs.put("VAMPIRISM", CombatTestCardDefs.vampirismDef());
        defs.put("PURIFY", purifyDef());
        defs.put("MAGIC_BOLT", simpleDamageIgnoreShieldDef());
        defs.put("POISON_BOLT", poisonBoltDef());
        defs.put("MAGIC_MIRROR", magicMirrorDef());
        defs.put("MAGIC_ARROW", magicArrowDef());
        defs.put("ARROW", arrowDef());
        defs.put("POISON_ARROW", poisonArrowDef());
        return defs;
    }

    private static CardDef swiftStrikeDef() {
        CardDef def = new CardDef();
        def.id = "SWIFT_STRIKE";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectConditionDef cond = new CardEffectConditionDef();
        cond.type = com.desertadventure.combat.system.effects.ConditionType.SLOT_INDEX_EQUALS;
        cond.slotIndex = 0;

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

    private static CardDef spellbladeDef() {
        CardDef def = new CardDef();
        def.id = "SPELLBLADE";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectConditionDef cond = new CardEffectConditionDef();
        cond.type = com.desertadventure.combat.system.effects.ConditionType.TURN_HAS_USED_CATEGORY;
        cond.category = CardCategoryId.UTILITY;

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

    private static CardDef chaseAttackDef() {
        CardDef def = new CardDef();
        def.id = "CHASE_ATTACK";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectConditionDef cond = new CardEffectConditionDef();
        cond.type = com.desertadventure.combat.system.effects.ConditionType.PREVIOUS_SLOT_PLAYER_OFFENSE;

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

    private static CardDef doubleBladeDef() {
        CardDef def = new CardDef();
        def.id = "DOUBLE_BLADE";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;
        CardEffectStepDef hit = new CardEffectStepDef();
        hit.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        hit.amount = 2;
        def.effects = List.of(hit, hit);
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
        cond.type = com.desertadventure.combat.system.effects.ConditionType.TURN_HAS_USED_CATEGORY;
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

    private static CardDef purifyDef() {
        CardDef def = new CardDef();
        def.id = "PURIFY";
        def.name = "Purify";
        def.category = CardCategoryId.UTILITY;
        def.cooldown = 2;
        def.targeting = CardTargetingId.SELF;

        CardEffectConditionDef cond = new CardEffectConditionDef();
        cond.type = com.desertadventure.combat.system.effects.ConditionType.CASTER_HAS_NEGATIVE_STATUS;

        CardEffectStepDef clear = new CardEffectStepDef();
        clear.when = cond;
        clear.template = com.desertadventure.combat.system.effects.EffectTemplateId.CLEAR_SELF_NEGATIVE_STATUS;

        CardEffectStepDef heal = new CardEffectStepDef();
        heal.template = com.desertadventure.combat.system.effects.EffectTemplateId.HEAL_SELF;
        heal.amount = 2;

        def.effects = List.of(clear, heal);
        return def;
    }

    private static CardDef simpleDamageIgnoreShieldDef() {
        CardDef def = new CardDef();
        def.id = "MAGIC_BOLT";
        def.name = "Magic Bolt";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 2;
        def.targeting = CardTargetingId.ENEMY;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE_IGNORE_SHIELD;
        step.amount = 3;
        def.effects = List.of(step);
        return def;
    }

    private static CardDef poisonBoltDef() {
        CardDef def = new CardDef();
        def.id = "POISON_BOLT";
        def.name = "Poison Bolt";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 2;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectStepDef damage = new CardEffectStepDef();
        damage.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        damage.amount = 2;

        CardEffectStepDef poison = new CardEffectStepDef();
        poison.template = com.desertadventure.combat.system.effects.EffectTemplateId.APPLY_RANDOM_POISON;

        def.effects = List.of(damage, poison);
        return def;
    }

    private static CardDef magicMirrorDef() {
        CardDef def = new CardDef();
        def.id = "MAGIC_MIRROR";
        def.name = "Magic Mirror";
        def.category = CardCategoryId.UTILITY;
        def.cooldown = 3;
        def.targeting = CardTargetingId.SELF;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template =
                com.desertadventure.combat.system.effects.EffectTemplateId.TRANSFER_NEGATIVE_STATUS_TO_OPPONENT;
        def.effects = List.of(step);
        return def;
    }

    private static CardDef magicArrowDef() {
        CardDef def = new CardDef();
        def.id = "MAGIC_ARROW";
        def.name = "Magic Arrow";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectConditionDef cond = new CardEffectConditionDef();
        cond.type = com.desertadventure.combat.system.effects.ConditionType.OPPONENT_HAS_NEGATIVE_STATUS;

        CardEffectStepDef bonus = new CardEffectStepDef();
        bonus.when = cond;
        bonus.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        bonus.amount = 4;

        CardEffectStepDef fallback = new CardEffectStepDef();
        fallback.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        fallback.amount = 3;

        def.effects = List.of(bonus, fallback);
        return def;
    }

    private static CardDef arrowDef() {
        return CombatTestCardDefs.damageDef("ARROW", "Arrow", 2, 3);
    }

    private static CardDef poisonArrowDef() {
        CardDef def = new CardDef();
        def.id = "POISON_ARROW";
        def.name = "Poison Arrow";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 2;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectStepDef damage = new CardEffectStepDef();
        damage.template = com.desertadventure.combat.system.effects.EffectTemplateId.DEAL_DAMAGE;
        damage.amount = 2;

        CardEffectStepDef poison = new CardEffectStepDef();
        poison.template = com.desertadventure.combat.system.effects.EffectTemplateId.APPLY_CHANCE_POISON;
        poison.chancePercent = 50;
        poison.turns = 2;

        def.effects = List.of(damage, poison);
        return def;
    }

    /**
     * Minimal combat controller that records template effects.
     * Lives in the same package so it can override package-private effect methods.
     */
    static final class FakeCombatController extends CombatController {
        float damageToEnemies;
        float damageIgnoringShieldToEnemies;
        float damageToPlayer;
        float damageIgnoringShieldToPlayer;
        float healPlayerAmount;
        final java.util.List<com.desertadventure.combat.model.CombatEntity> enemies = new java.util.ArrayList<>();
        com.desertadventure.combat.model.NegativeStatusType appliedNegativeStatus;
        int appliedNegativeStatusTurns;
        int halveEnemyHpCalls;
        final Map<Integer, ActionCardInstance> cardsByInstanceId = new HashMap<>();
        final com.desertadventure.combat.model.CombatEntity playerEntity =
                new com.desertadventure.combat.model.CombatEntity(
                        com.desertadventure.combat.model.CombatEntity.Kind.PLAYER,
                        0f, 0f, 10f);

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
        void dealDamageToEnemyIgnoringShield(float amount) {
            damageIgnoringShieldToEnemies += amount;
        }

        @Override
        void dealDamageToPlayer(float amount) {
            dealDamageToPlayer(amount, DamageSource.OTHER);
        }

        @Override
        void dealDamageToPlayer(float amount, DamageSource source) {
            if (source == DamageSource.OFFENSE_CARD) {
                var resolved = com.desertadventure.combat.status.StatusEffectRuntime
                        .resolveIncomingOffenseToSelf(amount, playerEntity);
                damageToPlayer += resolved.damageToBearer();
            } else {
                damageToPlayer += amount;
            }
        }

        @Override
        void dealDamageToPlayerIgnoringShield(float amount) {
            dealDamageToPlayerIgnoringShield(amount, DamageSource.OTHER);
        }

        @Override
        void dealDamageToPlayerIgnoringShield(float amount, DamageSource source) {
            if (source == DamageSource.OFFENSE_CARD) {
                var resolved = com.desertadventure.combat.status.StatusEffectRuntime
                        .resolveIncomingOffenseToSelf(amount, playerEntity);
                damageIgnoringShieldToPlayer += resolved.damageToBearer();
            } else {
                damageIgnoringShieldToPlayer += amount;
            }
        }

        @Override
        void applyPositiveStatusToPlayer(com.desertadventure.combat.model.PositiveStatusType type, int turns) {
            playerEntity.setPositiveStatus(type, turns);
        }

        @Override
        void clearNegativeStatusOnPlayer() {
            playerEntity.clearNegativeStatus();
        }

        @Override
        void clearNegativeStatusOnEnemies() {
            for (com.desertadventure.combat.model.CombatEntity enemy : enemies) {
                if (enemy.isAlive()) {
                    enemy.clearNegativeStatus();
                }
            }
        }

        @Override
        void transferNegativeStatusFromPlayerToEnemies() {
            if (!playerEntity.hasNegativeStatus()) {
                return;
            }
            var type = playerEntity.getNegativeStatusType();
            int turns = playerEntity.getNegativeTurnsRemaining();
            playerEntity.clearNegativeStatus();
            for (com.desertadventure.combat.model.CombatEntity enemy : enemies) {
                if (enemy.isAlive()) {
                    enemy.setNegativeStatus(type, turns);
                }
            }
        }

        @Override
        void transferNegativeStatusFromEnemyToPlayer() {
            for (com.desertadventure.combat.model.CombatEntity enemy : enemies) {
                if (enemy.isAlive() && enemy.hasNegativeStatus()) {
                    var type = enemy.getNegativeStatusType();
                    int turns = enemy.getNegativeTurnsRemaining();
                    enemy.clearNegativeStatus();
                    playerEntity.setNegativeStatus(type, turns);
                    return;
                }
            }
        }

        @Override
        boolean enemyHasNegativeStatus() {
            for (com.desertadventure.combat.model.CombatEntity enemy : enemies) {
                if (enemy.isAlive() && enemy.hasNegativeStatus()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        void healPlayer(float amount) {
            healPlayerAmount += amount;
        }

        @Override
        void healEnemy(float amount) {
            for (com.desertadventure.combat.model.CombatEntity enemy : enemies) {
                if (enemy.isAlive()) {
                    enemy.heal(amount);
                }
            }
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
            return enemies;
        }
    }
}

