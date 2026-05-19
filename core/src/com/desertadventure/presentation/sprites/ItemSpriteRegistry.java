package com.desertadventure.presentation.sprites;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.desertadventure.config.GameConfig;
import com.desertadventure.item.ItemType;

import java.util.EnumMap;
import java.util.Map;

/** Animated icons for inventory items (64x64 frames). */
public final class ItemSpriteRegistry implements Disposable {
    private final Map<ItemType, SpriteSheetAnimation> animations = new EnumMap<>(ItemType.class);

    public ItemSpriteRegistry() {
        for (ItemType type : ItemType.values()) {
            animations.put(type, new SpriteSheetAnimation(
                    type.getSpritePath(),
                    GameConfig.ITEM_SPRITE_FRAME_SIZE,
                    GameConfig.ITEM_SPRITE_FRAME_SIZE,
                    GameConfig.ITEM_SPRITE_FPS));
        }
    }

    public TextureRegion getFrame(ItemType type, float stateTime) {
        SpriteSheetAnimation animation = animations.get(type);
        if (animation == null) {
            throw new IllegalArgumentException("No sprite for item: " + type);
        }
        return animation.getFrame(stateTime);
    }

    @Override
    public void dispose() {
        for (SpriteSheetAnimation animation : animations.values()) {
            animation.dispose();
        }
        animations.clear();
    }
}
