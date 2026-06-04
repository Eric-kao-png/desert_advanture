package com.desertadventure.combat.card.data;

import com.desertadventure.combat.card.ActionCardType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensures every {@link ActionCardType} is referenced by resolver and integration test suites.
 */
class ActionCardCoverageTest {
    private static final Path TEST_ROOT = resolveTestRoot();

    @Test
    void everyActionCardType_hasDedicatedResolverTestMethod() throws Exception {
        String source = readTestSource(
                "com/desertadventure/combat/system/CardEffectResolverTest.java");
        assertCoversAllTypes(source, "CardEffectResolverTest");
    }

    @Test
    void productionResolverSmokeTest_iteratesAllActionCardTypes() throws Exception {
        String source = readTestSource(
                "com/desertadventure/combat/system/ProductionCardEffectResolverTest.java");
        assertTrue(source.contains("for (ActionCardType type : ActionCardType.values())"),
                "ProductionCardEffectResolverTest must loop all ActionCardType values");
    }

    @Test
    void integrationSmokeTest_iteratesAllActionCardTypes() throws Exception {
        String source = readTestSource(
                "com/desertadventure/combat/system/AllActionCardsIntegrationTest.java");
        assertTrue(source.contains("for (ActionCardType type : ActionCardType.values())"),
                "AllActionCardsIntegrationTest must loop all ActionCardType values");
    }

    @Test
    void documentedIntegrationSuites_coverKnownCards() throws Exception {
        Set<ActionCardType> covered = EnumSet.noneOf(ActionCardType.class);
        mergeTypesFromFile(covered, "com/desertadventure/combat/system/CombatFlowIntegrationTest.java");
        mergeTypesFromFile(covered, "com/desertadventure/combat/system/NewMagicCardsIntegrationTest.java");
        mergeTypesFromFile(covered, "com/desertadventure/combat/system/ArrowCardsIntegrationTest.java");
        mergeTypesFromFile(covered, "com/desertadventure/combat/system/CombatOutcomeFinalizeIntegrationTest.java");

        Set<ActionCardType> expectedDetailed = EnumSet.of(
                ActionCardType.POISON_MAGIC,
                ActionCardType.ATTACK,
                ActionCardType.BLADE,
                ActionCardType.GREAT_BLADE,
                ActionCardType.CHARGED_SLASH,
                ActionCardType.PURIFY,
                ActionCardType.MAGIC_BOLT,
                ActionCardType.POISON_BOLT,
                ActionCardType.MAGIC_MIRROR,
                ActionCardType.MAGIC_ARROW,
                ActionCardType.ARROW,
                ActionCardType.POISON_ARROW,
                ActionCardType.STRIKE);

        assertEquals(expectedDetailed, covered,
                "Update ActionCardCoverageTest when adding focused integration tests");
    }

    private static void mergeTypesFromFile(Set<ActionCardType> into, String relativePath) throws Exception {
        String source = readTestSource(relativePath);
        for (ActionCardType type : ActionCardType.values()) {
            if (source.contains("ActionCardType." + type.name())) {
                into.add(type);
            }
        }
    }

    private static void assertCoversAllTypes(String source, String label) {
        Set<ActionCardType> missing = EnumSet.allOf(ActionCardType.class).stream()
                .filter(type -> !source.contains("ActionCardType." + type.name()))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ActionCardType.class)));
        assertEquals(Set.of(), missing, label + " missing coverage for: " + missing);
    }

    private static String readTestSource(String relativePath) throws Exception {
        Path path = TEST_ROOT.resolve(relativePath);
        assertTrue(Files.exists(path), "Missing test file: " + path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /** Gradle :core:test uses module cwd; repo-root runs use project cwd. */
    private static Path resolveTestRoot() {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path fromRepoRoot = cwd.resolve("core/src/test/java");
        if (Files.isDirectory(fromRepoRoot)) {
            return fromRepoRoot;
        }
        Path fromCoreModule = cwd.resolve("src/test/java");
        if (Files.isDirectory(fromCoreModule)) {
            return fromCoreModule;
        }
        throw new IllegalStateException("Cannot locate test sources from cwd=" + cwd);
    }
}
