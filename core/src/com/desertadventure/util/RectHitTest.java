package com.desertadventure.util;

/** Axis-aligned rectangle hit tests in world/screen space. */
public final class RectHitTest {
    private RectHitTest() {
    }

    public static boolean contains(float worldX, float worldY, float rectX, float rectY, float width, float height) {
        return worldX >= rectX && worldX <= rectX + width && worldY >= rectY && worldY <= rectY + height;
    }
}
