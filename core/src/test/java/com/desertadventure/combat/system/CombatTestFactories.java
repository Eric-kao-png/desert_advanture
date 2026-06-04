package com.desertadventure.combat.system;

import com.desertadventure.combat.enemy.EnemyAi;
import com.desertadventure.combat.system.slots.PlayerSlotPlan;
import com.desertadventure.combat.system.slots.PlayerSlotRoller;
import com.desertadventure.player.PlayerStats;

/** Test-only combat session factories (same package as {@link CombatController}). */
public final class CombatTestFactories {
    private CombatTestFactories() {
    }

    public static CombatController passiveEnemyCombat() {
        PlayerStats stats = new PlayerStats();
        PlayerSlotRoller roller = () -> new PlayerSlotPlan(0, 2);
        EnemyAi passiveEnemy = candidates -> null;
        return new CombatController(stats, roller, passiveEnemy, bound -> 0);
    }
}
