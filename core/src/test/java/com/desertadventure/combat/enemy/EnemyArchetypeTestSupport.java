package com.desertadventure.combat.enemy;

import com.desertadventure.combat.enemy.data.EnemyArchetypeDatabase;
import com.desertadventure.combat.enemy.data.EnemyArchetypeManifestLoader;

/** Loads enemy JSON for unit tests without LibGDX. */
public final class EnemyArchetypeTestSupport {
    private EnemyArchetypeTestSupport() {
    }

    public static void ensureLoaded() {
        if (!EnemyArchetypeDatabase.isInitialized()) {
            EnemyArchetypeDatabase.initialize(EnemyArchetypeManifestLoader.loadFromProjectAssets());
        }
    }
}
