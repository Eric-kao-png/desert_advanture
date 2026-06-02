package com.desertadventure.config;

/** Player tuning constants (stats, speed, logical size). */
public final class PlayerConfig {
    private PlayerConfig() {
    }

    /** Logical player size used for bounds / layout (legacy blue rectangle size). */
    public static final float WIDTH = 48f;
    public static final float HEIGHT = 72f;

    public static final float SPEED = 280f;

    public static final int INITIAL_LEVEL = 1;
    public static final int INITIAL_EXPERIENCE_TO_NEXT = 30;
    public static final float INITIAL_MAX_HP = 20f;
    public static final int INITIAL_ATTACK = 10;
    public static final int INITIAL_DEFENSE = 2;

    public static final float LEVEL_HP_GAIN = 2f;
    public static final int LEVEL_ATTACK_GAIN = 3;
    public static final int LEVEL_DEFENSE_GAIN = 1;
    public static final int LEVEL_STEP_BONUS = 1;
    public static final float LEVEL_EXP_MULTIPLIER = 1.4f;
}

