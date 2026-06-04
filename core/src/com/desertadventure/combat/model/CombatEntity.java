package com.desertadventure.combat.model;

import com.desertadventure.config.GameConfig;

/** Combatant on the arena: HP, shield, and combat statuses. */
public final class CombatEntity {
    public enum Kind {
        PLAYER, ENEMY, BOSS
    }

    private final Kind kind;
    private float x;
    private float y;
    private final float width;
    private final float height;
    private float hp;
    private final float maxHp;
    private boolean alive = true;
    private int shield;
    private PositiveStatusType positiveType;
    private int positiveTurnsRemaining;
    private NegativeStatusType negativeType;
    private int negativeTurnsRemaining;

    public CombatEntity(Kind kind, float x, float y, float maxHp) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.maxHp = maxHp;
        this.hp = maxHp;
        float[] size = sizeFor(kind);
        this.width = size[0];
        this.height = size[1];
    }

    private static float[] sizeFor(Kind kind) {
        return switch (kind) {
            case PLAYER -> new float[] { GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT };
            case ENEMY -> new float[] { GameConfig.ENEMY_WIDTH, GameConfig.ENEMY_HEIGHT };
            case BOSS -> new float[] { GameConfig.BOSS_WIDTH, GameConfig.BOSS_HEIGHT };
        };
    }

    public Kind getKind() {
        return kind;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = Math.min(maxHp, Math.max(0f, hp));
        alive = this.hp > 0f;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public boolean isAlive() {
        return alive;
    }

    public int getShield() {
        return shield;
    }

    public void addShield(int amount) {
        if (!alive || amount <= 0) {
            return;
        }
        shield += amount;
    }

    public PositiveStatusType getPositiveStatusType() {
        return positiveType;
    }

    public int getPositiveTurnsRemaining() {
        return positiveTurnsRemaining;
    }

    public boolean hasPositiveStatus() {
        return positiveTurnsRemaining > 0 && positiveType != null;
    }

    public void setPositiveStatus(PositiveStatusType type, int turns) {
        if (!alive || type == null || turns <= 0) {
            return;
        }
        positiveType = type;
        positiveTurnsRemaining = turns;
    }

    public void clearPositiveStatus() {
        positiveType = null;
        positiveTurnsRemaining = 0;
    }

    public NegativeStatusType getNegativeStatusType() {
        return negativeType;
    }

    public int getNegativeTurnsRemaining() {
        return negativeTurnsRemaining;
    }

    public boolean hasNegativeStatus() {
        return negativeTurnsRemaining > 0 && negativeType != null;
    }

    public void clearNegativeStatus() {
        negativeType = null;
        negativeTurnsRemaining = 0;
    }

    public void setNegativeStatus(NegativeStatusType type, int turns) {
        if (!alive || type == null || turns <= 0) {
            return;
        }
        negativeType = type;
        negativeTurnsRemaining = turns;
    }

    public void clearCombatStatus() {
        shield = 0;
        clearPositiveStatus();
        clearNegativeStatus();
    }

    public void takeDamage(float amount) {
        if (!alive || amount <= 0f) {
            return;
        }
        if (shield > 0) {
            int absorbed = (int) Math.min(shield, amount);
            shield -= absorbed;
            amount -= absorbed;
        }
        if (amount > 0f) {
            applyDirectDamage(amount);
        }
    }

    public void takeStatusDamage(float amount) {
        if (!alive || amount <= 0f) {
            return;
        }
        applyDirectDamage(amount);
    }

    public void takeDamageIgnoringShield(float amount) {
        if (!alive || amount <= 0f) {
            return;
        }
        applyDirectDamage(amount);
    }

    public void applyRoundEndStatusEffects(float poisonDamagePerRound) {
        if (!alive) {
            return;
        }
        if (negativeTurnsRemaining > 0 && negativeType != null) {
            if (negativeType == NegativeStatusType.POISON) {
                takeStatusDamage(poisonDamagePerRound);
            } else if (negativeType == NegativeStatusType.BLEED) {
                takeStatusDamage(negativeTurnsRemaining);
            }
        }
        tickStatusDurations();
    }

    private void tickStatusDurations() {
        if (positiveTurnsRemaining > 0) {
            positiveTurnsRemaining--;
            if (positiveTurnsRemaining <= 0) {
                clearPositiveStatus();
            }
        }
        if (negativeTurnsRemaining > 0) {
            negativeTurnsRemaining--;
            if (negativeTurnsRemaining <= 0) {
                clearNegativeStatus();
            }
        }
    }

    public void heal(float amount) {
        if (!alive) {
            return;
        }
        hp = Math.min(maxHp, hp + amount);
    }

    private void applyDirectDamage(float amount) {
        hp -= amount;
        if (hp <= 0f) {
            hp = 0f;
            alive = false;
        }
    }
}
