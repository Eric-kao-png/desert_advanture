package com.desertadventure.exploration;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardDeckResetPolicy;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.player.PlayerStats;

public class StormResetService {

    public void applyCycleReset(
            GameMap map,
            PlayerStats stats,
            StepBudgetService stepBudget,
            ActionCardDeck actionCardDeck,
            ActionCardDeckResetPolicy deckResetPolicy) {
        map.resetCycleState();
        stats.healFull();
        stepBudget.resetForCycle(stats);
        deckResetPolicy.resetDeck(actionCardDeck);
    }
}
