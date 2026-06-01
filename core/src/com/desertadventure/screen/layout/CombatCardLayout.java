package com.desertadventure.screen.layout;

import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.GameConfig;

import java.util.ArrayList;
import java.util.List;

/** Screen layout for combat timeline, hand, and confirm button. */
public final class CombatCardLayout {
    public final float[] slotX = new float[4];
    public float slotY;
    public float slotW;
    public float slotH;

    public float handY;
    public float cardW;
    public float cardH;

    public float handViewportX;
    public float handViewportY;
    public float handViewportW;
    public float handViewportH;

    public float confirmX;
    public float confirmY;
    public final float confirmW = GameConfig.COMBAT_CONFIRM_WIDTH;
    public final float confirmH = GameConfig.COMBAT_CONFIRM_HEIGHT;

    private final List<HandEntry> handEntries = new ArrayList<>();
    private int hoveredHandInstanceId = -1;
    private int hoveredSlotIndex = -1;
    private float layoutBlend;
    private float handScrollX;
    private float lastHandContentWidth;

    public CombatCardLayout() {
        applyBlend(0f);
    }

    public void applyBlend(float blend) {
        layoutBlend = blend;
        slotY = CombatSceneLayout.slotY(blend);
        slotW = CombatSceneLayout.slotWidth(blend);
        slotH = CombatSceneLayout.slotHeight(blend);
        handY = CombatSceneLayout.handY(blend);
        cardW = CombatSceneLayout.cardWidth(blend);
        cardH = CombatSceneLayout.cardHeight(blend);

        float totalW = 4 * slotW + 3 * GameConfig.COMBAT_SLOT_GAP;
        float startX = (GameConfig.VIEW_WIDTH - totalW) / 2f;
        for (int i = 0; i < 4; i++) {
            slotX[i] = startX + i * (slotW + GameConfig.COMBAT_SLOT_GAP);
        }
        confirmX = GameConfig.VIEW_WIDTH - confirmW - GameConfig.HUD_LEFT_MARGIN;
        confirmY = handY;

        handViewportX = GameConfig.COMBAT_HAND_VIEWPORT_MARGIN_H;
        handViewportW = confirmX - GameConfig.COMBAT_HAND_VIEWPORT_CONFIRM_GAP - handViewportX;
        handViewportY = handY - GameConfig.COMBAT_HAND_VIEWPORT_PADDING - GameConfig.COMBAT_HAND_BORDER;
        handViewportH = cardH + 2f * (GameConfig.COMBAT_HAND_VIEWPORT_PADDING + GameConfig.COMBAT_HAND_BORDER);
    }

    public float getLayoutBlend() {
        return layoutBlend;
    }

    public void rebuildHand(CombatController combat) {
        handEntries.clear();
        List<ActionCardInstance> hand = combat.getVisibleHand();
        float contentW = hand.size() * cardW + Math.max(0, hand.size() - 1) * GameConfig.COMBAT_HAND_GAP;
        lastHandContentWidth = contentW;
        clampHandScroll();

        float innerW = handInnerWidth();
        float startX;
        if (contentW <= innerW) {
            handScrollX = 0f;
            startX = handContentStartX() + (innerW - contentW) / 2f;
        } else {
            startX = handContentStartX() - handScrollX;
        }
        for (int i = 0; i < hand.size(); i++) {
            float x = startX + i * (cardW + GameConfig.COMBAT_HAND_GAP);
            handEntries.add(new HandEntry(hand.get(i).getInstanceId(), x, handY));
        }
    }

    public float handInnerWidth() {
        return handViewportW - 2f * (GameConfig.COMBAT_HAND_BORDER + GameConfig.COMBAT_HAND_VIEWPORT_PADDING);
    }

    public float handContentStartX() {
        return handViewportX + GameConfig.COMBAT_HAND_BORDER + GameConfig.COMBAT_HAND_VIEWPORT_PADDING;
    }

    public float handClipX() {
        return handContentStartX();
    }

    public float handClipY() {
        return handY;
    }

    public float handClipW() {
        return handInnerWidth();
    }

    public float handClipH() {
        return cardH;
    }

    public float getHandScrollX() {
        return handScrollX;
    }

    public void setHandScrollX(float scrollX) {
        handScrollX = scrollX;
        clampHandScroll();
    }

    public void clampHandScroll() {
        float maxScroll = Math.max(0f, lastHandContentWidth - handInnerWidth());
        handScrollX = Math.max(0f, Math.min(handScrollX, maxScroll));
    }

    public void resetHandScroll() {
        handScrollX = 0f;
        lastHandContentWidth = 0f;
    }

    public boolean containsHandViewport(float worldX, float worldY) {
        return contains(worldX, worldY, handViewportX, handViewportY, handViewportW, handViewportH);
    }

    public List<HandEntry> getHandEntries() {
        return handEntries;
    }

    public void updateHover(float worldX, float worldY) {
        hoveredHandInstanceId = containsHandViewport(worldX, worldY)
                ? hitHandInstance(worldX, worldY)
                : -1;
        hoveredSlotIndex = hitSlot(worldX, worldY);
    }

    public void clearHover() {
        hoveredHandInstanceId = -1;
        hoveredSlotIndex = -1;
    }

    public int getHoveredHandInstanceId() {
        return hoveredHandInstanceId;
    }

    public int getHoveredSlotIndex() {
        return hoveredSlotIndex;
    }

    public float innerCardX(int slotIndex) {
        return slotX[slotIndex] + 8f;
    }

    public float innerCardY() {
        return slotY + 10f;
    }

    public float innerCardW() {
        return slotW - 16f;
    }

    public float innerCardH() {
        return slotH - 20f;
    }

    public int hitHandInstance(float worldX, float worldY) {
        if (!containsHandViewport(worldX, worldY)) {
            return -1;
        }
        for (HandEntry entry : handEntries) {
            if (contains(worldX, worldY, entry.x, entry.y, cardW, cardH)) {
                return entry.instanceId;
            }
        }
        return -1;
    }

    public int hitSlot(float worldX, float worldY) {
        for (int i = 0; i < 4; i++) {
            if (contains(worldX, worldY, slotX[i], slotY, slotW, slotH)) {
                return i;
            }
        }
        return -1;
    }

    public boolean hitConfirm(float worldX, float worldY) {
        return contains(worldX, worldY, confirmX, confirmY, confirmW, confirmH);
    }

    private static boolean contains(float px, float py, float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    public static final class HandEntry {
        public final int instanceId;
        public final float x;
        public final float y;

        HandEntry(int instanceId, float x, float y) {
            this.instanceId = instanceId;
            this.x = x;
            this.y = y;
        }
    }
}
