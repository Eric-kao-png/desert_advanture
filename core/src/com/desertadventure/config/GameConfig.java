package com.desertadventure.config;

import com.badlogic.gdx.graphics.Color;

public final class GameConfig {
    public static final int VIEW_WIDTH = 1280;
    public static final int VIEW_HEIGHT = 720;

    /** Warm beige sky behind parallax; also used for GL clear. */
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

    public static final int BASE_STEP_BUDGET = 12;
    public static final float TILE_TRAVEL_SECONDS = 0.4f;
    public static final float SCROLL_SPEED = 200f;
    /** Min step delta applied per travel update tick. */
    public static final float STEP_CONSUME_EPSILON = 1e-5f;
    /** Remaining plan distance treated as zero. */
    public static final float PLAN_DISTANCE_EPSILON = 1e-4f;
    /** Resume segment length below which travel completes immediately. */
    public static final float PATH_REMAINING_EPSILON = 0.01f;
    /** Logical scroll offset added per unit of path distance on segment complete. */
    public static final float SCROLL_OFFSET_PER_STEP = 50f;
    /** Parallax multipliers vs SCROLL_SPEED while moving (back slowest, forward fastest). */
    public static final float PARALLAX_BACK_MULT = 0.25f;
    public static final float PARALLAX_MIDDLE_MULT = 0.55f;
    public static final float PARALLAX_FORWARD_MULT = 1f;
    /** Base vertical shift shared by parallax layers (LibGDX Y; negative = lower on screen). */
    public static final float PARALLAX_VERTICAL_OFFSET = -40f;
    /** Extra shift for back parallax (added to {@link #PARALLAX_VERTICAL_OFFSET}). */
    public static final float PARALLAX_BACK_EXTRA_OFFSET = -150f;
    /** Extra shift for middle parallax and house spawn Y (added to {@link #PARALLAX_VERTICAL_OFFSET}). */
    public static final float PARALLAX_MIDDLE_EXTRA_OFFSET = -40f;
    /** Extra shift for forward parallax only (added to {@link #PARALLAX_VERTICAL_OFFSET}). */
    public static final float PARALLAX_FORWARD_EXTRA_OFFSET = -30f;
    /** Parallax speed for house props (drawn between back and middle layers). */
    public static final float HOUSE_PROP_PARALLAX_MULT = (PARALLAX_BACK_MULT + PARALLAX_MIDDLE_MULT) * 0.5f;
    /** Chance [0,1] to spawn when a spawn attempt is made. */
    public static final float HOUSE_SPAWN_CHANCE = 0.22f;
    /** Random scroll gap between spawn attempts while running. */
    public static final float HOUSE_SPAWN_GAP_MIN = 520f;
    public static final float HOUSE_SPAWN_GAP_MAX = 880f;
    /** Max houses whose horizontal bounds may overlap at once. */
    public static final int HOUSE_OVERLAP_MAX = 2;
    /** Random spawn attempts when repopulating (game start / sandstorm). */
    public static final int HOUSE_REPOPULATE_ATTEMPTS = 4;
    /** Horizontal jitter added to spawn scroll position while running. */
    public static final float HOUSE_SPAWN_JITTER = 200f;
    public static final float HOUSE_SPAWN_SCREEN_MARGIN = 180f;
    /** Screen Y range for house bases (bottom of sprite); includes parallax vertical offsets. */
    public static final float HOUSE_PROP_MIN_Y = 340f + PARALLAX_VERTICAL_OFFSET + PARALLAX_MIDDLE_EXTRA_OFFSET;
    public static final float HOUSE_PROP_MAX_Y = 390f + PARALLAX_VERTICAL_OFFSET + PARALLAX_MIDDLE_EXTRA_OFFSET;
    /** Drawn height for house sprites (width follows aspect ratio). */
    public static final float HOUSE_PROP_DISPLAY_HEIGHT = 112f;

    public static final int REQUIRED_EVENT_COUNT = 3;
    public static final int MAP_SIZE = 501;
    /** Visible tiles per side on the map overlay (world is MAP_SIZE x MAP_SIZE). */
    public static final int MAP_VIEW_TILES = 51;
    public static final int MAP_PAN_STEP = 8;
    public static final long MAP_GENERATOR_SEED = 42L;
    public static final int MAP_BLOCKED_DENSITY_PERCENT = 4;
    public static final int MAP_CAMP_CLEAR_RADIUS = 2;
    public static final int MAP_MIN_INTERACTABLE_DISTANCE = 8;
    public static final int MAP_COMBAT_ROLL_THRESHOLD = 12;
    public static final int MAP_ITEM_ROLL_THRESHOLD = 22;
    public static final int MAP_SCATTER_ROLL_DENOMINATOR = 1000;
    public static final int MAP_DISTANCE_BAND_DIVISOR = 3;

    /** Event / feedback lines stacked above the bottom-left control hint. */
    public static final int MESSAGE_FEED_MAX_LINES = 5;
    public static final float MESSAGE_FEED_DISPLAY_SECONDS = 4f;
    public static final float MESSAGE_FEED_FADE_SECONDS = 1f;
    public static final float MESSAGE_FEED_X = 16f;
    public static final float MESSAGE_FEED_BASE_Y = 66f;
    public static final float MESSAGE_FEED_LINE_HEIGHT = 22f;

    public static final float HUD_LEFT_MARGIN = 16f;
    public static final float HUD_BOTTOM_HINT_Y = 40f;
    public static final float HUD_LINE_STEP = 24f;
    public static final float HUD_FONT_SCALE = 1.1f;
    public static final float BOSS_HUD_RIGHT_OFFSET = 280f;
    public static final float STORM_TITLE_Y_RATIO = 0.55f;

    public static final float EXPLORE_GROUND_Y = 120f;
    public static final float EXPLORE_PLAYER_X_RATIO = 0.35f;
    public static final float HUD_TOP_BAR_HEIGHT = 60f;
    public static final float HUD_TOP_BAR_ALPHA = 0.35f;
    public static final float PLAYER_BOB_AMPLITUDE = 4f;
    public static final double PLAYER_BOB_FREQUENCY = 0.02;
    public static final float STORM_OVERLAY_ALPHA_SCALE = 0.85f;
    public static final float MAP_OVERLAY_DIM_ALPHA = 0.65f;
    public static final float CHARACTER_PANEL_WIDTH = 920f;
    public static final float CHARACTER_PANEL_HEIGHT = 520f;
    public static final float CHARACTER_PANEL_LINE_HEIGHT = 32f;
    public static final float CHARACTER_PANEL_PADDING = 24f;
    public static final float CHARACTER_PANEL_COLUMN_GAP = 16f;
    public static final float CHARACTER_PANEL_BG_ALPHA = 0.92f;
    public static final float CHARACTER_STAT_BAR_HEIGHT = 20f;
    public static final float CHARACTER_STAT_BAR_SECTION_GAP = 20f;
    public static final int INVENTORY_SLOT_COUNT = 12;
    /** Backpack grid in the left column (3×4). */
    public static final int INVENTORY_GRID_COLS = 3;
    public static final float INVENTORY_SLOT_SIZE = 64f;
    public static final float INVENTORY_SLOT_GAP = 10f;
    /** Min pointer movement (world px) before a press becomes a drag. */
    public static final float INVENTORY_DRAG_THRESHOLD = 10f;
    public static final float INVENTORY_DETAIL_WIDTH = 360f;
    public static final float INVENTORY_DETAIL_HEIGHT = 300f;
    public static final float INVENTORY_DETAIL_SPRITE_SIZE = 96f;
    public static final float INVENTORY_BUTTON_WIDTH = 120f;
    public static final float INVENTORY_BUTTON_HEIGHT = 36f;
    public static final float INVENTORY_BUTTON_GAP = 16f;
    public static final float INVENTORY_SLOT_ICON_SCALE = 0.88f;
    public static final float INVENTORY_DRAG_ICON_SCALE = 0.92f;
    public static final float INVENTORY_DRAG_ICON_ALPHA = 0.9f;
    public static final float CHARACTER_DETAIL_DIM_ALPHA = 0.55f;
    public static final float CHARACTER_STAT_BAR_INSET = 12f;
    public static final float CHARACTER_TITLE_LINE_MULT = 1.15f;
    public static final float CHARACTER_SECTION_LABEL_OFFSET_MULT = 0.25f;
    public static final float CHARACTER_INVENTORY_LABEL_GAP_MULT = 1.05f;
    public static final float CHARACTER_STAT_LABEL_ABOVE_BAR_MULT = 0.85f;
    public static final float CHARACTER_COMBAT_BLOCK_MULT = 1.95f;
    public static final float CHARACTER_PORTRAIT_TOP_MULT = 0.35f;
    public static final float CHARACTER_COLUMN_DIVIDER_WIDTH = 2f;
    public static final float CHARACTER_COLUMN_DIVIDER_TOP_MULT = 0.35f;
    public static final float CHARACTER_DETAIL_BUTTON_ROW_Y = 24f;
    public static final float CHARACTER_DETAIL_TEXT_PAD = 20f;
    public static final float CHARACTER_DETAIL_SPRITE_BOTTOM_OFFSET = 56f;
    public static final float CHARACTER_DETAIL_NAME_Y_OFFSET = 132f;
    public static final float CHARACTER_DETAIL_DESC_Y_OFFSET = 168f;
    public static final float CHARACTER_BAR_VALUE_TEXT_PAD = 6f;
    public static final float CHARACTER_SLOT_INSET = 2f;
    public static final float CHARACTER_SLOT_BORDER_WIDTH = 2f;
    public static final float HUD_STATUS_TOP_OFFSET = 16f;
    public static final int ITEM_SPRITE_FRAME_SIZE = 64;
    public static final float ITEM_SPRITE_FPS = 12f;
    public static final float MAP_CELL_INSET = 1f;
    public static final float MAP_PLAYER_MARKER_INSET = 2f;
    public static final float MAP_PLAYER_MARKER_SHRINK = 5f;

    public static final float PLAYER_WIDTH = 48f;
    public static final float PLAYER_HEIGHT = 72f;
    public static final float ENEMY_WIDTH = 40f;
    public static final float ENEMY_HEIGHT = 56f;
    public static final float BOSS_WIDTH = 80f;
    public static final float BOSS_HEIGHT = 100f;

    public static final float PLAYER_SPEED = 280f;
    /** Sandbag enemies do not move (MVP). */
    public static final float ENEMY_SPEED = 0f;
    public static final float BOSS_SPEED = 0f;

    public static final float BASIC_ATTACK_COOLDOWN = 0.35f;
    public static final float SKILL_COOLDOWN = 2.5f;
    public static final float ULTIMATE_COOLDOWN = 8f;
    /** Forward hitbox depth (player attacks to the right). */
    public static final float BASIC_ATTACK_DEPTH = 56f;
    public static final float BASIC_ATTACK_HEIGHT = 52f;
    public static final float BASIC_ATTACK_RANGE = 70f;
    public static final float SKILL_RANGE = 120f;
    public static final float ULTIMATE_RANGE = 160f;

    public static final float COMBAT_PLAYER_X_RATIO = 0.3f;
    public static final float COMBAT_ENEMY_X_RATIO = 0.72f;
    public static final float COMBAT_BOSS_X_RATIO = 0.78f;
    public static final float ENEMY_BASE_HP = 30f;
    public static final float ENEMY_HP_PER_DISTANCE_BAND = 10f;
    public static final float BOSS_BASE_HP = 300f;
    public static final float BOSS_HP_PER_DISTANCE_BAND = 40f;
    public static final int SKILL_ATTACK_BONUS = 15;
    public static final int ULTIMATE_ATTACK_BONUS = 50;
    public static final int VICTORY_EXPERIENCE = 20;
    public static final int ITEM_EXPERIENCE = 10;

    /** Chance [0,1] to find an item when entering an ITEM tile (once per cycle per tile). */
    public static final float ITEM_TILE_DROP_CHANCE = 0.95f;
    /** Loot weights: health potion, stamina potion, health gem, stamina gem. */
    public static final int[] ITEM_DROP_WEIGHTS = {35, 35, 15, 15};
    public static final float ITEM_HEALTH_POTION_RESTORE = 50f;
    public static final float ITEM_STAMINA_POTION_RESTORE = 5f;
    public static final float ITEM_HEALTH_GEM_BONUS = 50f;
    public static final float ITEM_STAMINA_GEM_BONUS = 5f;

    public static final int PLAYER_INITIAL_LEVEL = 1;
    public static final int PLAYER_INITIAL_EXPERIENCE_TO_NEXT = 30;
    public static final float PLAYER_INITIAL_MAX_HP = 100f;
    public static final int PLAYER_INITIAL_ATTACK = 10;
    public static final int PLAYER_INITIAL_DEFENSE = 2;
    public static final float PLAYER_LEVEL_HP_GAIN = 15f;
    public static final int PLAYER_LEVEL_ATTACK_GAIN = 3;
    public static final int PLAYER_LEVEL_DEFENSE_GAIN = 1;
    public static final int PLAYER_LEVEL_STEP_BONUS = 1;
    public static final float PLAYER_LEVEL_EXP_MULTIPLIER = 1.4f;

    public static final float STORM_FADE_SECONDS = 2f;

    private GameConfig() {
    }
}
