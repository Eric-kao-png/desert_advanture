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
        defs.put("FULL_POWER_ATTACK", fullPowerDef());
        defs.put("THRUST", thrustDef());
        defs.put("HEAL", simpleHealDef());
        return defs;
    }

    private static CardDef simpleHealDef() {
        CardDef def = new CardDef();
        def.id = "HEAL";
        def.name = "Heal";
        def.category = CardCategoryId.UTILITY;
        def.cooldown = 4;
        def.targeting = CardTargetingId.SELF;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = "HealSelf";
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
        cond.type = "TurnHasResolvedCategory";
        cond.category = CardCategoryId.UTILITY;
        cond.negate = true;

        CardEffectStepDef conditional = new CardEffectStepDef();
        conditional.when = cond;
        conditional.template = "DealDamage";
        conditional.amount = 6;

        CardEffectStepDef fallback = new CardEffectStepDef();
        fallback.template = "DealDamage";
        fallback.amount = 3;

        def.effects = List.of(conditional, fallback);
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
        cond.type = "RoundEquals";
        cond.round = 1;

        CardEffectStepDef conditional = new CardEffectStepDef();
        conditional.when = cond;
        conditional.template = "DealDamage";
        conditional.amount = 6;

        CardEffectStepDef fallback = new CardEffectStepDef();
        fallback.template = "DealDamage";
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
        final Map<Integer, ActionCardInstance> cardsByInstanceId = new HashMap<>();

        FakeCombatController() {
            super(new PlayerStats());
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
            // not needed for these tests
        }

        @Override
        void applyNegativeStatusToEnemies(String statusId, int turns) {
            // not needed for these tests
        }

        @Override
        void halveEnemyHp() {
            // not needed for these tests
        }
    }
}

