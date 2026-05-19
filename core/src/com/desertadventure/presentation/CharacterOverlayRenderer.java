package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameMessages;
import com.desertadventure.state.GameSession;

/** Character stats panel over the exploration scene (same layer as map overlay). */
public class CharacterOverlayRenderer {
    private final ShapeRenderer shapes = new ShapeRenderer();

    public void setProjection(Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
    }

    public void render(SpriteBatch batch, GameSession session, BitmapFont font) {
        float screenW = GameConfig.VIEW_WIDTH;
        float screenH = GameConfig.VIEW_HEIGHT;

        ShapeDrawer.fillRect(shapes, 0, 0, screenW, screenH,
                new Color(0f, 0f, 0f, GameConfig.MAP_OVERLAY_DIM_ALPHA));

        float panelW = GameConfig.CHARACTER_PANEL_WIDTH;
        float panelH = GameConfig.CHARACTER_PANEL_HEIGHT;
        float panelX = (screenW - panelW) / 2f;
        float panelY = (screenH - panelH) / 2f;
        ShapeDrawer.fillRect(shapes, panelX, panelY, panelW, panelH,
                new Color(0.12f, 0.11f, 0.1f, GameConfig.CHARACTER_PANEL_BG_ALPHA));

        float pad = GameConfig.CHARACTER_PANEL_PADDING;
        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        float textX = panelX + pad;
        float y = panelY + panelH - pad;

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, GameMessages.CHARACTER_PANEL_TITLE, textX, y);
        y -= lineH * 1.25f;

        font.setColor(0.85f, 0.75f, 0.55f, 1f);
        font.draw(batch, GameMessages.STAT_SECTION_HEALTH, textX, y);
        y -= lineH;
        font.setColor(Color.WHITE);
        font.draw(batch, formatHp(session), textX + 12f, y);
        y -= lineH * 1.1f;

        font.setColor(0.85f, 0.75f, 0.55f, 1f);
        font.draw(batch, GameMessages.STAT_SECTION_COMBAT, textX, y);
        y -= lineH;
        font.setColor(Color.WHITE);
        font.draw(batch, formatAttack(session), textX + 12f, y);
        y -= lineH * 1.1f;

        font.setColor(0.85f, 0.75f, 0.55f, 1f);
        font.draw(batch, GameMessages.STAT_SECTION_EXPLORATION, textX, y);
        y -= lineH;
        font.setColor(Color.WHITE);
        font.draw(batch, formatStamina(session), textX + 12f, y);

        font.setColor(Color.WHITE);
        batch.end();
    }

    public void dispose() {
        shapes.dispose();
    }

    private static String formatHp(GameSession session) {
        var stats = session.getPlayerStats();
        return String.format("%s %.0f / %.0f", GameMessages.STAT_HP, stats.getHp(), stats.getMaxHp());
    }

    private static String formatAttack(GameSession session) {
        return String.format("%s %d", GameMessages.STAT_ATTACK, session.getPlayerStats().getAttack());
    }

    private static String formatStamina(GameSession session) {
        var steps = session.getStepBudget();
        return String.format("%s %.1f / %.1f",
                GameMessages.STAT_STAMINA, steps.getRemainingSteps(), steps.getStepBudget());
    }
}
