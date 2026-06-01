package com.desertadventure.screen.input;

import com.badlogic.gdx.Gdx;
import com.desertadventure.combat.card.CombatPhase;
import com.desertadventure.config.GameInputBindings;
import com.desertadventure.presentation.GameViewport;
import com.desertadventure.screen.layout.CombatCardLayout;
import com.desertadventure.state.GameSession;
/** Turn-based combat: hand selection, slot assignment, confirm. */
public final class CombatCardInput {
    private final CombatCardLayout layout = new CombatCardLayout();
    private boolean pointerWasDown;

    public CombatCardLayout getLayout() {
        return layout;
    }

    public void handle(GameSession session, GameViewport viewport, float delta) {
        if (!session.getMode().isCombat()) {
            return;
        }
        var combat = session.getCombatController();
        combat.update(delta);

        if (combat.getPhase() != CombatPhase.PLANNING) {
            pointerWasDown = Gdx.input.isTouched();
            return;
        }

        if (GameInputBindings.justConfirmed()) {
            combat.confirmPlanning();
            return;
        }

        float worldX = viewport.pointerWorldX();
        float worldY = viewport.pointerWorldY();
        boolean pointerDown = Gdx.input.isTouched();

        if (pointerDown && Gdx.input.justTouched()) {
            handleClick(session, worldX, worldY);
        }
        pointerWasDown = pointerDown;
    }

    private void handleClick(GameSession session, float worldX, float worldY) {
        var combat = session.getCombatController();
        layout.rebuildHand(combat);

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
            Integer selected = combat.getSelectedInstanceId();
            if (selected != null && selected == handId) {
                combat.setSelectedInstanceId(null);
            } else {
                combat.setSelectedInstanceId(handId);
            }
        }
    }
}
