package com.desertadventure.combat.status;

import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.model.PositiveStatusType;
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

    @Test
    void positive_scaleArmorReducesIncomingOffense() {
        CombatEntity player = new CombatEntity(CombatEntity.Kind.PLAYER, 0f, 0f, 20f);
        player.setPositiveStatus(PositiveStatusType.SCALE_ARMOR, 2);

        var result = StatusEffectRuntime.resolveIncomingOffenseToSelf(3f, player);
        assertEquals(2f, result.damageToBearer(), 0.001f);
    }

    @Test
    void positive_dodgeBlocksAndClears() {
        CombatEntity player = new CombatEntity(CombatEntity.Kind.PLAYER, 0f, 0f, 20f);
        player.setPositiveStatus(PositiveStatusType.DODGE, 3);

        var result = StatusEffectRuntime.resolveIncomingOffenseToSelf(5f, player);
        assertEquals(0f, result.damageToBearer(), 0.001f);
        assertEquals(true, result.blocked());
        assertEquals(false, player.hasPositiveStatus());
    }

    @Test
    void positive_focusIgnoresShieldOnOffense() {
        CombatEntity player = new CombatEntity(CombatEntity.Kind.PLAYER, 0f, 0f, 20f);
        player.setPositiveStatus(PositiveStatusType.FOCUS, 3);

        assertEquals(true, StatusEffectRuntime.casterIgnoresShieldOnOffense(player));
    }

    @Test
    void positive_vampireFangHealOnOffenseWhenOpponentHadNoShield() {
        CombatEntity player = new CombatEntity(CombatEntity.Kind.PLAYER, 0f, 0f, 20f);
        player.setPositiveStatus(PositiveStatusType.VAMPIRE_FANG, 2);

        assertEquals(2f, StatusEffectRuntime.outgoingOffenseHealCaster(player, 0), 0.001f);
    }

    @Test
    void positive_vampireFangNoHealWhenOpponentHadShield() {
        CombatEntity player = new CombatEntity(CombatEntity.Kind.PLAYER, 0f, 0f, 20f);
        player.setPositiveStatus(PositiveStatusType.VAMPIRE_FANG, 2);

        assertEquals(0f, StatusEffectRuntime.outgoingOffenseHealCaster(player, 4), 0.001f);
    }

    @Test
    void positive_spikeShieldRetaliatesOnIncomingOffense() {
        CombatEntity player = new CombatEntity(CombatEntity.Kind.PLAYER, 0f, 0f, 20f);
        player.setPositiveStatus(PositiveStatusType.SPIKE_SHIELD, 2);

        var result = StatusEffectRuntime.resolveIncomingOffenseToSelf(2f, player);
        assertEquals(2f, result.retaliateAttacker(), 0.001f);
    }
}
