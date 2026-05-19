package com.desertadventure.presentation.character;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertadventure.config.GameConfig;
import com.desertadventure.config.GameMessages;
import com.desertadventure.config.UiColors;
import com.desertadventure.exploration.StepBudgetService;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.presentation.StatBarDrawer;
import com.desertadventure.screen.CharacterOverlayLayout;
import com.desertadventure.screen.layout.CharacterPanelGeometry;
import com.desertadventure.screen.layout.CharacterStatsLayout;
import com.desertadventure.state.GameSession;

/** Right-column stat bars and labels. */
public final class CharacterStatsView {
    private final GlyphLayout glyphLayout = new GlyphLayout();

    public void drawBars(ShapeRenderer shapes, GameSession session, CharacterOverlayLayout layout) {
        PlayerStats stats = session.getPlayerStats();
        StepBudgetService steps = session.getStepBudget();
        CharacterStatsLayout s = layout.stats();

        float hpRatio = stats.getMaxHp() > 0f ? stats.getHp() / stats.getMaxHp() : 0f;
        StatBarDrawer.draw(shapes, s.statBarX, s.hpBarBottom, s.statBarW, s.statBarH,
                hpRatio, UiColors.HP_BAR_BG, UiColors.HP_BAR_FILL, UiColors.BAR_BORDER);

        float maxSteps = steps.getStepBudget();
        float staminaRatio = maxSteps > 0f ? steps.getRemainingSteps() / maxSteps : 0f;
        StatBarDrawer.draw(shapes, s.statBarX, s.staminaBarBottom, s.statBarW, s.statBarH,
                staminaRatio, UiColors.STAMINA_BAR_BG, UiColors.STAMINA_BAR_FILL, UiColors.BAR_BORDER);
    }

    public void drawLabels(SpriteBatch batch, BitmapFont font, GameSession session, CharacterOverlayLayout layout) {
        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        CharacterStatsLayout s = layout.stats();
        PlayerStats stats = session.getPlayerStats();
        StepBudgetService steps = session.getStepBudget();

        font.setColor(UiColors.SECTION_LABEL);
        font.draw(batch, GameMessages.STAT_SECTION_HEALTH, s.statsTextX, s.statsTopY);
        drawBarValue(batch, font, s.statBarX, s.statBarW, s.hpBarBottom, s.statBarH,
                String.format("%.0f / %.0f", stats.getHp(), stats.getMaxHp()));

        font.setColor(UiColors.SECTION_LABEL);
        font.draw(batch, GameMessages.STAT_SECTION_COMBAT, s.statsTextX, s.combatSectionY);
        font.setColor(Color.WHITE);
        font.draw(batch, String.format("%s %d", GameMessages.STAT_ATTACK, stats.getAttack()),
                s.statsTextX + 8f, s.combatSectionY - lineH);

        font.setColor(UiColors.SECTION_LABEL);
        font.draw(batch, GameMessages.STAT_SECTION_EXPLORATION, s.statsTextX, s.explorationSectionY);
        drawBarValue(batch, font, s.statBarX, s.statBarW, s.staminaBarBottom, s.statBarH,
                String.format("%.1f / %.1f", steps.getRemainingSteps(), steps.getStepBudget()));
    }

    public void drawPortraitPlaceholder(SpriteBatch batch, BitmapFont font, CharacterOverlayLayout layout) {
        CharacterPanelGeometry panel = layout.panel();
        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        float portraitX = panel.centerColX;
        float portraitY = panel.contentBottom;
        float portraitW = panel.columnW;
        float portraitH = panel.headerBottom - lineH * GameConfig.CHARACTER_PORTRAIT_TOP_MULT - panel.contentBottom;

        glyphLayout.setText(font, GameMessages.CHARACTER_PORTRAIT_PLACEHOLDER);
        float textX = portraitX + (portraitW - glyphLayout.width) / 2f;
        float textY = portraitY + portraitH / 2f;
        font.setColor(UiColors.MUTED_TEXT);
        font.draw(batch, GameMessages.CHARACTER_PORTRAIT_PLACEHOLDER, textX, textY);
    }

    private void drawBarValue(
            SpriteBatch batch,
            BitmapFont font,
            float barX,
            float barW,
            float barBottom,
            float barHeight,
            String text) {
        glyphLayout.setText(font, text);
        float textX = barX + barW - glyphLayout.width - GameConfig.CHARACTER_BAR_VALUE_TEXT_PAD;
        float textY = barBottom + (barHeight + glyphLayout.height) / 2f;
        font.setColor(UiColors.BAR_VALUE_TEXT);
        font.draw(batch, text, textX, textY);
    }
}
