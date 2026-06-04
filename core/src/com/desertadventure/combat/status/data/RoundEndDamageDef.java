package com.desertadventure.combat.status.data;

/** Round-end damage rule for a status (loaded from JSON). */
public final class RoundEndDamageDef {
    public RoundEndDamageKind kind;
    /** Used when {@link #kind} is {@link RoundEndDamageKind#FIXED}. */
    public Integer amount;
}
