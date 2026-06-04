package com.desertadventure.player;

import com.desertadventure.config.PlayerConfig;

/** Persistent HP for the player between hub and combat. */
public final class PlayerStats {
    private float maxHp = PlayerConfig.INITIAL_MAX_HP;
    private float hp = PlayerConfig.INITIAL_MAX_HP;

    public float getMaxHp() {
        return maxHp;
    }

    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = Math.min(maxHp, Math.max(0f, hp));
    }

    public boolean isAlive() {
        return hp > 0f;
    }

    public void healFull() {
        hp = maxHp;
    }

    public void restoreHp(float amount) {
        setHp(hp + amount);
    }

    public void resetForNewGame() {
        maxHp = PlayerConfig.INITIAL_MAX_HP;
        hp = maxHp;
    }
}
