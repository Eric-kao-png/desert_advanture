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

    public static final float ENEMY_WIDTH = 120f;
    public static final float ENEMY_HEIGHT = 168f;
    /** Player combat sprite matches normal enemy size. */
    public static final float PLAYER_WIDTH = ENEMY_WIDTH;
    public static final float PLAYER_HEIGHT = ENEMY_HEIGHT;
    public static final float BOSS_WIDTH = 240f;
    public static final float BOSS_HEIGHT = 300f;

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
    public static final float COMBAT_STATUS_BELOW_BAR_GAP = 4f;
    public static final float COMBAT_STATUS_COLUMN_OFFSET = 6f;
    public static final float COMBAT_STATUS_PANEL_PADDING_H = 5f;
    public static final float COMBAT_STATUS_PANEL_PADDING_V = 3f;
    public static final float COMBAT_STATUS_BORDER_WIDTH = 1.5f;

    public static final float COMBAT_SLOT_WIDTH = 84f;
    public static final float COMBAT_SLOT_HEIGHT = 108f;
    public static final float COMBAT_SLOT_GAP = 20f;
    public static final float COMBAT_HAND_BOTTOM_MARGIN = 12f;
    public static final float COMBAT_HAND_ROW_GAP = 8f;
    public static final float COMBAT_SLOT_ABOVE_HAND_GAP = 20f;
    /** Space between slot row top and fighter feet; sized for bars below large sprites. */
    public static final float COMBAT_ENTITY_ABOVE_SLOT_GAP = 96f;
    public static final float COMBAT_CARD_WIDTH = 64f;
    public static final float COMBAT_CARD_HEIGHT = 80f;
    /** Fixed hand-row panel height (independent of card size). */
    public static final float COMBAT_HAND_ROW_VIEWPORT_HEIGHT = 110f;
    public static final float COMBAT_HAND_GAP = 10f;
    public static final float COMBAT_HAND_VIEWPORT_MARGIN_H = 16f;
    /** Max width of each hand row panel (centered); cards scroll inside. */
    public static final float COMBAT_HAND_VIEWPORT_WIDTH = 560f;
    public static final float COMBAT_HAND_VIEWPORT_PADDING = 8f;
    public static final float COMBAT_HAND_BORDER = 2f;
    public static final float COMBAT_HAND_SCROLL_THRESHOLD = 10f;
    public static final float COMBAT_CARD_DRAG_THRESHOLD = 12f;
    public static final float COMBAT_SLOT_DISMISS_SIZE = 24f;
    public static final String COMBAT_SLOT_DISMISS_TEXTURE = "sprites/button_close.png";
    public static final String COMBAT_SLOT_DISMISS_TEXTURE_HOVERED = "sprites/button_close_hovered.png";
    public static final String COMBAT_SLOT_DISMISS_TEXTURE_PRESSED = "sprites/button_close_mark.png";
    public static final float COMBAT_HAND_INFO_GAP = 16f;
    public static final float COMBAT_INFO_PANEL_BORDER = 2f;
    public static final float COMBAT_INFO_PANEL_PADDING = 12f;
    public static final float COMBAT_INFO_LINE_HEIGHT = 22f;
    public static final float COMBAT_INFO_FONT_SCALE = 1.05f;
    public static final float COMBAT_CONFIRM_ABOVE_INFO_GAP = 10f;
    public static final float COMBAT_CONFIRM_WIDTH = 130f;
    public static final float COMBAT_CONFIRM_HEIGHT = 40f;
    public static final float COMBAT_RESOLVE_SLOT_SECONDS = CombatConfig.RESOLVE_SLOT_SECONDS;

    public static final float PLAYER_INITIAL_MAX_HP = PlayerConfig.INITIAL_MAX_HP;

    private GameConfig() {
    }
}
