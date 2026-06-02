package com.desertadventure.combat.enemy;

import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.system.slots.RandomIntSource;

import java.util.List;

/** Data for one enemy archetype: HP range, action deck, victory loot pool. */
public final class EnemyArchetypeDef {
    private final EnemyArchetypeId id;
    private final String displayName;
    private final int hpMin;
    private final int hpMax;
    private final List<ActionCardType> deckCardTypes;
    private final List<ActionCardType> lootPool;

    public EnemyArchetypeDef(
            EnemyArchetypeId id,
            String displayName,
            int hpMin,
            int hpMax,
            List<ActionCardType> deckCardTypes,
            List<ActionCardType> lootPool) {
        if (id == null || displayName == null || deckCardTypes == null || lootPool == null) {
            throw new IllegalArgumentException("id, displayName, deck, and loot must be non-null");
        }
        if (hpMin <= 0 || hpMax < hpMin) {
            throw new IllegalArgumentException("invalid HP range: " + hpMin + ".." + hpMax);
        }
        if (deckCardTypes.isEmpty() || lootPool.isEmpty()) {
            throw new IllegalArgumentException("deck and loot pool must be non-empty");
        }
        this.id = id;
        this.displayName = displayName;
        this.hpMin = hpMin;
        this.hpMax = hpMax;
        this.deckCardTypes = List.copyOf(deckCardTypes);
        this.lootPool = List.copyOf(lootPool);
    }

    public EnemyArchetypeId id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public int hpMin() {
        return hpMin;
    }

    public int hpMax() {
        return hpMax;
    }

    public List<ActionCardType> deckCardTypes() {
        return deckCardTypes;
    }

    public List<ActionCardType> lootPool() {
        return lootPool;
    }

    /** Rolls max HP in [hpMin, hpMax] inclusive. */
    public float rollMaxHp(RandomIntSource rng) {
        int span = hpMax - hpMin + 1;
        return hpMin + rng.nextInt(span);
    }
}
