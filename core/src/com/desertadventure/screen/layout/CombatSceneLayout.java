package com.desertadventure.screen.layout;

import com.desertadventure.config.GameConfig;

/** Fixed combat UI anchors (no hub/combat blend). */
public final class CombatSceneLayout {
    private CombatSceneLayout() {
    }

    public static float groundY() {
        return GameConfig.COMBAT_GROUND_Y;
    }

    public static float handY() {
        return GameConfig.COMBAT_HAND_Y;
    }

    public static float slotY() {
        return GameConfig.COMBAT_SLOT_Y;
    }

    public static float slotWidth() {
        return GameConfig.COMBAT_SLOT_WIDTH;
    }

    public static float slotHeight() {
        return GameConfig.COMBAT_SLOT_HEIGHT;
    }

    public static float cardWidth() {
        return GameConfig.COMBAT_CARD_WIDTH;
    }

    public static float cardHeight() {
        return GameConfig.COMBAT_CARD_HEIGHT;
    }

    public static float entityGroundY() {
        return groundY();
    }
}
