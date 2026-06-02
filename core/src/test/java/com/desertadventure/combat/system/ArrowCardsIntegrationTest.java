package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardCategoryId;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardEffectStepDef;
import com.desertadventure.combat.card.data.CardTargetingId;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import com.desertadventure.combat.enemy.EnemyAi;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.system.effects.EffectTemplateId;
import com.desertadventure.combat.system.slots.PlayerSlotPlan;
import com.desertadventure.combat.system.slots.PlayerSlotRoller;
import com.desertadventure.player.PlayerStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Integration tests for ARROW and POISON_ARROW through {@link CombatController}. */
class ArrowCardsIntegrationTest {
    @BeforeEach
    void setUpCards() {
        CardDatabase.initialize(new InMemoryCardRepository(arrowCardDefs()));
    }

    @Test
    void arrow_deals3DamageToEnemy() {
        CombatController combat = newCombat();
        ActionCardDeck deck = deckWith(ActionCardType.ARROW);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        combat.getEnemies().get(0).setHp(10f);

        assignAndResolve(combat, deck, ActionCardType.ARROW);

        assertEquals(7f, combat.getEnemies().get(0).getHp(), 0.001f);
    }

    @Test
    void poisonArrow_appliesPoisonOnDeterministicRoll() {
        CombatController combat = newCombat();
        combat.setCardEffectRngForTests(bound -> bound == 100 ? 49 : 0);
        ActionCardDeck deck = deckWith(ActionCardType.POISON_ARROW);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        var enemy = combat.getEnemies().get(0);
        enemy.setHp(10f);

        assignAndResolve(combat, deck, ActionCardType.POISON_ARROW);

        assertEquals(8f, enemy.getHp(), 0.001f);
        assertEquals(NegativeStatusType.POISON, enemy.getNegativeStatusType());
        assertEquals(2, enemy.getNegativeTurnsRemaining());
    }

    @Test
    void poisonArrow_noPoisonOnFailedRoll() {
        CombatController combat = newCombat();
        combat.setCardEffectRngForTests(bound -> bound == 100 ? 50 : 0);
        ActionCardDeck deck = deckWith(ActionCardType.POISON_ARROW);
        combat.startCombat(0, true, 800f, 120f, deck, ignored -> {
        });
        var enemy = combat.getEnemies().get(0);
        enemy.setHp(10f);

        assignAndResolve(combat, deck, ActionCardType.POISON_ARROW);

        assertEquals(8f, enemy.getHp(), 0.001f);
        assertNull(enemy.getNegativeStatusType());
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
        combat.update(999f);
        combat.finalizePendingOutcome();
    }

    private static Map<String, CardDef> arrowCardDefs() {
        Map<String, CardDef> defs = new HashMap<>();
        defs.put("ARROW", arrowDef());
        defs.put("POISON_ARROW", poisonArrowDef());
        return defs;
    }

    private static CardDef arrowDef() {
        CardDef def = new CardDef();
        def.id = "ARROW";
        def.name = "Arrow";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 2;
        def.targeting = CardTargetingId.ENEMY;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = EffectTemplateId.DEAL_DAMAGE;
        step.amount = 3;
        def.effects = List.of(step);
        return def;
    }

    private static CardDef poisonArrowDef() {
        CardDef def = new CardDef();
        def.id = "POISON_ARROW";
        def.name = "Poison Arrow";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 2;
        def.targeting = CardTargetingId.ENEMY;
        CardEffectStepDef damage = new CardEffectStepDef();
        damage.template = EffectTemplateId.DEAL_DAMAGE;
        damage.amount = 2;
        CardEffectStepDef poison = new CardEffectStepDef();
        poison.template = EffectTemplateId.APPLY_CHANCE_POISON;
        poison.chancePercent = 50;
        poison.turns = 2;
        def.effects = List.of(damage, poison);
        return def;
    }
}
