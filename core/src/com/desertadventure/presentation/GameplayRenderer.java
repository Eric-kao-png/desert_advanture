package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.GameConfig;
import com.desertadventure.screen.layout.CombatCardLayout;

import java.util.List;

/** Facade for combat shapes and card UI. Screen clear color is set by {@link com.desertadventure.screen.GameplayScreen}. */
public class GameplayRenderer implements com.badlogic.gdx.utils.Disposable {
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Matrix4 screenProjection = new Matrix4();
    private final CombatEntityRenderer combatEntities = new CombatEntityRenderer();
    private final CombatCardRenderer combatCards;

    public GameplayRenderer() {
        screenProjection.setToOrtho2D(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT);
        combatCards = new CombatCardRenderer(shapes);
    }

    @Override
    public void dispose() {
        combatCards.dispose();
        shapes.dispose();
    }

    public void setProjectionMatrix(Matrix4 projection) {
        screenProjection.set(projection);
    }

    public void renderCombatHand(
            CombatController combat,
            CombatCardLayout layout,
            SpriteBatch batch,
            BitmapFont font,
            int draggingInstanceId,
            com.desertadventure.combat.card.ActionCardType inspectedType,
            com.desertadventure.combat.card.ActionCardInstance inspectedInstance) {
        shapes.setProjectionMatrix(screenProjection);
        combatCards.renderHand(combat, layout, batch, font, draggingInstanceId, inspectedType, inspectedInstance);
    }

    public void renderCombatDraggedCard(
            CombatController combat,
            CombatCardLayout layout,
            SpriteBatch batch,
            BitmapFont font,
            int draggingInstanceId,
            float pointerX,
            float pointerY) {
        shapes.setProjectionMatrix(screenProjection);
        combatCards.renderDraggedCard(combat, layout, batch, font, draggingInstanceId, pointerX, pointerY);
    }

    public void renderCombatSlotsAndControls(
            CombatController combat,
            CombatCardLayout layout,
            SpriteBatch batch,
            BitmapFont font,
            int hoveredDismissSlot,
            boolean pressedDismiss) {
        shapes.setProjectionMatrix(screenProjection);
        combatCards.renderSlotsAndControls(combat, layout, batch, font, hoveredDismissSlot, pressedDismiss);
    }

    public void renderCombatEntities(CombatController combat, List<CombatEntity> entities, SpriteBatch batch, BitmapFont font) {
        shapes.setProjectionMatrix(screenProjection);
        combatEntities.drawBodies(shapes, entities);
        combatEntities.drawOverlays(shapes, entities, font);
        batch.setProjectionMatrix(screenProjection);
        batch.begin();
        combatEntities.drawTexts(batch, entities, font, combat);
        batch.end();
    }

    static boolean shouldDrawCombatEntity(CombatEntity entity) {
        return entity.isAlive() || entity.getKind() == CombatEntity.Kind.PLAYER;
    }
}
