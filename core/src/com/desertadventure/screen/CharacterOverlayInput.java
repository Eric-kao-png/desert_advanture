package com.desertadventure.screen;

public class CharacterOverlayInput {
    private int hoveredSlot = -1;
    private int selectedSlot = -1;
    private boolean hoveredUse;
    private boolean hoveredClose;

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
}
