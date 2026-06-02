package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardCategory;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardEffectConditionDef;
import com.desertadventure.combat.card.data.CardEffectStepDef;
import com.desertadventure.combat.card.data.CardCategoryId;

import java.util.HashMap;
import java.util.Map;

/** Resolves JSON-defined cards by invoking effect templates with a {@link CombatContext}. */
final class CardEffectResolver {
    private static final String TEMPLATE_DEAL_DAMAGE = "DealDamage";
    private static final String TEMPLATE_HEAL_SELF = "HealSelf";
    private static final String TEMPLATE_ADD_SHIELD = "AddShield";
    private static final String TEMPLATE_HALVE_ENEMY_HP = "HalveEnemyHp";
    private static final String TEMPLATE_APPLY_NEGATIVE_STATUS = "ApplyNegativeStatus";

    private static final String COND_ROUND_EQUALS = "RoundEquals";
    private static final String COND_TURN_HAS_RESOLVED_CATEGORY = "TurnHasResolvedCategory";

    private final Map<String, CardEffectTemplate> templates = new HashMap<>();

    CardEffectResolver() {
        templates.put(TEMPLATE_DEAL_DAMAGE, (ctx, step) -> ctx.dealDamageToEnemies(step.amount));
        templates.put(TEMPLATE_HEAL_SELF, (ctx, step) -> ctx.healPlayer(step.amount));
        templates.put(TEMPLATE_ADD_SHIELD, (ctx, step) -> ctx.addPlayerShield(step.amount));
        templates.put(TEMPLATE_HALVE_ENEMY_HP, (ctx, step) -> ctx.halveEnemyHp());
        templates.put(TEMPLATE_APPLY_NEGATIVE_STATUS, (ctx, step) ->
                ctx.applyNegativeStatusToEnemies(step.status, step.turns));
    }

    void resolve(CombatContext ctx, ActionCardType type) {
        CardDef def = CardDatabase.getRequired().getRequired(type.name());
        boolean matchedConditional = false;
        for (CardEffectStepDef step : def.effects) {
            if (step == null) {
                continue;
            }
            if (step.when != null) {
                if (!matches(ctx, step.when)) {
                    continue;
                }
                matchedConditional = true;
                applyStep(ctx, step);
                return; // first matching conditional wins (fallback steps come after)
            }
            // unconditional: only execute if no prior conditional matched
            if (!matchedConditional) {
                applyStep(ctx, step);
            }
        }
    }

    private void applyStep(CombatContext ctx, CardEffectStepDef step) {
        CardEffectTemplate template = templates.get(step.template);
        if (template == null) {
            throw new IllegalStateException("Unknown effect template: " + step.template
                    + " (context=" + ctx + ")");
        }
        template.apply(ctx, step);
    }

    private boolean matches(CombatContext ctx, CardEffectConditionDef when) {
        String type = when.type;
        boolean negate = Boolean.TRUE.equals(when.negate);
        boolean result = switch (type) {
            case COND_ROUND_EQUALS -> when.round != null && ctx.roundNumber() == when.round;
            case COND_TURN_HAS_RESOLVED_CATEGORY -> {
                ActionCardCategory category = ActionCardCategory.fromData(
                        when.category != null ? when.category : CardCategoryId.UTILITY);
                yield ctx.turnHasResolvedCategory(category);
            }
            default -> false;
        };
        return negate ? !result : result;
    }

    private interface CardEffectTemplate {
        void apply(CombatContext ctx, CardEffectStepDef step);
    }
}

