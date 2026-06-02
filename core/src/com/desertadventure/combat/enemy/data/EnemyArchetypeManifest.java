package com.desertadventure.combat.enemy.data;

import java.util.ArrayList;
import java.util.List;

/** Root JSON file for all enemy archetypes. */
public final class EnemyArchetypeManifest {
    public int version;
    public List<String> normalEncounterPool = new ArrayList<>();
    public List<EnemyArchetypeJsonDef> archetypes = new ArrayList<>();
}
