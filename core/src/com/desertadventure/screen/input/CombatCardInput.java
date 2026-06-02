package com.desertadventure.screen.input;

import com.badlogic.gdx.Gdx;
import com.desertadventure.combat.card.CombatPhase;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameInputBindings;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.screen.layout.CombatCardLayout;
import com.desertadventure.screen.layout.CombatCardLayout.HandZonePanel;
import com.desertadventure.state.GameSession;
/** Turn-based combat: hand selection, slot assignment, confirm. */
public final class CombatCardInput {
    private final CombatCardLayout layout = new CombatCardLayout();
    private boolean pointerWasDown;
    private boolean handScrollTracking;
    private boolean handScrollActive;
    private float handScrollPointerStartX;
    private float handScrollAtPointerDown;
    private HandZonePanel handScrollPanel;

    public CombatCardLayout getLayout() {
        return layout;
    }

    public void handle(GameSession session, GameViewport viewport, float delta) {
        if (!session.getMode().isCombat()) {
            resetNonCombatState();
            return;
        }
        var combat = session.getCombatController();
        combat.update(delta);

        float worldX = viewport.pointerWorldX();
        float worldY = viewport.pointerWorldY();

        if (combat.getPhase() != CombatPhase.PLANNING) {
            pointerWasDown = Gdx.input.isTouched();
            resetHandScrollGesture();
            layout.rebuildHand(combat);
            layout.updateHover(worldX, worldY);
            return;
        }

        if (GameInputBindings.justConfirmed()) {
            combat.confirmPlanning();
            return;
        }

        boolean pointerDown = Gdx.input.isTouched();

        if (pointerDown) {
            if (Gdx.input.justTouched()) {
                beginHandScrollGesture(worldX, worldY);
            } else {
                updateHandScrollGesture(worldX);
            }
        } else if (pointerWasDown) {
            if (!handScrollActive) {
                handleClick(session, worldX, worldY);
            }
            resetHandScrollGesture();
        }

        pointerWasDown = pointerDown;
        layout.rebuildHand(combat);
        layout.updateHover(worldX, worldY);
    }

    private void resetNonCombatState() {
        layout.clearHover();
        layout.resetHandScroll();
        resetHandScrollGesture();
        pointerWasDown = false;
    }

    private void beginHandScrollGesture(float worldX, float worldY) {
        resetHandScrollGesture();
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

    private void handleClick(GameSession session, float worldX, float worldY) {
        var combat = session.getCombatController();

        if (layout.hitConfirm(worldX, worldY)) {
            combat.confirmPlanning();
            return;
        }

        int slot = layout.hitSlot(worldX, worldY);
        if (slot >= 0 && combat.isPlayerSlot(slot)) {
            Integer existing = combat.getSlotInstanceId(slot);
            if (existing != null) {
                combat.clearPlayerSlot(slot);
                combat.setSelectedInstanceId(existing);
                return;
            }
            Integer selected = combat.getSelectedInstanceId();
            if (selected != null) {
                combat.assignToPlayerSlot(slot, selected);
            }
            return;
        }

        int handId = layout.hitHandInstance(worldX, worldY);
        if (handId >= 0) {
            var card = combat.findCard(handId);
            if (card == null || !combat.canAssignCard(card)) {
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
}
