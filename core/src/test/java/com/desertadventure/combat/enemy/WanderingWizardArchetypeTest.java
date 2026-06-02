package com.desertadventure.combat.enemy;

import com.desertadventure.combat.card.ActionCardRewards;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.system.slots.RandomIntSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WanderingWizardArchetypeTest {
    @BeforeAll
    static void loadEnemies() {
        EnemyArchetypeTestSupport.ensureLoaded();
    }

    @Test
    void wanderingWizard_hpRoll_isInclusiveBetween6And7() {
        EnemyArchetypeDef def = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.WANDERING_WIZARD);
        assertEquals(6, def.hpMin());
        assertEquals(7, def.hpMax());

        for (int roll = 0; roll <= 1; roll++) {
            int fixed = roll;
            RandomIntSource rng = bound -> fixed;
            assertEquals(6 + roll, def.rollMaxHp(rng), 0.001f);
        }
    }

    @Test
    void wanderingWizard_lootPool_containsMagicBoltMagicArrowPurify() {
        EnemyArchetypeDef def = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.WANDERING_WIZARD);
        Set<ActionCardType> expected = EnumSet.of(
                ActionCardType.MAGIC_BOLT,
                ActionCardType.MAGIC_ARROW,
                ActionCardType.PURIFY);
        assertEquals(expected, Set.copyOf(def.lootPool()));
    }

    @Test
    void wanderingWizard_victoryRoll_onlyFromLootPool() {
        Set<ActionCardType> pool = Set.copyOf(
                EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.WANDERING_WIZARD).lootPool());
        for (int i = 0; i < 30; i++) {
            assertTrue(pool.contains(ActionCardRewards.rollVictoryCard(EnemyArchetypeId.WANDERING_WIZARD)));
        }
    }

    @Test
    void wanderingWizard_deck_hasExpectedCards() {
        EnemyArchetypeDef def = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.WANDERING_WIZARD);
        assertEquals(7, def.deckCardTypes().size());
        assertEquals(2, def.deckCardTypes().stream().filter(t -> t == ActionCardType.MAGIC_BOLT).count());
        assertTrue(def.deckCardTypes().contains(ActionCardType.MAGIC_ARROW));
        assertTrue(def.deckCardTypes().contains(ActionCardType.POISON_BOLT));
        assertTrue(def.deckCardTypes().contains(ActionCardType.POISON_MAGIC));
        assertTrue(def.deckCardTypes().contains(ActionCardType.PURIFY));
        assertTrue(def.deckCardTypes().contains(ActionCardType.MAGIC_MIRROR));
    }

    @Test
    void rollNormalEncounter_includesWanderingWizard() {
        RandomIntSource wizardOnly = bound -> 1;
        assertEquals(EnemyArchetypeId.WANDERING_WIZARD, EnemyArchetypeRegistry.rollNormalEncounter(wizardOnly));
    }

    @Test
    void resolveNormalEncounter_usesTileArchetypeOrRolls() {
        RandomIntSource zombieOnly = bound -> 0;
        assertEquals(
                EnemyArchetypeId.WANDERING_WIZARD,
                EnemyArchetypeRegistry.resolveNormalEncounter(EnemyArchetypeId.WANDERING_WIZARD, zombieOnly));
        assertEquals(
                EnemyArchetypeId.DESERT_ZOMBIE,
                EnemyArchetypeRegistry.resolveNormalEncounter(null, zombieOnly));
    }
}
