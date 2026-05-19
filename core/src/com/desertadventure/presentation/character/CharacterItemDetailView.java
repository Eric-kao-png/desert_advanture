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
import com.desertadventure.screen.layout.CharacterPanelGeometry;
import com.desertadventure.screen.layout.ItemDetailLayout;

/** Item detail popup overlay. */
public final class CharacterItemDetailView {
    private final ItemSpriteRegistry itemSprites;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    public CharacterItemDetailView(ItemSpriteRegistry itemSprites) {
        this.itemSprites = itemSprites;
    }

    public void drawOverlay(ShapeRenderer shapes, CharacterOverlayLayout layout, CharacterOverlayInput input) {
        CharacterPanelGeometry panel = layout.panel();
        ItemDetailLayout detail = layout.detail();
        ShapeDrawer.fillRect(shapes, panel.panelX, panel.panelY, panel.panelW, panel.panelH, UiColors.DETAIL_DIM);
        ShapeDrawer.fillRect(shapes, detail.detailX, detail.detailY, detail.detailW, detail.detailH,
                UiColors.DETAIL_PANEL_FILL);
        ShapeDrawer.strokeRect(shapes, detail.detailX, detail.detailY, detail.detailW, detail.detailH,
                UiColors.DETAIL_PANEL_BORDER, GameConfig.CHARACTER_SLOT_BORDER_WIDTH);
        drawButton(shapes, detail, input.isHoveredUse(), true);
        drawButton(shapes, detail, input.isHoveredClose(), false);
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
        ItemDetailLayout detail = layout.detail();
        float pad = GameConfig.CHARACTER_DETAIL_TEXT_PAD;
        float spriteSize = GameConfig.INVENTORY_DETAIL_SPRITE_SIZE;
        TextureRegion frame = itemSprites.getFrame(type, animTime);
        float spriteX = detail.spriteCenterX() - spriteSize / 2f;
        float spriteY = detail.spriteBottom() - spriteSize;
        batch.draw(frame, spriteX, spriteY, spriteSize, spriteSize);

        font.setColor(Color.WHITE);
        font.draw(batch, type.getDisplayName(), detail.detailX + pad, detail.nameY());
        font.setColor(UiColors.DETAIL_BODY_TEXT);
        font.draw(batch, type.getDescription(), detail.detailX + pad, detail.descY());
        drawButtonLabel(batch, font, detail, GameMessages.INVENTORY_USE, input.isHoveredUse(), true);
        drawButtonLabel(batch, font, detail, GameMessages.INVENTORY_CLOSE, input.isHoveredClose(), false);
    }

    private void drawButton(ShapeRenderer shapes, ItemDetailLayout detail, boolean hovered, boolean use) {
        float x = use ? detail.useButtonX : detail.closeButtonX;
        float y = use ? detail.useButtonY : detail.closeButtonY;
        ShapeDrawer.fillRect(shapes, x, y, detail.buttonW, detail.buttonH,
                hovered ? UiColors.BUTTON_FILL_HOVERED : UiColors.BUTTON_FILL);
        ShapeDrawer.strokeRect(shapes, x, y, detail.buttonW, detail.buttonH, UiColors.BUTTON_BORDER,
                GameConfig.CHARACTER_SLOT_BORDER_WIDTH);
    }

    private void drawButtonLabel(
            SpriteBatch batch,
            BitmapFont font,
            ItemDetailLayout detail,
            String label,
            boolean hovered,
            boolean use) {
        float x = use ? detail.useButtonX : detail.closeButtonX;
        float y = use ? detail.useButtonY : detail.closeButtonY;
        glyphLayout.setText(font, label);
        font.setColor(hovered ? Color.WHITE : UiColors.BUTTON_LABEL);
        font.draw(batch, label, x + (detail.buttonW - glyphLayout.width) / 2f,
                y + (detail.buttonH + glyphLayout.height) / 2f);
    }
}
