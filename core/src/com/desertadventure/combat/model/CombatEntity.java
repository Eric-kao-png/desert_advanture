package com.desertadventure.combat.model;

import com.badlogic.gdx.math.Rectangle;
import com.desertadventure.config.GameConfig;

public class CombatEntity {
    public enum Kind {
        PLAYER, ENEMY, BOSS
    }

    private final Kind kind;
    private float x;
    private float y;
    private float width;
    private float height;
    private float hp;
    private float maxHp;
    private int attack;
    private float speed;
    private float contactDamageCooldown;
    private boolean alive = true;
    private float hurtFlash;

    public CombatEntity(Kind kind, float x, float y, float maxHp, int attack, float speed) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.speed = speed;
        switch (kind) {
            case PLAYER -> {
                width = GameConfig.PLAYER_WIDTH;
                height = GameConfig.PLAYER_HEIGHT;
            }
            case ENEMY -> {
                width = GameConfig.ENEMY_WIDTH;
                height = GameConfig.ENEMY_HEIGHT;
            }
            case BOSS -> {
                width = GameConfig.BOSS_WIDTH;
                height = GameConfig.BOSS_HEIGHT;
            }
            default -> {
                width = 40f;
                height = 40f;
            }
        }
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

    public float getMaxHp() {
        return maxHp;
    }

    public int getAttack() {
        return attack;
    }

    public float getSpeed() {
        return speed;
    }

    public boolean isAlive() {
        return alive;
    }

    public float getHurtFlash() {
        return hurtFlash;
    }

    public Rectangle getBounds() {
        return new Rectangle(x - width / 2f, y, width, height);
    }

    /** Hitbox extending forward (to the right) from the player. */
    public Rectangle getForwardAttackHitbox(float depth, float hitHeight) {
        float frontX = x + width * 0.2f;
        float hitY = y + (height - hitHeight) * 0.5f;
        return new Rectangle(frontX, hitY, depth, hitHeight);
    }

    public void takeDamage(float amount) {
        if (!alive) {
            return;
        }
        hp -= amount;
        hurtFlash = 0.15f;
        if (hp <= 0f) {
            hp = 0f;
            alive = false;
        }
    }

    public void update(float delta, float arenaWidth) {
        if (hurtFlash > 0f) {
            hurtFlash -= delta;
        }
        if (contactDamageCooldown > 0f) {
            contactDamageCooldown -= delta;
        }
        float halfW = width / 2f;
        x = Math.max(halfW, Math.min(arenaWidth - halfW, x));
    }

    public boolean canDealContactDamage() {
        return contactDamageCooldown <= 0f;
    }

    public void resetContactCooldown() {
        contactDamageCooldown = 0.8f;
    }

    public void moveBy(float dx) {
        x += dx;
    }
}
