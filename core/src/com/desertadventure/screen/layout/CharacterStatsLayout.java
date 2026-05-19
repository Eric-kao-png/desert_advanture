package com.desertadventure.screen.layout;

import com.desertadventure.config.GameConfig;

/** Stat bar and label positions in the right column. */
public final class CharacterStatsLayout {
    public final float statsTextX;
    public final float statsTopY;
    public final float statBarX;
    public final float statBarW;
    public final float statBarH;
    public final float hpBarBottom;
    public final float combatSectionY;
    public final float explorationSectionY;
    public final float staminaBarBottom;

    public CharacterStatsLayout(float rightColX, float columnW, float headerBottom) {
        float lineH = GameConfig.CHARACTER_PANEL_LINE_HEIGHT;
        statsTextX = rightColX;
        statsTopY = headerBottom - lineH * GameConfig.CHARACTER_SECTION_LABEL_OFFSET_MULT;

        statBarH = GameConfig.CHARACTER_STAT_BAR_HEIGHT;
        statBarW = columnW - GameConfig.CHARACTER_STAT_BAR_INSET;
        statBarX = statsTextX;
        hpBarBottom = statsTopY - lineH * GameConfig.CHARACTER_STAT_LABEL_ABOVE_BAR_MULT - statBarH;
        combatSectionY = hpBarBottom - GameConfig.CHARACTER_STAT_BAR_SECTION_GAP;
        explorationSectionY = combatSectionY - lineH * GameConfig.CHARACTER_COMBAT_BLOCK_MULT;
        staminaBarBottom = explorationSectionY - lineH * GameConfig.CHARACTER_STAT_LABEL_ABOVE_BAR_MULT - statBarH;
    }
}
