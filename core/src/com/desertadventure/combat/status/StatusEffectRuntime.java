package com.desertadventure.combat.status;

import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;
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
        if (def.modifiers == null || def.modifiers.isEmpty()) {
            return amount;
        }
        float result = amount;
        for (StatusModifierDef modifier : def.modifiers) {
            if (modifier != null && modifier.trigger == StatusModifierTriggerId.INCOMING_OFFENSE_CARD_DAMAGE) {
                result += modifier.add;
            }
        }
        return result;
    }
}
