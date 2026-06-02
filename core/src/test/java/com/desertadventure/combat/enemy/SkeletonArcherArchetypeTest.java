package com.desertadventure.combat.enemy;

import com.desertadventure.combat.card.ActionCardRewards;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.system.slots.RandomIntSource;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkeletonArcherArchetypeTest {
    @Test
    void skeletonArcher_hpRoll_isInclusiveBetween6And7() {
        EnemyArchetypeDef def = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.SKELETON_ARCHER);
        assertEquals(6, def.hpMin());
        assertEquals(7, def.hpMax());

        for (int roll = 0; roll <= 1; roll++) {
            int fixed = roll;
            RandomIntSource rng = bound -> fixed;
            assertEquals(6 + roll, def.rollMaxHp(rng), 0.001f);
        }
    }

    @Test
    void skeletonArcher_lootPool_containsArrowAndPoisonArrow() {
        EnemyArchetypeDef def = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.SKELETON_ARCHER);
        Set<ActionCardType> expected = EnumSet.of(
                ActionCardType.ARROW,
                ActionCardType.POISON_ARROW);
        assertEquals(expected, Set.copyOf(def.lootPool()));
    }

    @Test
    void skeletonArcher_victoryRoll_onlyFromLootPool() {
        Set<ActionCardType> pool = Set.copyOf(
                EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.SKELETON_ARCHER).lootPool());
        for (int i = 0; i < 30; i++) {
            assertTrue(pool.contains(ActionCardRewards.rollVictoryCard(EnemyArchetypeId.SKELETON_ARCHER)));
        }
    }

    @Test
    void skeletonArcher_deck_hasExpectedCards() {
        EnemyArchetypeDef def = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.SKELETON_ARCHER);
        assertEquals(5, def.deckCardTypes().size());
        assertEquals(3, def.deckCardTypes().stream().filter(t -> t == ActionCardType.ARROW).count());
        assertEquals(2, def.deckCardTypes().stream().filter(t -> t == ActionCardType.POISON_ARROW).count());
    }

    @Test
    void rollNormalEncounter_includesSkeletonArcher() {
        RandomIntSource archerOnly = bound -> 2;
        assertEquals(EnemyArchetypeId.SKELETON_ARCHER, EnemyArchetypeRegistry.rollNormalEncounter(archerOnly));
    }

    @Test
    void resolveNormalEncounter_usesTileArchetypeOrRolls() {
        RandomIntSource zombieOnly = bound -> 0;
        assertEquals(
                EnemyArchetypeId.SKELETON_ARCHER,
                EnemyArchetypeRegistry.resolveNormalEncounter(EnemyArchetypeId.SKELETON_ARCHER, zombieOnly));
        assertEquals(
                EnemyArchetypeId.DESERT_ZOMBIE,
                EnemyArchetypeRegistry.resolveNormalEncounter(null, zombieOnly));
    }
}
