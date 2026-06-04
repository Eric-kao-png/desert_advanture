package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.desertadventure.config.GameConfig;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.CombatPhase;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.UiColors;
import com.desertadventure.screen.layout.CombatCardLayout;
import com.desertadventure.screen.layout.CombatCardLayout.HandZonePanel;

public final class CombatCardRenderer {
    private static final Color SLOT_BG = new Color(0.15f, 0.14f, 0.12f, 0.85f);
    private static final Color SLOT_BORDER = new Color(0.45f, 0.4f, 0.32f, 1f);
    private static final Color SLOT_ACTIVE = new Color(0.55f, 0.48f, 0.2f, 1f);
    private static final Color CARD_ATTACK = new Color(0.75f, 0.28f, 0.22f, 1f);
    private static final Color CARD_SHIELD = new Color(0.35f, 0.55f, 0.85f, 1f);
    private static final Color CARD_FULL_POWER = new Color(0.85f, 0.35f, 0.20f, 1f);
    private static final Color CARD_LIFE_MAGIC = new Color(0.55f, 0.25f, 0.75f, 1f);
    private static final Color CARD_THRUST = new Color(0.70f, 0.45f, 0.22f, 1f);
    private static final Color CARD_POISON = new Color(0.35f, 0.72f, 0.32f, 1f);
    private static final Color CARD_STRONG = new Color(0.85f, 0.4f, 0.15f, 1f);
    private static final Color CARD_HEAL = new Color(0.25f, 0.65f, 0.35f, 1f);
    private static final Color CARD_SELECTED = new Color(1f, 0.92f, 0.5f, 1f);
    private static final Color ENEMY_CARD = new Color(0.5f, 0.2f, 0.2f, 0.95f);
    private static final Color CONFIRM = new Color(0.22f, 0.5f, 0.32f, 1f);
    private static final Color CONFIRM_DISABLED = new Color(0.3f, 0.3f, 0.3f, 0.8f);
    private static final Color HAND_VIEWPORT_BG = new Color(0.12f, 0.11f, 0.1f, 0.88f);
    private static final Color HAND_VIEWPORT_BORDER = new Color(0.42f, 0.38f, 0.3f, 1f);

    private final ShapeRenderer shapes;
    private final SlotCardDismissButton dismissButton = new SlotCardDismissButton();
    private final OrthographicCamera scissorCamera = new OrthographicCamera();
    private final Matrix4 identityTransform = new Matrix4();
    private final Rectangle scissorBounds = new Rectangle();
    private final Rectangle scissorResult = new Rectangle();

    public CombatCardRenderer(ShapeRenderer shapes) {
        this.shapes = shapes;
    }

    public void dispose() {
        dismissButton.dispose();
    }

    /** Centered hand rows, info panel on the right, confirm above info panel. */
    public void renderHand(
            CombatController combat,
            CombatCardLayout layout,
            SpriteBatch batch,
            BitmapFont font,
            int draggingInstanceId,
            ActionCardType inspectedType,
            ActionCardInstance inspectedInstance) {
        layout.rebuildHand(combat);
        drawInfoPanelFrame(layout);
        drawHandViewportFrame(layout.attackPanel);
        drawHandViewportFrame(layout.changePanel);
        drawHandZone(combat, layout, layout.attackPanel, batch, font, draggingInstanceId);
        drawHandZone(combat, layout, layout.changePanel, batch, font, draggingInstanceId);
        drawConfirmShape(layout, combat);
        batch.begin();
        CombatInfoPanelDrawer.draw(batch, font, layout, inspectedType, inspectedInstance);
        batch.end();
    }

    /** Card ghost following the pointer while dragging from hand to a slot. */
    public void renderDraggedCard(
            CombatController combat,
            CombatCardLayout layout,
            SpriteBatch batch,
            BitmapFont font,
            int instanceId,
            float pointerX,
            float pointerY) {
        ActionCardInstance card = findHandCard(combat, instanceId);
        if (card == null) {
            return;
        }
        float x = pointerX - layout.cardW / 2f;
        float y = pointerY - layout.cardH / 2f;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(CARD_SELECTED);
        shapes.rect(x - 3f, y - 3f, layout.cardW + 6f, layout.cardH + 6f);
        Color fill = colorFor(card.getType());
        fill.a = 0.82f;
        drawCardShape(x, y, layout.cardW, layout.cardH, fill);
        fill.a = 1f;
        shapes.end();
        batch.begin();
        ActionCardUiText.drawCardFaceCentered(batch, font, card.getType(),
                x, y, layout.cardW, layout.cardH, Color.WHITE);
        batch.end();
    }

    /** Four timeline slots between hand rows and fighters. */
    public void renderSlotsAndControls(
            CombatController combat,
            CombatCardLayout layout,
            SpriteBatch batch,
            BitmapFont font,
            int hoveredDismissSlot,
            boolean pressedDismiss) {
        CombatPhase phase = combat.getPhase();
        int resolvingSlot = combat.getResolvingSlotDisplayIndex();
        layout.rebuildHand(combat);

        drawSlotShapes(combat, layout, phase, resolvingSlot);

        batch.begin();
        drawSlotNames(batch, font, layout, combat);
        drawSlotDismissButtons(combat, layout, batch, hoveredDismissSlot, pressedDismiss);
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
            if (combat.isEnemySlotIndex(i)) {
                drawCardShape(innerX, innerY, innerW, innerH, ENEMY_CARD);
                continue;
            }
            ActionCardInstance card = combat.getSlotCard(i);
            if (card != null) {
                Integer selected = combat.getSelectedInstanceId();
                if (selected != null && selected == card.getInstanceId()) {
                    shapes.setColor(CARD_SELECTED);
                    shapes.rect(innerX - 3f, innerY - 3f, innerW + 6f, innerH + 6f);
                }
                Color fill = card.isOnCooldown() ? UiColors.CARD_ON_COOLDOWN_FILL : colorFor(card.getType());
                drawCardShape(innerX, innerY, innerW, innerH, fill);
            }
        }
        shapes.end();
    }

    private void drawInfoPanelFrame(CombatCardLayout layout) {
        float x = layout.infoPanelX;
        float y = layout.infoPanelY;
        float w = layout.infoPanelW;
        float h = layout.infoPanelH;
        float border = GameConfig.COMBAT_INFO_PANEL_BORDER;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(HAND_VIEWPORT_BORDER);
        shapes.rect(x, y, w, border);
        shapes.rect(x, y + h - border, w, border);
        shapes.rect(x, y, border, h);
        shapes.rect(x + w - border, y, border, h);
        shapes.setColor(HAND_VIEWPORT_BG);
        shapes.rect(x + border, y + border, w - 2f * border, h - 2f * border);
        shapes.end();
    }

    private void drawHandViewportFrame(HandZonePanel panel) {
        float x = panel.viewportX;
        float y = panel.viewportY;
        float w = panel.viewportW;
        float h = panel.viewportH;
        float border = GameConfig.COMBAT_HAND_BORDER;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(HAND_VIEWPORT_BORDER);
        shapes.rect(x, y, w, border);
        shapes.rect(x, y + h - border, w, border);
        shapes.rect(x, y, border, h);
        shapes.rect(x + w - border, y, border, h);
        shapes.setColor(HAND_VIEWPORT_BG);
        shapes.rect(x + border, y + border, w - 2f * border, h - 2f * border);
        shapes.end();
    }

    private void drawHandZone(
            CombatController combat,
            CombatCardLayout layout,
            HandZonePanel panel,
            SpriteBatch batch,
            BitmapFont font,
            int draggingInstanceId) {
        if (!pushHandClip(shapes.getProjectionMatrix(), layout, panel)) {
            return;
        }
        drawHandShapes(combat, layout, panel, draggingInstanceId);
        batch.begin();
        drawHandNames(batch, font, layout, combat, panel, draggingInstanceId);
        batch.end();
        popHandClip();
    }

    private boolean pushHandClip(Matrix4 projection, CombatCardLayout layout, HandZonePanel panel) {
        scissorBounds.set(panel.clipX(), panel.clipY(), panel.clipW(), panel.clipH());
        scissorCamera.combined.set(projection);
        ScissorStack.calculateScissors(scissorCamera, identityTransform, scissorBounds, scissorResult);
        return ScissorStack.pushScissors(scissorResult);
    }

    private static void popHandClip() {
        ScissorStack.popScissors();
    }

    private void drawHandShapes(
            CombatController combat,
            CombatCardLayout layout,
            HandZonePanel panel,
            int draggingInstanceId) {
        Integer selected = combat.getSelectedInstanceId();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (CombatCardLayout.HandEntry entry : panel.getEntries()) {
            if (entry.instanceId == draggingInstanceId) {
                continue;
            }
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

    private void drawSlotDismissButtons(
            CombatController combat,
            CombatCardLayout layout,
            SpriteBatch batch,
            int hoveredDismissSlot,
            boolean pressedDismiss) {
        if (combat.getPhase() != CombatPhase.PLANNING) {
            return;
        }
        Integer selected = combat.getSelectedInstanceId();
        if (selected == null) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            if (!combat.isPlayerSlot(i)) {
                continue;
            }
            ActionCardInstance card = combat.getSlotCard(i);
            if (card == null || card.getInstanceId() != selected) {
                continue;
            }
            boolean hovered = i == hoveredDismissSlot;
            boolean pressed = pressedDismiss && hovered;
            dismissButton.draw(batch, layout.slotDismissX(i), layout.slotDismissY(i), hovered, pressed);
        }
    }

    private void drawSlotNames(SpriteBatch batch, BitmapFont font, CombatCardLayout layout, CombatController combat) {
        for (int i = 0; i < 4; i++) {
            float innerX = layout.innerCardX(i);
            float innerY = layout.innerCardY();
            float innerW = layout.innerCardW();
            float innerH = layout.innerCardH();
            if (combat.isEnemySlotIndex(i)) {
                ActionCardType type = combat.getPlannedEnemyCardForSlot(i);
                if (type != null) {
                    ActionCardUiText.drawCardFaceCentered(batch, font, type,
                            innerX, innerY, innerW, innerH, Color.WHITE);
                }
                continue;
            }
            ActionCardInstance card = combat.getSlotCard(i);
            if (card != null) {
                Color textColor = card.isOnCooldown() ? UiColors.CARD_ON_COOLDOWN_TEXT : Color.WHITE;
                ActionCardUiText.drawCardFaceCentered(batch, font, card.getType(),
                        innerX, innerY, innerW, innerH, textColor);
            }
        }
    }

    private void drawHandNames(
            SpriteBatch batch,
            BitmapFont font,
            CombatCardLayout layout,
            CombatController combat,
            HandZonePanel panel,
            int draggingInstanceId) {
        for (CombatCardLayout.HandEntry entry : panel.getEntries()) {
            if (entry.instanceId == draggingInstanceId) {
                continue;
            }
            ActionCardInstance card = findHandCard(combat, entry.instanceId);
            if (card == null) {
                continue;
            }
            Color textColor = card.isOnCooldown() ? UiColors.CARD_ON_COOLDOWN_TEXT : Color.WHITE;
            ActionCardUiText.drawCardFaceCentered(batch, font, card.getType(),
                    entry.x, entry.y, layout.cardW, layout.cardH, textColor);
        }
    }

    private void drawCardShape(float x, float y, float w, float h, Color fill) {
        shapes.setColor(fill);
        shapes.rect(x, y, w, h);
    }

    private static Color colorFor(ActionCardType type) {
        return switch (type) {
            case ATTACK -> CARD_ATTACK;
            case STRIKE -> CARD_STRONG;
            case HEAL -> CARD_HEAL;
            case SHIELD -> CARD_SHIELD;
            case ASSAULT -> CARD_FULL_POWER;
            case LIFE_MAGIC -> CARD_LIFE_MAGIC;
            case AMBUSH -> CARD_THRUST;
            case POISON_MAGIC -> CARD_POISON;
            case CLAW, CHARGED_SLASH, BLADE, GREAT_BLADE, VAMPIRISM, MAGIC_BOLT, POISON_BOLT, MAGIC_ARROW,
                    ARROW, POISON_ARROW ->
                    CARD_STRONG;
            case PURIFY, MAGIC_MIRROR -> CARD_HEAL;
        };
    }

    private static ActionCardInstance findHandCard(CombatController combat, int instanceId) {
        return combat.findCard(instanceId);
    }
}
