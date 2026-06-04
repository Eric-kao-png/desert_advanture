package com.desertadventure.screen.layout;

import com.desertadventure.config.GameConfig;

/**
 * Combat UI stacked bottom-to-top: two hand rows, four slots, then fighters.
 * LibGDX Y increases upward; {@link #changeHandViewportY()} is the bottom hand row.
 */
public final class CombatSceneLayout {
    private CombatSceneLayout() {
    }

    /** Bottom hand row viewport origin Y. */
    public static float changeHandViewportY() {
        return GameConfig.COMBAT_HAND_BOTTOM_MARGIN;
    }

    /** Upper hand row viewport origin Y. */
    public static float attackHandViewportY() {
        return changeHandViewportY()
                + GameConfig.COMBAT_HAND_ROW_VIEWPORT_HEIGHT
                + GameConfig.COMBAT_HAND_ROW_GAP;
    }

    /** Four timeline slots above the hand rows. */
    public static float slotY() {
        return attackHandViewportY()
                + GameConfig.COMBAT_HAND_ROW_VIEWPORT_HEIGHT
                + GameConfig.COMBAT_SLOT_ABOVE_HAND_GAP;
    }

    /** Fighter feet Y (bottom of entity rect), above slots. */
    public static float entityGroundY() {
        return slotY() + slotHeight() + GameConfig.COMBAT_ENTITY_ABOVE_SLOT_GAP;
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

    public static float handRowViewportHeight() {
        return GameConfig.COMBAT_HAND_ROW_VIEWPORT_HEIGHT;
    }
}
