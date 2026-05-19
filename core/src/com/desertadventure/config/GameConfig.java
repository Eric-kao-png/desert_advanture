package com.desertadventure.config;

import com.badlogic.gdx.graphics.Color;

public final class GameConfig {
    public static final int VIEW_WIDTH = 1280;
    public static final int VIEW_HEIGHT = 720;

    /** Warm beige sky behind parallax; also used for GL clear. */
    public static final Color SKY_BASE_COLOR = new Color(0.96f, 0.90f, 0.78f, 1f);
    public static final String SUN_TEXTURE_PATH = "sprites/Sun.png";
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

    public static final float STORM_FADE_SECONDS = 2f;

    private GameConfig() {
    }
}
