package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.screen.layout.CombatCardLayout;
import com.desertadventure.screen.layout.CombatSceneLayout;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.UiColors;
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
    private final OverlayCloseButton overlayCloseButton = new OverlayCloseButton();
    private final MapOverlayRenderer mapOverlay;
    private final CharacterOverlayRenderer characterOverlay;
    private final CombatCardRenderer combatCards;

    public GameplayRenderer() {
        screenProjection.setToOrtho2D(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT);
        desertSprites = new DesertSpriteAtlas();
        sky = new SkyBackdrop();
        parallax = new ParallaxBackground(desertSprites.get(DesertSpriteAtlas.FLOOR_TILE));
        houses = new BackgroundHouseSpawner(desertSprites);
        mapOverlay = new MapOverlayRenderer(overlayCloseButton);
        characterOverlay = new CharacterOverlayRenderer(overlayCloseButton);
        combatCards = new CombatCardRenderer(shapes);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        sky.dispose();
        parallax.dispose();
        desertSprites.dispose();
        overlayCloseButton.dispose();
        mapOverlay.dispose();
        characterOverlay.dispose();
    }

    public void setProjectionMatrix(Matrix4 projection) {
        screenProjection.set(projection);
        mapOverlay.setProjection(projection);
        characterOverlay.setProjection(projection);
    }

    public void repopulateHouseProps(float screenW) {
        houses.repopulate(screenW);
    }

    /** sky → sun → back → houses → middle → forward (no floor). Batch must already be begun. */
    public void drawParallaxBackground(SpriteBatch batch, boolean running, float delta, float layoutBlend) {
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
    }

    /** Tiled ground plane from y=0 to groundY. Batch must already be begun. */
    public void drawParallaxFloor(SpriteBatch batch, float layoutBlend) {
        batch.setProjectionMatrix(screenProjection);
        parallax.drawFloor(batch, GameConfig.VIEW_WIDTH, CombatSceneLayout.groundY(layoutBlend));
    }

    public void renderExploreForeground(
            GameSession session,
            boolean running,
            float layoutBlend,
            SpriteBatch batch,
            BitmapFont font) {
        ExplorePlayerPose pose = explorePlayerPose(running, layoutBlend);
        drawPlayer(pose);
        PlayerStats stats = session.getPlayerStats();
        shapes.setProjectionMatrix(screenProjection);
        CombatHpBarDrawer.draw(shapes, pose.centerX, pose.bottomY, GameConfig.PLAYER_WIDTH,
                GameConfig.PLAYER_HEIGHT, stats.getHp(), stats.getMaxHp());
        batch.setProjectionMatrix(screenProjection);
        batch.begin();
        CombatHpBarDrawer.drawHpText(batch, font,
                CombatHpBarDrawer.layout(pose.centerX, pose.bottomY, GameConfig.PLAYER_WIDTH,
                        GameConfig.PLAYER_HEIGHT),
                stats.getHp(), stats.getMaxHp());
        batch.end();
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

    public void renderCombatEntities(List<CombatEntity> entities, SpriteBatch batch, BitmapFont font) {
        shapes.setProjectionMatrix(screenProjection);
        drawCombatEntityBodies(entities);
        drawCombatEntityOverlays(entities, font);
        batch.setProjectionMatrix(screenProjection);
        batch.begin();
        drawCombatEntityTexts(entities, batch, font);
        batch.end();
    }

    public void renderMapOverlay(
            GameSession session,
            MapOverlayLayout layout,
            GridPos hoverCell,
            SpriteBatch batch,
            boolean hoveredDismiss,
            boolean pressedDismiss) {
        mapOverlay.render(session, layout, hoverCell, batch, hoveredDismiss, pressedDismiss);
    }

    public void renderCharacterOverlay(
            SpriteBatch batch,
            GameSession session,
            BitmapFont font,
            com.desertadventure.screen.CharacterOverlayLayout layout,
            com.desertadventure.screen.CharacterOverlayInput input,
            float delta,
            boolean hoveredDismiss,
            boolean pressedDismiss) {
        characterOverlay.render(batch, session, font, layout, input, delta, hoveredDismiss, pressedDismiss);
    }

    public void renderStorm(float progress) {
        float alpha = Math.min(1f, progress);
        shapes.setProjectionMatrix(screenProjection);
        ShapeDrawer.fillRect(shapes, 0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT,
                UiColors.STORM_TINT.cpy().mul(1f, 1f, 1f, alpha * GameConfig.STORM_OVERLAY_ALPHA_SCALE));
    }

    private ExplorePlayerPose explorePlayerPose(boolean running, float layoutBlend) {
        float centerX = GameConfig.VIEW_WIDTH * GameConfig.EXPLORE_PLAYER_X_RATIO;
        float groundY = CombatSceneLayout.groundY(layoutBlend);
        float bob = running
                ? (float) (Math.sin(System.currentTimeMillis() * GameConfig.PLAYER_BOB_FREQUENCY)
                * GameConfig.PLAYER_BOB_AMPLITUDE)
                : 0f;
        return new ExplorePlayerPose(centerX, groundY + bob);
    }

    private void drawPlayer(ExplorePlayerPose pose) {
        shapes.setProjectionMatrix(screenProjection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(UiColors.EXPLORE_PLAYER);
        shapes.rect(pose.centerX - GameConfig.PLAYER_WIDTH / 2f, pose.bottomY,
                GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT);
        shapes.end();
    }

    private static final class ExplorePlayerPose {
        final float centerX;
        final float bottomY;

        ExplorePlayerPose(float centerX, float bottomY) {
            this.centerX = centerX;
            this.bottomY = bottomY;
        }
    }

    private void drawCombatEntityBodies(List<CombatEntity> entities) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (CombatEntity entity : entities) {
            if (!shouldDrawCombatEntity(entity)) {
                continue;
            }
            shapes.setColor(CombatEntityColors.forEntity(entity, entity.getHurtFlash() > 0f));
            shapes.rect(entity.getX() - entity.getWidth() / 2f, entity.getY(), entity.getWidth(), entity.getHeight());
        }
        shapes.end();
    }

    private void drawCombatEntityOverlays(List<CombatEntity> entities, BitmapFont font) {
        for (CombatEntity entity : entities) {
            if (!shouldDrawCombatEntity(entity)) {
                continue;
            }
            CombatHpBarDrawer.draw(shapes, entity);
            CombatStatusDrawer.drawPanels(shapes, entity, font);
        }
    }

    private void drawCombatEntityTexts(List<CombatEntity> entities, SpriteBatch batch, BitmapFont font) {
        for (CombatEntity entity : entities) {
            if (!shouldDrawCombatEntity(entity)) {
                continue;
            }
            CombatHpBarDrawer.drawHpText(batch, font, entity);
            CombatStatusDrawer.drawText(batch, font, entity);
        }
    }

    private static boolean shouldDrawCombatEntity(CombatEntity entity) {
        return entity.isAlive() || entity.getKind() == CombatEntity.Kind.PLAYER;
    }

}
