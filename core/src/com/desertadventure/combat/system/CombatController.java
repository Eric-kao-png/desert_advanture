package com.desertadventure.combat.system;

import com.badlogic.gdx.math.Rectangle;
import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.config.GameConfig;
import com.desertadventure.player.PlayerStats;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CombatController {
    private final PlayerStats playerStats;
    private CombatEntity player;
    private final List<CombatEntity> enemies = new ArrayList<>();
    private boolean bossFight;
    private float basicCooldown;
    private float skillCooldown;
    private float ultimateCooldown;
    private float arenaWidth;
    private float groundY;
    private Consumer<CombatOutcome> onCombatEnd;
    private boolean combatEnded;

    public CombatController(PlayerStats playerStats) {
        this.playerStats = playerStats;
    }

    public boolean isActive() {
        return player != null && !combatEnded;
    }

    public void startCombat(int distanceBand, boolean boss, float arenaWidth, float groundY, Consumer<CombatOutcome> onEnd) {
        this.bossFight = boss;
        this.arenaWidth = arenaWidth;
        this.groundY = groundY;
        this.onCombatEnd = onEnd;
        enemies.clear();
        combatEnded = false;

        float playerX = arenaWidth * GameConfig.COMBAT_PLAYER_X_RATIO;
        player = new CombatEntity(CombatEntity.Kind.PLAYER, playerX, groundY,
                playerStats.getMaxHp(), playerStats.getAttack(), playerStats.getMoveSpeed());
        playerStats.setHp(playerStats.getMaxHp());

        float enemyHp = GameConfig.ENEMY_BASE_HP + distanceBand * GameConfig.ENEMY_HP_PER_DISTANCE_BAND;
        int enemyAttack = 0;
        float enemyX = arenaWidth * GameConfig.COMBAT_ENEMY_X_RATIO;
        if (boss) {
            enemyHp = GameConfig.BOSS_BASE_HP + distanceBand * GameConfig.BOSS_HP_PER_DISTANCE_BAND;
            enemyX = arenaWidth * GameConfig.COMBAT_BOSS_X_RATIO;
            enemies.add(new CombatEntity(CombatEntity.Kind.BOSS, enemyX, groundY,
                    enemyHp, enemyAttack, GameConfig.BOSS_SPEED));
        } else {
            enemies.add(new CombatEntity(CombatEntity.Kind.ENEMY, enemyX, groundY,
                    enemyHp, enemyAttack, GameConfig.ENEMY_SPEED));
        }

        basicCooldown = 0f;
        skillCooldown = 0f;
        ultimateCooldown = 0f;
    }

    public CombatEntity getPlayer() {
        return player;
    }

    public List<CombatEntity> getEnemies() {
        return enemies;
    }

    public boolean isBossFight() {
        return bossFight;
    }

    public void update(float delta, float moveInput) {
        if (player == null || combatEnded) {
            return;
        }
        basicCooldown = Math.max(0f, basicCooldown - delta);
        skillCooldown = Math.max(0f, skillCooldown - delta);
        ultimateCooldown = Math.max(0f, ultimateCooldown - delta);

        player.moveBy(moveInput * playerStats.getMoveSpeed() * delta);
        player.update(delta, arenaWidth);
        playerStats.setHp(player.getHp());

        for (CombatEntity enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            enemy.update(delta, arenaWidth);
        }

        enemies.removeIf(e -> !e.isAlive());

        if (!player.isAlive()) {
            endCombat(CombatOutcome.DEFEAT);
            return;
        }
        if (enemies.isEmpty()) {
            endCombat(bossFight ? CombatOutcome.BOSS_VICTORY : CombatOutcome.VICTORY);
        }
    }

    private void endCombat(CombatOutcome result) {
        if (combatEnded) {
            return;
        }
        combatEnded = true;
        if (onCombatEnd != null) {
            onCombatEnd.accept(result);
        }
    }

    public void tryBasicAttack() {
        if (basicCooldown > 0f || player == null) {
            return;
        }
        Rectangle hitbox = player.getForwardAttackHitbox(
                GameConfig.BASIC_ATTACK_DEPTH, GameConfig.BASIC_ATTACK_HEIGHT);
        if (damageEnemiesInHitbox(hitbox, playerStats.getAttack())) {
            basicCooldown = GameConfig.BASIC_ATTACK_COOLDOWN;
        }
    }

    public void trySkill() {
        if (skillCooldown > 0f || player == null) {
            return;
        }
        Rectangle hitbox = player.getForwardAttackHitbox(GameConfig.SKILL_RANGE, player.getHeight());
        if (damageEnemiesInHitbox(hitbox, playerStats.getAttack() + GameConfig.SKILL_ATTACK_BONUS)) {
            skillCooldown = GameConfig.SKILL_COOLDOWN;
        }
    }

    public void tryUltimate() {
        if (ultimateCooldown > 0f || player == null) {
            return;
        }
        Rectangle hitbox = player.getForwardAttackHitbox(GameConfig.ULTIMATE_RANGE, player.getHeight());
        if (damageEnemiesInHitbox(hitbox, playerStats.getAttack() + GameConfig.ULTIMATE_ATTACK_BONUS)) {
            ultimateCooldown = GameConfig.ULTIMATE_COOLDOWN;
        }
    }

    private boolean damageEnemiesInHitbox(Rectangle hitbox, int attackPower) {
        boolean hit = false;
        for (CombatEntity enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            if (hitbox.overlaps(enemy.getBounds())) {
                enemy.takeDamage(DamageCalculator.compute(attackPower, 0));
                hit = true;
            }
        }
        return hit;
    }
}
