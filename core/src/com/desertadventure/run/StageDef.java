package com.desertadventure.run;

import com.desertadventure.combat.enemy.EnemyArchetypeId;

/** One linear stage in a card run. */
public record StageDef(
        String id,
        String displayName,
        EnemyArchetypeId enemyArchetype,
        boolean boss) {

    public String label() {
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        if (boss) {
            return "Boss";
        }
        return enemyArchetype != null ? enemyArchetype.name() : id;
    }
}
