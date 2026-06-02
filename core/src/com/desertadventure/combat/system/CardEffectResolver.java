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
    private final Map<String, CardEffectTemplate> templates = new HashMap<>();

    CardEffectResolver() {
        templates.put("DealDamage", (ctx, step) -> ctx.dealDamageToEnemies(step.amount));
        templates.put("HealSelf", (ctx, step) -> ctx.healPlayer(step.amount));
        templates.put("AddShield", (ctx, step) -> ctx.addPlayerShield(step.amount));
        templates.put("HalveEnemyHp", (ctx, step) -> ctx.halveEnemyHp());
        templates.put("ApplyNegativeStatus", (ctx, step) -> ctx.applyNegativeStatusToEnemies(step.status, step.turns));
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
            throw new IllegalStateException("Unknown effect template: " + step.template);
        }
        template.apply(ctx, step);
    }

    private boolean matches(CombatContext ctx, CardEffectConditionDef when) {
        String type = when.type;
        boolean negate = Boolean.TRUE.equals(when.negate);
        boolean result = switch (type) {
            case "RoundEquals" -> when.round != null && ctx.roundNumber() == when.round;
            case "TurnHasResolvedCategory" -> {
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

