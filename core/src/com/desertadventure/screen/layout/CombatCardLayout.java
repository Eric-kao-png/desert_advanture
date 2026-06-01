package com.desertadventure.screen.layout;

import com.desertadventure.combat.card.ActionCardCategory;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.GameConfig;

import java.util.ArrayList;
import java.util.List;

/** Screen layout for combat timeline, split hand zones, and centered confirm. */
public final class CombatCardLayout {
    public final float[] slotX = new float[4];
    public float slotY;
    public float slotW;
    public float slotH;

    public float handY;
    public float cardW;
    public float cardH;

    public final HandZonePanel attackPanel = new HandZonePanel(ActionCardCategory.ATTACK);
    public final HandZonePanel changePanel = new HandZonePanel(ActionCardCategory.CHANGE);

    public float confirmX;
    public float confirmY;
    public final float confirmW = GameConfig.COMBAT_CONFIRM_WIDTH;
    public final float confirmH = GameConfig.COMBAT_CONFIRM_HEIGHT;

    private int hoveredHandInstanceId = -1;
    private int hoveredSlotIndex = -1;
    private float layoutBlend;

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

        confirmX = (GameConfig.VIEW_WIDTH - confirmW) / 2f;
        confirmY = handY;

        float panelY = handY - GameConfig.COMBAT_HAND_VIEWPORT_PADDING - GameConfig.COMBAT_HAND_BORDER;
        float panelH = cardH + 2f * (GameConfig.COMBAT_HAND_VIEWPORT_PADDING + GameConfig.COMBAT_HAND_BORDER);
        float margin = GameConfig.COMBAT_HAND_VIEWPORT_MARGIN_H;
        float centerGap = GameConfig.COMBAT_HAND_CENTER_GAP;

        attackPanel.viewportX = margin;
        attackPanel.viewportW = confirmX - centerGap - attackPanel.viewportX;
        attackPanel.viewportY = panelY;
        attackPanel.viewportH = panelH;

        changePanel.viewportX = confirmX + confirmW + centerGap;
        changePanel.viewportW = GameConfig.VIEW_WIDTH - margin - changePanel.viewportX;
        changePanel.viewportY = panelY;
        changePanel.viewportH = panelH;
    }

    public float getLayoutBlend() {
        return layoutBlend;
    }

    public void rebuildHand(CombatController combat) {
        attackPanel.entries.clear();
        changePanel.entries.clear();
        List<ActionCardInstance> hand = combat.getVisibleHand();
        List<ActionCardInstance> attackCards = new ArrayList<>();
        List<ActionCardInstance> changeCards = new ArrayList<>();
        for (ActionCardInstance instance : hand) {
            if (instance.getType().getCategory() == ActionCardCategory.ATTACK) {
                attackCards.add(instance);
            } else {
                changeCards.add(instance);
            }
        }
        layoutZone(attackPanel, attackCards);
        layoutZone(changePanel, changeCards);
    }

    private void layoutZone(HandZonePanel panel, List<ActionCardInstance> cards) {
        float contentW = cards.size() * cardW + Math.max(0, cards.size() - 1) * GameConfig.COMBAT_HAND_GAP;
        panel.lastContentWidth = contentW;
        panel.clampScroll();

        float innerW = panel.innerWidth();
        float startX;
        if (contentW <= innerW) {
            panel.scrollX = 0f;
            startX = panel.contentStartX() + (innerW - contentW) / 2f;
        } else {
            startX = panel.contentStartX() - panel.scrollX;
        }
        for (int i = 0; i < cards.size(); i++) {
            float x = startX + i * (cardW + GameConfig.COMBAT_HAND_GAP);
            panel.entries.add(new HandEntry(cards.get(i).getInstanceId(), x, handY));
        }
    }

    public void resetHandScroll() {
        attackPanel.resetScroll();
        changePanel.resetScroll();
    }

    public boolean containsHandViewport(float worldX, float worldY) {
        return attackPanel.contains(worldX, worldY) || changePanel.contains(worldX, worldY);
    }

    public HandZonePanel panelAt(float worldX, float worldY) {
        if (attackPanel.contains(worldX, worldY)) {
            return attackPanel;
        }
        if (changePanel.contains(worldX, worldY)) {
            return changePanel;
        }
        return null;
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
        int id = hitHandInPanel(attackPanel, worldX, worldY);
        if (id >= 0) {
            return id;
        }
        return hitHandInPanel(changePanel, worldX, worldY);
    }

    private int hitHandInPanel(HandZonePanel panel, float worldX, float worldY) {
        if (!panel.contains(worldX, worldY)) {
            return -1;
        }
        for (HandEntry entry : panel.entries) {
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

    public static final class HandZonePanel {
        public final ActionCardCategory category;
        public float viewportX;
        public float viewportY;
        public float viewportW;
        public float viewportH;
        private final List<HandEntry> entries = new ArrayList<>();
        private float scrollX;
        private float lastContentWidth;

        HandZonePanel(ActionCardCategory category) {
            this.category = category;
        }

        public List<HandEntry> getEntries() {
            return entries;
        }

        public float getScrollX() {
            return scrollX;
        }

        public void setScrollX(float scroll) {
            scrollX = scroll;
            clampScroll();
        }

        public void clampScroll() {
            float maxScroll = Math.max(0f, lastContentWidth - innerWidth());
            scrollX = Math.max(0f, Math.min(scrollX, maxScroll));
        }

        public void resetScroll() {
            scrollX = 0f;
            lastContentWidth = 0f;
        }

        public float innerWidth() {
            return viewportW - 2f * (GameConfig.COMBAT_HAND_BORDER + GameConfig.COMBAT_HAND_VIEWPORT_PADDING);
        }

        public float contentStartX() {
            return viewportX + GameConfig.COMBAT_HAND_BORDER + GameConfig.COMBAT_HAND_VIEWPORT_PADDING;
        }

        public float clipX() {
            return contentStartX();
        }

        public float clipY(float handY, float cardH) {
            return handY;
        }

        public float clipW() {
            return innerWidth();
        }

        public float clipH(float cardH) {
            return cardH;
        }

        public boolean contains(float worldX, float worldY) {
            return CombatCardLayout.contains(worldX, worldY, viewportX, viewportY, viewportW, viewportH);
        }
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
