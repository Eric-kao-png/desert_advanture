package com.desertadventure.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

/** HUD text for exploration, combat, and transient messages. */
public class GameplayHud {
    private final BitmapFont font;

    public GameplayHud(BitmapFont font) {
        this.font = font;
    }

    public void draw(SpriteBatch batch, GameSession session, GameplayMode mode, float messageTimer) {
        font.setColor(Color.WHITE);

        float top = GameConfig.VIEW_HEIGHT;
        font.draw(batch, String.format("HP: %.0f/%.0f",
                session.getPlayerStats().getHp(), session.getPlayerStats().getMaxHp()), 16, top - 16);
        font.draw(batch, String.format("Steps: %.1f/%.1f",
                session.getStepBudget().getRemainingSteps(), session.getStepBudget().getStepBudget()), 16, top - 40);
        font.draw(batch, String.format("Required Events: %d/%d",
                session.getEventTracker().getCompletedCount(), session.getEventTracker().getRequiredCount()),
                16, top - 64);

        GridPos tile = session.getDisplayGridPos();
        font.draw(batch, String.format("Tile: %s", tile), 16, top - 88);
        font.draw(batch, String.format("Distance from origin: %.1f", session.getDistanceFromOrigin()), 16, top - 112);

        drawModeHint(batch, session, mode);
        drawPendingMessage(batch, session, messageTimer);
    }

    private void drawModeHint(SpriteBatch batch, GameSession session, GameplayMode mode) {
        switch (mode) {
            case EXPLORE_IDLE -> font.draw(batch, "[M] Map", 16, 40);
            case MAP_OVERLAY -> font.draw(batch, "Click destination | Arrows: pan | [Esc] Cancel", 16, 40);
            case RUNNING -> font.draw(batch, "Moving...", 16, 40);
            case COMBAT, BOSS_COMBAT -> {
                font.draw(batch, "WASD: Move | J: Attack K: Skill L: Ultimate", 16, 40);
                CombatEntity boss = findBoss(session);
                if (boss != null) {
                    font.draw(batch, String.format("Boss HP: %.0f/%.0f", boss.getHp(), boss.getMaxHp()),
                            GameConfig.VIEW_WIDTH - 280, GameConfig.VIEW_HEIGHT - 16);
                }
            }
            case STORM -> {
                font.setColor(0.2f, 0.15f, 0.05f, 1f);
                drawCentered(batch, "Sandstorm!", GameConfig.VIEW_HEIGHT * 0.55f);
                font.setColor(Color.WHITE);
            }
            default -> {
            }
        }
    }

    private void drawPendingMessage(SpriteBatch batch, GameSession session, float messageTimer) {
        String message = session.getPendingMessage();
        if (message != null && messageTimer > 0f) {
            font.setColor(Color.YELLOW);
            drawCentered(batch, message, GameConfig.VIEW_HEIGHT * 0.2f);
            font.setColor(Color.WHITE);
        } else if (messageTimer <= 0f) {
            session.clearPendingMessage();
        }
    }

    private static CombatEntity findBoss(GameSession session) {
        for (CombatEntity enemy : session.getCombatController().getEnemies()) {
            if (enemy.getKind() == CombatEntity.Kind.BOSS) {
                return enemy;
            }
        }
        return null;
    }

    private void drawCentered(SpriteBatch batch, String text, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, (GameConfig.VIEW_WIDTH - layout.width) / 2f, y);
    }
}
