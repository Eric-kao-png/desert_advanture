package com.desertadventure.combat.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exploration action cards (separate from item inventory). */
public final class ActionCardDeck {
    private final List<ActionCardInstance> instances = new ArrayList<>();
    private int nextInstanceId = 1;

    public void resetToDefault() {
        resetFromCardTypes(List.of(
                ActionCardType.ATTACK,
                ActionCardType.ATTACK,
                ActionCardType.SWIFT_STRIKE,
                ActionCardType.HEAL,
                ActionCardType.SHIELD,
                ActionCardType.CHARGED_SLASH));
    }

    /** Replaces all instances with one per entry (duplicate types allowed). */
    public void resetFromCardTypes(List<ActionCardType> types) {
        instances.clear();
        nextInstanceId = 1;
        if (types == null) {
            return;
        }
        for (ActionCardType type : types) {
            if (type != null) {
                addCard(type);
            }
        }
    }

    public static ActionCardDeck fromCardTypes(List<ActionCardType> types) {
        ActionCardDeck deck = new ActionCardDeck();
        deck.resetFromCardTypes(types);
        return deck;
    }

    public List<ActionCardInstance> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    public ActionCardInstance findById(int instanceId) {
        for (ActionCardInstance instance : instances) {
            if (instance.getInstanceId() == instanceId) {
                return instance;
            }
        }
        return null;
    }

    public void tickCooldownsForNewRound() {
        for (ActionCardInstance instance : instances) {
            instance.tickCooldown();
        }
    }

    /** Sets every instance's cooldown to zero without removing or rebuilding instances. */
    public void clearAllCooldowns() {
        for (ActionCardInstance instance : instances) {
            instance.setCooldownRemaining(0);
        }
    }

    public void addCard(ActionCardType type) {
        instances.add(new ActionCardInstance(nextInstanceId++, type));
    }
}
