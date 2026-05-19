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
    public static final float EXPLORE_GROUND_Y = 120f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Matrix4 screenProjection = new Matrix4();
    private final DesertSpriteAtlas desertSprites;
    private final ParallaxBackground parallax;
    private final BackgroundHouseSpawner houses;
    private final MapOverlayRenderer mapOverlay;

    public GameplayRenderer() {
        screenProjection.setToOrtho2D(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT);
        desertSprites = new DesertSpriteAtlas();
        parallax = new ParallaxBackground(desertSprites.get(DesertSpriteAtlas.FLOOR_TILE));
        houses = new BackgroundHouseSpawner(desertSprites);
        mapOverlay = new MapOverlayRenderer();
    }

    @Override
    public void dispose() {
        shapes.dispose();
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

    /** back → houses → middle → forward → floor. Batch must already be begun. */
    public void drawParallaxBackground(SpriteBatch batch, boolean running, float delta) {
        float w = GameConfig.VIEW_WIDTH;
        float h = GameConfig.VIEW_HEIGHT;
        parallax.scroll(delta, running);
        houses.scroll(delta, running);
        batch.setProjectionMatrix(screenProjection);
        parallax.drawBack(batch, w, h);
        houses.draw(batch, w);
        parallax.drawMiddle(batch, w, h);
        parallax.drawForward(batch, w, h);
        parallax.drawFloor(batch, w, EXPLORE_GROUND_Y);
    }

    public void renderExploreForeground(GameSession session, boolean running) {
        float w = GameConfig.VIEW_WIDTH;
        float h = GameConfig.VIEW_HEIGHT;
        drawPlayer(w * 0.35f, EXPLORE_GROUND_Y, running);
        fillRect(0, h - 60, w, 60, new Color(0f, 0f, 0f, 0.35f));
    }

    public void renderCombatEntities(List<CombatEntity> entities) {
        shapes.setProjectionMatrix(screenProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (CombatEntity entity : entities) {
            if (!entity.isAlive() && entity.getKind() != CombatEntity.Kind.PLAYER) {
                continue;
            }
            shapes.setColor(colorForEntity(entity, entity.getHurtFlash() > 0f));
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
        fillRect(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT,
                new Color(0.9f, 0.75f, 0.35f, alpha * 0.85f));
    }

    private void drawPlayer(float x, float y, boolean running) {
        shapes.setProjectionMatrix(screenProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.2f, 0.5f, 0.95f, 1f);
        float bob = running ? (float) Math.sin(System.currentTimeMillis() * 0.02) * 4f : 0f;
        shapes.rect(x - GameConfig.PLAYER_WIDTH / 2f, y + bob,
                GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT);
        shapes.end();
    }

    private void fillRect(float x, float y, float w, float h, Color color) {
        shapes.setProjectionMatrix(screenProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(color);
        shapes.rect(x, y, w, h);
        shapes.end();
    }

    private static Color colorForEntity(CombatEntity entity, boolean hurt) {
        if (hurt) {
            return Color.WHITE;
        }
        return switch (entity.getKind()) {
            case PLAYER -> new Color(0.2f, 0.5f, 0.95f, 1f);
            case ENEMY -> new Color(0.9f, 0.25f, 0.2f, 1f);
            case BOSS -> new Color(0.55f, 0.2f, 0.85f, 1f);
        };
    }
}
