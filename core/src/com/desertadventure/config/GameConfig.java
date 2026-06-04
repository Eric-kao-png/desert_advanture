package com.desertadventure.config;

import com.badlogic.gdx.graphics.Color;

/**
 * Compatibility facade for layout and combat UI constants.
 * Prefer {@link CombatConfig}, {@link PlayerConfig}, and {@link UiLayoutConfig} in new code.
 */
public final class GameConfig {
    public static final int VIEW_WIDTH = UiLayoutConfig.VIEW_WIDTH;
    public static final int VIEW_HEIGHT = UiLayoutConfig.VIEW_HEIGHT;

    /** Solid gameplay background (GL clear color). */
    public static final Color SKY_BASE_COLOR = new Color(0.96f, 0.90f, 0.78f, 1f);

    public static final float HUD_LEFT_MARGIN = UiLayoutConfig.HUD_LEFT_MARGIN;
    public static final float HUD_LINE_STEP = UiLayoutConfig.HUD_LINE_STEP;
    public static final float HUD_FONT_SCALE = UiLayoutConfig.HUD_FONT_SCALE;

    public static final float PLAYER_WIDTH = PlayerConfig.WIDTH;
    public static final float PLAYER_HEIGHT = PlayerConfig.HEIGHT;
    public static final float ENEMY_WIDTH = 40f;
    public static final float ENEMY_HEIGHT = 56f;
    public static final float BOSS_WIDTH = 80f;
    public static final float BOSS_HEIGHT = 100f;

    public static final float COMBAT_PLAYER_X_RATIO = CombatConfig.COMBAT_PLAYER_X_RATIO;
    public static final float COMBAT_ENEMY_X_RATIO = CombatConfig.COMBAT_ENEMY_X_RATIO;
    public static final float COMBAT_BOSS_X_RATIO = CombatConfig.COMBAT_BOSS_X_RATIO;
    public static final int ENEMY_HP_MIN = CombatConfig.ENEMY_HP_MIN;
    public static final int ENEMY_HP_MAX = CombatConfig.ENEMY_HP_MAX;
    public static final float BOSS_BASE_HP = CombatConfig.BOSS_BASE_HP;
    public static final float BOSS_HP_PER_DISTANCE_BAND = CombatConfig.BOSS_HP_PER_DISTANCE_BAND;

    public static final float COMBAT_HP_BAR_HEIGHT = 12f;
    public static final float COMBAT_HP_BAR_GAP = 8f;
    public static final float COMBAT_OPPONENT_NAME_GAP = 4f;
    public static final float COMBAT_SHIELD_BAR_HEIGHT = 10f;
    public static final float COMBAT_SHIELD_BAR_GAP = 4f;
    public static final float COMBAT_SHIELD_BAR_DISPLAY_MAX = 12f;
    public static final float COMBAT_HP_BAR_WIDTH_SCALE = 2.4f;
    public static final float COMBAT_HP_BAR_MIN_WIDTH = 96f;
    public static final float COMBAT_HP_BAR_TEXT_GAP = 6f;
    public static final float COMBAT_HP_BAR_BORDER_WIDTH = 2.5f;
    public static final float COMBAT_HP_BAR_COMPACT_MAX_HP = 3f;

    public static final float COMBAT_SLOT_WIDTH = 84f;
    public static final float COMBAT_SLOT_HEIGHT = 108f;
    public static final float COMBAT_SLOT_Y = 340f;
    public static final float COMBAT_SLOT_GAP = 20f;
    public static final float COMBAT_HAND_Y = 12f;
    public static final float COMBAT_CARD_WIDTH = 72f;
    public static final float COMBAT_CARD_HEIGHT = 90f;
    public static final float COMBAT_HAND_GAP = 10f;
    public static final float COMBAT_HAND_VIEWPORT_MARGIN_H = 16f;
    public static final float COMBAT_HAND_CENTER_GAP = 12f;
    public static final float COMBAT_HAND_VIEWPORT_PADDING = 8f;
    public static final float COMBAT_HAND_BORDER = 2f;
    public static final float COMBAT_HAND_SCROLL_THRESHOLD = 10f;
    public static final float COMBAT_CONFIRM_WIDTH = 130f;
    public static final float COMBAT_CONFIRM_HEIGHT = 40f;
    public static final float COMBAT_RESOLVE_SLOT_SECONDS = CombatConfig.RESOLVE_SLOT_SECONDS;
    public static final float COMBAT_GROUND_Y = CombatConfig.COMBAT_GROUND_Y;

    public static final float PLAYER_INITIAL_MAX_HP = PlayerConfig.INITIAL_MAX_HP;

    private GameConfig() {
    }
}
