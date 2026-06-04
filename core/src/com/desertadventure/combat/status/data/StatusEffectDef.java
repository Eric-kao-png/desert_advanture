package com.desertadventure.combat.status.data;

import java.util.ArrayList;
import java.util.List;

/** Data definition for a single combat status, loaded from JSON. */
public final class StatusEffectDef {
    public String id;
    public String name;
    public StatusPolarityId polarity;
    public RoundEndDamageDef roundEnd;
    public List<StatusModifierDef> modifiers = new ArrayList<>();
}
