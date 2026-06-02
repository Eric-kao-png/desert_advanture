package com.desertadventure.state;

import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.exploration.StepBudgetService;
import com.desertadventure.map.model.GameMap;
import com.desertadventure.map.model.GridPos;
import com.desertadventure.map.model.Tile;

/** Bridge from travel / tile logic back to {@link GameSession}. */
public interface ExplorationCallbacks {
    GameplayMode getMode();

    void setMode(GameplayMode mode);

    GameMap getMap();

    GridPos getPlayerGridPos();

    void setPlayerGridPos(int x, int y);

    GridPos getDisplayGridPos();

    StepBudgetService getStepBudget();

    void addScrollOffset(float amount);

    void setPendingMessage(String message);

    /** Stores the combat tile's enemy archetype until {@link GameSession#consumePendingCombatArchetype()}. */
    void beginCombatEncounter(EnemyArchetypeId tileArchetype);

    void handleTileInteraction(Tile tile, boolean duringMove);

    void triggerStorm();
}
