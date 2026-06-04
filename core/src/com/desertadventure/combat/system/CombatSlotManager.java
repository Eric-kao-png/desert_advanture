package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Player slot indices and instance-id assignments for the current round. */
final class CombatSlotManager {
    static final int SLOT_COUNT = 4;
    private static final int FIRST_SLOT_INDEX = 0;

    private final Integer[] slotInstanceIds = new Integer[SLOT_COUNT];
    private int playerSlotA = 0;
    private int playerSlotB = 2;

    int getPlayerSlotA() {
        return playerSlotA;
    }

    int getPlayerSlotB() {
        return playerSlotB;
    }

    boolean isPlayerSlot(int slotIndex) {
        return slotIndex == playerSlotA || slotIndex == playerSlotB;
    }

    boolean isEnemySlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex < SLOT_COUNT && !isPlayerSlot(slotIndex);
    }

    Integer getSlotInstanceId(int slotIndex) {
        if (!isValidSlotIndex(slotIndex)) {
            return null;
        }
        return slotInstanceIds[slotIndex];
    }

    void setPlayerSlotInstanceForTest(int slotIndex, int instanceId) {
        if (isPlayerSlot(slotIndex)) {
            slotInstanceIds[slotIndex] = instanceId;
        }
    }

    boolean assignToPlayerSlot(int slotIndex, int instanceId, ActionCardDeck deck) {
        if (!isPlayerSlot(slotIndex) || deck == null) {
            return false;
        }
        ActionCardInstance card = deck.findById(instanceId);
        if (card == null || card.isOnCooldown()) {
            return false;
        }
        int otherSlot = otherPlayerSlot(slotIndex);
        Integer otherId = slotInstanceIds[otherSlot];
        if (otherId != null && otherId == instanceId) {
            return false;
        }
        removeInstanceFromSlots(instanceId);
        slotInstanceIds[slotIndex] = instanceId;
        return true;
    }

    void clearPlayerSlot(int slotIndex) {
        if (!isPlayerSlot(slotIndex)) {
            return;
        }
        slotInstanceIds[slotIndex] = null;
    }

    void setPlayerSlots(int a, int b) {
        playerSlotA = a;
        playerSlotB = b;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slotInstanceIds[i] != null && !isPlayerSlot(i)) {
                slotInstanceIds[i] = null;
            }
        }
    }

    void clearPlayerSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            slotInstanceIds[i] = null;
        }
    }

    void removeInstanceFromSlots(int instanceId) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slotInstanceIds[i] != null && slotInstanceIds[i] == instanceId) {
                slotInstanceIds[i] = null;
            }
        }
    }

    Set<Integer> assignedInstanceIds() {
        Set<Integer> ids = new HashSet<>();
        for (Integer id : slotInstanceIds) {
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    List<ActionCardInstance> collectUnassignedHandCards(ActionCardDeck deck, boolean excludeCooldown) {
        List<ActionCardInstance> hand = new ArrayList<>();
        if (deck == null) {
            return hand;
        }
        Set<Integer> assigned = assignedInstanceIds();
        for (ActionCardInstance instance : deck.getInstances()) {
            if (instance == null) {
                continue;
            }
            if (excludeCooldown && instance.isOnCooldown()) {
                continue;
            }
            if (assigned.contains(instance.getInstanceId())) {
                continue;
            }
            hand.add(instance);
        }
        return hand;
    }

    boolean canAssignCard(ActionCardInstance instance) {
        if (instance == null || instance.isOnCooldown()) {
            return false;
        }
        return !assignedInstanceIds().contains(instance.getInstanceId());
    }

    private int otherPlayerSlot(int slotIndex) {
        return slotIndex == playerSlotA ? playerSlotB : playerSlotA;
    }

    private boolean isValidSlotIndex(int slotIndex) {
        return slotIndex >= FIRST_SLOT_INDEX && slotIndex < SLOT_COUNT;
    }
}
