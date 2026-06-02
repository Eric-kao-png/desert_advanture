package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.desertadventure.presentation.sprites.GridSpriteSheetAnimation;
import com.desertadventure.state.GameSession;

import java.util.List;

/** Facade for gameplay visuals: parallax, houses, map overlay, combat shapes. */
public class GameplayRenderer implements com.badlogic.gdx.utils.Disposable {
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Matrix4 screenProjection = new Matrix4();
    private final DesertSpriteAtlas desertSprites;
    private final GridSpriteSheetAnimation playerIdle;
    private final GridSpriteSheetAnimation playerRun;
    private final GridSpriteSheetAnimation playerAttack;
    private final SkyBackdrop sky;
    private final ParallaxBackground parallax;
    private final BackgroundHouseSpawner houses;
    private final OverlayCloseButton overlayCloseButton = new OverlayCloseButton();
    private final MapOverlayRenderer mapOverlay;
    private final CharacterOverlayRenderer characterOverlay;
    private final CombatCardRenderer combatCards;
    private static final String PLAYER_SHEET_PATH = "sprites/Warrior_SheetnoEffect.png";
    /** Source frame size in the sprite sheet (do not change; affects region slicing). */
    private static final int PLAYER_FRAME_W = 69;
    private static final int PLAYER_FRAME_H = 44;
    private static final float PLAYER_IDLE_FPS = 8f;
    private static final float PLAYER_RUN_FPS = 10f;
    private static final float PLAYER_ATTACK_FPS = 14f;
    private static final float PLAYER_ATTACK_SECONDS = GameConfig.COMBAT_PLAYER_ATTACK_ANIM_SECONDS;
    /**
     * Draw player sprite with fixed height equal to the legacy logical rectangle height,
     * scaling width proportionally to preserve aspect ratio of sprite frames.
     */
    private static final float PLAYER_DRAW_HEIGHT = GameConfig.PLAYER_HEIGHT;
    private static final float PLAYER_DRAW_WIDTH = PLAYER_DRAW_HEIGHT * ((float) PLAYER_FRAME_W / (float) PLAYER_FRAME_H);

    public GameplayRenderer() {
        screenProjection.setToOrtho2D(0, 0, GameConfig.VIEW_WIDTH, GameConfig.VIEW_HEIGHT);
        desertSprites = new DesertSpriteAtlas();
        playerIdle = new GridSpriteSheetAnimation(
                PLAYER_SHEET_PATH, PLAYER_FRAME_W, PLAYER_FRAME_H,
                0, 0, 5, 0, PLAYER_IDLE_FPS);
        playerRun = new GridSpriteSheetAnimation(
                PLAYER_SHEET_PATH, PLAYER_FRAME_W, PLAYER_FRAME_H,
                0, 1, 1, 2, PLAYER_RUN_FPS);
        playerAttack = new GridSpriteSheetAnimation(
                PLAYER_SHEET_PATH, PLAYER_FRAME_W, PLAYER_FRAME_H,
                2, 2, 3, 3, PLAYER_ATTACK_FPS);
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
        playerIdle.dispose();
        playerRun.dispose();
        playerAttack.dispose();
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
        PlayerStats stats = session.getPlayerStats();
        shapes.setProjectionMatrix(screenProjection);
        CombatHpBarDrawer.draw(shapes, pose.centerX, pose.bottomY, GameConfig.PLAYER_WIDTH,
                GameConfig.PLAYER_HEIGHT, stats.getHp(), stats.getMaxHp());
        batch.setProjectionMatrix(screenProjection);
        batch.begin();
        drawExplorePlayerSprite(batch, pose, running);
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

    public void renderCombatEntities(CombatController combat, List<CombatEntity> entities, SpriteBatch batch, BitmapFont font) {
        shapes.setProjectionMatrix(screenProjection);
        drawCombatEntityBodies(entities);
        drawCombatEntityOverlays(entities, font);
        batch.setProjectionMatrix(screenProjection);
        batch.begin();
        drawCombatPlayerSprite(combat, entities, batch);
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
        return new ExplorePlayerPose(centerX, groundY);
    }

    private void drawExplorePlayerSprite(SpriteBatch batch, ExplorePlayerPose pose, boolean running) {
        float t = (System.currentTimeMillis() % 100000L) / 1000f;
        TextureRegion frame = running ? playerRun.getLoopFrame(t) : playerIdle.getLoopFrame(t);
        batch.setColor(Color.WHITE);
        batch.draw(frame,
                pose.centerX - PLAYER_DRAW_WIDTH / 2f,
                pose.bottomY,
                PLAYER_DRAW_WIDTH,
                PLAYER_DRAW_HEIGHT);
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
            if (entity.getKind() == CombatEntity.Kind.PLAYER) {
                continue;
            }
            shapes.setColor(CombatEntityColors.forEntity(entity, entity.getHurtFlash() > 0f));
            shapes.rect(entity.getX() - entity.getWidth() / 2f, entity.getY(), entity.getWidth(), entity.getHeight());
        }
        shapes.end();
    }

    private void drawCombatPlayerSprite(CombatController combat, List<CombatEntity> entities, SpriteBatch batch) {
        if (combat == null) {
            return;
        }
        CombatEntity player = combat.getPlayer();
        if (player == null) {
            return;
        }
        if (!shouldDrawCombatEntity(player)) {
            return;
        }
        TextureRegion frame;
        if (combat.isPlayerAttacking()) {
            float p = combat.getPlayerAttackProgress(PLAYER_ATTACK_SECONDS);
            frame = playerAttack.getOnceFrame(p);
        } else {
            float t = (System.currentTimeMillis() % 100000L) / 1000f;
            frame = playerIdle.getLoopFrame(t);
        }
        if (player.getHurtFlash() > 0f) {
            batch.setColor(1f, 0.6f, 0.6f, 1f);
        } else {
            batch.setColor(Color.WHITE);
        }
        batch.draw(frame,
                player.getX() - PLAYER_DRAW_WIDTH / 2f,
                player.getY(),
                PLAYER_DRAW_WIDTH,
                PLAYER_DRAW_HEIGHT);
        batch.setColor(Color.WHITE);
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
