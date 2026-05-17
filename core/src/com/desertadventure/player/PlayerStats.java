package com.desertadventure.player;

import com.desertadventure.config.GameConfig;

public class PlayerStats {
    private int level = 1;
    private int experience;
    private int experienceToNext = 30;
    private float maxHp = 100f;
    private float hp = 100f;
    private int attack = 10;
    private int defense = 2;
    private int stepBudgetBonus;
    private float moveSpeed = GameConfig.PLAYER_SPEED;

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public int getExperienceToNext() {
        return experienceToNext;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = Math.min(maxHp, Math.max(0f, hp));
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getStepBudgetBonus() {
        return stepBudgetBonus;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public float getTotalStepBudget() {
        return GameConfig.BASE_STEP_BUDGET + stepBudgetBonus;
    }

    public boolean isAlive() {
        return hp > 0f;
    }

    public void healFull() {
        hp = maxHp;
    }

    public void addExperience(int amount) {
        experience += amount;
        while (experience >= experienceToNext) {
            experience -= experienceToNext;
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        maxHp += 15f;
        hp = maxHp;
        attack += 3;
        defense += 1;
        stepBudgetBonus += 1;
        experienceToNext = (int) (experienceToNext * 1.4f);
    }

    public void applyItemBonus() {
        attack += 2;
        stepBudgetBonus += 1;
    }

    public void resetForNewGame() {
        level = 1;
        experience = 0;
        experienceToNext = 30;
        maxHp = 100f;
        hp = 100f;
        attack = 10;
        defense = 2;
        stepBudgetBonus = 0;
        moveSpeed = GameConfig.PLAYER_SPEED;
    }
}
