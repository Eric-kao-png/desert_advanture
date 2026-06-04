package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.system.CombatController;

import java.util.List;

/** Renders combat bodies, bars, and allowed numeric/name labels. */
final class CombatEntityRenderer {
    void drawBodies(ShapeRenderer shapes, List<CombatEntity> entities) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (CombatEntity entity : entities) {
            if (!GameplayRenderer.shouldDrawCombatEntity(entity)) {
                continue;
            }
            shapes.setColor(CombatEntityColors.forEntity(entity));
            shapes.rect(entity.getX() - entity.getWidth() / 2f, entity.getY(), entity.getWidth(), entity.getHeight());
        }
        shapes.end();
    }

    void drawOverlays(ShapeRenderer shapes, List<CombatEntity> entities, BitmapFont font) {
        for (CombatEntity entity : entities) {
            if (!GameplayRenderer.shouldDrawCombatEntity(entity)) {
                continue;
            }
            CombatHpBarDrawer.draw(shapes, entity);
            CombatStatusDrawer.drawPanels(shapes, entity, font);
        }
    }

    void drawTexts(SpriteBatch batch, List<CombatEntity> entities, BitmapFont font, CombatController combat) {
        String opponentName = combat.getOpponentDisplayName();
        for (CombatEntity entity : entities) {
            if (!GameplayRenderer.shouldDrawCombatEntity(entity)) {
                continue;
            }
            if (entity.getKind() != CombatEntity.Kind.PLAYER && opponentName != null) {
                CombatHpBarDrawer.drawOpponentNameAboveBar(batch, font, entity, opponentName);
            }
            CombatHpBarDrawer.drawHpText(batch, font, entity);
            CombatStatusDrawer.drawText(batch, font, entity);
        }
    }
}
