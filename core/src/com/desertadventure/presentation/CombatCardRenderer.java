package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.CombatPhase;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.GameConfig;
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
        drawConfirmShape(layout, combat);

        batch.begin();
        font.setColor(Color.WHITE);
        drawSlotText(batch, font, layout, combat);
        drawHandText(batch, font, layout, combat);
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

            if (combat.getEnemyCardForSlot(i) != null) {
                drawCardShape(x + 8f, y + 10f, layout.slotW - 16f, layout.slotH - 20f, ENEMY_CARD);
            } else {
                ActionCardInstance card = combat.getSlotCard(i);
                if (card != null) {
                    drawCardShape(x + 8f, y + 10f, layout.slotW - 16f, layout.slotH - 20f, colorFor(card.getType()));
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

    private void drawConfirmShape(CombatCardLayout layout, CombatController combat) {
        boolean enabled = combat.canConfirmPlanning();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(enabled ? CONFIRM : CONFIRM_DISABLED);
        shapes.rect(layout.confirmX, layout.confirmY, layout.confirmW, layout.confirmH);
        shapes.end();
    }

    private void drawSlotText(SpriteBatch batch, BitmapFont font, CombatCardLayout layout, CombatController combat) {
        font.setColor(UiColors.MUTED_TEXT);
        for (int i = 0; i < 4; i++) {
            font.draw(batch, String.valueOf(i + 1), layout.slotX[i] + layout.slotW / 2f - 4f,
                    layout.slotY + layout.slotH + 14f);
            if (combat.getEnemyCardForSlot(i) != null) {
                font.setColor(Color.WHITE);
                font.draw(batch, "Attack", layout.slotX[i] + 10f, layout.slotY + layout.slotH - 28f);
                font.draw(batch, GameConfig.ENEMY_CARD_ATTACK_DAMAGE + " dmg",
                        layout.slotX[i] + 10f, layout.slotY + layout.slotH - 44f);
                font.setColor(UiColors.MUTED_TEXT);
            } else {
                ActionCardInstance card = combat.getSlotCard(i);
                if (card != null) {
                    font.setColor(Color.WHITE);
                    drawCardLabel(batch, font, card.getType(), layout.slotX[i] + 10f, layout.slotY + layout.slotH - 28f);
                    font.setColor(UiColors.MUTED_TEXT);
                }
            }
        }
    }

    private void drawHandText(SpriteBatch batch, BitmapFont font, CombatCardLayout layout, CombatController combat) {
        for (CombatCardLayout.HandEntry entry : layout.getHandEntries()) {
            ActionCardInstance card = findHandCard(combat, entry.instanceId);
            if (card != null) {
                drawHandCardLabel(batch, font, card, entry.x + 6f, entry.y + layout.cardH - 18f);
            }
        }
    }

    private void drawConfirmText(SpriteBatch batch, BitmapFont font, CombatCardLayout layout, CombatController combat) {
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

    private static void drawCardLabel(SpriteBatch batch, BitmapFont font, ActionCardType type, float x, float y) {
        font.draw(batch, type.getDisplayName(), x, y);
        font.draw(batch, effectLine(type), x, y - 16f);
        if (type.getCooldownTurns() > 0) {
            font.draw(batch, "CD:" + type.getCooldownTurns(), x, y - 32f);
        }
    }

    private static void drawHandCardLabel(SpriteBatch batch, BitmapFont font, ActionCardInstance card, float x, float y) {
        ActionCardType type = card.getType();
        font.setColor(card.isOnCooldown() ? UiColors.CARD_ON_COOLDOWN_TEXT : Color.WHITE);
        font.draw(batch, type.getDisplayName(), x, y);
        font.draw(batch, effectLine(type), x, y - 16f);
        if (card.isOnCooldown()) {
            font.draw(batch, "CD: " + card.getCooldownRemaining(), x, y - 32f);
        } else if (type.getCooldownTurns() > 0) {
            font.draw(batch, "CD:" + type.getCooldownTurns(), x, y - 32f);
        }
    }

    private static String effectLine(ActionCardType type) {
        return switch (type.getTarget()) {
            case ENEMY -> type.getPrimaryValue() + " dmg";
            case SELF -> "+" + type.getPrimaryValue() + " HP";
        };
    }

    private static ActionCardInstance findHandCard(CombatController combat, int instanceId) {
        for (ActionCardInstance c : combat.getVisibleHand()) {
            if (c.getInstanceId() == instanceId) {
                return c;
            }
        }
        return null;
    }
}
