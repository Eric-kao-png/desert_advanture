package com.desertadventure.presentation.sprites;

/** JSON root for {@code data/desert_sprite_sheet.json}. */
public class DesertSpriteSheetData {
    public String texture;
    /** Coordinate origin for sprite rects, e.g. {@code bottom-left}. */
    public String origin;
    public SpriteEntry[] sprites;

    public static class SpriteEntry {
        public String name;
        public int left;
        public int bottom;
        public int right;
        public int top;

        public int width() {
            return right - left;
        }

        public int height() {
            return top - bottom;
        }
    }
}
