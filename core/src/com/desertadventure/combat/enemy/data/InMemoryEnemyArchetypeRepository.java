package com.desertadventure.combat.enemy.data;

import com.desertadventure.combat.enemy.EnemyArchetypeDef;
import com.desertadventure.combat.enemy.EnemyArchetypeId;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class InMemoryEnemyArchetypeRepository implements EnemyArchetypeRepository {
    private final Map<EnemyArchetypeId, EnemyArchetypeDef> byId;
    private final List<EnemyArchetypeId> normalEncounterPool;

    public InMemoryEnemyArchetypeRepository(
            Map<EnemyArchetypeId, EnemyArchetypeDef> byId,
            List<EnemyArchetypeId> normalEncounterPool) {
        this.byId = new EnumMap<>(byId);
        this.normalEncounterPool = List.copyOf(normalEncounterPool);
    }

    @Override
    public EnemyArchetypeDef getRequired(EnemyArchetypeId id) {
        EnemyArchetypeDef def = byId.get(id);
        if (def == null) {
            throw new IllegalStateException("Missing enemy archetype def: " + id);
        }
        return def;
    }

    @Override
    public List<EnemyArchetypeId> normalEncounterPool() {
        return normalEncounterPool;
    }
}
