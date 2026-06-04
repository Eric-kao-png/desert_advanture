package com.desertadventure.combat.system;

import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;

final class PlayerCombatPerspective implements CombatPerspective {
    private final CombatController combat;

    PlayerCombatPerspective(CombatController combat) {
        this.combat = combat;
    }

    @Override
    public EffectCaster effectCaster() {
        return EffectCaster.PLAYER;
    }

    @Override
    public void dealDamageToEnemies(float amount) {
        combat.dealDamageToEnemy(amount, CombatController.DamageSource.OFFENSE_CARD);
    }

    @Override
    public void dealDamageToEnemiesIgnoringShield(float amount) {
        combat.dealDamageToEnemyIgnoringShield(amount);
    }

    @Override
    public void clearCasterNegativeStatus() {
        combat.clearNegativeStatusOnPlayer();
    }

    @Override
    public void transferCasterNegativeToOpponent() {
        combat.transferNegativeStatusFromPlayerToEnemies();
    }

    @Override
    public boolean casterHasNegativeStatus() {
        CombatEntity p = combat.getPlayer();
        return p != null && p.hasNegativeStatus();
    }

    @Override
    public boolean opponentHasNegativeStatus() {
        return combat.enemyHasNegativeStatus();
    }

    @Override
    public void healPlayer(float amount) {
        combat.healPlayer(amount);
    }

    @Override
    public void addPlayerShield(int amount) {
        CombatEntity p = combat.getPlayer();
        if (p != null) {
            p.addShield(amount);
        }
    }

    @Override
    public void halveEnemyHp() {
        combat.halveEnemyHp();
    }

    @Override
    public void applyNegativeStatusToEnemies(NegativeStatusType type, int turns) {
        combat.applyNegativeStatusToEnemies(type, turns);
    }
}
