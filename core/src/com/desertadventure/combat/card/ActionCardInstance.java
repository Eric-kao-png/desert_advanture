package com.desertadventure.combat.card;

public final class ActionCardInstance {
    private final int instanceId;
    private final ActionCardType type;
    private int cooldownRemaining;

    public ActionCardInstance(int instanceId, ActionCardType type) {
        this.instanceId = instanceId;
        this.type = type;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public ActionCardType getType() {
        return type;
    }

    public int getCooldownRemaining() {
        return cooldownRemaining;
    }

    public void setCooldownRemaining(int turns) {
        cooldownRemaining = Math.max(0, turns);
    }

    public boolean isOnCooldown() {
        return cooldownRemaining > 0;
    }

    public void tickCooldown() {
        if (cooldownRemaining > 0) {
            cooldownRemaining--;
        }
    }
}
