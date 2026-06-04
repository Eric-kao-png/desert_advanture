package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardCategory;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.model.PositiveStatusType;

import java.util.List;

/** Snapshot-like context passed to effect templates. */
public final class CombatContext {
    private final CombatController combat;
    private final int roundNumber;
    private final int resolvingSlotIndex;
    private final CombatPerspective perspective;

    CombatContext(CombatController combat, int roundNumber) {
        this(combat, roundNumber, -1, EffectCaster.PLAYER);
    }

    CombatContext(CombatController combat, int roundNumber, int resolvingSlotIndex) {
        this(combat, roundNumber, resolvingSlotIndex, EffectCaster.PLAYER);
    }

    CombatContext(
            CombatController combat,
            int roundNumber,
            int resolvingSlotIndex,
            EffectCaster caster) {
        this.combat = combat;
        this.roundNumber = roundNumber;
        this.resolvingSlotIndex = resolvingSlotIndex;
        this.perspective = CombatPerspectives.forCaster(
                combat, caster != null ? caster : EffectCaster.PLAYER);
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

    /** Whether the caster assigned a card of {@code category} to a slot this round (player or enemy slots). */
    public boolean turnHasUsedCategory(ActionCardCategory category) {
        return combat.roundHasUsedCategory(category, perspective.effectCaster());
    }

    /** Resolve-order slot immediately before {@link #resolvingSlotIndex()} is a player offense card. */
    public boolean previousSlotIsPlayerOffense() {
        return combat.previousSlotIsPlayerOffense(perspective.effectCaster(), resolvingSlotIndex);
    }

    /** 0-based slot index (0..3) for the card currently being resolved; -1 if unknown. */
    public int resolvingSlotIndex() {
        return resolvingSlotIndex;
    }

    // --- operations ---

    public void dealDamageToEnemies(float amount) {
        perspective.dealDamageToEnemies(amount);
    }

    public void dealDamageToEnemiesIgnoringShield(float amount) {
        perspective.dealDamageToEnemiesIgnoringShield(amount);
    }

    public void clearCasterNegativeStatus() {
        perspective.clearCasterNegativeStatus();
    }

    public void transferCasterNegativeToOpponent() {
        perspective.transferCasterNegativeToOpponent();
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
        return perspective.casterHasNegativeStatus();
    }

    public boolean opponentHasNegativeStatus() {
        return perspective.opponentHasNegativeStatus();
    }

    public void healPlayer(float amount) {
        perspective.healPlayer(amount);
    }

    public void addPlayerShield(int amount) {
        perspective.addPlayerShield(amount);
    }

    public void halveEnemyHp() {
        perspective.halveEnemyHp();
    }

    public void applyNegativeStatusToEnemies(NegativeStatusType type, int turns) {
        perspective.applyNegativeStatusToEnemies(type, turns);
    }

    public void applyPositiveStatusToCaster(PositiveStatusType type, int turns) {
        perspective.applyPositiveStatusToCaster(type, turns);
    }

    @Override
    public String toString() {
        return "CombatContext{round=" + roundNumber + ", caster=" + perspective.effectCaster() + "}";
    }
}
