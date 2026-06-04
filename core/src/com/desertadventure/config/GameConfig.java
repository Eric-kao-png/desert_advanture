package com.desertadventure.config;

import com.badlogic.gdx.graphics.Color;
import com.desertadventure.config.CombatConfig;
import com.desertadventure.config.PlayerConfig;
import com.desertadventure.config.UiLayoutConfig;

public final class GameConfig {
    // -------------------------------------------------------------------------
    // NOTE: This file is intentionally kept as a compatibility facade.
    // New code should prefer domain-scoped config classes (CombatConfig, PlayerConfig, UiLayoutConfig, ...).
    // -------------------------------------------------------------------------
    public static final int VIEW_WIDTH = UiLayoutConfig.VIEW_WIDTH;
    public static final int VIEW_HEIGHT = UiLayoutConfig.VIEW_HEIGHT;

    /** Warm beige sky; also used for GL clear. */
    public static final Color SKY_BASE_COLOR = new Color(0.96f, 0.90f, 0.78f, 1f);
    public static final String SUN_TEXTURE_PATH = "sprites/sun.png";
    /** Drawn size for 32×32 sun sprite (screen space). */
    public static final float SUN_DISPLAY_SIZE = 112f;
    /** Sun bottom-left corner (fixed, does not scroll). */
    public static final float SUN_MARGIN_LEFT = 64f;
    public static final float SUN_MARGIN_TOP = 48f;
    /** Downward shift for sun (LibGDX Y; negative = lower on screen). */
    public static final float SUN_VERTICAL_OFFSET = -190f;
    public static final float SUN_X = SUN_MARGIN_LEFT;
    public static final float SUN_Y = VIEW_HEIGHT - SUN_MARGIN_TOP - SUN_DISPLAY_SIZE + SUN_VERTICAL_OFFSET;

    /** Event / feedback lines in the bottom-left HUD area. */
    public static final int MESSAGE_FEED_MAX_LINES = UiLayoutConfig.MESSAGE_FEED_MAX_LINES;
    public static final float MESSAGE_FEED_DISPLAY_SECONDS = UiLayoutConfig.MESSAGE_FEED_DISPLAY_SECONDS;
    public static final float MESSAGE_FEED_FADE_SECONDS = UiLayoutConfig.MESSAGE_FEED_FADE_SECONDS;
    public static final float MESSAGE_FEED_X = UiLayoutConfig.MESSAGE_FEED_X;
    public static final float MESSAGE_FEED_BASE_Y = UiLayoutConfig.MESSAGE_FEED_BASE_Y;
    public static final float MESSAGE_FEED_LINE_HEIGHT = UiLayoutConfig.MESSAGE_FEED_LINE_HEIGHT;

    public static final float HUD_LEFT_MARGIN = UiLayoutConfig.HUD_LEFT_MARGIN;
    public static final float HUD_LINE_STEP = UiLayoutConfig.HUD_LINE_STEP;
    public static final float HUD_FONT_SCALE = UiLayoutConfig.HUD_FONT_SCALE;
    public static final float BOSS_HUD_RIGHT_OFFSET = 280f;
    public static final float HUD_STATUS_TOP_OFFSET = 16f;

    /** Logical player size used for bounds / layout (legacy blue rectangle size). */
    public static final float PLAYER_WIDTH = PlayerConfig.WIDTH;
    public static final float PLAYER_HEIGHT = PlayerConfig.HEIGHT;
    public static final float ENEMY_WIDTH = 40f;
    public static final float ENEMY_HEIGHT = 56f;
    public static final float BOSS_WIDTH = 80f;
    public static final float BOSS_HEIGHT = 100f;

    public static final float PLAYER_SPEED = PlayerConfig.SPEED;
    /** Sandbag enemies do not move (MVP). */
    public static final float ENEMY_SPEED = 0f;
    public static final float BOSS_SPEED = 0f;

    public static final float COMBAT_PLAYER_X_RATIO = CombatConfig.COMBAT_PLAYER_X_RATIO;
    public static final float COMBAT_ENEMY_X_RATIO = CombatConfig.COMBAT_ENEMY_X_RATIO;
    public static final float COMBAT_BOSS_X_RATIO = CombatConfig.COMBAT_BOSS_X_RATIO;
    /** Normal enemy HP rolled uniformly in [min, max] at combat start. */
    public static final int ENEMY_HP_MIN = CombatConfig.ENEMY_HP_MIN;
    public static final int ENEMY_HP_MAX = CombatConfig.ENEMY_HP_MAX;
    public static final float BOSS_BASE_HP = CombatConfig.BOSS_BASE_HP;
    public static final float BOSS_HP_PER_DISTANCE_BAND = CombatConfig.BOSS_HP_PER_DISTANCE_BAND;
    /** HP bar above combat entity sprites (width = max(entity width × scale, min width)). */
    public static final float COMBAT_HP_BAR_HEIGHT = 12f;
    public static final float COMBAT_HP_BAR_GAP = 8f;
    /** Gap between opponent name label and the top of the overhead bar stack. */
    public static final float COMBAT_OPPONENT_NAME_GAP = 4f;
    public static final float COMBAT_SHIELD_BAR_HEIGHT = 10f;
    /** Vertical gap between the top of the HP bar and the bottom of the shield bar. */
    public static final float COMBAT_SHIELD_BAR_GAP = 4f;
    /** Shield bar fill at 100% when current shield reaches this value. */
    public static final float COMBAT_SHIELD_BAR_DISPLAY_MAX = 12f;
    public static final float COMBAT_HP_BAR_WIDTH_SCALE = 2.4f;
    public static final float COMBAT_HP_BAR_MIN_WIDTH = 96f;
    /** Gap between numeric HP label and the left edge of the bar. */
    public static final float COMBAT_HP_BAR_TEXT_GAP = 6f;
    public static final float COMBAT_HP_BAR_BORDER_WIDTH = 2.5f;
    /** Max HP at or below this uses a single current-HP digit on the bar. */
    public static final float COMBAT_HP_BAR_COMPACT_MAX_HP = 3f;
    /** Gap from entity feet (bottom Y) down to the bottom of the status panels. */
    public static final float COMBAT_STATUS_BELOW_FEET_GAP = 4f;
    /** Horizontal gap between entity center X and the inner edge of each status column. */
    public static final float COMBAT_STATUS_COLUMN_OFFSET = 6f;
    public static final float COMBAT_STATUS_PANEL_PADDING_H = 5f;
    public static final float COMBAT_STATUS_PANEL_PADDING_V = 3f;
    public static final float COMBAT_STATUS_BORDER_WIDTH = 1.5f;

    // --- Turn-based action cards ---
    public static final int CARD_ATTACK_DAMAGE = CombatConfig.CARD_ATTACK_DAMAGE;
    public static final int CARD_STRONG_ATTACK_DAMAGE = CombatConfig.CARD_STRONG_ATTACK_DAMAGE;
    public static final int CARD_HEAL_AMOUNT = CombatConfig.CARD_HEAL_AMOUNT;
    public static final int CARD_ATTACK_COOLDOWN_TURNS = CombatConfig.CARD_ATTACK_COOLDOWN_TURNS;
    public static final int CARD_STRONG_ATTACK_COOLDOWN_TURNS = CombatConfig.CARD_STRONG_ATTACK_COOLDOWN_TURNS;
    public static final int CARD_HEAL_COOLDOWN_TURNS = CombatConfig.CARD_HEAL_COOLDOWN_TURNS;

    public static final int CARD_SHIELD_AMOUNT = CombatConfig.CARD_SHIELD_AMOUNT;
    public static final int CARD_SHIELD_COOLDOWN_TURNS = CombatConfig.CARD_SHIELD_COOLDOWN_TURNS;

    public static final int CARD_FULL_POWER_DAMAGE_LOW = CombatConfig.CARD_FULL_POWER_DAMAGE_LOW;
    public static final int CARD_FULL_POWER_DAMAGE_HIGH = CombatConfig.CARD_FULL_POWER_DAMAGE_HIGH;
    public static final int CARD_FULL_POWER_COOLDOWN_TURNS = CombatConfig.CARD_FULL_POWER_COOLDOWN_TURNS;

    public static final int CARD_LIFE_MAGIC_COOLDOWN_TURNS = CombatConfig.CARD_LIFE_MAGIC_COOLDOWN_TURNS;

    public static final int CARD_THRUST_DAMAGE_ROUND_ONE = CombatConfig.CARD_THRUST_DAMAGE_ROUND_ONE;
    public static final int CARD_THRUST_DAMAGE_OTHER = CombatConfig.CARD_THRUST_DAMAGE_OTHER;
    public static final int CARD_THRUST_COOLDOWN_TURNS = CombatConfig.CARD_THRUST_COOLDOWN_TURNS;

    public static final int CARD_POISON_DURATION_TURNS = CombatConfig.CARD_POISON_DURATION_TURNS;
    public static final int CARD_POISON_DAMAGE_PER_ROUND = CombatConfig.CARD_POISON_DAMAGE_PER_ROUND;
    public static final int CARD_POISON_COOLDOWN_TURNS = CombatConfig.CARD_POISON_COOLDOWN_TURNS;

    /** Player slot pair roll weights per round (higher = more likely). */
    public static final int COMBAT_PLAYER_SLOTS_WEIGHT_13 = CombatConfig.PLAYER_SLOTS_WEIGHT_13;
    public static final int COMBAT_PLAYER_SLOTS_WEIGHT_24 = CombatConfig.PLAYER_SLOTS_WEIGHT_24;
    public static final int COMBAT_PLAYER_SLOTS_WEIGHT_12 = CombatConfig.PLAYER_SLOTS_WEIGHT_12;
    public static final int COMBAT_PLAYER_SLOTS_WEIGHT_34 = CombatConfig.PLAYER_SLOTS_WEIGHT_34;

    public static final float COMBAT_SLOT_WIDTH = 84f;
    public static final float COMBAT_SLOT_HEIGHT = 108f;
    /** Timeline row between fighters, biased above screen center. */
    public static final float COMBAT_SLOT_Y = 340f;
    public static final float COMBAT_SLOT_GAP = 20f;
    /** Hand row at the bottom, visually under the ground plane. */
    public static final float COMBAT_HAND_Y = 12f;
    public static final float COMBAT_CARD_WIDTH = 72f;
    public static final float COMBAT_CARD_HEIGHT = 90f;
    public static final float COMBAT_HAND_GAP = 10f;
    /** Hand row viewport: horizontal inset from screen edge. */
    public static final float COMBAT_HAND_VIEWPORT_MARGIN_H = 16f;
    /** Gap between each hand zone panel and the centered confirm button. */
    public static final float COMBAT_HAND_CENTER_GAP = 12f;
    /** Padding inside the hand frame, around cards. */
    public static final float COMBAT_HAND_VIEWPORT_PADDING = 8f;
    /** Border stroke width for the hand viewport frame. */
    public static final float COMBAT_HAND_BORDER = 2f;
    /** Min pointer movement (world px) before a hand press becomes scroll. */
    public static final float COMBAT_HAND_SCROLL_THRESHOLD = 10f;
    public static final float COMBAT_CONFIRM_WIDTH = 130f;
    public static final float COMBAT_CONFIRM_HEIGHT = 40f;
    /** Time between slot resolves. */
    public static final float COMBAT_RESOLVE_SLOT_SECONDS = CombatConfig.RESOLVE_SLOT_SECONDS;
    public static final float COMBAT_LAYOUT_BLEND_SECONDS = CombatConfig.LAYOUT_BLEND_SECONDS;
    public static final float COMBAT_GROUND_Y = CombatConfig.COMBAT_GROUND_Y;
    public static final int VICTORY_EXPERIENCE = CombatConfig.VICTORY_EXPERIENCE;

    public static final int PLAYER_INITIAL_LEVEL = PlayerConfig.INITIAL_LEVEL;
    public static final int PLAYER_INITIAL_EXPERIENCE_TO_NEXT = PlayerConfig.INITIAL_EXPERIENCE_TO_NEXT;
    public static final float PLAYER_INITIAL_MAX_HP = PlayerConfig.INITIAL_MAX_HP;
    public static final int PLAYER_INITIAL_ATTACK = PlayerConfig.INITIAL_ATTACK;
    public static final int PLAYER_INITIAL_DEFENSE = PlayerConfig.INITIAL_DEFENSE;
    public static final float PLAYER_LEVEL_HP_GAIN = PlayerConfig.LEVEL_HP_GAIN;
    public static final int PLAYER_LEVEL_ATTACK_GAIN = PlayerConfig.LEVEL_ATTACK_GAIN;
    public static final int PLAYER_LEVEL_DEFENSE_GAIN = PlayerConfig.LEVEL_DEFENSE_GAIN;
    public static final float PLAYER_LEVEL_EXP_MULTIPLIER = PlayerConfig.LEVEL_EXP_MULTIPLIER;

    // --- Victory screen ---
    public static final float VICTORY_TITLE_FONT_SCALE = 1.8f;
    public static final float VICTORY_BODY_FONT_SCALE = 1.1f;
    public static final float VICTORY_TITLE_Y_RATIO = 0.7f;
    public static final float VICTORY_SUBTITLE_Y_RATIO = 0.45f;
    public static final float VICTORY_PROMPT_Y_RATIO = 0.3f;
    public static final float VICTORY_BADGE_Y_RATIO = 0.55f;
    public static final float VICTORY_BADGE_RADIUS = 80f;

    private GameConfig() {
    }
}
