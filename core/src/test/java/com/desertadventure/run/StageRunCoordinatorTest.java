package com.desertadventure.run;

import com.desertadventure.combat.CombatOutcome;
import com.desertadventure.combat.card.ActionCardDeck;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.player.PlayerStats;
import com.desertadventure.state.GameplayMode;
import com.desertadventure.state.MessageFeed;
import com.desertadventure.state.PermanentProgress;
import com.desertadventure.combat.system.support.CombatTestDataBootstrap;
import com.desertadventure.state.SessionModeAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageRunCoordinatorTest {
    private StageCatalog catalog;
    private RunProgress runProgress;
    private PlayerStats playerStats;
    private ActionCardDeck deck;
    private TestMode mode;
    private StageRunCoordinator coordinator;

    @BeforeEach
    void setUp() throws Exception {
        CombatTestDataBootstrap.ensureProductionDatabases();
        var path = com.desertadventure.run.data.StageManifestLoader.resolveManifestPath();
        String json = Files.readString(path, StandardCharsets.UTF_8);
        catalog = com.desertadventure.run.data.StageManifestLoader.loadFromJson(json, path.toString());
        runProgress = new RunProgress(catalog);
        playerStats = new PlayerStats();
        deck = ActionCardDeck.fromCardTypes(List.of(ActionCardType.ATTACK, ActionCardType.HEAL));
        mode = new TestMode();
        coordinator = new StageRunCoordinator(
                runProgress,
                playerStats,
                new PermanentProgress(),
                new MessageFeed(),
                deck,
                mode);
    }

    @Test
    void advanceAfterVictory_incrementsStageIndex() {
        assertEquals(0, runProgress.getCurrentStageIndex());
        assertTrue(runProgress.advanceAfterVictory());
        assertEquals(1, runProgress.getCurrentStageIndex());
        assertEquals("Wandering Mage", runProgress.getCurrentStage().label());
    }

    @Test
    void defeat_rewindsToStageOneAndClearsCooldowns() {
        deck.findById(1).setCooldownRemaining(3);
        deck.findById(2).setCooldownRemaining(2);
        runProgress.advanceAfterVictory();
        runProgress.advanceAfterVictory();
        assertEquals(2, runProgress.getCurrentStageIndex());

        coordinator.apply(CombatOutcome.DEFEAT, null);

        assertEquals(0, runProgress.getCurrentStageIndex());
        assertEquals(0, deck.findById(1).getCooldownRemaining());
        assertEquals(0, deck.findById(2).getCooldownRemaining());
        assertEquals(2, deck.getInstances().size());
        assertEquals(GameplayMode.HUB, mode.get());
    }

    @Test
    void cooldownPersistsBetweenStagesWithoutRewind() {
        deck.findById(1).setCooldownRemaining(2);
        runProgress.advanceAfterVictory();
        assertEquals(2, deck.findById(1).getCooldownRemaining());
        assertEquals(1, runProgress.getCurrentStageIndex());
        assertEquals(2, deck.getInstances().size());
    }

    private static final class TestMode implements SessionModeAccess {
        private GameplayMode mode = GameplayMode.HUB;
        private final List<GameplayMode> history = new ArrayList<>();

        @Override
        public GameplayMode get() {
            return mode;
        }

        @Override
        public void set(GameplayMode mode) {
            this.mode = mode;
            history.add(mode);
        }
    }
}
