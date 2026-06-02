package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.combat.model.CombatEntity;

import java.util.List;

/**
 * Renders non-player combat bodies and combat overlays/text.
 *
 * <p>Behavior is intentionally identical to the previous inlined implementation in {@link GameplayRenderer}.</p>
 */
final class CombatEntityRenderer {
    void drawBodies(ShapeRenderer shapes, List<CombatEntity> entities) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (CombatEntity entity : entities) {
            if (!GameplayRenderer.shouldDrawCombatEntity(entity)) {
                continue;
            }
            if (entity.getKind() == CombatEntity.Kind.PLAYER) {
                continue;
            }
            shapes.setColor(CombatEntityColors.forEntity(entity, entity.getHurtFlash() > 0f));
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

    void drawTexts(SpriteBatch batch, List<CombatEntity> entities, BitmapFont font) {
        for (CombatEntity entity : entities) {
            if (!GameplayRenderer.shouldDrawCombatEntity(entity)) {
                continue;
            }
            CombatHpBarDrawer.drawHpText(batch, font, entity);
            CombatStatusDrawer.drawText(batch, font, entity);
        }
    }
}

