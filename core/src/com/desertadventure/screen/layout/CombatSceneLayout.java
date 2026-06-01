package com.desertadventure.screen.layout;

import com.desertadventure.config.GameConfig;

/** Combat vs exploration scene anchors; positions interpolate with layout blend [0, 1]. */
public final class CombatSceneLayout {
    private CombatSceneLayout() {
    }

    public static float groundY(float blend) {
        return lerp(GameConfig.EXPLORE_GROUND_Y, GameConfig.COMBAT_GROUND_Y, blend);
    }

    public static float handY(float blend) {
        return lerp(GameConfig.COMBAT_HAND_Y_EXPLORE, GameConfig.COMBAT_HAND_Y, blend);
    }

    public static float slotY(float blend) {
        return lerp(GameConfig.COMBAT_SLOT_Y_EXPLORE, GameConfig.COMBAT_SLOT_Y, blend);
    }

    public static float slotWidth(float blend) {
        return lerp(GameConfig.COMBAT_SLOT_WIDTH_EXPLORE, GameConfig.COMBAT_SLOT_WIDTH, blend);
    }

    public static float slotHeight(float blend) {
        return lerp(GameConfig.COMBAT_SLOT_HEIGHT_EXPLORE, GameConfig.COMBAT_SLOT_HEIGHT, blend);
    }

    public static float cardWidth(float blend) {
        return lerp(GameConfig.COMBAT_CARD_WIDTH_EXPLORE, GameConfig.COMBAT_CARD_WIDTH, blend);
    }

    public static float cardHeight(float blend) {
        return lerp(GameConfig.COMBAT_CARD_HEIGHT_EXPLORE, GameConfig.COMBAT_CARD_HEIGHT, blend);
    }

    public static float entityGroundY(float blend) {
        return groundY(blend);
    }

    private static float lerp(float from, float to, float blend) {
        return from + (to - from) * clamp01(blend);
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
