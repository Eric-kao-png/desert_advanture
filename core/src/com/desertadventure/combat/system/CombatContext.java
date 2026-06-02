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

    CombatContext(CombatController combat, int roundNumber, Set<Integer> resolvedInstanceIdsThisRound) {
        this(combat, roundNumber, resolvedInstanceIdsThisRound, -1);
    }

    CombatContext(CombatController combat, int roundNumber, Set<Integer> resolvedInstanceIdsThisRound, int resolvingSlotIndex) {
        this.combat = combat;
        this.roundNumber = roundNumber;
        this.resolvedInstanceIdsThisRound = resolvedInstanceIdsThisRound;
        this.resolvingSlotIndex = resolvingSlotIndex;
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
        combat.dealDamageToEnemy(amount, CombatController.DamageSource.OFFENSE_CARD);
    }

    public void healPlayer(float amount) {
        combat.healPlayer(amount);
    }

    public void addPlayerShield(int amount) {
        CombatEntity p = combat.getPlayer();
        if (p != null) {
            p.addShield(amount);
        }
    }

    public void halveEnemyHp() {
        combat.halveEnemyHp();
    }

    public void applyNegativeStatusToEnemies(NegativeStatusType type, int turns) {
        combat.applyNegativeStatusToEnemies(type, turns);
    }

    @Override
    public String toString() {
        return "CombatContext{round=" + roundNumber + "}";
    }
}

