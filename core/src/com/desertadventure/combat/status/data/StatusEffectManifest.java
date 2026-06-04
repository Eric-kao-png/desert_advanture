package com.desertadventure.combat.status.data;

import java.util.ArrayList;
import java.util.List;

/** Root JSON object for {@code combat/status_effects.json}. */
public final class StatusEffectManifest {
    public List<StatusEffectDef> negative = new ArrayList<>();
    public List<StatusEffectDef> positive = new ArrayList<>();
}
