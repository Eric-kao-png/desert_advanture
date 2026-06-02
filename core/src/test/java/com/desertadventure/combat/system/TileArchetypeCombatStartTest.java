package com.desertadventure.combat.system;

import com.badlogic.gdx.utils.Json;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.card.data.CardDatabase;
import com.desertadventure.combat.card.data.CardDef;
import com.desertadventure.combat.card.data.CardManifest;
import com.desertadventure.combat.card.data.InMemoryCardRepository;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.combat.enemy.EnemyArchetypeRegistry;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.state.GameplayMode;
import com.desertadventure.state.GameSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileArchetypeCombatStartTest {
    @BeforeEach
    void setUpCards() throws Exception {
        if (CardDatabase.isInitialized()) {
            return;
        }
        Path manifestPath = resolveManifestPath();
        String jsonText = Files.readString(manifestPath, StandardCharsets.UTF_8);
        CardManifest manifest = new Json().fromJson(CardManifest.class, jsonText);
        Map<String, CardDef> defs = new HashMap<>();
        for (CardDef def : manifest.cards) {
            defs.put(def.id, def);
        }
        CardDatabase.initialize(new InMemoryCardRepository(defs));
    }

    @Test
    void combatTile_wanderingWizard_startsCombatWithWizardArchetype() {
        GameSession session = new GameSession();
        var tile = session.getMap().getTile(3, 0);
        assertNotNull(tile);
        session.handleTileInteraction(tile, false);

        assertEquals(GameplayMode.COMBAT, session.getMode());
        assertEquals(EnemyArchetypeId.WANDERING_WIZARD, session.consumePendingCombatArchetype());

        CombatController combat = session.getCombatController();
        combat.startCombat(
                session.getCurrentDistanceBand(),
                false,
                EnemyArchetypeId.WANDERING_WIZARD,
                800f,
                120f,
                new ActionCardDeck(),
                ignored -> {
                });

        assertEquals(EnemyArchetypeId.WANDERING_WIZARD, combat.getCurrentEnemyArchetype());
        assertEquals("Wandering Wizard", combat.getOpponentDisplayName());
        var wizardDef = EnemyArchetypeRegistry.getRequired(EnemyArchetypeId.WANDERING_WIZARD);
        float maxHp = combat.getEnemies().get(0).getMaxHp();
        assertTrue(maxHp >= wizardDef.hpMin() && maxHp <= wizardDef.hpMax());
    }

    @Test
    void combatTile_desertZombie_startsCombatWithZombieDeck() {
        GameSession session = new GameSession();
        var tile = session.getMap().getTile(-3, 0);
        assertNotNull(tile);
        session.handleTileInteraction(tile, false);

        assertEquals(EnemyArchetypeId.DESERT_ZOMBIE, session.consumePendingCombatArchetype());

        CombatController combat = session.getCombatController();
        combat.startCombat(
                0,
                false,
                EnemyArchetypeId.DESERT_ZOMBIE,
                800f,
                120f,
                new ActionCardDeck(),
                ignored -> {
                });

        assertEquals(EnemyArchetypeId.DESERT_ZOMBIE, combat.getCurrentEnemyArchetype());
        assertEquals("Desert Zombie", combat.getOpponentDisplayName());
        assertTrue(combat.enemyDeckInstancesForTests().stream()
                .anyMatch(i -> i.getType() == ActionCardType.CLAW));
    }

    @Test
    void bossFight_ignoresTileArchetypeAndShowsBossLabel() {
        PlayerStats stats = new PlayerStats();
        CombatController combat = new CombatController(stats);
        combat.startCombat(
                0,
                true,
                EnemyArchetypeId.WANDERING_WIZARD,
                800f,
                120f,
                new ActionCardDeck(),
                ignored -> {
                });

        assertNull(combat.getCurrentEnemyArchetype());
        assertEquals("Boss", combat.getOpponentDisplayName());
    }

    private static Path resolveManifestPath() {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path p1 = cwd.resolve("core/assets/cards/action_cards.json");
        if (Files.exists(p1)) {
            return p1;
        }
        return cwd.resolve("assets/cards/action_cards.json");
    }

}
