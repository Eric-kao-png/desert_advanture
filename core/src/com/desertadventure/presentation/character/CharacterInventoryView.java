package com.desertadventure.presentation.character;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameMessages;
import com.desertadventure.config.UiColors;
import com.desertadventure.item.Inventory;
import com.desertadventure.item.ItemType;
import com.desertadventure.presentation.ShapeDrawer;
import com.desertadventure.presentation.sprites.ItemSpriteRegistry;
import com.desertadventure.screen.CharacterOverlayInput;
import com.desertadventure.screen.CharacterOverlayLayout;
import com.desertadventure.screen.layout.InventoryGridLayout;

/** Backpack grid frames, icons, and drag ghost. */
public final class CharacterInventoryView {
    private final ItemSpriteRegistry itemSprites;

    public CharacterInventoryView(ItemSpriteRegistry itemSprites) {
        this.itemSprites = itemSprites;
    }

    public void drawFrames(ShapeRenderer shapes, CharacterOverlayLayout layout, CharacterOverlayInput input) {
        InventoryGridLayout grid = layout.grid();
        float slotSize = grid.slotSize;
        float inset = GameConfig.CHARACTER_SLOT_INSET;
        for (int i = 0; i < grid.slotCount; i++) {
            float left = grid.slotLeft(i);
            float bottom = grid.slotBottom(i);
            ShapeDrawer.fillRect(shapes, left + inset, bottom + inset,
                    slotSize - inset * 2f, slotSize - inset * 2f, UiColors.SLOT_INNER_FILL);
            ShapeDrawer.strokeRect(shapes, left, bottom, slotSize, slotSize,
                    borderColor(grid, input, i), GameConfig.CHARACTER_SLOT_BORDER_WIDTH);
        }
    }

    public void drawLabelsAndIcons(
            SpriteBatch batch,
            BitmapFont font,
            CharacterOverlayLayout layout,
            Inventory inventory,
            CharacterOverlayInput input,
            float animTime) {
        InventoryGridLayout grid = layout.grid();
        font.setColor(UiColors.SECTION_LABEL);
        font.draw(batch, GameMessages.INVENTORY_SECTION, layout.inventoryTitleX, layout.inventoryTitleY);

        float slotDrawSize = grid.slotSize * GameConfig.INVENTORY_SLOT_ICON_SCALE;
        batch.setColor(Color.WHITE);
        for (int i = 0; i < grid.slotCount; i++) {
            if (input.isDragging() && i == input.getDragSourceSlot()) {
                continue;
            }
            ItemType type = inventory.getItemAt(i);
            if (type == null) {
                continue;
            }
            drawIcon(batch, grid, type, i, slotDrawSize, animTime);
        }
        drawDragGhost(batch, grid, input, animTime);
    }

    private void drawDragGhost(
            SpriteBatch batch,
            InventoryGridLayout grid,
            CharacterOverlayInput input,
            float animTime) {
        if (!input.isDragging() || input.getDragItem() == null) {
            return;
        }
        float size = grid.slotSize * GameConfig.INVENTORY_DRAG_ICON_SCALE;
        float iconX = input.getDragX() - size / 2f;
        float iconY = input.getDragY() - size / 2f;
        TextureRegion frame = itemSprites.getFrame(input.getDragItem(), animTime);
        batch.setColor(1f, 1f, 1f, GameConfig.INVENTORY_DRAG_ICON_ALPHA);
        batch.draw(frame, iconX, iconY, size, size);
        batch.setColor(Color.WHITE);
    }

    private void drawIcon(
            SpriteBatch batch,
            InventoryGridLayout grid,
            ItemType type,
            int slotIndex,
            float drawSize,
            float animTime) {
        float left = grid.slotLeft(slotIndex);
        float bottom = grid.slotBottom(slotIndex);
        float iconX = left + (grid.slotSize - drawSize) / 2f;
        float iconY = bottom + (grid.slotSize - drawSize) / 2f;
        TextureRegion frame = itemSprites.getFrame(type, animTime);
        batch.draw(frame, iconX, iconY, drawSize, drawSize);
    }

    private static Color borderColor(InventoryGridLayout grid, CharacterOverlayInput input, int slot) {
        if (input.isDragging() && slot == input.getHoveredSlot() && slot != input.getDragSourceSlot()) {
            return UiColors.SLOT_BORDER_DROP_TARGET;
        }
        if (input.hasSelection() && input.getSelectedSlot() == slot) {
            return UiColors.SLOT_BORDER_SELECTED;
        }
        if (slot == input.getHoveredSlot()) {
            return UiColors.SLOT_BORDER_HOVERED;
        }
        return UiColors.SLOT_BORDER_DEFAULT;
    }
}
