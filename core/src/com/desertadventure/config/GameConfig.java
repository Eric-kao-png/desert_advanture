package com.desertadventure.config;

public final class GameConfig {
    public static final int VIEW_WIDTH = 1280;
    public static final int VIEW_HEIGHT = 720;

    public static final int BASE_STEP_BUDGET = 12;
    public static final float TILE_TRAVEL_SECONDS = 0.4f;
    public static final float SCROLL_SPEED = 200f;

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
