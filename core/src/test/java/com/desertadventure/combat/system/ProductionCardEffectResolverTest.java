package com.desertadventure.combat.system;

import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.model.CombatEntity;
import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.system.support.CombatCardTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/** Smoke-tests every card type using production offense/change JSON via {@link CardEffectResolver}. */
class ProductionCardEffectResolverTest {
    private static CardEffectResolver resolver;

    @BeforeAll
    static void loadProductionCards() throws Exception {
        CombatCardTestSupport.initializeProductionCardDatabase();
        resolver = new CardEffectResolver();
    }

    @Test
    void everyCardType_resolvesFromProductionManifest() {
        for (ActionCardType type : ActionCardType.values()) {
            CardEffectResolverTest.FakeCombatController combat =
                    new CardEffectResolverTest.FakeCombatController();
            CombatEntity enemy = new CombatEntity(CombatEntity.Kind.ENEMY, 0f, 0f, 40f);
            combat.enemies.add(enemy);
            combat.playerEntity.setNegativeStatus(NegativeStatusType.POISON, 1);

            assertDoesNotThrow(
                    () -> resolver.resolve(new CombatContext(combat, 1), type),
                    () -> "resolve failed for " + type);
        }
    }
}
