package com.desertadventure.combat.enemy.data;

import com.desertadventure.combat.enemy.EnemyArchetypeDef;
import com.desertadventure.combat.enemy.EnemyArchetypeId;

import java.util.List;

public interface EnemyArchetypeRepository {
    EnemyArchetypeDef getRequired(EnemyArchetypeId id);

    List<EnemyArchetypeId> normalEncounterPool();
}
