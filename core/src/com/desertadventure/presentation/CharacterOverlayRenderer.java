package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.desertadventure.config.GameMessages;
import com.desertadventure.item.ItemType;
import com.desertadventure.presentation.character.CharacterInventoryView;
import com.desertadventure.presentation.character.CharacterItemDetailView;
import com.desertadventure.presentation.character.CharacterPanelBackdrop;
import com.desertadventure.presentation.character.CharacterStatsView;
import com.desertadventure.presentation.sprites.ItemSpriteRegistry;
import com.desertadventure.screen.CharacterOverlayInput;
import com.desertadventure.screen.CharacterOverlayLayout;
import com.desertadventure.state.GameSession;

/** Coordinates character panel sub-renderers. */
public class CharacterOverlayRenderer {
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final ItemSpriteRegistry itemSprites = new ItemSpriteRegistry();
    private final CharacterInventoryView inventoryView;
    private final CharacterStatsView statsView = new CharacterStatsView();
    private final CharacterItemDetailView detailView;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private float animTime;

    public CharacterOverlayRenderer() {
        inventoryView = new CharacterInventoryView(itemSprites);
        detailView = new CharacterItemDetailView(itemSprites);
    }

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

        CharacterPanelBackdrop.draw(shapes, layout);
        inventoryView.drawFrames(shapes, layout, input);
        statsView.drawBars(shapes, session, layout);

        batch.begin();
        drawTitle(batch, font, layout);
        inventoryView.drawLabelsAndIcons(batch, font, layout, session.getInventory(), input, animTime);
        statsView.drawPortraitPlaceholder(batch, font, layout);
        statsView.drawLabels(batch, font, session, layout);
        batch.end();

        if (showDetail) {
            detailView.drawOverlay(shapes, layout, input);
            batch.begin();
            ItemType type = session.getInventory().getItemAt(input.getSelectedSlot());
            detailView.drawContent(batch, font, layout, input, type, animTime);
            font.setColor(Color.WHITE);
            batch.end();
        }
    }

    public void dispose() {
        shapes.dispose();
        itemSprites.dispose();
    }

    private void drawTitle(SpriteBatch batch, BitmapFont font, CharacterOverlayLayout layout) {
        glyphLayout.setText(font, GameMessages.CHARACTER_PANEL_TITLE);
        float titleX = layout.getPanelX() + (layout.getPanelW() - glyphLayout.width) / 2f;
        font.setColor(Color.WHITE);
        font.draw(batch, GameMessages.CHARACTER_PANEL_TITLE, titleX, layout.getTitleY());
    }

    private boolean shouldShowDetail(GameSession session, CharacterOverlayInput input) {
        if (!input.hasSelection()) {
            return false;
        }
        return !session.getInventory().isSlotEmpty(input.getSelectedSlot());
    }
}
