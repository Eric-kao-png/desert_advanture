package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameMessages;
import com.desertadventure.item.Inventory;
import com.desertadventure.item.ItemType;
import com.desertadventure.presentation.sprites.ItemSpriteRegistry;
import com.desertadventure.screen.CharacterOverlayInput;
import com.desertadventure.screen.CharacterOverlayLayout;
import com.desertadventure.state.GameSession;

/** Three-column character panel: backpack, portrait placeholder, stats. */
public class CharacterOverlayRenderer {
    private static final Color COLUMN_DIVIDER = new Color(0.35f, 0.32f, 0.28f, 0.9f);
    private static final Color PORTRAIT_FILL = new Color(0.09f, 0.08f, 0.07f, 0.85f);
    private static final Color PORTRAIT_BORDER = new Color(0.45f, 0.4f, 0.34f, 1f);
    private static final Color BAR_BORDER = new Color(0.5f, 0.45f, 0.38f, 1f);
    private static final Color HP_BAR_BG = new Color(0.22f, 0.1f, 0.1f, 0.95f);
    private static final Color HP_BAR_FILL = new Color(0.82f, 0.22f, 0.2f, 1f);
    private static final Color STAMINA_BAR_BG = new Color(0.14f, 0.16f, 0.1f, 0.95f);
    private static final Color STAMINA_BAR_FILL = new Color(0.78f, 0.72f, 0.28f, 1f);

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final ItemSpriteRegistry itemSprites = new ItemSpriteRegistry();
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private float animTime;

    public void setProjection(Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
    }

    public void render(
            SpriteBatch batch,
            GameSession session,
            BitmapFont font,
            CharacterOverlayLayout layout,
            CharacterOverlayInput input,
            float delta) {
        animTime += delta;
        boolean showDetail = shouldShowDetail(session, input) && !input.isDragging();

        drawBaseShapes(layout, input);
        drawStatBars(session, layout);

        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        batch.begin();
        drawTitle(batch, font, layout);
        drawInventorySection(batch, font, layout, session, input);
        drawPortraitPlaceholder(batch, font, layout);
        drawStatsSection(batch, font, session, layout, lineH);
        batch.end();

        if (showDetail) {
            drawDetailOverlay(layout, input);
            batch.begin();
            drawDetailContent(batch, font, layout, input, session);
            font.setColor(Color.WHITE);
            batch.end();
        }
    }

    private void drawTitle(SpriteBatch batch, BitmapFont font, CharacterOverlayLayout layout) {
        glyphLayout.setText(font, GameMessages.CHARACTER_PANEL_TITLE);
        float titleX = layout.getPanelX() + (layout.getPanelW() - glyphLayout.width) / 2f;
        font.setColor(Color.WHITE);
        font.draw(batch, GameMessages.CHARACTER_PANEL_TITLE, titleX, layout.getTitleY());
    }

    private void drawInventorySection(
            SpriteBatch batch,
            BitmapFont font,
            CharacterOverlayLayout layout,
            GameSession session,
            CharacterOverlayInput input) {
        font.setColor(0.85f, 0.75f, 0.55f, 1f);
        font.draw(batch, GameMessages.INVENTORY_SECTION,
                layout.getInventoryTitleX(), layout.getInventoryTitleY());
        drawSlotIcons(batch, layout, session, input);
        drawDraggedIcon(batch, layout, input);
    }

    private void drawPortraitPlaceholder(SpriteBatch batch, BitmapFont font, CharacterOverlayLayout layout) {
        glyphLayout.setText(font, GameMessages.CHARACTER_PORTRAIT_PLACEHOLDER);
        float textX = layout.getPortraitX() + (layout.getPortraitW() - glyphLayout.width) / 2f;
        float textY = layout.getPortraitY() + layout.getPortraitH() / 2f;
        font.setColor(0.45f, 0.42f, 0.38f, 1f);
        font.draw(batch, GameMessages.CHARACTER_PORTRAIT_PLACEHOLDER, textX, textY);
    }

    private void drawStatBars(GameSession session, CharacterOverlayLayout layout) {
        var stats = session.getPlayerStats();
        var steps = session.getStepBudget();

        float barX = layout.getStatBarX();
        float barW = layout.getStatBarW();
        float barH = layout.getStatBarH();

        float hpRatio = stats.getMaxHp() > 0f ? stats.getHp() / stats.getMaxHp() : 0f;
        StatBarDrawer.draw(shapes, barX, layout.getHpBarBottom(), barW, barH,
                hpRatio, HP_BAR_BG, HP_BAR_FILL, BAR_BORDER);

        float maxSteps = steps.getStepBudget();
        float staminaRatio = maxSteps > 0f ? steps.getRemainingSteps() / maxSteps : 0f;
        StatBarDrawer.draw(shapes, barX, layout.getStaminaBarBottom(), barW, barH,
                staminaRatio, STAMINA_BAR_BG, STAMINA_BAR_FILL, BAR_BORDER);
    }

    private void drawStatsSection(
            SpriteBatch batch,
            BitmapFont font,
            GameSession session,
            CharacterOverlayLayout layout,
            float lineH) {
        float textX = layout.getStatsTextX();
        var stats = session.getPlayerStats();
        var steps = session.getStepBudget();

        font.setColor(0.85f, 0.75f, 0.55f, 1f);
        font.draw(batch, GameMessages.STAT_SECTION_HEALTH, textX, layout.getStatsTopY());
        drawBarValueLabel(batch, font, layout.getStatBarX(), layout.getStatBarW(),
                layout.getHpBarBottom(), layout.getStatBarH(),
                String.format("%.0f / %.0f", stats.getHp(), stats.getMaxHp()));

        font.draw(batch, GameMessages.STAT_SECTION_COMBAT, textX, layout.getCombatSectionY());
        font.setColor(Color.WHITE);
        font.draw(batch, formatAttack(session), textX + 8f, layout.getCombatSectionY() - lineH);

        font.setColor(0.85f, 0.75f, 0.55f, 1f);
        font.draw(batch, GameMessages.STAT_SECTION_EXPLORATION, textX, layout.getExplorationSectionY());
        drawBarValueLabel(batch, font, layout.getStatBarX(), layout.getStatBarW(),
                layout.getStaminaBarBottom(), layout.getStatBarH(),
                String.format("%.1f / %.1f", steps.getRemainingSteps(), steps.getStepBudget()));
    }

    private void drawBarValueLabel(
            SpriteBatch batch,
            BitmapFont font,
            float barX,
            float barW,
            float barBottom,
            float barHeight,
            String text) {
        glyphLayout.setText(font, text);
        float textX = barX + barW - glyphLayout.width - 6f;
        float textY = barBottom + (barHeight + glyphLayout.height) / 2f;
        font.setColor(0.95f, 0.95f, 0.95f, 1f);
        font.draw(batch, text, textX, textY);
    }

    private void drawBaseShapes(CharacterOverlayLayout layout, CharacterOverlayInput input) {
        ShapeDrawer.fillRect(shapes, 0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT,
                new Color(0f, 0f, 0f, GameConfig.MAP_OVERLAY_DIM_ALPHA));

        ShapeDrawer.fillRect(shapes, layout.getPanelX(), layout.getPanelY(),
                layout.getPanelW(), layout.getPanelH(),
                new Color(0.12f, 0.11f, 0.1f, GameConfig.CHARACTER_PANEL_BG_ALPHA));

        drawColumnDividers(layout);
        drawPortraitFrame(layout);
        drawSlotFrames(layout, input);
    }

    private void drawColumnDividers(CharacterOverlayLayout layout) {
        float top = layout.getHeaderBottom() - GameConfig.CHARACTER_PANEL_LINE_HEIGHT * 0.35f;
        float bottom = layout.getContentBottom();
        float h = top - bottom;
        float w = 2f;
        float x1 = layout.getCenterColX() - GameConfig.CHARACTER_PANEL_COLUMN_GAP / 2f - w / 2f;
        float x2 = layout.getRightColX() - GameConfig.CHARACTER_PANEL_COLUMN_GAP / 2f - w / 2f;
        ShapeDrawer.fillRect(shapes, x1, bottom, w, h, COLUMN_DIVIDER);
        ShapeDrawer.fillRect(shapes, x2, bottom, w, h, COLUMN_DIVIDER);
    }

    private void drawPortraitFrame(CharacterOverlayLayout layout) {
        ShapeDrawer.fillRect(shapes, layout.getPortraitX(), layout.getPortraitY(),
                layout.getPortraitW(), layout.getPortraitH(), PORTRAIT_FILL);
        ShapeDrawer.strokeRect(shapes, layout.getPortraitX(), layout.getPortraitY(),
                layout.getPortraitW(), layout.getPortraitH(), PORTRAIT_BORDER, 2f);
    }

    private void drawDetailOverlay(CharacterOverlayLayout layout, CharacterOverlayInput input) {
        ShapeDrawer.fillRect(shapes, layout.getPanelX(), layout.getPanelY(),
                layout.getPanelW(), layout.getPanelH(),
                new Color(0f, 0f, 0f, 0.55f));
        ShapeDrawer.fillRect(shapes, layout.getDetailX(), layout.getDetailY(),
                layout.getDetailW(), layout.getDetailH(), new Color(0.1f, 0.09f, 0.08f, 0.98f));
        ShapeDrawer.strokeRect(shapes, layout.getDetailX(), layout.getDetailY(),
                layout.getDetailW(), layout.getDetailH(), new Color(0.75f, 0.65f, 0.4f, 1f), 2f);
        drawButtonShapes(layout, input.isHoveredUse(), true);
        drawButtonShapes(layout, input.isHoveredClose(), false);
    }

    private void drawSlotFrames(CharacterOverlayLayout layout, CharacterOverlayInput input) {
        float slotSize = layout.slotSize();
        for (int i = 0; i < layout.getSlotCount(); i++) {
            float left = layout.slotLeft(i);
            float bottom = layout.slotBottom(i);
            ShapeDrawer.fillRect(shapes, left + 2f, bottom + 2f, slotSize - 4f, slotSize - 4f,
                    new Color(0.08f, 0.07f, 0.06f, 0.75f));
            boolean selected = input.hasSelection() && input.getSelectedSlot() == i;
            boolean dropTarget = input.isDragging() && i == input.getHoveredSlot()
                    && i != input.getDragSourceSlot();
            boolean hovered = i == input.getHoveredSlot();
            Color border = dropTarget
                    ? new Color(0.55f, 0.85f, 0.45f, 1f)
                    : selected
                    ? new Color(0.95f, 0.8f, 0.35f, 1f)
                    : hovered
                    ? new Color(0.55f, 0.5f, 0.42f, 1f)
                    : new Color(0.28f, 0.26f, 0.22f, 1f);
            ShapeDrawer.strokeRect(shapes, left, bottom, slotSize, slotSize, border, 2f);
        }
    }

    private void drawSlotIcons(
            SpriteBatch batch,
            CharacterOverlayLayout layout,
            GameSession session,
            CharacterOverlayInput input) {
        Inventory inventory = session.getInventory();
        float slotDrawSize = layout.slotSize() * 0.88f;
        batch.setColor(Color.WHITE);
        for (int i = 0; i < layout.getSlotCount(); i++) {
            if (input.isDragging() && i == input.getDragSourceSlot()) {
                continue;
            }
            ItemType type = inventory.getItemAt(i);
            if (type == null) {
                continue;
            }
            drawItemIcon(batch, layout, type, i, slotDrawSize, 1f);
        }
    }

    private void drawDraggedIcon(
            SpriteBatch batch,
            CharacterOverlayLayout layout,
            CharacterOverlayInput input) {
        if (!input.isDragging() || input.getDragItem() == null) {
            return;
        }
        float slotDrawSize = layout.slotSize() * 0.92f;
        float iconX = input.getDragX() - slotDrawSize / 2f;
        float iconY = input.getDragY() - slotDrawSize / 2f;
        TextureRegion frame = itemSprites.getFrame(input.getDragItem(), animTime);
        batch.setColor(1f, 1f, 1f, 0.9f);
        batch.draw(frame, iconX, iconY, slotDrawSize, slotDrawSize);
        batch.setColor(Color.WHITE);
    }

    private void drawItemIcon(
            SpriteBatch batch,
            CharacterOverlayLayout layout,
            ItemType type,
            int slotIndex,
            float drawSize,
            float alpha) {
        float left = layout.slotLeft(slotIndex);
        float bottom = layout.slotBottom(slotIndex);
        float iconX = left + (layout.slotSize() - drawSize) / 2f;
        float iconY = bottom + (layout.slotSize() - drawSize) / 2f;
        TextureRegion frame = itemSprites.getFrame(type, animTime);
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(frame, iconX, iconY, drawSize, drawSize);
        batch.setColor(Color.WHITE);
    }

    private boolean shouldShowDetail(GameSession session, CharacterOverlayInput input) {
        if (!input.hasSelection()) {
            return false;
        }
        return !session.getInventory().isSlotEmpty(input.getSelectedSlot());
    }

    private void drawDetailContent(
            SpriteBatch batch,
            BitmapFont font,
            CharacterOverlayLayout layout,
            CharacterOverlayInput input,
            GameSession session) {
        ItemType type = session.getInventory().getItemAt(input.getSelectedSlot());
        if (type == null) {
            return;
        }
        float dx = layout.getDetailX();

        float spriteSize = GameConfig.INVENTORY_DETAIL_SPRITE_SIZE;
        TextureRegion frame = itemSprites.getFrame(type, animTime);
        float spriteX = layout.detailSpriteCenterX() - spriteSize / 2f;
        float spriteY = layout.detailSpriteBottom() - spriteSize;
        batch.draw(frame, spriteX, spriteY, spriteSize, spriteSize);

        font.setColor(Color.WHITE);
        font.draw(batch, type.getDisplayName(), dx + 20f, layout.detailNameY());
        font.setColor(0.82f, 0.82f, 0.82f, 1f);
        font.draw(batch, type.getDescription(), dx + 20f, layout.detailDescY());
        drawButtonLabels(batch, font, layout, GameMessages.INVENTORY_USE, input.isHoveredUse(), true);
        drawButtonLabels(batch, font, layout, GameMessages.INVENTORY_CLOSE, input.isHoveredClose(), false);
    }

    private void drawButtonShapes(CharacterOverlayLayout layout, boolean hovered, boolean useButton) {
        float x = useButton ? layout.getUseButtonX() : layout.getCloseButtonX();
        float y = useButton ? layout.getUseButtonY() : layout.getCloseButtonY();
        float w = layout.getButtonW();
        float h = layout.getButtonH();
        Color fill = hovered
                ? new Color(0.45f, 0.4f, 0.28f, 1f)
                : new Color(0.32f, 0.28f, 0.2f, 1f);
        ShapeDrawer.fillRect(shapes, x, y, w, h, fill);
        ShapeDrawer.strokeRect(shapes, x, y, w, h, new Color(0.7f, 0.62f, 0.4f, 1f), 2f);
    }

    private void drawButtonLabels(
            SpriteBatch batch,
            BitmapFont font,
            CharacterOverlayLayout layout,
            String label,
            boolean hovered,
            boolean useButton) {
        float x = useButton ? layout.getUseButtonX() : layout.getCloseButtonX();
        float y = useButton ? layout.getUseButtonY() : layout.getCloseButtonY();
        float w = layout.getButtonW();
        float h = layout.getButtonH();
        glyphLayout.setText(font, label);
        font.setColor(hovered ? Color.WHITE : new Color(0.9f, 0.9f, 0.9f, 1f));
        font.draw(batch, label,
                x + (w - glyphLayout.width) / 2f,
                y + (h + glyphLayout.height) / 2f);
    }

    public void dispose() {
        shapes.dispose();
        itemSprites.dispose();
    }

    private static String formatAttack(GameSession session) {
        return String.format("%s %d", GameMessages.STAT_ATTACK, session.getPlayerStats().getAttack());
    }
}
