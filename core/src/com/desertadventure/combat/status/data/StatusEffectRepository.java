package com.desertadventure.combat.status.data;

import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.model.PositiveStatusType;

/** Read-only access to loaded status effect definitions. */
public interface StatusEffectRepository {
    StatusEffectDef getNegativeRequired(NegativeStatusType type);

    StatusEffectDef getPositiveRequired(PositiveStatusType type);

    boolean hasNegative(NegativeStatusType type);

    boolean hasPositive(PositiveStatusType type);
}
