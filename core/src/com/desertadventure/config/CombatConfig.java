package com.desertadventure.config;

/**
 * Combat-specific tuning constants.
 *
 * <p>Kept separate from {@link GameConfig} to avoid a single global constant bucket.</p>
 */
public final class CombatConfig {
    private CombatConfig() {
    }

    public static final float COMBAT_PLAYER_X_RATIO = 0.3f;
    public static final float COMBAT_ENEMY_X_RATIO = 0.72f;
    public static final float COMBAT_BOSS_X_RATIO = 0.78f;

    public static final float BOSS_BASE_HP = 35f;
    public static final float BOSS_HP_PER_DISTANCE_BAND = 8f;

    /** Player slot pair roll weights per round (higher = more likely). */
    public static final int PLAYER_SLOTS_WEIGHT_13 = 70;
    public static final int PLAYER_SLOTS_WEIGHT_24 = 20;
    public static final int PLAYER_SLOTS_WEIGHT_12 = 5;
    public static final int PLAYER_SLOTS_WEIGHT_34 = 5;

    // --- Combat UI timing ---
    public static final float RESOLVE_SLOT_SECONDS = 0.45f;
}
