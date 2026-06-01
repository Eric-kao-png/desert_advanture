package com.desertadventure.combat.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exploration action cards (separate from item inventory). */
public final class ActionCardDeck {
    private final List<ActionCardInstance> instances = new ArrayList<>();
    private int nextInstanceId = 1;

    public void resetToDefault() {
        instances.clear();
        nextInstanceId = 1;
        add(ActionCardType.ATTACK);
        add(ActionCardType.ATTACK);
        add(ActionCardType.STRONG_ATTACK);
        add(ActionCardType.HEAL);
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

    private void add(ActionCardType type) {
        instances.add(new ActionCardInstance(nextInstanceId++, type));
    }
}
