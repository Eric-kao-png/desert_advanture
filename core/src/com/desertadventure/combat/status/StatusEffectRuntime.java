package com.desertadventure.combat.status;

import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.model.PositiveStatusType;
import com.desertadventure.combat.status.data.RoundEndDamageDef;
import com.desertadventure.combat.status.data.RoundEndDamageKind;
import com.desertadventure.combat.status.data.StatusEffectDatabase;
import com.desertadventure.combat.status.data.StatusEffectDef;
import com.desertadventure.combat.status.data.StatusModifierDef;
import com.desertadventure.combat.status.data.StatusModifierTriggerId;

/** Applies JSON-driven status rules at combat runtime. */
public final class StatusEffectRuntime {
    private StatusEffectRuntime() {
    }

    public static float computeRoundEndDamage(CombatEntity entity) {
        if (!entity.hasNegativeStatus()) {
            return 0f;
        }
        NegativeStatusType type = entity.getNegativeStatusType();
        StatusEffectDef def = StatusEffectDatabase.getRequired().getNegativeRequired(type);
        RoundEndDamageDef roundEnd = def.roundEnd;
        if (roundEnd == null || roundEnd.kind == null) {
            return 0f;
        }
        return switch (roundEnd.kind) {
            case FIXED -> roundEnd.amount != null ? roundEnd.amount.floatValue() : 0f;
            case REMAINING_TURNS -> entity.getNegativeTurnsRemaining();
        };
    }

    public static float applyIncomingOffenseCardDamageModifiers(float amount, CombatEntity target) {
        if (!target.hasNegativeStatus()) {
            return amount;
        }
        StatusEffectDef def = StatusEffectDatabase.getRequired().getNegativeRequired(target.getNegativeStatusType());
        return applyModifiersForTrigger(amount, def, StatusModifierTriggerId.INCOMING_OFFENSE_CARD_DAMAGE);
    }

    /**
     * Resolves offense-card damage against a bearer with positive buffs (player).
     *
     * @return final damage to apply; {@code blocked} true if dodge consumed the hit
     */
    public static IncomingOffenseToSelfResult resolveIncomingOffenseToSelf(float amount, CombatEntity bearer) {
        if (!bearer.hasPositiveStatus() || amount <= 0f) {
            return new IncomingOffenseToSelfResult(amount, false, 0f);
        }
        StatusEffectDef def = StatusEffectDatabase.getRequired().getPositiveRequired(bearer.getPositiveStatusType());
        float damage = amount;
        boolean blocked = false;
        float retaliate = 0f;
        if (def.modifiers != null) {
            for (StatusModifierDef modifier : def.modifiers) {
                if (modifier == null || modifier.trigger != StatusModifierTriggerId.INCOMING_OFFENSE_CARD_DAMAGE_TO_SELF) {
                    continue;
                }
                if (modifier.reduce != null && modifier.reduce > 0f) {
                    damage = Math.max(0f, damage - modifier.reduce);
                }
                if (modifier.retaliateAttacker != null && modifier.retaliateAttacker > 0f) {
                    retaliate = Math.max(retaliate, modifier.retaliateAttacker);
                }
                if (Boolean.TRUE.equals(modifier.blockOffenseHit)) {
                    blocked = true;
                }
            }
        }
        if (blocked) {
            bearer.clearPositiveStatus();
            damage = 0f;
        }
        return new IncomingOffenseToSelfResult(damage, blocked, retaliate);
    }

    public static boolean casterIgnoresShieldOnOffense(CombatEntity caster) {
        if (!caster.hasPositiveStatus()) {
            return false;
        }
        StatusEffectDef def = StatusEffectDatabase.getRequired().getPositiveRequired(caster.getPositiveStatusType());
        if (def.modifiers == null) {
            return false;
        }
        for (StatusModifierDef modifier : def.modifiers) {
            if (modifier != null
                    && modifier.trigger == StatusModifierTriggerId.OUTGOING_OFFENSE_CARD_DAMAGE
                    && Boolean.TRUE.equals(modifier.ignoreShieldOnOffense)) {
                return true;
            }
        }
        return false;
    }

    public static float outgoingOffenseHealCaster(CombatEntity caster) {
        if (!caster.hasPositiveStatus()) {
            return 0f;
        }
        StatusEffectDef def = StatusEffectDatabase.getRequired().getPositiveRequired(caster.getPositiveStatusType());
        float heal = 0f;
        if (def.modifiers != null) {
            for (StatusModifierDef modifier : def.modifiers) {
                if (modifier != null
                        && modifier.trigger == StatusModifierTriggerId.OUTGOING_OFFENSE_CARD_DAMAGE
                        && modifier.healCasterOnOffense != null) {
                    heal = Math.max(heal, modifier.healCasterOnOffense);
                }
            }
        }
        return heal;
    }

    private static float applyModifiersForTrigger(
            float amount, StatusEffectDef def, StatusModifierTriggerId trigger) {
        if (def.modifiers == null || def.modifiers.isEmpty()) {
            return amount;
        }
        float result = amount;
        for (StatusModifierDef modifier : def.modifiers) {
            if (modifier != null && modifier.trigger == trigger) {
                result += modifier.add;
            }
        }
        return result;
    }

    public record IncomingOffenseToSelfResult(float damageToBearer, boolean blocked, float retaliateAttacker) {
    }
}
