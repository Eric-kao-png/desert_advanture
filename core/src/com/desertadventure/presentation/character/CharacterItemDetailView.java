package com.desertadventure.presentation.character;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameMessages;
import com.desertadventure.config.UiColors;
import com.desertadventure.item.ItemType;
import com.desertadventure.presentation.ShapeDrawer;
import com.desertadventure.presentation.sprites.ItemSpriteRegistry;
import com.desertadventure.screen.CharacterOverlayInput;
import com.desertadventure.screen.CharacterOverlayLayout;

/** Item detail popup overlay. */
public final class CharacterItemDetailView {
    private final ItemSpriteRegistry itemSprites;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    public CharacterItemDetailView(ItemSpriteRegistry itemSprites) {
        this.itemSprites = itemSprites;
    }

    public void drawOverlay(ShapeRenderer shapes, CharacterOverlayLayout layout, CharacterOverlayInput input) {
        ShapeDrawer.fillRect(shapes, layout.getPanelX(), layout.getPanelY(),
                layout.getPanelW(), layout.getPanelH(), UiColors.DETAIL_DIM);
        ShapeDrawer.fillRect(shapes, layout.getDetailX(), layout.getDetailY(),
                layout.getDetailW(), layout.getDetailH(), UiColors.DETAIL_PANEL_FILL);
        ShapeDrawer.strokeRect(shapes, layout.getDetailX(), layout.getDetailY(),
                layout.getDetailW(), layout.getDetailH(), UiColors.DETAIL_PANEL_BORDER,
                GameConfig.CHARACTER_SLOT_BORDER_WIDTH);
        drawButton(shapes, layout, input.isHoveredUse(), true);
        drawButton(shapes, layout, input.isHoveredClose(), false);
    }

    public void drawContent(
            SpriteBatch batch,
            BitmapFont font,
            CharacterOverlayLayout layout,
            CharacterOverlayInput input,
            ItemType type,
            float animTime) {
        if (type == null) {
            return;
        }
        float pad = GameConfig.CHARACTER_DETAIL_TEXT_PAD;
        float spriteSize = GameConfig.INVENTORY_DETAIL_SPRITE_SIZE;
        TextureRegion frame = itemSprites.getFrame(type, animTime);
        float spriteX = layout.detailSpriteCenterX() - spriteSize / 2f;
        float spriteY = layout.detailSpriteBottom() - spriteSize;
        batch.draw(frame, spriteX, spriteY, spriteSize, spriteSize);

        font.setColor(Color.WHITE);
        font.draw(batch, type.getDisplayName(), layout.getDetailX() + pad, layout.detailNameY());
        font.setColor(UiColors.DETAIL_BODY_TEXT);
        font.draw(batch, type.getDescription(), layout.getDetailX() + pad, layout.detailDescY());
        drawButtonLabel(batch, font, layout, GameMessages.INVENTORY_USE, input.isHoveredUse(), true);
        drawButtonLabel(batch, font, layout, GameMessages.INVENTORY_CLOSE, input.isHoveredClose(), false);
    }

    private void drawButton(ShapeRenderer shapes, CharacterOverlayLayout layout, boolean hovered, boolean use) {
        float x = use ? layout.getUseButtonX() : layout.getCloseButtonX();
        float y = use ? layout.getUseButtonY() : layout.getCloseButtonY();
        float w = layout.getButtonW();
        float h = layout.getButtonH();
        ShapeDrawer.fillRect(shapes, x, y, w, h, hovered ? UiColors.BUTTON_FILL_HOVERED : UiColors.BUTTON_FILL);
        ShapeDrawer.strokeRect(shapes, x, y, w, h, UiColors.BUTTON_BORDER, GameConfig.CHARACTER_SLOT_BORDER_WIDTH);
    }

    private void drawButtonLabel(
            SpriteBatch batch,
            BitmapFont font,
            CharacterOverlayLayout layout,
            String label,
            boolean hovered,
            boolean use) {
        float x = use ? layout.getUseButtonX() : layout.getCloseButtonX();
        float y = use ? layout.getUseButtonY() : layout.getCloseButtonY();
        float w = layout.getButtonW();
        float h = layout.getButtonH();
        glyphLayout.setText(font, label);
        font.setColor(hovered ? Color.WHITE : UiColors.BUTTON_LABEL);
        font.draw(batch, label, x + (w - glyphLayout.width) / 2f, y + (h + glyphLayout.height) / 2f);
    }
}
