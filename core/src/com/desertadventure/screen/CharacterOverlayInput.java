package com.desertadventure.screen;

import com.desertadventure.config.GameConfig;
import com.desertadventure.item.Inventory;
import com.desertadventure.item.ItemType;

/** Pointer state for backpack grid: tap-to-detail and drag-to-reorder. */
public class CharacterOverlayInput {
    private int hoveredSlot = -1;
    private int selectedSlot = -1;
    private boolean hoveredUse;
    private boolean hoveredClose;

    private int pointerDownSlot = -1;
    private float pointerDownX;
    private float pointerDownY;
    private int dragSourceSlot = -1;
    private ItemType dragItem;
    private float dragX;
    private float dragY;
    private boolean dragging;

    public void clearSelection() {
        selectedSlot = -1;
    }

    public void selectSlot(int index) {
        selectedSlot = index;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public boolean hasSelection() {
        return selectedSlot >= 0;
    }

    public void resetPointer() {
        pointerDownSlot = -1;
        dragSourceSlot = -1;
        dragItem = null;
        dragging = false;
    }

    public void onTouchDown(float worldX, float worldY, CharacterOverlayLayout layout, Inventory inventory) {
        pointerDownSlot = layout.slotAt(worldX, worldY);
        pointerDownX = worldX;
        pointerDownY = worldY;
        dragX = worldX;
        dragY = worldY;
        dragging = false;
        if (pointerDownSlot >= 0 && !inventory.isSlotEmpty(pointerDownSlot)) {
            dragSourceSlot = pointerDownSlot;
            dragItem = inventory.getItemAt(pointerDownSlot);
        } else {
            dragSourceSlot = -1;
            dragItem = null;
        }
    }

    public void onTouchDrag(float worldX, float worldY) {
        dragX = worldX;
        dragY = worldY;
        if (dragSourceSlot < 0 || dragging) {
            return;
        }
        float dx = worldX - pointerDownX;
        float dy = worldY - pointerDownY;
        float threshold = GameConfig.INVENTORY_DRAG_THRESHOLD;
        if (dx * dx + dy * dy >= threshold * threshold) {
            dragging = true;
            clearSelection();
        }
    }

    /**
     * @return slot index for a tap (no drag), or -1 if the gesture was a drag or invalid
     */
    public int onTouchUp(float worldX, float worldY, CharacterOverlayLayout layout, Inventory inventory) {
        dragX = worldX;
        dragY = worldY;
        int tapSlot = -1;
        if (dragging && dragSourceSlot >= 0) {
            int targetSlot = layout.slotAt(worldX, worldY);
            if (targetSlot >= 0) {
                inventory.swapSlots(dragSourceSlot, targetSlot);
            }
        } else if (pointerDownSlot >= 0) {
            tapSlot = pointerDownSlot;
        }
        resetPointer();
        return tapSlot;
    }

    public void updateHover(CharacterOverlayLayout layout, float worldX, float worldY) {
        hoveredSlot = layout.slotAt(worldX, worldY);
        hoveredUse = layout.useButtonContains(worldX, worldY);
        hoveredClose = layout.closeButtonContains(worldX, worldY);
    }

    public int getHoveredSlot() {
        return hoveredSlot;
    }

    public boolean isHoveredUse() {
        return hoveredUse;
    }

    public boolean isHoveredClose() {
        return hoveredClose;
    }

    public boolean isDragging() {
        return dragging;
    }

    public int getDragSourceSlot() {
        return dragSourceSlot;
    }

    public ItemType getDragItem() {
        return dragItem;
    }

    public float getDragX() {
        return dragX;
    }

    public float getDragY() {
        return dragY;
    }
}
