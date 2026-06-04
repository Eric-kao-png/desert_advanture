package com.desertadventure.combat.status;

import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.status.data.StatusEffectDatabase;
import com.desertadventure.combat.status.data.StatusEffectManifestLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusEffectRuntimeTest {
    @BeforeAll
    static void loadStatusManifest() {
        if (!StatusEffectDatabase.isInitialized()) {
            StatusEffectDatabase.initialize(StatusEffectManifestLoader.loadFromProjectAssets());
        }
    }

    @Test
    void roundEnd_poisonUsesFixedDamageFromManifest() {
        CombatEntity entity = new CombatEntity(CombatEntity.Kind.ENEMY, 0f, 0f, 10f);
        entity.setNegativeStatus(NegativeStatusType.POISON, 2);

        assertEquals(2f, StatusEffectRuntime.computeRoundEndDamage(entity), 0.001f);
    }

    @Test
    void roundEnd_bleedUsesRemainingTurns() {
        CombatEntity entity = new CombatEntity(CombatEntity.Kind.ENEMY, 0f, 0f, 10f);
        entity.setNegativeStatus(NegativeStatusType.BLEED, 3);

        assertEquals(3f, StatusEffectRuntime.computeRoundEndDamage(entity), 0.001f);
    }

    @Test
    void roundEnd_fearHasNoRoundEndDamage() {
        CombatEntity entity = new CombatEntity(CombatEntity.Kind.ENEMY, 0f, 0f, 10f);
        entity.setNegativeStatus(NegativeStatusType.FEAR, 1);

        assertEquals(0f, StatusEffectRuntime.computeRoundEndDamage(entity), 0.001f);
    }

    @Test
    void modifier_fearAddsOneToIncomingOffenseDamage() {
        CombatEntity enemy = new CombatEntity(CombatEntity.Kind.ENEMY, 0f, 0f, 10f);
        enemy.setNegativeStatus(NegativeStatusType.FEAR, 1);

        assertEquals(3f, StatusEffectRuntime.applyIncomingOffenseCardDamageModifiers(2f, enemy), 0.001f);
    }

    @Test
    void modifier_noStatusLeavesDamageUnchanged() {
        CombatEntity enemy = new CombatEntity(CombatEntity.Kind.ENEMY, 0f, 0f, 10f);

        assertEquals(2f, StatusEffectRuntime.applyIncomingOffenseCardDamageModifiers(2f, enemy), 0.001f);
    }
}
