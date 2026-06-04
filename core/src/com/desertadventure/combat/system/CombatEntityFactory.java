package com.desertadventure.combat.system;

import com.desertadventure.combat.enemy.EnemyArchetypeDef;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.combat.enemy.EnemyArchetypeRegistry;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.system.slots.RandomIntSource;
import com.desertadventure.config.CombatConfig;
import com.desertadventure.player.PlayerStats;

/** Spawns player and opponent entities for a combat session. */
final class CombatEntityFactory {
    private final PlayerStats playerStats;
    private final RandomIntSource enemyHpRng;

    CombatEntityFactory(PlayerStats playerStats, RandomIntSource enemyHpRng) {
        this.playerStats = playerStats;
        this.enemyHpRng = enemyHpRng;
    }

    CombatEntity createPlayerEntity(float arenaWidth, float groundY) {
        float playerX = arenaWidth * CombatConfig.COMBAT_PLAYER_X_RATIO;
        CombatEntity playerEntity = new CombatEntity(
                CombatEntity.Kind.PLAYER, playerX, groundY, playerStats.getMaxHp());
        playerEntity.setHp(playerStats.getHp());
        playerEntity.clearCombatStatus();
        return playerEntity;
    }

    CombatEntity createOpponentEntity(
            int stageIndex,
            boolean boss,
            float arenaWidth,
            float groundY,
            EnemyArchetypeId currentEnemyArchetype) {
        if (boss) {
            float bossHp = CombatConfig.BOSS_BASE_HP + stageIndex * CombatConfig.BOSS_HP_PER_DISTANCE_BAND;
            float bossX = arenaWidth * CombatConfig.COMBAT_BOSS_X_RATIO;
            CombatEntity bossEntity = new CombatEntity(CombatEntity.Kind.BOSS, bossX, groundY, bossHp);
            bossEntity.clearCombatStatus();
            return bossEntity;
        }

        float enemyX = arenaWidth * CombatConfig.COMBAT_ENEMY_X_RATIO;
        EnemyArchetypeDef archetype = EnemyArchetypeRegistry.getRequired(currentEnemyArchetype);
        float enemyHp = archetype.rollMaxHp(enemyHpRng);
        CombatEntity enemy = new CombatEntity(CombatEntity.Kind.ENEMY, enemyX, groundY, enemyHp);
        enemy.clearCombatStatus();
        return enemy;
    }
}
