package com.desertadventure.combat.system;

import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.status.StatusEffectRuntime;

import java.util.List;
import java.util.function.Consumer;

/** Shared iteration helpers for combat entities. */
final class CombatEntityRoster {
    private CombatEntityRoster() {
    }

    static void forEachAliveEnemy(List<CombatEntity> enemies, Consumer<CombatEntity> action) {
        for (CombatEntity enemy : enemies) {
            if (enemy.isAlive()) {
                action.accept(enemy);
            }
        }
    }

    static CombatEntity firstAliveEnemy(List<CombatEntity> enemies) {
        for (CombatEntity enemy : enemies) {
            if (enemy.isAlive()) {
                return enemy;
            }
        }
        return null;
    }

    static float offenseDamageWithStatusModifiers(float amount, CombatEntity enemy) {
        return StatusEffectRuntime.applyIncomingOffenseCardDamageModifiers(amount, enemy);
    }
}
