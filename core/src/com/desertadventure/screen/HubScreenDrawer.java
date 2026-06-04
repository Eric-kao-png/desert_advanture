package com.desertadventure.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.UiColors;
import com.desertadventure.run.RunProgress;
import com.desertadventure.run.StageDef;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.HubPanel;

/** Simple text UI for the card-run hub and deck sub-panel. */
public final class HubScreenDrawer {
    private final GlyphLayout layout = new GlyphLayout();

    public void draw(SpriteBatch batch, BitmapFont font, GameSession session) {
        font.setColor(Color.WHITE);
        if (session.getHubPanel() == HubPanel.DECK) {
            drawDeck(batch, font, session);
        } else {
            drawMain(batch, font, session);
        }
    }

    public boolean handlePointerTap(GameSession session, float screenX, float screenY) {
        if (session.getHubPanel() != HubPanel.MAIN) {
            return false;
        }
        float centerX = GameConfig.VIEW_WIDTH / 2f;
        float buttonTop = GameConfig.VIEW_HEIGHT * 0.58f;
        float buttonH = GameConfig.HUD_LINE_STEP * 1.4f;
        for (int i = 0; i < 2; i++) {
            float top = buttonTop - i * buttonH * 1.35f;
            float bottom = top - buttonH;
            if (Math.abs(screenX - centerX) < 220f && screenY >= bottom && screenY <= top) {
                if (i == 0) {
                    return session.tryStartCurrentStageCombat();
                }
                session.setHubPanel(HubPanel.DECK);
                return true;
            }
        }
        return false;
    }

    private void drawMain(SpriteBatch batch, BitmapFont font, GameSession session) {
        RunProgress run = session.getRunProgress();
        StageDef stage = run.getCurrentStage();
        drawCentered(batch, font, "Camp Hub", GameConfig.VIEW_HEIGHT * 0.82f, 1.15f);
        font.getData().setScale(GameConfig.HUD_FONT_SCALE);
        drawCentered(batch, font,
                String.format("HP: %.0f / %.0f", session.getPlayerStats().getHp(), session.getPlayerStats().getMaxHp()),
                GameConfig.VIEW_HEIGHT * 0.74f, 1f);
        drawCentered(batch, font,
                String.format("Stage %d / %d — %s", run.getDisplayStageNumber(), run.getTotalStages(), stage.label()),
                GameConfig.VIEW_HEIGHT * 0.68f, 1f);
        font.setColor(UiColors.MENU_GROUND_BAND);
        float y = GameConfig.VIEW_HEIGHT * 0.56f;
        drawCentered(batch, font, "[1] Start Battle — " + stage.label(), y, 1f);
        y -= GameConfig.HUD_LINE_STEP * 1.35f;
        drawCentered(batch, font, "[2] Deck", y, 1f);
        font.setColor(Color.WHITE);
        drawCentered(batch, font, "Esc — Main menu  |  Click a line to select",
                GameConfig.VIEW_HEIGHT * 0.22f, 0.85f);
    }

    private void drawDeck(SpriteBatch batch, BitmapFont font, GameSession session) {
        drawCentered(batch, font, "Action Deck", GameConfig.VIEW_HEIGHT * 0.86f, 1.1f);
        font.getData().setScale(GameConfig.HUD_FONT_SCALE);
        float y = GameConfig.VIEW_HEIGHT * 0.78f;
        int index = 1;
        for (ActionCardInstance card : session.getActionCardDeck().getInstances()) {
            String cd = card.getCooldownRemaining() > 0
                    ? " (CD " + card.getCooldownRemaining() + ")"
                    : "";
            font.draw(batch, index + ". " + card.getType().getDisplayName() + cd,
                    GameConfig.HUD_LEFT_MARGIN, y);
            y -= GameConfig.HUD_LINE_STEP;
            index++;
            if (y < GameConfig.VIEW_HEIGHT * 0.2f) {
                break;
            }
        }
        drawCentered(batch, font, "B / Esc — Back to hub", GameConfig.VIEW_HEIGHT * 0.14f, 0.85f);
    }

    private void drawCentered(SpriteBatch batch, BitmapFont font, String text, float y, float scale) {
        float prev = font.getData().scaleX;
        font.getData().setScale(scale * GameConfig.HUD_FONT_SCALE);
        layout.setText(font, text);
        font.draw(batch, text, (GameConfig.VIEW_WIDTH - layout.width) / 2f, y);
        font.getData().setScale(prev);
    }
}
