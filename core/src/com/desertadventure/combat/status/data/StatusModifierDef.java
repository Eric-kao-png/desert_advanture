package com.desertadventure.combat.status.data;

/** Modifier applied when a trigger condition is met (loaded from JSON). */
public final class StatusModifierDef {
    public StatusModifierTriggerId trigger;
    public float add;
    public Float reduce;
    public Boolean blockOffenseHit;
    public Boolean ignoreShieldOnOffense;
    public Float healCasterOnOffense;
    public Float retaliateAttacker;
}
