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
import com.desertadventure.state.MessageFeed;

import java.util.Iterator;

/** HUD text for exploration, combat, and transient messages. */
public class GameplayHud {
    private final BitmapFont font;

    public GameplayHud(BitmapFont font) {
        this.font = font;
    }

    public void draw(SpriteBatch batch, GameSession session, GameplayMode mode) {
        font.setColor(Color.WHITE);

        if (mode != GameplayMode.CHARACTER_OVERLAY) {
            drawStatusLines(batch, session);
        }

        drawModeHint(batch, session, mode);
        drawMessageFeed(batch, session.getMessageFeed());
    }

    private void drawStatusLines(SpriteBatch batch, GameSession session) {
        float top = GameConfig.VIEW_HEIGHT;
        float left = GameConfig.HUD_LEFT_MARGIN;
        font.draw(batch, String.format("HP: %.0f/%.0f",
                session.getPlayerStats().getHp(), session.getPlayerStats().getMaxHp()), left, top - 16);
        font.draw(batch, String.format("Steps: %.1f/%.1f",
                session.getStepBudget().getRemainingSteps(), session.getStepBudget().getStepBudget()),
                left, top - 16 - GameConfig.HUD_LINE_STEP);
        font.draw(batch, String.format("Required Events: %d/%d",
                session.getEventTracker().getCompletedCount(), session.getEventTracker().getRequiredCount()),
                left, top - 16 - GameConfig.HUD_LINE_STEP * 2);

        GridPos tile = session.getDisplayGridPos();
        font.draw(batch, String.format("Tile: %s", tile), left, top - 16 - GameConfig.HUD_LINE_STEP * 3);
        font.draw(batch, String.format("Distance from origin: %.1f", session.getDistanceFromOrigin()),
                left, top - 16 - GameConfig.HUD_LINE_STEP * 4);
    }

    private void drawModeHint(SpriteBatch batch, GameSession session, GameplayMode mode) {
        switch (mode) {
            case EXPLORE_IDLE -> font.draw(batch, "[M] Map  [C] Character",
                    GameConfig.HUD_LEFT_MARGIN, GameConfig.HUD_BOTTOM_HINT_Y);
            case MAP_OVERLAY -> font.draw(batch, "Click destination | Arrows: pan | [Esc] Cancel",
                    GameConfig.HUD_LEFT_MARGIN, GameConfig.HUD_BOTTOM_HINT_Y);
            case CHARACTER_OVERLAY -> font.draw(batch, "Click item to use | [Esc] or [C] Close",
                    GameConfig.HUD_LEFT_MARGIN, GameConfig.HUD_BOTTOM_HINT_Y);
            case RUNNING -> font.draw(batch, "Moving... | [M] Map  [C] Character",
                    GameConfig.HUD_LEFT_MARGIN, GameConfig.HUD_BOTTOM_HINT_Y);
            case COMBAT, BOSS_COMBAT -> {
                font.draw(batch, "WASD: Move | J: Attack K: Skill L: Ultimate",
                        GameConfig.HUD_LEFT_MARGIN, GameConfig.HUD_BOTTOM_HINT_Y);
                CombatEntity boss = findBoss(session);
                if (boss != null) {
                    font.draw(batch, String.format("Boss HP: %.0f/%.0f", boss.getHp(), boss.getMaxHp()),
                            GameConfig.VIEW_WIDTH - GameConfig.BOSS_HUD_RIGHT_OFFSET, GameConfig.VIEW_HEIGHT - 16);
                }
            }
            case STORM -> {
                font.setColor(0.2f, 0.15f, 0.05f, 1f);
                drawCentered(batch, "Sandstorm!", GameConfig.VIEW_HEIGHT * GameConfig.STORM_TITLE_Y_RATIO);
                font.setColor(Color.WHITE);
            }
            default -> {
            }
        }
    }

    private void drawMessageFeed(SpriteBatch batch, MessageFeed feed) {
        if (feed.size() == 0) {
            return;
        }
        int index = 0;
        for (Iterator<MessageFeed.Line> it = feed.newestFirst(); it.hasNext(); index++) {
            MessageFeed.Line line = it.next();
            float alpha = line.getAlpha();
            if (alpha <= 0f) {
                continue;
            }
            font.setColor(1f, 1f, 0f, alpha);
            float y = GameConfig.MESSAGE_FEED_BASE_Y + index * GameConfig.MESSAGE_FEED_LINE_HEIGHT;
            font.draw(batch, line.getText(), GameConfig.MESSAGE_FEED_X, y);
        }
        font.setColor(Color.WHITE);
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
