package com.desertadventure.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.UiColors;
import com.desertadventure.run.RunProgress;
import com.desertadventure.run.StageDef;
import com.desertadventure.state.GameSession;

/** Simple text UI for the card-run hub. */
public final class HubScreenDrawer {
    private final GlyphLayout layout = new GlyphLayout();

    public void draw(SpriteBatch batch, BitmapFont font, GameSession session) {
        font.setColor(Color.WHITE);
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
        drawCentered(batch, font, "[1] Start Battle — " + stage.label(),
                GameConfig.VIEW_HEIGHT * 0.56f, 1f);
        font.setColor(Color.WHITE);
        drawCentered(batch, font, "Click the line above or press 1",
                GameConfig.VIEW_HEIGHT * 0.22f, 0.85f);
    }

    public boolean handlePointerTap(GameSession session, float screenX, float screenY) {
        float centerX = GameConfig.VIEW_WIDTH / 2f;
        float top = GameConfig.VIEW_HEIGHT * 0.58f;
        float bottom = top - GameConfig.HUD_LINE_STEP * 1.4f;
        if (Math.abs(screenX - centerX) < 220f && screenY >= bottom && screenY <= top) {
            return session.tryStartCurrentStageCombat();
        }
        return false;
    }

    private void drawCentered(SpriteBatch batch, BitmapFont font, String text, float y, float scale) {
        float prev = font.getData().scaleX;
        font.getData().setScale(scale * GameConfig.HUD_FONT_SCALE);
        layout.setText(font, text);
        font.draw(batch, text, (GameConfig.VIEW_WIDTH - layout.width) / 2f, y);
        font.getData().setScale(prev);
    }
}
