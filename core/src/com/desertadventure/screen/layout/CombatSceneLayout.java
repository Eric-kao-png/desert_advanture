package com.desertadventure.screen.layout;

import com.desertadventure.config.GameConfig;

/** Hub vs combat scene anchors; positions interpolate with layout blend [0, 1]. */
public final class CombatSceneLayout {
    private static final float HUB_GROUND_Y = 120f;
    private static final float HUB_HAND_Y = 48f;
    private static final float HUB_SLOT_Y = 280f;
    private static final float HUB_SLOT_WIDTH = 100f;
    private static final float HUB_SLOT_HEIGHT = 130f;
    private static final float HUB_CARD_WIDTH = 88f;
    private static final float HUB_CARD_HEIGHT = 110f;

    private CombatSceneLayout() {
    }

    public static float groundY(float blend) {
        return lerp(HUB_GROUND_Y, GameConfig.COMBAT_GROUND_Y, blend);
    }

    public static float handY(float blend) {
        return lerp(HUB_HAND_Y, GameConfig.COMBAT_HAND_Y, blend);
    }

    public static float slotY(float blend) {
        return lerp(HUB_SLOT_Y, GameConfig.COMBAT_SLOT_Y, blend);
    }

    public static float slotWidth(float blend) {
        return lerp(HUB_SLOT_WIDTH, GameConfig.COMBAT_SLOT_WIDTH, blend);
    }

    public static float slotHeight(float blend) {
        return lerp(HUB_SLOT_HEIGHT, GameConfig.COMBAT_SLOT_HEIGHT, blend);
    }

    public static float cardWidth(float blend) {
        return lerp(HUB_CARD_WIDTH, GameConfig.COMBAT_CARD_WIDTH, blend);
    }

    public static float cardHeight(float blend) {
        return lerp(HUB_CARD_HEIGHT, GameConfig.COMBAT_CARD_HEIGHT, blend);
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
