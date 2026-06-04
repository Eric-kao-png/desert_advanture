package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.enemy.EnemyAi;
import com.desertadventure.combat.enemy.EnemyArchetypeId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Enemy slot card planning and resolution helpers. */
final class EnemySlotPlanner {
    private final Integer[] enemySlotInstanceIds = new Integer[CombatSlotManager.SLOT_COUNT];
    private final ActionCardType[] resolvedEnemySlotCards = new ActionCardType[CombatSlotManager.SLOT_COUNT];
    private final EnemyAi enemyAi;
    private final CombatSlotManager playerSlots;

    private ActionCardDeck enemyDeck;
    private EnemyArchetypeId currentEnemyArchetype;

    EnemySlotPlanner(EnemyAi enemyAi, CombatSlotManager playerSlots) {
        this.enemyAi = enemyAi;
        this.playerSlots = playerSlots;
    }

    void setEnemyDeck(ActionCardDeck enemyDeck) {
        this.enemyDeck = enemyDeck;
    }

    void setCurrentEnemyArchetype(EnemyArchetypeId currentEnemyArchetype) {
        this.currentEnemyArchetype = currentEnemyArchetype;
    }

    EnemyArchetypeId getCurrentEnemyArchetype() {
        return currentEnemyArchetype;
    }

    ActionCardDeck getEnemyDeck() {
        return enemyDeck;
    }

    List<ActionCardInstance> enemyDeckInstancesForTests() {
        if (enemyDeck == null) {
            return List.of();
        }
        return enemyDeck.getInstances();
    }

    ActionCardInstance getEnemySlotCard(int slotIndex) {
        if (!playerSlots.isEnemySlot(slotIndex) || currentEnemyArchetype == null || enemyDeck == null) {
            return null;
        }
        Integer instanceId = enemySlotInstanceIds[slotIndex];
        if (instanceId == null) {
            return null;
        }
        return enemyDeck.findById(instanceId);
    }

    ActionCardType getEnemyCardForSlot(int slotIndex) {
        ActionCardInstance instance = getEnemySlotCard(slotIndex);
        if (instance != null) {
            return instance.getType();
        }
        if (!playerSlots.isEnemySlot(slotIndex)) {
            return null;
        }
        return currentEnemyArchetype == null ? ActionCardType.ATTACK : null;
    }

    ActionCardType getResolvedEnemyCardForSlot(int slotIndex) {
        if (!playerSlots.isEnemySlot(slotIndex)) {
            return null;
        }
        if (currentEnemyArchetype == null) {
            return ActionCardType.ATTACK;
        }
        return resolvedEnemySlotCards[slotIndex];
    }

    void clearResolvedEnemySlotCards() {
        Arrays.fill(resolvedEnemySlotCards, null);
    }

    void rollEnemySlotCards() {
        clearEnemySlots();
        if (currentEnemyArchetype == null || enemyDeck == null) {
            return;
        }
        for (int i = 0; i < CombatSlotManager.SLOT_COUNT; i++) {
            if (!playerSlots.isEnemySlot(i)) {
                continue;
            }
            List<ActionCardInstance> candidates = enemySlotPickCandidates();
            ActionCardInstance picked = enemyAi.pickCard(candidates);
            if (picked != null) {
                enemySlotInstanceIds[i] = picked.getInstanceId();
            }
        }
    }

    void clearEnemySlots() {
        Arrays.fill(enemySlotInstanceIds, null);
    }

    /**
     * Resolves the card for the given enemy slot index; may assign a fallback pick.
     * Returns null when no card should be played (boss uses separate path).
     */
    ActionCardInstance resolveCardForSlot(int slotIndex) {
        if (currentEnemyArchetype == null) {
            return null;
        }
        ActionCardInstance card = getEnemySlotCard(slotIndex);
        if (card == null) {
            ActionCardInstance picked = enemyAi.pickCard(enemySlotPickCandidates());
            if (picked == null) {
                return null;
            }
            card = picked;
            enemySlotInstanceIds[slotIndex] = picked.getInstanceId();
        }
        resolvedEnemySlotCards[slotIndex] = card.getType();
        return card;
    }

    boolean isBossEncounter() {
        return currentEnemyArchetype == null;
    }

    private List<ActionCardInstance> enemySlotPickCandidates() {
        List<ActionCardInstance> candidates = new ArrayList<>();
        if (enemyDeck == null) {
            return candidates;
        }
        Set<Integer> assigned = assignedEnemySlotInstanceIds();
        for (ActionCardInstance instance : enemyDeck.getInstances()) {
            if (instance.isOnCooldown()) {
                continue;
            }
            if (assigned.contains(instance.getInstanceId())) {
                continue;
            }
            candidates.add(instance);
        }
        return candidates;
    }

    private Set<Integer> assignedEnemySlotInstanceIds() {
        Set<Integer> ids = new HashSet<>();
        for (Integer id : enemySlotInstanceIds) {
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }
}
