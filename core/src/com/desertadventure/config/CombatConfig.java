package com.desertadventure.config;

/**
 * Combat-specific tuning constants.
 *
 * <p>Kept separate from {@link GameConfig} to avoid a single global constant bucket.</p>
 */
public final class CombatConfig {
    private CombatConfig() {
    }

    public static final float COMBAT_GROUND_Y = 160f;

    public static final float COMBAT_PLAYER_X_RATIO = 0.3f;
    public static final float COMBAT_ENEMY_X_RATIO = 0.72f;
    public static final float COMBAT_BOSS_X_RATIO = 0.78f;

    public static final int ENEMY_HP_MIN = 1;
    public static final int ENEMY_HP_MAX = 3;
    public static final float BOSS_BASE_HP = 35f;
    public static final float BOSS_HP_PER_DISTANCE_BAND = 8f;

    // --- Turn-based action cards ---
    public static final int CARD_ATTACK_DAMAGE = 2;
    public static final int CARD_STRONG_ATTACK_DAMAGE = 3;
    public static final int CARD_HEAL_AMOUNT = 4;
    public static final int CARD_ATTACK_COOLDOWN_TURNS = 1;
    public static final int CARD_STRONG_ATTACK_COOLDOWN_TURNS = 2;
    public static final int CARD_HEAL_COOLDOWN_TURNS = 4;

    public static final int CARD_SHIELD_AMOUNT = 4;
    public static final int CARD_SHIELD_COOLDOWN_TURNS = 3;

    public static final int CARD_FULL_POWER_DAMAGE_LOW = 3;
    public static final int CARD_FULL_POWER_DAMAGE_HIGH = 6;
    public static final int CARD_FULL_POWER_COOLDOWN_TURNS = 3;

    public static final int CARD_LIFE_MAGIC_COOLDOWN_TURNS = 5;

    public static final int CARD_THRUST_DAMAGE_ROUND_ONE = 6;
    public static final int CARD_THRUST_DAMAGE_OTHER = 3;
    public static final int CARD_THRUST_COOLDOWN_TURNS = 3;

    public static final int CARD_POISON_DURATION_TURNS = 2;
    public static final int CARD_POISON_DAMAGE_PER_ROUND = 2;
    public static final int CARD_POISON_COOLDOWN_TURNS = 2;

    /** Player slot pair roll weights per round (higher = more likely). */
    public static final int PLAYER_SLOTS_WEIGHT_13 = 70;
    public static final int PLAYER_SLOTS_WEIGHT_24 = 20;
    public static final int PLAYER_SLOTS_WEIGHT_12 = 5;
    public static final int PLAYER_SLOTS_WEIGHT_34 = 5;

    // --- Combat UI timing ---
    public static final float RESOLVE_SLOT_SECONDS = 0.45f;
    public static final float LAYOUT_BLEND_SECONDS = 0.4f;

    // --- Rewards ---
    public static final int VICTORY_EXPERIENCE = 20;
}

