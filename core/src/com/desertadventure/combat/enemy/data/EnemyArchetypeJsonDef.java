package com.desertadventure.combat.enemy.data;

import java.util.ArrayList;
import java.util.List;

/** One enemy archetype entry in the JSON manifest. */
public final class EnemyArchetypeJsonDef {
    public String id;
    public String name;
    public int hpMin;
    public int hpMax;
    public List<String> deck = new ArrayList<>();
    public List<String> loot = new ArrayList<>();
}
