package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.view.MapOverlayLayout;
import com.desertadventure.presentation.sprites.DesertSpriteAtlas;
import com.desertadventure.state.GameSession;

import java.util.List;

/** Facade for gameplay visuals: parallax, houses, map overlay, combat shapes. */
public class GameplayRenderer implements com.badlogic.gdx.utils.Disposable {
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Matrix4 screenProjection = new Matrix4();
    private final DesertSpriteAtlas desertSprites;
    private final SkyBackdrop sky;
    private final ParallaxBackground parallax;
    private final BackgroundHouseSpawner houses;
    private final MapOverlayRenderer mapOverlay;

    public GameplayRenderer() {
        screenProjection.setToOrtho2D(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT);
        desertSprites = new DesertSpriteAtlas();
        sky = new SkyBackdrop();
        parallax = new ParallaxBackground(desertSprites.get(DesertSpriteAtlas.FLOOR_TILE));
        houses = new BackgroundHouseSpawner(desertSprites);
        mapOverlay = new MapOverlayRenderer();
    }

    @Override
    public void dispose() {
        shapes.dispose();
        sky.dispose();
        parallax.dispose();
        desertSprites.dispose();
        mapOverlay.dispose();
    }

    public void setProjectionMatrix(Matrix4 projection) {
        screenProjection.set(projection);
        mapOverlay.setProjection(projection);
    }

    public void repopulateHouseProps(float screenW) {
        houses.repopulate(screenW);
    }

    /** sky → sun → back → houses → middle → forward → floor. Batch must already be begun. */
    public void drawParallaxBackground(SpriteBatch batch, boolean running, float delta) {
        float w = GameConfig.VIEW_WIDTH;
        float h = GameConfig.VIEW_HEIGHT;
        parallax.scroll(delta, running);
        houses.scroll(delta, running);
        batch.setProjectionMatrix(screenProjection);
        sky.draw(batch, w, h);
        parallax.drawBack(batch, w, h);
        houses.draw(batch, w);
        parallax.drawMiddle(batch, w, h);
        parallax.drawForward(batch, w, h);
        parallax.drawFloor(batch, w, GameConfig.EXPLORE_GROUND_Y);
    }

    public void renderExploreForeground(GameSession session, boolean running) {
        float w = GameConfig.VIEW_WIDTH;
        float h = GameConfig.VIEW_HEIGHT;
        drawPlayer(w * GameConfig.EXPLORE_PLAYER_X_RATIO, GameConfig.EXPLORE_GROUND_Y, running);
        ShapeDrawer.fillRect(shapes, 0, h - GameConfig.HUD_TOP_BAR_HEIGHT, w, GameConfig.HUD_TOP_BAR_HEIGHT,
                new Color(0f, 0f, 0f, GameConfig.HUD_TOP_BAR_ALPHA));
    }

    public void renderCombatEntities(List<CombatEntity> entities) {
        shapes.setProjectionMatrix(screenProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (CombatEntity entity : entities) {
            if (!entity.isAlive() && entity.getKind() != CombatEntity.Kind.PLAYER) {
                continue;
            }
            shapes.setColor(CombatEntityColors.forEntity(entity, entity.getHurtFlash() > 0f));
            shapes.rect(entity.getX() - entity.getWidth() / 2f, entity.getY(),
                    entity.getWidth(), entity.getHeight());
        }
        shapes.end();
    }

    public void renderMapOverlay(GameSession session, MapOverlayLayout layout, GridPos hoverCell) {
        mapOverlay.render(session, layout, hoverCell);
    }

    public void renderStorm(float progress) {
        float alpha = Math.min(1f, progress);
        shapes.setProjectionMatrix(screenProjection);
        ShapeDrawer.fillRect(shapes, 0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT,
                new Color(0.9f, 0.75f, 0.35f, alpha * GameConfig.STORM_OVERLAY_ALPHA_SCALE));
    }

    private void drawPlayer(float x, float y, boolean running) {
        shapes.setProjectionMatrix(screenProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.2f, 0.5f, 0.95f, 1f);
        float bob = running
                ? (float) (Math.sin(System.currentTimeMillis() * GameConfig.PLAYER_BOB_FREQUENCY)
                * GameConfig.PLAYER_BOB_AMPLITUDE)
                : 0f;
        shapes.rect(x - GameConfig.PLAYER_WIDTH / 2f, y + bob,
                GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT);
        shapes.end();
    }

}
