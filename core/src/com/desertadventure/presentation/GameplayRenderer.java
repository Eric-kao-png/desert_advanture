package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.GameConfig;
import com.desertadventure.presentation.sprites.DesertSpriteAtlas;
import com.desertadventure.screen.layout.CombatCardLayout;
import com.desertadventure.screen.layout.CombatSceneLayout;

import java.util.List;

/** Facade for hub/combat visuals: sky, ground, combat shapes. */
public class GameplayRenderer implements com.badlogic.gdx.utils.Disposable {
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Matrix4 screenProjection = new Matrix4();
    private final DesertSpriteAtlas desertSprites;
    private final PlayerSpriteRenderer playerSprites;
    private final CombatEntityRenderer combatEntities = new CombatEntityRenderer();
    private final SkyBackdrop sky;
    private final TextureRegion floorTile;
    private final CombatCardRenderer combatCards;

    public GameplayRenderer() {
        screenProjection.setToOrtho2D(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT);
        desertSprites = new DesertSpriteAtlas();
        playerSprites = new PlayerSpriteRenderer();
        sky = new SkyBackdrop();
        floorTile = desertSprites.get(DesertSpriteAtlas.FLOOR_TILE);
        combatCards = new CombatCardRenderer(shapes);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        sky.dispose();
        desertSprites.dispose();
        playerSprites.dispose();
    }

    public void setProjectionMatrix(Matrix4 projection) {
        screenProjection.set(projection);
    }

    /** Sky and tiled ground for hub and combat. Batch must already be begun. */
    public void drawSceneBackground(SpriteBatch batch, float layoutBlend) {
        float w = GameConfig.VIEW_WIDTH;
        float h = GameConfig.VIEW_HEIGHT;
        float groundY = CombatSceneLayout.groundY(layoutBlend);
        batch.setProjectionMatrix(screenProjection);
        sky.draw(batch, w, h);
        drawFloor(batch, w, groundY);
    }

    private void drawFloor(SpriteBatch batch, float screenW, float groundY) {
        float tileH = groundY;
        float tileW = tileH * (floorTile.getRegionWidth() / (float) floorTile.getRegionHeight());
        for (float x = 0f; x < screenW; x += tileW) {
            batch.draw(floorTile, x, 0f, tileW, tileH);
        }
    }

    public void renderCombatHand(
            CombatController combat,
            CombatCardLayout layout,
            SpriteBatch batch,
            BitmapFont font) {
        shapes.setProjectionMatrix(screenProjection);
        combatCards.renderHand(combat, layout, batch, font);
    }

    public void renderCombatSlotsAndControls(
            CombatController combat,
            CombatCardLayout layout,
            SpriteBatch batch,
            BitmapFont font) {
        shapes.setProjectionMatrix(screenProjection);
        combatCards.renderSlotsAndControls(combat, layout, batch, font);
    }

    public void renderCombatEntities(CombatController combat, List<CombatEntity> entities, SpriteBatch batch, BitmapFont font) {
        shapes.setProjectionMatrix(screenProjection);
        combatEntities.drawBodies(shapes, entities);
        combatEntities.drawOverlays(shapes, entities, font);
        batch.setProjectionMatrix(screenProjection);
        batch.begin();
        playerSprites.drawCombatPlayerSprite(combat, batch);
        combatEntities.drawTexts(batch, entities, font, combat);
        batch.end();
    }

    static boolean shouldDrawCombatEntity(CombatEntity entity) {
        return entity.isAlive() || entity.getKind() == CombatEntity.Kind.PLAYER;
    }
}
