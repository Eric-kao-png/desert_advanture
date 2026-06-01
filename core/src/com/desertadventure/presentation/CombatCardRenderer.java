package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.CombatPhase;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.UiColors;
import com.desertadventure.screen.layout.CombatCardLayout;

public final class CombatCardRenderer {
    private static final Color SLOT_BG = new Color(0.15f, 0.14f, 0.12f, 0.85f);
    private static final Color SLOT_BORDER = new Color(0.45f, 0.4f, 0.32f, 1f);
    private static final Color SLOT_ACTIVE = new Color(0.55f, 0.48f, 0.2f, 1f);
    private static final Color CARD_ATTACK = new Color(0.75f, 0.28f, 0.22f, 1f);
    private static final Color CARD_STRONG = new Color(0.85f, 0.4f, 0.15f, 1f);
    private static final Color CARD_HEAL = new Color(0.25f, 0.65f, 0.35f, 1f);
    private static final Color CARD_SELECTED = new Color(1f, 0.92f, 0.5f, 1f);
    private static final Color ENEMY_CARD = new Color(0.5f, 0.2f, 0.2f, 0.95f);
    private static final Color CONFIRM = new Color(0.22f, 0.5f, 0.32f, 1f);
    private static final Color CONFIRM_DISABLED = new Color(0.3f, 0.3f, 0.3f, 0.8f);

    private final ShapeRenderer shapes;

    public CombatCardRenderer(ShapeRenderer shapes) {
        this.shapes = shapes;
    }

    public void render(
            CombatController combat,
            CombatCardLayout layout,
            SpriteBatch batch,
            BitmapFont font) {
        CombatPhase phase = combat.getPhase();
        int resolvingSlot = combat.getResolvingSlotDisplayIndex();
        layout.rebuildHand(combat);

        drawSlotShapes(combat, layout, phase, resolvingSlot);
        drawHandShapes(combat, layout);
        drawTooltipShapes(layout, combat, font);
        drawConfirmShape(layout, combat);

        batch.begin();
        drawSlotNames(batch, font, layout, combat);
        drawHandNames(batch, font, layout, combat);
        drawTooltipTexts(batch, font, layout, combat);
        drawConfirmText(batch, font, layout, combat);
        batch.end();
    }

    private void drawSlotShapes(CombatController combat, CombatCardLayout layout, CombatPhase phase, int resolvingSlot) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 4; i++) {
            boolean active = phase == CombatPhase.RESOLVING && resolvingSlot == i + 1;
            shapes.setColor(active ? SLOT_ACTIVE : SLOT_BORDER);
            float x = layout.slotX[i];
            float y = layout.slotY;
            shapes.rect(x - 2f, y - 2f, layout.slotW + 4f, layout.slotH + 4f);
            shapes.setColor(SLOT_BG);
            shapes.rect(x, y, layout.slotW, layout.slotH);

            float innerX = layout.innerCardX(i);
            float innerY = layout.innerCardY();
            float innerW = layout.innerCardW();
            float innerH = layout.innerCardH();
            if (combat.getEnemyCardForSlot(i) != null) {
                drawCardShape(innerX, innerY, innerW, innerH, ENEMY_CARD);
            } else {
                ActionCardInstance card = combat.getSlotCard(i);
                if (card != null) {
                    Color fill = card.isOnCooldown() ? UiColors.CARD_ON_COOLDOWN_FILL : colorFor(card.getType());
                    drawCardShape(innerX, innerY, innerW, innerH, fill);
                }
            }
        }
        shapes.end();
    }

    private void drawHandShapes(CombatController combat, CombatCardLayout layout) {
        Integer selected = combat.getSelectedInstanceId();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (CombatCardLayout.HandEntry entry : layout.getHandEntries()) {
            ActionCardInstance card = findHandCard(combat, entry.instanceId);
            if (card == null) {
                continue;
            }
            if (selected != null && selected == entry.instanceId && combat.canAssignCard(card)) {
                shapes.setColor(CARD_SELECTED);
                shapes.rect(entry.x - 3f, entry.y - 3f, layout.cardW + 6f, layout.cardH + 6f);
            }
            Color fill = card.isOnCooldown() ? UiColors.CARD_ON_COOLDOWN_FILL : colorFor(card.getType());
            drawCardShape(entry.x, entry.y, layout.cardW, layout.cardH, fill);
        }
        shapes.end();
    }

    private void drawTooltipShapes(CombatCardLayout layout, CombatController combat, BitmapFont font) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        int hoveredHand = layout.getHoveredHandInstanceId();
        if (hoveredHand >= 0) {
            for (CombatCardLayout.HandEntry entry : layout.getHandEntries()) {
                if (entry.instanceId == hoveredHand) {
                    ActionCardInstance card = combat.findCard(hoveredHand);
                    if (card != null) {
                        drawTooltipPanel(font, ActionCardUiText.detailLines(card),
                                entry.x, entry.y, layout.cardW, layout.cardH);
                    }
                    break;
                }
            }
        }

        int hoveredSlot = layout.getHoveredSlotIndex();
        if (hoveredSlot >= 0) {
            float innerX = layout.innerCardX(hoveredSlot);
            float innerY = layout.innerCardY();
            float innerW = layout.innerCardW();
            float innerH = layout.innerCardH();
            if (combat.getEnemyCardForSlot(hoveredSlot) != null) {
                drawTooltipPanel(font, ActionCardUiText.enemyAttackDetailLines(),
                        innerX, innerY, innerW, innerH);
            } else {
                ActionCardInstance card = combat.getSlotCard(hoveredSlot);
                if (card != null) {
                    drawTooltipPanel(font, ActionCardUiText.detailLines(card),
                            innerX, innerY, innerW, innerH);
                }
            }
        }
        shapes.end();
    }

    private void drawTooltipPanel(BitmapFont font, String[] lines, float cardX, float cardY, float cardW, float cardH) {
        ActionCardUiText.TooltipBounds bounds = ActionCardUiText.measureTooltip(font, lines);
        float panelX = ActionCardUiText.tooltipPanelXCentered(cardX, cardW, bounds.width());
        float panelY = ActionCardUiText.tooltipPanelYAbove(cardY, cardH, bounds.height());
        shapes.setColor(UiColors.CARD_TOOLTIP_FILL);
        shapes.rect(panelX, panelY, bounds.width(), bounds.height());
        shapes.setColor(UiColors.CARD_TOOLTIP_BORDER);
        float t = 2f;
        shapes.rect(panelX, panelY, bounds.width(), t);
        shapes.rect(panelX, panelY + bounds.height() - t, bounds.width(), t);
        shapes.rect(panelX, panelY, t, bounds.height());
        shapes.rect(panelX + bounds.width() - t, panelY, t, bounds.height());
    }

    private void drawConfirmShape(CombatCardLayout layout, CombatController combat) {
        boolean enabled = combat.canConfirmPlanning();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(enabled ? CONFIRM : CONFIRM_DISABLED);
        shapes.rect(layout.confirmX, layout.confirmY, layout.confirmW, layout.confirmH);
        shapes.end();
    }

    private void drawSlotNames(SpriteBatch batch, BitmapFont font, CombatCardLayout layout, CombatController combat) {
        font.setColor(UiColors.MUTED_TEXT);
        for (int i = 0; i < 4; i++) {
            font.draw(batch, String.valueOf(i + 1), layout.slotX[i] + layout.slotW / 2f - 4f,
                    layout.slotY + layout.slotH + 14f);

            float innerX = layout.innerCardX(i);
            float innerY = layout.innerCardY();
            float innerW = layout.innerCardW();
            float innerH = layout.innerCardH();
            if (combat.getEnemyCardForSlot(i) != null) {
                ActionCardUiText.drawNameCentered(batch, font, "Attack", innerX, innerY, innerW, innerH, Color.WHITE);
            } else {
                ActionCardInstance card = combat.getSlotCard(i);
                if (card != null) {
                    Color textColor = card.isOnCooldown() ? UiColors.CARD_ON_COOLDOWN_TEXT : Color.WHITE;
                    ActionCardUiText.drawNameCentered(batch, font, card.getType().getDisplayName(),
                            innerX, innerY, innerW, innerH, textColor);
                }
            }
        }
    }

    private void drawHandNames(SpriteBatch batch, BitmapFont font, CombatCardLayout layout, CombatController combat) {
        for (CombatCardLayout.HandEntry entry : layout.getHandEntries()) {
            ActionCardInstance card = findHandCard(combat, entry.instanceId);
            if (card == null) {
                continue;
            }
            Color textColor = card.isOnCooldown() ? UiColors.CARD_ON_COOLDOWN_TEXT : Color.WHITE;
            ActionCardUiText.drawNameCentered(batch, font, card.getType().getDisplayName(),
                    entry.x, entry.y, layout.cardW, layout.cardH, textColor);
        }
    }

    private void drawTooltipTexts(SpriteBatch batch, BitmapFont font, CombatCardLayout layout, CombatController combat) {
        int hoveredHand = layout.getHoveredHandInstanceId();
        if (hoveredHand >= 0) {
            for (CombatCardLayout.HandEntry entry : layout.getHandEntries()) {
                if (entry.instanceId == hoveredHand) {
                    ActionCardInstance card = combat.findCard(hoveredHand);
                    if (card != null) {
                        drawTooltipForCard(batch, font, ActionCardUiText.detailLines(card),
                                entry.x, entry.y, layout.cardW, layout.cardH);
                    }
                    break;
                }
            }
        }

        int hoveredSlot = layout.getHoveredSlotIndex();
        if (hoveredSlot >= 0) {
            float innerX = layout.innerCardX(hoveredSlot);
            float innerY = layout.innerCardY();
            float innerW = layout.innerCardW();
            float innerH = layout.innerCardH();
            if (combat.getEnemyCardForSlot(hoveredSlot) != null) {
                drawTooltipForCard(batch, font, ActionCardUiText.enemyAttackDetailLines(),
                        innerX, innerY, innerW, innerH);
            } else {
                ActionCardInstance card = combat.getSlotCard(hoveredSlot);
                if (card != null) {
                    drawTooltipForCard(batch, font, ActionCardUiText.detailLines(card),
                            innerX, innerY, innerW, innerH);
                }
            }
        }
    }

    private void drawTooltipForCard(
            SpriteBatch batch,
            BitmapFont font,
            String[] lines,
            float cardX,
            float cardY,
            float cardW,
            float cardH) {
        ActionCardUiText.TooltipBounds bounds = ActionCardUiText.measureTooltip(font, lines);
        float panelX = ActionCardUiText.tooltipPanelXCentered(cardX, cardW, bounds.width());
        float panelY = ActionCardUiText.tooltipPanelYAbove(cardY, cardH, bounds.height());
        ActionCardUiText.drawTooltipText(batch, font, lines, panelX, panelY, Color.WHITE);
    }

    private void drawConfirmText(SpriteBatch batch, BitmapFont font, CombatCardLayout layout, CombatController combat) {
        font.setColor(Color.WHITE);
        String label = combat.getPhase() == CombatPhase.RESOLVING ? "Resolving..." : "Confirm";
        font.draw(batch, label, layout.confirmX + 28f, layout.confirmY + layout.confirmH / 2f + 6f);
    }

    private void drawCardShape(float x, float y, float w, float h, Color fill) {
        shapes.setColor(fill);
        shapes.rect(x, y, w, h);
    }

    private static Color colorFor(ActionCardType type) {
        return switch (type) {
            case ATTACK -> CARD_ATTACK;
            case STRONG_ATTACK -> CARD_STRONG;
            case HEAL -> CARD_HEAL;
        };
    }

    private static ActionCardInstance findHandCard(CombatController combat, int instanceId) {
        return combat.findCard(instanceId);
    }
}
