package com.desertadventure.config;

import com.badlogic.gdx.graphics.Color;

/** Shared UI palette for overlays and the character panel. */
public final class UiColors {
    public static final Color SECTION_LABEL = new Color(0.85f, 0.75f, 0.55f, 1f);
    public static final Color MUTED_TEXT = new Color(0.45f, 0.42f, 0.38f, 1f);
    public static final Color BAR_VALUE_TEXT = new Color(0.95f, 0.95f, 0.95f, 1f);
    public static final Color DETAIL_BODY_TEXT = new Color(0.82f, 0.82f, 0.82f, 1f);
    public static final Color BUTTON_LABEL = new Color(0.9f, 0.9f, 0.9f, 1f);

    public static final Color PANEL_FILL = new Color(0.12f, 0.11f, 0.1f, GameConfig.CHARACTER_PANEL_BG_ALPHA);
    public static final Color COLUMN_DIVIDER = new Color(0.35f, 0.32f, 0.28f, 0.9f);
    public static final Color PORTRAIT_FILL = new Color(0.09f, 0.08f, 0.07f, 0.85f);
    public static final Color PORTRAIT_BORDER = new Color(0.45f, 0.4f, 0.34f, 1f);

    public static final Color SLOT_INNER_FILL = new Color(0.08f, 0.07f, 0.06f, 0.75f);
    public static final Color SLOT_BORDER_DEFAULT = new Color(0.28f, 0.26f, 0.22f, 1f);
    public static final Color SLOT_BORDER_HOVERED = new Color(0.55f, 0.5f, 0.42f, 1f);
    public static final Color SLOT_BORDER_SELECTED = new Color(0.95f, 0.8f, 0.35f, 1f);
    public static final Color SLOT_BORDER_DROP_TARGET = new Color(0.55f, 0.85f, 0.45f, 1f);

    public static final Color BAR_BORDER = new Color(0.5f, 0.45f, 0.38f, 1f);
    public static final Color HP_BAR_BG = new Color(0.22f, 0.1f, 0.1f, 0.95f);
    public static final Color HP_BAR_FILL = new Color(0.82f, 0.22f, 0.2f, 1f);
    public static final Color STAMINA_BAR_BG = new Color(0.14f, 0.16f, 0.1f, 0.95f);
    public static final Color STAMINA_BAR_FILL = new Color(0.78f, 0.72f, 0.28f, 1f);

    public static final Color DETAIL_DIM = new Color(0f, 0f, 0f, GameConfig.CHARACTER_DETAIL_DIM_ALPHA);
    public static final Color DETAIL_PANEL_FILL = new Color(0.1f, 0.09f, 0.08f, 0.98f);
    public static final Color DETAIL_PANEL_BORDER = new Color(0.75f, 0.65f, 0.4f, 1f);
    public static final Color BUTTON_FILL = new Color(0.32f, 0.28f, 0.2f, 1f);
    public static final Color BUTTON_FILL_HOVERED = new Color(0.45f, 0.4f, 0.28f, 1f);
    public static final Color BUTTON_BORDER = new Color(0.7f, 0.62f, 0.4f, 1f);

    public static final Color PLAYER_BODY = new Color(0.2f, 0.5f, 0.95f, 1f);
    public static final Color EXPLORE_PLAYER = PLAYER_BODY;
    public static final Color STORM_TINT = new Color(0.9f, 0.75f, 0.35f, 1f);
    public static final Color HUD_TOP_BAR = new Color(0f, 0f, 0f, GameConfig.HUD_TOP_BAR_ALPHA);
    public static final Color HUD_STORM_TITLE = new Color(0.2f, 0.15f, 0.05f, 1f);
    public static final Color MENU_SKY_CLEAR = new Color(0.75f, 0.6f, 0.35f, 1f);
    public static final Color MENU_SKY_BAND = new Color(0.45f, 0.65f, 0.95f, 1f);
    public static final Color MENU_GROUND_BAND = new Color(0.85f, 0.72f, 0.42f, 1f);
    public static final Color VICTORY_CLEAR = new Color(0.2f, 0.15f, 0.35f, 1f);
    public static final Color VICTORY_BADGE = new Color(0.95f, 0.85f, 0.3f, 1f);

    private UiColors() {
    }
}
