package com.desertadventure.combat.system.support;

import com.desertadventure.combat.card.data.CardCategoryId;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardEffectConditionDef;
import com.desertadventure.combat.card.data.CardEffectStepDef;
import com.desertadventure.combat.card.data.CardTargetingId;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.system.effects.ConditionType;
import com.desertadventure.combat.system.effects.EffectTemplateId;

import java.util.List;
import java.util.Map;

/** Shared minimal {@link CardDef} builders for combat integration tests. */
public final class CombatTestCardDefs {
    private CombatTestCardDefs() {
    }

    public static Map<String, CardDef> combatFlowMinimalDefs() {
        return Map.of(
                "ATTACK", damageDef("ATTACK", "Attack", 1, 2),
                "POISON_MAGIC", poisonDef(),
                "BLADE", bladeDef(),
                "GREAT_BLADE", greatBladeDef(),
                "CHARGED_SLASH", chargedSlashDef());
    }

    public static Map<String, CardDef> outcomeFinalizeMinimalDefs() {
        return Map.of(
                "ATTACK", damageDef("ATTACK", "Attack", 1, 2),
                "STRIKE", damageDef("STRIKE", "斬擊", 2, 3));
    }

    public static Map<String, CardDef> enemyResolveMinimalDefs() {
        return Map.of(
                "ATTACK", damageDef("ATTACK", "ATTACK", 1, 2),
                "HEAL", healDef(),
                "SHIELD", shieldDef(),
                "CLAW", damageDef("CLAW", "CLAW", 2, 3),
                "VAMPIRISM", vampirismDef());
    }

    public static CardDef damageDef(String id, String name, int cooldown, int damage) {
        CardDef def = new CardDef();
        def.id = id;
        def.name = name;
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = cooldown;
        def.targeting = CardTargetingId.ENEMY;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = EffectTemplateId.DEAL_DAMAGE;
        step.amount = damage;
        def.effects = List.of(step);
        return def;
    }

    public static CardDef healDef() {
        CardDef def = new CardDef();
        def.id = "HEAL";
        def.name = "Heal";
        def.category = CardCategoryId.UTILITY;
        def.cooldown = 4;
        def.targeting = CardTargetingId.SELF;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = EffectTemplateId.HEAL_SELF;
        step.amount = 4;
        def.effects = List.of(step);
        return def;
    }

    public static CardDef shieldDef() {
        CardDef def = new CardDef();
        def.id = "SHIELD";
        def.name = "Shield";
        def.category = CardCategoryId.UTILITY;
        def.cooldown = 3;
        def.targeting = CardTargetingId.SELF;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = EffectTemplateId.ADD_SHIELD;
        step.amount = 4;
        def.effects = List.of(step);
        return def;
    }

    public static CardDef poisonDef() {
        CardDef def = new CardDef();
        def.id = "POISON_MAGIC";
        def.name = "Poison Magic";
        def.category = CardCategoryId.UTILITY;
        def.cooldown = 2;
        def.targeting = CardTargetingId.ENEMY;
        CardEffectStepDef step = new CardEffectStepDef();
        step.template = EffectTemplateId.APPLY_NEGATIVE_STATUS;
        step.status = NegativeStatusType.POISON;
        step.turns = 2;
        def.effects = List.of(step);
        return def;
    }

    public static CardDef bladeDef() {
        CardDef def = new CardDef();
        def.id = "BLADE";
        def.name = "Blade";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectStepDef damage = new CardEffectStepDef();
        damage.template = EffectTemplateId.DEAL_DAMAGE;
        damage.amount = 3;

        CardEffectStepDef status = new CardEffectStepDef();
        status.template = EffectTemplateId.APPLY_NEGATIVE_STATUS;
        status.status = NegativeStatusType.BLEED;
        status.turns = 2;

        def.effects = List.of(damage, status);
        return def;
    }

    public static CardDef greatBladeDef() {
        CardDef def = new CardDef();
        def.id = "GREAT_BLADE";
        def.name = "Great Blade";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectStepDef damage = new CardEffectStepDef();
        damage.template = EffectTemplateId.DEAL_DAMAGE;
        damage.amount = 3;

        CardEffectStepDef status = new CardEffectStepDef();
        status.template = EffectTemplateId.APPLY_NEGATIVE_STATUS;
        status.status = NegativeStatusType.FEAR;
        status.turns = 2;

        def.effects = List.of(damage, status);
        return def;
    }

    public static CardDef chargedSlashDef() {
        CardDef def = new CardDef();
        def.id = "CHARGED_SLASH";
        def.name = "Charged Slash";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectConditionDef cond = new CardEffectConditionDef();
        cond.type = ConditionType.SLOT_INDEX_EQUALS;
        cond.slotIndex = 3;

        CardEffectStepDef conditional = new CardEffectStepDef();
        conditional.when = cond;
        conditional.template = EffectTemplateId.DEAL_DAMAGE;
        conditional.amount = 6;

        CardEffectStepDef fallback = new CardEffectStepDef();
        fallback.template = EffectTemplateId.DEAL_DAMAGE;
        fallback.amount = 3;

        def.effects = List.of(conditional, fallback);
        return def;
    }

    public static CardDef vampirismDef() {
        CardDef def = new CardDef();
        def.id = "VAMPIRISM";
        def.name = "Vampirism";
        def.category = CardCategoryId.OFFENSE;
        def.cooldown = 3;
        def.targeting = CardTargetingId.ENEMY;

        CardEffectStepDef damage = new CardEffectStepDef();
        damage.template = EffectTemplateId.DEAL_DAMAGE;
        damage.amount = 3;

        CardEffectStepDef heal = new CardEffectStepDef();
        heal.template = EffectTemplateId.HEAL_SELF;
        heal.amount = 3;

        def.effects = List.of(damage, heal);
        return def;
    }
}
