package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardCategory;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;

import java.util.List;
import java.util.Set;

/** Snapshot-like context passed to effect templates. */
public final class CombatContext {
    private final CombatController combat;
    private final int roundNumber;
    private final Set<Integer> resolvedInstanceIdsThisRound;
    private final int resolvingSlotIndex;
    private final EffectCaster caster;

    CombatContext(CombatController combat, int roundNumber, Set<Integer> resolvedInstanceIdsThisRound) {
        this(combat, roundNumber, resolvedInstanceIdsThisRound, -1, EffectCaster.PLAYER);
    }

    CombatContext(
            CombatController combat,
            int roundNumber,
            Set<Integer> resolvedInstanceIdsThisRound,
            int resolvingSlotIndex) {
        this(combat, roundNumber, resolvedInstanceIdsThisRound, resolvingSlotIndex, EffectCaster.PLAYER);
    }

    CombatContext(
            CombatController combat,
            int roundNumber,
            Set<Integer> resolvedInstanceIdsThisRound,
            int resolvingSlotIndex,
            EffectCaster caster) {
        this.combat = combat;
        this.roundNumber = roundNumber;
        this.resolvedInstanceIdsThisRound = resolvedInstanceIdsThisRound;
        this.resolvingSlotIndex = resolvingSlotIndex;
        this.caster = caster != null ? caster : EffectCaster.PLAYER;
    }

    public int roundNumber() {
        return roundNumber;
    }

    public CombatEntity player() {
        return combat.getPlayer();
    }

    public List<CombatEntity> enemies() {
        return combat.getEnemies();
    }

    public boolean turnHasResolvedCategory(ActionCardCategory category) {
        for (int instanceId : resolvedInstanceIdsThisRound) {
            ActionCardInstance instance = combat.findCard(instanceId);
            if (instance != null && instance.getType().getCategory() == category) {
                return true;
            }
        }
        return false;
    }

    /** 0-based slot index (0..3) for the card currently being resolved; -1 if unknown. */
    public int resolvingSlotIndex() {
        return resolvingSlotIndex;
    }

    // --- operations ---

    public void dealDamageToEnemies(float amount) {
        if (caster == EffectCaster.ENEMY) {
            combat.dealDamageToPlayer(amount);
        } else {
            combat.dealDamageToEnemy(amount, CombatController.DamageSource.OFFENSE_CARD);
        }
    }

    public void dealDamageToEnemiesIgnoringShield(float amount) {
        if (caster == EffectCaster.ENEMY) {
            combat.dealDamageToPlayerIgnoringShield(amount);
        } else {
            combat.dealDamageToEnemyIgnoringShield(amount);
        }
    }

    public void clearCasterNegativeStatus() {
        if (caster == EffectCaster.ENEMY) {
            combat.clearNegativeStatusOnEnemies();
        } else {
            combat.clearNegativeStatusOnPlayer();
        }
    }

    public void transferCasterNegativeToOpponent() {
        if (caster == EffectCaster.ENEMY) {
            combat.transferNegativeStatusFromEnemyToPlayer();
        } else {
            combat.transferNegativeStatusFromPlayerToEnemies();
        }
    }

    public void applyRandomPoisonToEnemies() {
        int turns = combat.rollPoisonBoltPoisonTurns();
        if (turns > 0) {
            applyNegativeStatusToEnemies(NegativeStatusType.POISON, turns);
        }
    }

    /** Single roll: either poison for {@code turns} or nothing (mutually exclusive). */
    public void applyChancePoisonToEnemies(int chancePercent, int turns) {
        if (combat.rollPercentChance(chancePercent)) {
            applyNegativeStatusToEnemies(NegativeStatusType.POISON, turns);
        }
    }

    public boolean casterHasNegativeStatus() {
        if (caster == EffectCaster.ENEMY) {
            return combat.enemyHasNegativeStatus();
        }
        CombatEntity p = combat.getPlayer();
        return p != null && p.hasNegativeStatus();
    }

    public boolean opponentHasNegativeStatus() {
        if (caster == EffectCaster.ENEMY) {
            CombatEntity p = combat.getPlayer();
            return p != null && p.hasNegativeStatus();
        }
        return combat.enemyHasNegativeStatus();
    }

    public void healPlayer(float amount) {
        if (caster == EffectCaster.ENEMY) {
            combat.healEnemy(amount);
        } else {
            combat.healPlayer(amount);
        }
    }

    public void addPlayerShield(int amount) {
        if (caster == EffectCaster.ENEMY) {
            combat.addEnemyShield(amount);
        } else {
            CombatEntity p = combat.getPlayer();
            if (p != null) {
                p.addShield(amount);
            }
        }
    }

    public void halveEnemyHp() {
        if (caster == EffectCaster.ENEMY) {
            combat.halvePlayerHp();
        } else {
            combat.halveEnemyHp();
        }
    }

    public void applyNegativeStatusToEnemies(NegativeStatusType type, int turns) {
        if (caster == EffectCaster.ENEMY) {
            combat.applyNegativeStatusToPlayer(type, turns);
        } else {
            combat.applyNegativeStatusToEnemies(type, turns);
        }
    }

    @Override
    public String toString() {
        return "CombatContext{round=" + roundNumber + ", caster=" + caster + "}";
    }
}
