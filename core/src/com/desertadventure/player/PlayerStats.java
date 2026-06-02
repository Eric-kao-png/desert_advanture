package com.desertadventure.player;

import com.desertadventure.config.GameConfig;
import com.desertadventure.config.PlayerConfig;

public class PlayerStats {
    private int level = PlayerConfig.INITIAL_LEVEL;
    private int experience;
    private int experienceToNext = PlayerConfig.INITIAL_EXPERIENCE_TO_NEXT;
    private float maxHp = PlayerConfig.INITIAL_MAX_HP;
    private float hp = PlayerConfig.INITIAL_MAX_HP;
    private int attack = PlayerConfig.INITIAL_ATTACK;
    private int defense = PlayerConfig.INITIAL_DEFENSE;
    private int stepBudgetBonus;
    private float moveSpeed = PlayerConfig.SPEED;

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

    public void restoreHp(float amount) {
        setHp(hp + amount);
    }

    public void increaseMaxHp(float amount) {
        maxHp += amount;
        hp += amount;
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
        maxHp += PlayerConfig.LEVEL_HP_GAIN;
        hp = maxHp;
        attack += PlayerConfig.LEVEL_ATTACK_GAIN;
        defense += PlayerConfig.LEVEL_DEFENSE_GAIN;
        stepBudgetBonus += PlayerConfig.LEVEL_STEP_BONUS;
        experienceToNext = (int) (experienceToNext * PlayerConfig.LEVEL_EXP_MULTIPLIER);
    }

    public void resetForNewGame() {
        level = PlayerConfig.INITIAL_LEVEL;
        experience = 0;
        experienceToNext = PlayerConfig.INITIAL_EXPERIENCE_TO_NEXT;
        maxHp = PlayerConfig.INITIAL_MAX_HP;
        hp = PlayerConfig.INITIAL_MAX_HP;
        attack = PlayerConfig.INITIAL_ATTACK;
        defense = PlayerConfig.INITIAL_DEFENSE;
        stepBudgetBonus = 0;
        moveSpeed = PlayerConfig.SPEED;
    }
}
