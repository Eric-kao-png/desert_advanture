package com.desertadventure.screen.layout;

import com.desertadventure.combat.card.ActionCardCategory;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.GameConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen layout: centered hand rows, info panel to their right, confirm above info panel.
 */
public final class CombatCardLayout {
    public final float[] slotX = new float[4];
    public float slotY;
    public float slotW;
    public float slotH;

    public float cardW;
    public float cardH;

    public final HandZonePanel attackPanel = new HandZonePanel(ActionCardCategory.ATTACK);
    public final HandZonePanel changePanel = new HandZonePanel(ActionCardCategory.CHANGE);

    public float handViewportX;
    public float handViewportW;

    public float infoPanelX;
    public float infoPanelY;
    public float infoPanelW;
    public float infoPanelH;

    public float confirmX;
    public float confirmY;
    public final float confirmW = GameConfig.COMBAT_CONFIRM_WIDTH;
    public final float confirmH = GameConfig.COMBAT_CONFIRM_HEIGHT;

    private int hoveredHandInstanceId = -1;
    private int hoveredSlotIndex = -1;

    public CombatCardLayout() {
        refreshLayout();
    }

    public void refreshLayout() {
        slotY = CombatSceneLayout.slotY();
        slotW = CombatSceneLayout.slotWidth();
        slotH = CombatSceneLayout.slotHeight();
        cardW = CombatSceneLayout.cardWidth();
        cardH = CombatSceneLayout.cardHeight();

        float totalW = 4 * slotW + 3 * GameConfig.COMBAT_SLOT_GAP;
        float startX = (GameConfig.VIEW_WIDTH - totalW) / 2f;
        for (int i = 0; i < 4; i++) {
            slotX[i] = startX + i * (slotW + GameConfig.COMBAT_SLOT_GAP);
        }

        float margin = GameConfig.COMBAT_HAND_VIEWPORT_MARGIN_H;
        float maxHandW = GameConfig.VIEW_WIDTH - 2f * margin;
        handViewportW = Math.min(GameConfig.COMBAT_HAND_VIEWPORT_WIDTH, maxHandW);
        handViewportX = (GameConfig.VIEW_WIDTH - handViewportW) / 2f;

        float handStackBottom = CombatSceneLayout.changeHandViewportY();
        float handStackTop = CombatSceneLayout.attackHandViewportY() + CombatSceneLayout.handRowViewportHeight();

        infoPanelX = handViewportX + handViewportW + GameConfig.COMBAT_HAND_INFO_GAP;
        infoPanelY = handStackBottom;
        infoPanelW = GameConfig.VIEW_WIDTH - infoPanelX - margin;
        infoPanelH = handStackTop - handStackBottom;

        confirmX = infoPanelX + (infoPanelW - confirmW) / 2f;
        confirmY = handStackTop + GameConfig.COMBAT_CONFIRM_ABOVE_INFO_GAP;

        layoutHandRow(attackPanel, CombatSceneLayout.attackHandViewportY());
        layoutHandRow(changePanel, CombatSceneLayout.changeHandViewportY());
    }

    private void layoutHandRow(HandZonePanel panel, float viewportY) {
        panel.viewportW = handViewportW;
        panel.viewportX = handViewportX;
        panel.viewportY = viewportY;
        panel.viewportH = CombatSceneLayout.handRowViewportHeight();
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
        float cardY = panel.centeredCardY(cardH);
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
            panel.entries.add(new HandEntry(cards.get(i).getInstanceId(), x, cardY));
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

    /** Top-right dismiss button origin for a slotted card (LibGDX bottom-left of button). */
    public float slotDismissX(int slotIndex) {
        return innerCardX(slotIndex) + innerCardW() - GameConfig.COMBAT_SLOT_DISMISS_SIZE;
    }

    public float slotDismissY(int slotIndex) {
        return innerCardY() + innerCardH() - GameConfig.COMBAT_SLOT_DISMISS_SIZE;
    }

    /** Player slot index whose dismiss control was hit, or -1. */
    public int hitSlotDismiss(float worldX, float worldY) {
        float size = GameConfig.COMBAT_SLOT_DISMISS_SIZE;
        for (int i = 0; i < 4; i++) {
            float x = slotDismissX(i);
            float y = slotDismissY(i);
            if (worldX >= x && worldX <= x + size && worldY >= y && worldY <= y + size) {
                return i;
            }
        }
        return -1;
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

        public float centeredCardY(float cardH) {
            float pad = GameConfig.COMBAT_HAND_VIEWPORT_PADDING;
            float border = GameConfig.COMBAT_HAND_BORDER;
            float innerH = viewportH - 2f * (pad + border);
            return viewportY + border + pad + (innerH - cardH) / 2f;
        }

        public float clipX() {
            return contentStartX();
        }

        public float clipY() {
            return viewportY + GameConfig.COMBAT_HAND_BORDER + GameConfig.COMBAT_HAND_VIEWPORT_PADDING;
        }

        public float clipW() {
            return innerWidth();
        }

        public float clipH() {
            float pad = GameConfig.COMBAT_HAND_VIEWPORT_PADDING;
            float border = GameConfig.COMBAT_HAND_BORDER;
            return viewportH - 2f * (pad + border);
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
