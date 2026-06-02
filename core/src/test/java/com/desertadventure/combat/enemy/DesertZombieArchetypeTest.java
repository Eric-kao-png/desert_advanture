package com.desertadventure.combat.enemy;

import com.desertadventure.combat.card.ActionCardRewards;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.system.slots.RandomIntSource;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesertZombieArchetypeTest {
    @Test
    void desertZombie_hpRoll_isInclusiveBetween7And8() {
        EnemyArchetypeDef def = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.DESERT_ZOMBIE);
        assertEquals(7, def.hpMin());
        assertEquals(8, def.hpMax());

        for (int roll = 0; roll <= 1; roll++) {
            int fixed = roll;
            RandomIntSource rng = bound -> fixed;
            assertEquals(7 + roll, def.rollMaxHp(rng), 0.001f);
        }
    }

    @Test
    void desertZombie_lootPool_containsOnlyHealShieldVampirism() {
        EnemyArchetypeDef def = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.DESERT_ZOMBIE);
        Set<ActionCardType> expected = EnumSet.of(
                ActionCardType.HEAL,
                ActionCardType.SHIELD,
                ActionCardType.VAMPIRISM);
        assertEquals(expected, Set.copyOf(def.lootPool()));
    }

    @Test
    void desertZombie_victoryRoll_onlyFromLootPool() {
        Set<ActionCardType> pool = Set.copyOf(
                EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.DESERT_ZOMBIE).lootPool());
        for (int i = 0; i < 30; i++) {
            assertTrue(pool.contains(ActionCardRewards.rollVictoryCard(EnemyArchetypeId.DESERT_ZOMBIE)));
        }
    }

    @Test
    void desertZombie_deck_hasExpectedCardCounts() {
        EnemyArchetypeDef def = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.DESERT_ZOMBIE);
        assertEquals(7, def.deckCardTypes().size());
        assertTrue(def.deckCardTypes().contains(ActionCardType.CLAW));
        assertTrue(def.deckCardTypes().contains(ActionCardType.ATTACK));
        assertTrue(def.deckCardTypes().contains(ActionCardType.HEAL));
        assertTrue(def.deckCardTypes().contains(ActionCardType.SHIELD));
        assertTrue(def.deckCardTypes().contains(ActionCardType.VAMPIRISM));
    }
}
