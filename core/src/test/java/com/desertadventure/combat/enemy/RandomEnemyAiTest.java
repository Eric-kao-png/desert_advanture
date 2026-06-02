package com.desertadventure.combat.enemy;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.system.slots.RandomIntSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RandomEnemyAiTest {
    @Test
    void pickCard_usesInjectedRngIndex() {
        ActionCardDeck deck = ActionCardDeck.fromCardTypes(List.of(
                ActionCardType.CLAW,
                ActionCardType.ATTACK,
                ActionCardType.HEAL));
        List<ActionCardInstance> candidates = deck.getInstances();
        RandomIntSource rng = bound -> 1;
        EnemyAi ai = new RandomEnemyAi(rng);

        assertEquals(ActionCardType.ATTACK, ai.pickCard(candidates).getType());
    }

    @Test
    void pickCard_returnsNullWhenNoCandidates() {
        EnemyAi ai = new RandomEnemyAi(bound -> 0);
        assertNull(ai.pickCard(List.of()));
    }
}
