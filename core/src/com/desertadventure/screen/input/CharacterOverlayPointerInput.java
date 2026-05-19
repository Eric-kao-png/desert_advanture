package com.desertadventure.screen.input;

import com.badlogic.gdx.Gdx;
import com.desertadventure.config.GameInputBindings;
import com.desertadventure.item.Inventory;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.presentation.OverlayCloseButton;
import com.desertadventure.screen.CharacterOverlayInput;
import com.desertadventure.screen.CharacterOverlayLayout;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

/** Character overlay: inventory drag/tap, item detail, dismiss. */
public final class CharacterOverlayPointerInput {
    private final GameSession session;
    private final GameViewport viewport;
    private final CharacterOverlayInput input;
    private final CharacterOverlayLayout layout;
    private boolean pointerWasDown;

    public CharacterOverlayPointerInput(
            GameSession session,
            GameViewport viewport,
            CharacterOverlayInput input,
            CharacterOverlayLayout layout) {
        this.session = session;
        this.viewport = viewport;
        this.input = input;
        this.layout = layout;
    }

    public void resetPointerTracking() {
        pointerWasDown = false;
    }

    public void handle() {
        if (GameInputBindings.justCloseInventory()) {
            closeOverlay();
            return;
        }

        float worldX = viewport.pointerWorldX();
        float worldY = viewport.pointerWorldY();
        boolean pointerDown = Gdx.input.isTouched();

        updateHover();

        if (pointerDown) {
            if (Gdx.input.justTouched()) {
                handleTouchDown(worldX, worldY);
            } else {
                input.onTouchDrag(worldX, worldY);
            }
        } else if (pointerWasDown) {
            handleTouchUp(worldX, worldY);
        }

        pointerWasDown = pointerDown;
    }

    public void updateHover() {
        if (session.getMode() != GameplayMode.CHARACTER_OVERLAY) {
            return;
        }
        input.updateHover(layout, viewport.pointerWorldX(), viewport.pointerWorldY());
    }

    public boolean isDismissHovered() {
        return input.isHoveredOverlayDismiss();
    }

    public boolean isDismissPressed() {
        return Gdx.input.isTouched() && input.isHoveredOverlayDismiss();
    }

    private void closeOverlay() {
        input.clearSelection();
        input.resetPointer();
        session.closeCharacterOverlay();
    }

    private void handleTouchDown(float worldX, float worldY) {
        if (OverlayCloseButton.contains(worldX, worldY)) {
            closeOverlay();
            return;
        }
        input.resetPointer();
        if (input.hasSelection()) {
            if (layout.detail().closeButtonContains(worldX, worldY)) {
                input.clearSelection();
                return;
            }
            if (layout.detail().useButtonContains(worldX, worldY)) {
                int slot = input.getSelectedSlot();
                session.tryUseInventoryItemAtSlot(slot);
                if (session.getInventory().isSlotEmpty(slot)) {
                    input.clearSelection();
                }
                return;
            }
            if (layout.detail().contains(worldX, worldY)) {
                return;
            }
        }
        input.onTouchDown(worldX, worldY, layout, session.getInventory());
    }

    private void handleTouchUp(float worldX, float worldY) {
        if (input.isDragging()) {
            input.onTouchUp(worldX, worldY, layout, session.getInventory());
            return;
        }

        int tapSlot = input.onTouchUp(worldX, worldY, layout, session.getInventory());
        Inventory inventory = session.getInventory();
        if (tapSlot >= 0) {
            if (!inventory.isSlotEmpty(tapSlot)) {
                input.selectSlot(tapSlot);
            } else {
                input.clearSelection();
            }
            return;
        }

        if (input.hasSelection() && !layout.detail().contains(worldX, worldY)) {
            input.clearSelection();
        }
    }
}
