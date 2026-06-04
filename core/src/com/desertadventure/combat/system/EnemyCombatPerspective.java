package com.desertadventure.combat.system;

import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.model.PositiveStatusType;

final class EnemyCombatPerspective implements CombatPerspective {
    private final CombatController combat;

    EnemyCombatPerspective(CombatController combat) {
        this.combat = combat;
    }

    @Override
    public EffectCaster effectCaster() {
        return EffectCaster.ENEMY;
    }

    @Override
    public void dealDamageToEnemies(float amount) {
        combat.dealDamageToPlayer(amount, CombatController.DamageSource.OFFENSE_CARD);
    }

    @Override
    public void dealDamageToEnemiesIgnoringShield(float amount) {
        combat.dealDamageToPlayerIgnoringShield(amount, CombatController.DamageSource.OFFENSE_CARD);
    }

    @Override
    public void clearCasterNegativeStatus() {
        combat.clearNegativeStatusOnEnemies();
    }

    @Override
    public void transferCasterNegativeToOpponent() {
        combat.transferNegativeStatusFromEnemyToPlayer();
    }

    @Override
    public boolean casterHasNegativeStatus() {
        return combat.enemyHasNegativeStatus();
    }

    @Override
    public boolean opponentHasNegativeStatus() {
        CombatEntity p = combat.getPlayer();
        return p != null && p.hasNegativeStatus();
    }

    @Override
    public void healPlayer(float amount) {
        combat.healEnemy(amount);
    }

    @Override
    public void addPlayerShield(int amount) {
        combat.addEnemyShield(amount);
    }

    @Override
    public void halveEnemyHp() {
        combat.halvePlayerHp();
    }

    @Override
    public void applyNegativeStatusToEnemies(NegativeStatusType type, int turns) {
        combat.applyNegativeStatusToPlayer(type, turns);
    }

    @Override
    public void applyPositiveStatusToCaster(PositiveStatusType type, int turns) {
        CombatEntity enemy = CombatEntityRoster.firstAliveEnemy(combat.getEnemies());
        if (enemy != null) {
            enemy.setPositiveStatus(type, turns);
        }
    }
}
