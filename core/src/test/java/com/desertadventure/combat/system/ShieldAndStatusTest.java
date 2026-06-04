package com.desertadventure.combat.system;

import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.status.data.StatusEffectDatabase;
import com.desertadventure.combat.status.data.StatusEffectManifestLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ShieldAndStatusTest {
    @BeforeAll
    static void loadStatusManifest() {
        if (!StatusEffectDatabase.isInitialized()) {
            StatusEffectDatabase.initialize(StatusEffectManifestLoader.loadFromProjectAssets());
        }
    }

    @Test
    void takeDamage_consumesShieldFirst_thenHp() {
        CombatEntity e = new CombatEntity(CombatEntity.Kind.PLAYER, 0f, 0f, 10f);
        e.clearCombatStatus();
        e.setHp(10f);
        e.addShield(3);

        e.takeDamage(2f);
        assertEquals(1, e.getShield(), "shield should absorb damage first");
        assertEquals(10f, e.getHp(), 0.001f, "hp should not drop while shield absorbs all damage");

        e.takeDamage(5f);
        assertEquals(0, e.getShield(), "shield should be fully consumed");
        assertEquals(6f, e.getHp(), 0.001f, "remaining damage should reduce hp");
    }

    @Test
    void takeStatusDamage_bypassesShield() {
        CombatEntity e = new CombatEntity(CombatEntity.Kind.ENEMY, 0f, 0f, 10f);
        e.clearCombatStatus();
        e.setHp(10f);
        e.addShield(999);

        e.takeStatusDamage(2f);
        assertEquals(999, e.getShield(), "status damage should not consume shield");
        assertEquals(8f, e.getHp(), 0.001f, "status damage should reduce hp directly");
    }

    @Test
    void poison_roundEndDealsStatusDamage_bypassingShield_andTicksDurationToClear() {
        CombatEntity e = new CombatEntity(CombatEntity.Kind.ENEMY, 0f, 0f, 10f);
        e.clearCombatStatus();
        e.setHp(10f);
        e.addShield(5);
        e.setNegativeStatus(NegativeStatusType.POISON, 1);

        e.applyRoundEndStatusEffects();

        assertEquals(5, e.getShield(), "poison tick should bypass shield");
        assertEquals(8f, e.getHp(), 0.001f, "poison tick should deal damage");
        assertNull(e.getNegativeStatusType(), "duration should tick down and clear poison at 0");
        assertEquals(0, e.getNegativeTurnsRemaining());
    }

    @Test
    void negativeStatus_isSingleSlot_newStatusOverwritesTypeAndTurns() {
        CombatEntity e = new CombatEntity(CombatEntity.Kind.ENEMY, 0f, 0f, 10f);
        e.clearCombatStatus();

        e.setNegativeStatus(NegativeStatusType.POISON, 2);
        assertEquals(NegativeStatusType.POISON, e.getNegativeStatusType());
        assertEquals(2, e.getNegativeTurnsRemaining());

        e.setNegativeStatus(NegativeStatusType.BLEED, 2);
        assertEquals(NegativeStatusType.BLEED, e.getNegativeStatusType(), "new status should overwrite old status type");
        assertEquals(2, e.getNegativeTurnsRemaining(), "new status should overwrite turns");

        e.setNegativeStatus(NegativeStatusType.FEAR, 1);
        assertEquals(NegativeStatusType.FEAR, e.getNegativeStatusType(), "new status should overwrite old status type");
        assertEquals(1, e.getNegativeTurnsRemaining(), "new status should overwrite turns");
    }
}

