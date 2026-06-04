package com.desertadventure.combat.system.support;

import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.system.CombatController;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardManifestLoader;
import com.desertadventure.combat.card.data.CardTargetingId;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Shared helpers for card resolver and combat integration tests. */
public final class CombatCardTestSupport {
    private CombatCardTestSupport() {
    }

    public static void initializeProductionCardDatabase() throws Exception {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Map<String, CardDef> defs = CardManifestLoader.loadMergedFromFiles(
                CardManifestLoader.resolveDefaultManifestFiles(cwd));
        CardDatabase.initialize(new InMemoryCardRepository(defs));
    }

    public static ActionCardDeck deckWithSingleCard(ActionCardType type) {
        ActionCardDeck deck = new ActionCardDeck();
        deck.addCard(type);
        return deck;
    }

    public static void assignToFirstPlayerSlotAndResolveRound(
            CombatController combat, ActionCardDeck deck, ActionCardType type) {
        int id = deck.getInstances().stream()
                .filter(i -> i.getType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing card: " + type))
                .getInstanceId();
        int slot = CombatIntegrationTestSupport.firstPlayerSlot(combat);
        combat.assignToPlayerSlot(slot, id);
        assertNotNull(combat.getSlotCard(slot), "card should be assigned to player slot " + slot);
        combat.confirmPlanning();
        combat.update(999f);
        combat.finalizePendingOutcome();
    }

    public static CardDef productionDef(ActionCardType type) {
        return CardDatabase.getRequired().getRequired(type.name());
    }

    public static boolean targetsEnemy(CardDef def) {
        return def.targeting == CardTargetingId.ENEMY;
    }

    public static boolean targetsSelf(CardDef def) {
        return def.targeting == CardTargetingId.SELF;
    }
}
