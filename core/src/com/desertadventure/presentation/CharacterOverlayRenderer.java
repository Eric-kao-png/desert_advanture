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

/** Character stats, backpack grid, and item detail popup. */
public class CharacterOverlayRenderer {
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
        boolean showDetail = shouldShowDetail(session, input);

        drawBaseShapes(layout, input);

        float pad = GameConfig.CHARACTER_PANEL_PADDING;
        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        float panelX = layout.getPanelX();
        float panelY = layout.getPanelY();
        float panelH = layout.getPanelH();
        float textX = panelX + pad;
        float y = panelY + panelH - pad;

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, GameMessages.CHARACTER_PANEL_TITLE, textX, y);
        y -= lineH * 1.15f;

        font.setColor(0.85f, 0.75f, 0.55f, 1f);
        font.draw(batch, GameMessages.STAT_SECTION_HEALTH, textX, y);
        y -= lineH;
        font.setColor(Color.WHITE);
        font.draw(batch, formatHp(session), textX + 12f, y);
        y -= lineH * 0.95f;

        font.setColor(0.85f, 0.75f, 0.55f, 1f);
        font.draw(batch, GameMessages.STAT_SECTION_COMBAT, textX, y);
        y -= lineH;
        font.setColor(Color.WHITE);
        font.draw(batch, formatAttack(session), textX + 12f, y);
        y -= lineH * 0.95f;

        font.setColor(0.85f, 0.75f, 0.55f, 1f);
        font.draw(batch, GameMessages.STAT_SECTION_EXPLORATION, textX, y);
        y -= lineH;
        font.setColor(Color.WHITE);
        font.draw(batch, formatStamina(session), textX + 12f, y);
        y -= lineH * 1.1f;

        font.setColor(0.85f, 0.75f, 0.55f, 1f);
        font.draw(batch, GameMessages.INVENTORY_SECTION, textX, y);

        drawSlotIcons(batch, layout, session);
        font.setColor(Color.WHITE);
        batch.end();

        if (showDetail) {
            drawDetailOverlay(layout, input);
            batch.begin();
            drawDetailContent(batch, font, layout, input, session);
            font.setColor(Color.WHITE);
            batch.end();
        }
    }

    private void drawBaseShapes(CharacterOverlayLayout layout, CharacterOverlayInput input) {
        ShapeDrawer.fillRect(shapes, 0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT,
                new Color(0f, 0f, 0f, GameConfig.MAP_OVERLAY_DIM_ALPHA));

        ShapeDrawer.fillRect(shapes, layout.getPanelX(), layout.getPanelY(),
                layout.getPanelW(), layout.getPanelH(),
                new Color(0.12f, 0.11f, 0.1f, GameConfig.CHARACTER_PANEL_BG_ALPHA));

        drawSlotFrames(layout, input);
    }

    /** Dims the panel, then draws the detail card and buttons on top of stats / grid. */
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
            boolean hovered = i == input.getHoveredSlot();
            Color border = selected
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
            GameSession session) {
        Inventory inventory = session.getInventory();
        float slotDrawSize = layout.slotSize() * 0.88f;
        batch.setColor(Color.WHITE);
        for (int i = 0; i < layout.getSlotCount(); i++) {
            ItemType type = inventory.getItemAt(i);
            if (type == null) {
                continue;
            }
            float left = layout.slotLeft(i);
            float bottom = layout.slotBottom(i);
            float iconX = left + (layout.slotSize() - slotDrawSize) / 2f;
            float iconY = bottom + (layout.slotSize() - slotDrawSize) / 2f;
            TextureRegion frame = itemSprites.getFrame(type, animTime);
            batch.draw(frame, iconX, iconY, slotDrawSize, slotDrawSize);
        }
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

    private static String formatHp(GameSession session) {
        var stats = session.getPlayerStats();
        return String.format("%s %.0f / %.0f", GameMessages.STAT_HP, stats.getHp(), stats.getMaxHp());
    }

    private static String formatAttack(GameSession session) {
        return String.format("%s %d", GameMessages.STAT_ATTACK, session.getPlayerStats().getAttack());
    }

    private static String formatStamina(GameSession session) {
        var steps = session.getStepBudget();
        return String.format("%s %.1f / %.1f",
                GameMessages.STAT_STAMINA, steps.getRemainingSteps(), steps.getStepBudget());
    }
}
