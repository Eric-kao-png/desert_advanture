package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.config.GameConfig;
import com.desertadventure.presentation.sprites.GridSpriteSheetAnimation;

/**
 * Renders the player sprite in explore and combat modes.
 *
 * <p>Behavior is intentionally identical to the previous inlined implementation in {@link GameplayRenderer}.</p>
 */
final class PlayerSpriteRenderer {
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

    private final GridSpriteSheetAnimation idle;
    private final GridSpriteSheetAnimation run;
    private final GridSpriteSheetAnimation attack;

    PlayerSpriteRenderer() {
        idle = new GridSpriteSheetAnimation(
                PLAYER_SHEET_PATH, PLAYER_FRAME_W, PLAYER_FRAME_H,
                0, 0, 5, 0, PLAYER_IDLE_FPS);
        run = new GridSpriteSheetAnimation(
                PLAYER_SHEET_PATH, PLAYER_FRAME_W, PLAYER_FRAME_H,
                0, 1, 1, 2, PLAYER_RUN_FPS);
        attack = new GridSpriteSheetAnimation(
                PLAYER_SHEET_PATH, PLAYER_FRAME_W, PLAYER_FRAME_H,
                2, 2, 3, 3, PLAYER_ATTACK_FPS);
    }

    void dispose() {
        idle.dispose();
        run.dispose();
        attack.dispose();
    }

    void drawExplorePlayerSprite(SpriteBatch batch, float centerX, float bottomY, boolean running) {
        float t = (System.currentTimeMillis() % 100000L) / 1000f;
        TextureRegion frame = running ? run.getLoopFrame(t) : idle.getLoopFrame(t);
        batch.setColor(Color.WHITE);
        batch.draw(frame,
                centerX - PLAYER_DRAW_WIDTH / 2f,
                bottomY,
                PLAYER_DRAW_WIDTH,
                PLAYER_DRAW_HEIGHT);
    }

    void drawCombatPlayerSprite(CombatController combat, SpriteBatch batch) {
        if (combat == null) {
            return;
        }
        var player = combat.getPlayer();
        if (player == null) {
            return;
        }
        if (!GameplayRenderer.shouldDrawCombatEntity(player)) {
            return;
        }

        TextureRegion frame;
        if (combat.isPlayerAttacking()) {
            float p = combat.getPlayerAttackProgress(PLAYER_ATTACK_SECONDS);
            frame = attack.getOnceFrame(p);
        } else {
            float t = (System.currentTimeMillis() % 100000L) / 1000f;
            frame = idle.getLoopFrame(t);
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
}

