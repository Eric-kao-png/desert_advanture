package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardCategory;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardEffectConditionDef;
import com.desertadventure.combat.card.data.CardEffectStepDef;
import com.desertadventure.combat.card.data.CardCategoryId;
import com.desertadventure.combat.system.effects.ConditionType;
import com.desertadventure.combat.system.effects.EffectTemplateId;

import java.util.HashMap;
import java.util.Map;

/** Resolves JSON-defined cards by invoking effect templates with a {@link CombatContext}. */
final class CardEffectResolver {
    private final Map<EffectTemplateId, CardEffectTemplate> templates = new HashMap<>();
    private final Map<ConditionType, ConditionMatcher> conditions = new HashMap<>();

    CardEffectResolver() {
        registerTemplate(EffectTemplateId.DEAL_DAMAGE, (ctx, step) -> ctx.dealDamageToEnemies(step.amount));
        registerTemplate(EffectTemplateId.DEAL_DAMAGE_IGNORE_SHIELD,
                (ctx, step) -> ctx.dealDamageToEnemiesIgnoringShield(step.amount));
        registerTemplate(EffectTemplateId.HEAL_SELF, (ctx, step) -> ctx.healPlayer(step.amount));
        registerTemplate(EffectTemplateId.ADD_SHIELD, (ctx, step) -> ctx.addPlayerShield(step.amount));
        registerTemplate(EffectTemplateId.HALVE_ENEMY_HP, (ctx, step) -> ctx.halveEnemyHp());
        registerTemplate(EffectTemplateId.APPLY_NEGATIVE_STATUS, (ctx, step) ->
                ctx.applyNegativeStatusToEnemies(step.status, step.turns));
        registerTemplate(EffectTemplateId.APPLY_RANDOM_POISON, (ctx, step) -> ctx.applyRandomPoisonToEnemies());
        registerTemplate(EffectTemplateId.APPLY_CHANCE_POISON, (ctx, step) -> {
            int chance = step.chancePercent != null ? step.chancePercent : 50;
            int turns = step.turns != null ? step.turns : 2;
            ctx.applyChancePoisonToEnemies(chance, turns);
        });
        registerTemplate(EffectTemplateId.CLEAR_SELF_NEGATIVE_STATUS, (ctx, step) -> ctx.clearCasterNegativeStatus());
        registerTemplate(EffectTemplateId.TRANSFER_NEGATIVE_STATUS_TO_OPPONENT,
                (ctx, step) -> ctx.transferCasterNegativeToOpponent());

        registerCondition(ConditionType.ROUND_EQUALS,
                (ctx, when) -> when.round != null && ctx.roundNumber() == when.round);
        registerCondition(ConditionType.TURN_HAS_RESOLVED_CATEGORY, (ctx, when) -> {
            ActionCardCategory category = ActionCardCategory.fromData(
                    when.category != null ? when.category : CardCategoryId.UTILITY);
            return ctx.turnHasResolvedCategory(category);
        });
        registerCondition(ConditionType.TURN_HAS_USED_CATEGORY, (ctx, when) -> {
            ActionCardCategory category = ActionCardCategory.fromData(
                    when.category != null ? when.category : CardCategoryId.UTILITY);
            return ctx.turnHasUsedCategory(category);
        });
        registerCondition(ConditionType.SLOT_INDEX_EQUALS,
                (ctx, when) -> when.slotIndex != null && ctx.resolvingSlotIndex() == when.slotIndex);
        registerCondition(ConditionType.CASTER_HAS_NEGATIVE_STATUS, (ctx, when) -> ctx.casterHasNegativeStatus());
        registerCondition(ConditionType.OPPONENT_HAS_NEGATIVE_STATUS, (ctx, when) -> ctx.opponentHasNegativeStatus());
    }

    void resolve(CombatContext ctx, ActionCardType type) {
        CardDef def = CardDatabase.getRequired().getRequired(type.name());
        boolean matchedAnyConditional = false;
        for (CardEffectStepDef step : def.effects) {
            if (step == null) {
                continue;
            }
            if (step.when != null) {
                if (!matches(ctx, step.when)) {
                    continue;
                }
                matchedAnyConditional = true;
                applyStep(ctx, step);
                return; // first matching conditional wins (fallback steps come after)
            }
            // unconditional: only execute if no prior conditional matched
            if (!matchedAnyConditional) {
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
        boolean negate = Boolean.TRUE.equals(when.negate);
        ConditionMatcher matcher = conditions.get(when.type);
        boolean result = matcher != null && matcher.matches(ctx, when);
        return negate ? !result : result;
    }

    void registerTemplate(EffectTemplateId templateId, CardEffectTemplate template) {
        if (templateId == null || template == null) {
            throw new IllegalArgumentException("templateId/template must be non-null");
        }
        templates.put(templateId, template);
    }

    void registerCondition(ConditionType conditionType, ConditionMatcher matcher) {
        if (conditionType == null || matcher == null) {
            throw new IllegalArgumentException("conditionType/matcher must be non-null");
        }
        conditions.put(conditionType, matcher);
    }

    private interface CardEffectTemplate {
        void apply(CombatContext ctx, CardEffectStepDef step);
    }

    private interface ConditionMatcher {
        boolean matches(CombatContext ctx, CardEffectConditionDef when);
    }
}

