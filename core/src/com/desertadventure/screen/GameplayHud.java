package com.desertadventure.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;
import com.desertadventure.state.MessageFeed;

import java.util.Iterator;

/** HUD text for hub, combat, and transient messages. */
public class GameplayHud {
    private final BitmapFont font;

    public GameplayHud(BitmapFont font) {
        this.font = font;
    }

    public void draw(SpriteBatch batch, GameSession session, GameplayMode mode) {
        font.setColor(Color.WHITE);

        if (!mode.isHub()) {
            drawStatusLines(batch, session, mode);
        }

        drawModeOverlay(batch, session, mode);
        drawMessageFeed(batch, session.getMessageFeed());
    }

    private void drawStatusLines(SpriteBatch batch, GameSession session, GameplayMode mode) {
        float top = GameConfig.VIEW_HEIGHT;
        float left = GameConfig.HUD_LEFT_MARGIN;
        float offset = GameConfig.HUD_STATUS_TOP_OFFSET;
        font.draw(batch, String.format("HP: %.0f/%.0f",
                        session.getPlayerStats().getHp(), session.getPlayerStats().getMaxHp()),
                left, top - offset);
    }

    private void drawModeOverlay(SpriteBatch batch, GameSession session, GameplayMode mode) {
        switch (mode) {
            case COMBAT, BOSS_COMBAT -> {
                CombatEntity enemy = firstLivingEnemy(session);
                if (enemy != null) {
                    font.draw(batch, String.format("Enemy HP: %.0f/%.0f", enemy.getHp(), enemy.getMaxHp()),
                            GameConfig.VIEW_WIDTH - GameConfig.BOSS_HUD_RIGHT_OFFSET,
                            GameConfig.VIEW_HEIGHT - GameConfig.HUD_STATUS_TOP_OFFSET);
                }
                font.draw(batch, String.format("Round %d", session.getCombatController().getRoundNumber()),
                        GameConfig.VIEW_WIDTH - GameConfig.BOSS_HUD_RIGHT_OFFSET,
                        GameConfig.VIEW_HEIGHT - GameConfig.HUD_STATUS_TOP_OFFSET - GameConfig.HUD_LINE_STEP);
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

    private static CombatEntity firstLivingEnemy(GameSession session) {
        for (CombatEntity enemy : session.getCombatController().getEnemies()) {
            if (enemy.isAlive()) {
                return enemy;
            }
        }
        return null;
    }

}
