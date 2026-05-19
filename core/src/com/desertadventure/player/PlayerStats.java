package com.desertadventure.player;

import com.desertadventure.config.GameConfig;

public class PlayerStats {
    private int level = GameConfig.PLAYER_INITIAL_LEVEL;
    private int experience;
    private int experienceToNext = GameConfig.PLAYER_INITIAL_EXPERIENCE_TO_NEXT;
    private float maxHp = GameConfig.PLAYER_INITIAL_MAX_HP;
    private float hp = GameConfig.PLAYER_INITIAL_MAX_HP;
    private int attack = GameConfig.PLAYER_INITIAL_ATTACK;
    private int defense = GameConfig.PLAYER_INITIAL_DEFENSE;
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
        maxHp += GameConfig.PLAYER_LEVEL_HP_GAIN;
        hp = maxHp;
        attack += GameConfig.PLAYER_LEVEL_ATTACK_GAIN;
        defense += GameConfig.PLAYER_LEVEL_DEFENSE_GAIN;
        stepBudgetBonus += GameConfig.PLAYER_LEVEL_STEP_BONUS;
        experienceToNext = (int) (experienceToNext * GameConfig.PLAYER_LEVEL_EXP_MULTIPLIER);
    }

    public void applyItemBonus() {
        attack += GameConfig.PLAYER_ITEM_ATTACK_BONUS;
        stepBudgetBonus += GameConfig.PLAYER_ITEM_STEP_BONUS;
    }

    public void resetForNewGame() {
        level = GameConfig.PLAYER_INITIAL_LEVEL;
        experience = 0;
        experienceToNext = GameConfig.PLAYER_INITIAL_EXPERIENCE_TO_NEXT;
        maxHp = GameConfig.PLAYER_INITIAL_MAX_HP;
        hp = GameConfig.PLAYER_INITIAL_MAX_HP;
        attack = GameConfig.PLAYER_INITIAL_ATTACK;
        defense = GameConfig.PLAYER_INITIAL_DEFENSE;
        stepBudgetBonus = 0;
        moveSpeed = GameConfig.PLAYER_SPEED;
    }
}
