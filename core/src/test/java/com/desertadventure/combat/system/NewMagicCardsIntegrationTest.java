package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardCategoryId;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardEffectConditionDef;
import com.desertadventure.combat.card.data.CardEffectStepDef;
import com.desertadventure.combat.card.data.CardTargetingId;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import com.desertadventure.combat.enemy.EnemyAi;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.system.effects.ConditionType;
import com.desertadventure.combat.system.effects.EffectTemplateId;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for PURIFY, MAGIC_BOLT, POISON_BOLT, MAGIC_MIRROR, MAGIC_ARROW
 * through the real {@link CombatController} round flow.
 */
class NewMagicCardsIntegrationTest {
    @BeforeEach
    void setUpCards() {
        CardDatabase.initialize(new InMemoryCardRepository(allNewCardDefs()));
    }

    @Test
    void purify_clearsPlayerPoison_whenDebuffed() {
        CombatController combat = newCombat();
        ActionCardDeck deck = deckWith(ActionCardType.PURIFY);
        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });
        combat.getEnemies().get(0).setHp(999f);
        combat.getPlayer().setNegativeStatus(NegativeStatusType.POISON, 2);
        float hpBefore = combat.getPlayer().getHp();

        assignAndResolve(combat, deck, ActionCardType.PURIFY);

        assertFalse(combat.getPlayer().hasNegativeStatus());
        assertEquals(hpBefore, combat.getPlayer().getHp(), 0.001f, "purify with debuff should not heal");
    }

    @Test
    void purify_heals2_whenNoDebuff() {
        CombatController combat = newCombat();
        ActionCardDeck deck = deckWith(ActionCardType.PURIFY);
        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });
        combat.getEnemies().get(0).setHp(999f);
        combat.getPlayer().setHp(combat.getPlayer().getHp() - 3f);
        float hpBefore = combat.getPlayer().getHp();

        assignAndResolve(combat, deck, ActionCardType.PURIFY);

        assertEquals(hpBefore + 2f, combat.getPlayer().getHp(), 0.001f);
    }

    @Test
    void magicBolt_ignoresEnemyShield() {
        CombatController combat = newCombat();
        ActionCardDeck deck = deckWith(ActionCardType.MAGIC_BOLT);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        var enemy = combat.getEnemies().get(0);
        enemy.setHp(10f);
        enemy.addShield(5);

        assignAndResolve(combat, deck, ActionCardType.MAGIC_BOLT);

        assertEquals(7f, enemy.getHp(), 0.001f, "3 damage should bypass 5 shield");
        assertEquals(5, enemy.getShield(), "shield should remain untouched");
    }

    @Test
    void poisonBolt_appliesPoisonOnDeterministicRoll() {
        CombatController combat = newCombat();
        combat.setCardEffectRngForTests(bound -> 0);
        ActionCardDeck deck = deckWith(ActionCardType.POISON_BOLT);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        combat.getEnemies().get(0).setHp(10f);

        assignAndResolve(combat, deck, ActionCardType.POISON_BOLT);

        assertEquals(8f, combat.getEnemies().get(0).getHp(), 0.001f);
        assertEquals(NegativeStatusType.POISON, combat.getEnemies().get(0).getNegativeStatusType());
        assertEquals(1, combat.getEnemies().get(0).getNegativeTurnsRemaining());
    }

    @Test
    void magicMirror_movesPlayerDebuffToEnemy() {
        CombatController combat = newCombat();
        ActionCardDeck deck = deckWith(ActionCardType.MAGIC_MIRROR);
        combat.startCombat(0, false, 800f, 120f, deck, ignored -> {
        });
        combat.getEnemies().get(0).setHp(999f);
        combat.getPlayer().setNegativeStatus(NegativeStatusType.BLEED, 2);
        var enemy = combat.getEnemies().get(0);

        assignAndResolve(combat, deck, ActionCardType.MAGIC_MIRROR);

        assertFalse(combat.getPlayer().hasNegativeStatus());
        assertEquals(NegativeStatusType.BLEED, enemy.getNegativeStatusType());
        assertEquals(2, enemy.getNegativeTurnsRemaining());
    }

    @Test
    void magicArrow_deals4AgainstDebuffedEnemy() {
        CombatController combat = newCombat();
        ActionCardDeck deck = deckWith(ActionCardType.MAGIC_ARROW);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        var enemy = combat.getEnemies().get(0);
        enemy.setHp(10f);
        enemy.setNegativeStatus(NegativeStatusType.POISON, 1);

        assignAndResolve(combat, deck, ActionCardType.MAGIC_ARROW);

        assertEquals(6f, enemy.getHp(), 0.001f);
    }

    private static CombatController newCombat() {
        PlayerStats stats = new PlayerStats();
        PlayerSlotRoller roller = () -> new PlayerSlotPlan(0, 2);
        EnemyAi passiveEnemy = candidates -> null;
        return new CombatController(stats, roller, passiveEnemy, bound -> 0);
    }

    private static ActionCardDeck deckWith(ActionCardType type) {
        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(type);
        return deck;
    }

    private static void assignAndResolve(CombatController combat, ActionCardDeck deck, ActionCardType type) {
        int id = deck.getInstances().stream()
                .filter(i -> i.getType() == type)
                .findFirst()
                .orElseThrow()
                .getInstanceId();
        combat.assignToPlayerSlot(0, id);
        assertNotNull(combat.getSlotCard(0), "card should be assigned to player slot 0");
        combat.confirmPlanning();
        // Player slot 0 resolves first; one update is enough for these single-card tests.
        combat.update(999f);
        combat.finalizePendingOutcome();
    }

    private static Map<String, CardDef> allNewCardDefs() {
        Map<String, CardDef> defs = new HashMap<>();
        defs.put("PURIFY", purifyDef());
        defs.put("MAGIC_BOLT", magicBoltDef());
        defs.put("POISON_BOLT", poisonBoltDef());
        defs.put("MAGIC_MIRROR", magicMirrorDef());
        defs.put("MAGIC_ARROW", magicArrowDef());
        return defs;
    }

    private static CardDef purifyDef() {
        CardDef def = new CardDef();
        def.id = "PURIFY";
        def.name = "Purify";
        def.category = CardCategoryId.UTILITY;
        def.cooldown = 2;
        def.targeting = CardTargetingId.SELF;
        CardEffectConditionDef cond = new CardEffectConditionDef();
        cond.type = ConditionType.CASTER_HAS_NEGATIVE_STATUS;
        CardEffectStepDef clear = new CardEffectStepDef();
        clear.when = cond;
        clear.template = EffectTemplateId.CLEAR_SELF_NEGATIVE_STATUS;
        CardEffectStepDef heal = new CardEffectStepDef();
        heal.template = EffectTemplateId.HEAL_SELF;
        heal.amount = 2;
        def.effects = List.of(clear, heal);
        return def;
    }

    private static CardDef magicBoltDef() {
        CardDef def = new CardDef();
        def.id = "MAGIC_BOLT";
        def.name = "Magic Bolt";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 2;
        def.targeting = CardTargetingId.ENEMY;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = EffectTemplateId.DEAL_DAMAGE_IGNORE_SHIELD;
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
        damage.template = EffectTemplateId.DEAL_DAMAGE;
        damage.amount = 2;
        CardEffectStepDef poison = new CardEffectStepDef();
        poison.template = EffectTemplateId.APPLY_RANDOM_POISON;
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
        step.template = EffectTemplateId.TRANSFER_NEGATIVE_STATUS_TO_OPPONENT;
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
        cond.type = ConditionType.OPPONENT_HAS_NEGATIVE_STATUS;
        CardEffectStepDef bonus = new CardEffectStepDef();
        bonus.when = cond;
        bonus.template = EffectTemplateId.DEAL_DAMAGE;
        bonus.amount = 4;
        CardEffectStepDef fallback = new CardEffectStepDef();
        fallback.template = EffectTemplateId.DEAL_DAMAGE;
        fallback.amount = 3;
        def.effects = List.of(bonus, fallback);
        return def;
    }
}
