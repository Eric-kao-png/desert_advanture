package com.desertadventure.screen.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.CombatPhase;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameInputBindings;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.screen.layout.CombatCardLayout;
import com.desertadventure.screen.layout.CombatCardLayout.HandZonePanel;
import com.desertadventure.state.GameSession;

/** Turn-based combat: hand selection, drag-to-slot, slot assignment, confirm. */
public final class CombatCardInput {
    private final CombatCardLayout layout = new CombatCardLayout();
    private final Vector2 dragStart = new Vector2();

    private boolean pointerWasDown;
    private boolean handScrollTracking;
    private boolean handScrollActive;
    private float handScrollPointerStartX;
    private float handScrollAtPointerDown;
    private HandZonePanel handScrollPanel;

    private int dragCandidateInstanceId = -1;
    private boolean dragActive;
    private float dragPointerX;
    private float dragPointerY;

    private int hoveredDismissSlot = -1;
    private boolean pressedDismiss;

    private ActionCardType inspectedCardType;
    private ActionCardInstance inspectedCardInstance;

    public CombatCardLayout getLayout() {
        return layout;
    }

    public boolean isDragging() {
        return dragActive;
    }

    public int getDragInstanceId() {
        return dragActive ? dragCandidateInstanceId : -1;
    }

    public float getDragPointerX() {
        return dragPointerX;
    }

    public float getDragPointerY() {
        return dragPointerY;
    }

    public int getHoveredDismissSlot() {
        return hoveredDismissSlot;
    }

    public boolean isPressedDismiss() {
        return pressedDismiss;
    }

    public ActionCardType getInspectedCardType() {
        return inspectedCardType;
    }

    public ActionCardInstance getInspectedCardInstance() {
        return inspectedCardInstance;
    }

    public void handle(GameSession session, GameViewport viewport, float delta) {
        if (!session.getMode().isCombat()) {
            resetNonCombatState();
            return;
        }
        var combat = session.getCombatController();

        float worldX = viewport.pointerWorldX();
        float worldY = viewport.pointerWorldY();

        if (combat.getPhase() != CombatPhase.PLANNING) {
            handleNonPlanningPhase(combat, worldX, worldY);
            return;
        }

        if (handlePlanningHotkeys(combat)) {
            return;
        }

        handlePlanningPointer(session, combat, worldX, worldY);
        rebuildAndUpdateHover(combat, worldX, worldY);
    }

    private void handleNonPlanningPhase(CombatController combat, float worldX, float worldY) {
        boolean pointerDown = Gdx.input.isTouched();
        if (!pointerDown && pointerWasDown) {
            inspectCardAtPointer(combat, worldX, worldY);
        }
        pointerWasDown = pointerDown;
        resetDragGesture();
        resetHandScrollGesture();
        rebuildAndUpdateHover(combat, worldX, worldY);
    }

    private boolean handlePlanningHotkeys(CombatController combat) {
        if (!GameInputBindings.justConfirmed()) {
            return false;
        }
        combat.confirmPlanning();
        return true;
    }

    private void handlePlanningPointer(GameSession session, CombatController combat, float worldX, float worldY) {
        boolean pointerDown = Gdx.input.isTouched();

        if (pointerDown) {
            if (Gdx.input.justTouched()) {
                beginPointerGesture(combat, worldX, worldY);
            } else {
                updatePointerGesture(combat, worldX, worldY);
            }
        } else if (pointerWasDown) {
            endPointerGesture(session, worldX, worldY);
        }

        pointerWasDown = pointerDown;
    }

    private void beginPointerGesture(CombatController combat, float worldX, float worldY) {
        resetHandScrollGesture();
        resetDragGesture();
        layout.rebuildHand(combat);

        int handId = layout.hitHandInstance(worldX, worldY);
        if (handId >= 0) {
            ActionCardInstance card = combat.findCard(handId);
            if (card != null && combat.canAssignCard(card)) {
                dragCandidateInstanceId = handId;
                dragPointerX = worldX;
                dragPointerY = worldY;
                dragStart.set(worldX, worldY);
                return;
            }
        }
        beginHandScrollGesture(worldX, worldY);
    }

    private void updatePointerGesture(CombatController combat, float worldX, float worldY) {
        dragPointerX = worldX;
        dragPointerY = worldY;

        if (dragCandidateInstanceId >= 0 && !dragActive) {
            float dx = worldX - dragStart.x;
            float dy = worldY - dragStart.y;
            if (dx * dx + dy * dy >= GameConfig.COMBAT_CARD_DRAG_THRESHOLD * GameConfig.COMBAT_CARD_DRAG_THRESHOLD) {
                dragActive = true;
                resetHandScrollGesture();
                combat.setSelectedInstanceId(dragCandidateInstanceId);
            }
        }

        if (dragActive) {
            return;
        }
        updateHandScrollGesture(worldX);
    }

    private void endPointerGesture(GameSession session, float worldX, float worldY) {
        var combat = session.getCombatController();

        if (dragActive) {
            tryAssignDragDrop(combat, worldX, worldY);
            resetDragGesture();
        } else if (dragCandidateInstanceId >= 0) {
            handleHandCardClick(combat, dragCandidateInstanceId);
            resetDragGesture();
        } else if (!handScrollActive) {
            handleClick(session, worldX, worldY);
        }

        resetHandScrollGesture();
    }

    private void tryAssignDragDrop(CombatController combat, float worldX, float worldY) {
        int slot = layout.hitSlot(worldX, worldY);
        if (slot < 0 || !combat.isPlayerSlot(slot)) {
            return;
        }
        if (combat.getSlotInstanceId(slot) != null) {
            return;
        }
        combat.assignToPlayerSlot(slot, dragCandidateInstanceId);
    }

    private void rebuildAndUpdateHover(CombatController combat, float worldX, float worldY) {
        layout.rebuildHand(combat);
        layout.updateHover(worldX, worldY);
        updateDismissHover(combat, worldX, worldY);
        pressedDismiss = Gdx.input.isTouched() && hoveredDismissSlot >= 0;
    }

    private void updateDismissHover(CombatController combat, float worldX, float worldY) {
        hoveredDismissSlot = -1;
        int slot = layout.hitSlotDismiss(worldX, worldY);
        if (slot < 0 || !combat.isPlayerSlot(slot)) {
            return;
        }
        Integer slotCardId = combat.getSlotInstanceId(slot);
        Integer selected = combat.getSelectedInstanceId();
        if (slotCardId != null && slotCardId.equals(selected)) {
            hoveredDismissSlot = slot;
        }
    }

    private void resetNonCombatState() {
        layout.clearHover();
        layout.resetHandScroll();
        resetHandScrollGesture();
        resetDragGesture();
        hoveredDismissSlot = -1;
        pressedDismiss = false;
        inspectedCardType = null;
        inspectedCardInstance = null;
        pointerWasDown = false;
    }

    private void beginHandScrollGesture(float worldX, float worldY) {
        HandZonePanel panel = layout.panelAt(worldX, worldY);
        if (panel == null) {
            return;
        }
        handScrollPanel = panel;
        handScrollTracking = true;
        handScrollPointerStartX = worldX;
        handScrollAtPointerDown = panel.getScrollX();
    }

    private void updateHandScrollGesture(float worldX) {
        if (!handScrollTracking || handScrollPanel == null) {
            return;
        }
        float deltaX = worldX - handScrollPointerStartX;
        if (!handScrollActive && Math.abs(deltaX) >= GameConfig.COMBAT_HAND_SCROLL_THRESHOLD) {
            handScrollActive = true;
        }
        if (handScrollActive) {
            handScrollPanel.setScrollX(handScrollAtPointerDown - deltaX);
        }
    }

    private void resetHandScrollGesture() {
        handScrollTracking = false;
        handScrollActive = false;
        handScrollPanel = null;
    }

    private void resetDragGesture() {
        dragCandidateInstanceId = -1;
        dragActive = false;
    }

    private void handleClick(GameSession session, float worldX, float worldY) {
        var combat = session.getCombatController();

        if (layout.hitConfirm(worldX, worldY)) {
            combat.confirmPlanning();
            return;
        }

        if (tryRecallSelectedSlotCard(combat, worldX, worldY)) {
            return;
        }

        int slot = layout.hitSlot(worldX, worldY);
        if (slot >= 0) {
            if (handleSlotClick(combat, slot)) {
                return;
            }
        }

        int handId = layout.hitHandInstance(worldX, worldY);
        if (handId >= 0) {
            handleHandCardClick(combat, handId);
        }
    }

    private boolean handleSlotClick(CombatController combat, int slot) {
        if (combat.isEnemySlotIndex(slot)) {
            ActionCardType enemyCard = combat.getEnemyCardForSlot(slot);
            if (enemyCard != null) {
                setInspectedCard(enemyCard, null);
            }
            return true;
        }
        if (!combat.isPlayerSlot(slot)) {
            return false;
        }
        ActionCardInstance slotted = combat.getSlotCard(slot);
        if (slotted != null) {
            setInspectedCard(slotted.getType(), slotted);
            combat.setSelectedInstanceId(slotted.getInstanceId());
            return true;
        }
        Integer selected = combat.getSelectedInstanceId();
        if (selected != null) {
            combat.assignToPlayerSlot(slot, selected);
            return true;
        }
        return false;
    }

    private void inspectCardAtPointer(CombatController combat, float worldX, float worldY) {
        if (layout.hitConfirm(worldX, worldY)) {
            return;
        }
        int handId = layout.hitHandInstance(worldX, worldY);
        if (handId >= 0) {
            ActionCardInstance card = combat.findCard(handId);
            if (card != null) {
                setInspectedCard(card.getType(), card);
            }
            return;
        }
        int slot = layout.hitSlot(worldX, worldY);
        if (slot >= 0) {
            handleSlotClick(combat, slot);
        }
    }

    private void setInspectedCard(ActionCardType type, ActionCardInstance instance) {
        inspectedCardType = type;
        inspectedCardInstance = instance;
    }

    private boolean tryRecallSelectedSlotCard(CombatController combat, float worldX, float worldY) {
        int slot = layout.hitSlotDismiss(worldX, worldY);
        if (slot < 0 || !combat.isPlayerSlot(slot)) {
            return false;
        }
        Integer slotCardId = combat.getSlotInstanceId(slot);
        Integer selected = combat.getSelectedInstanceId();
        if (slotCardId == null || !slotCardId.equals(selected)) {
            return false;
        }
        combat.clearPlayerSlot(slot);
        combat.setSelectedInstanceId(null);
        return true;
    }

    private void handleHandCardClick(CombatController combat, int handId) {
        ActionCardInstance card = combat.findCard(handId);
        if (card == null) {
            return;
        }
        setInspectedCard(card.getType(), card);
        if (!combat.canAssignCard(card)) {
            return;
        }
        Integer selected = combat.getSelectedInstanceId();
        if (selected != null && selected == handId) {
            combat.setSelectedInstanceId(null);
        } else {
            combat.setSelectedInstanceId(handId);
        }
    }
}
