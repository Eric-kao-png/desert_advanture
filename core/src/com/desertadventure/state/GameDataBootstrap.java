package com.desertadventure.state;

import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.GdxCardRepositoryLoader;
import com.desertadventure.combat.enemy.data.EnemyArchetypeDatabase;
import com.desertadventure.combat.enemy.data.GdxEnemyArchetypeLoader;

/** One-time card and enemy archetype DB initialization for a game session. */
public final class GameDataBootstrap {
    private GameDataBootstrap() {
    }

    public static void initializeIfNeeded() {
        if (!CardDatabase.isInitialized()) {
            CardDatabase.initialize(GdxCardRepositoryLoader.loadDefault());
        }
        if (!EnemyArchetypeDatabase.isInitialized()) {
            EnemyArchetypeDatabase.initialize(GdxEnemyArchetypeLoader.loadDefault());
        }
    }
}
