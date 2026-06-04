package com.desertadventure.combat.card.data;

import com.badlogic.gdx.utils.Json;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Loads and merges offense + change card manifest JSON files. */
public final class CardManifestLoader {
    public static final String OFFENSE_MANIFEST = "cards/offense_cards.json";
    public static final String CHANGE_MANIFEST = "cards/change_cards.json";

    private static final List<String> DEFAULT_MANIFESTS = List.of(OFFENSE_MANIFEST, CHANGE_MANIFEST);

    private CardManifestLoader() {
    }

    public static Map<String, CardDef> loadMergedFromJsonTexts(List<String> jsonTexts, List<String> sourceLabels) {
        if (jsonTexts.size() != sourceLabels.size()) {
            throw new IllegalArgumentException("jsonTexts and sourceLabels size mismatch");
        }
        Map<String, CardDef> defs = new HashMap<>();
        for (int i = 0; i < jsonTexts.size(); i++) {
            mergeInto(defs, jsonTexts.get(i), sourceLabels.get(i));
        }
        return defs;
    }

    public static Map<String, CardDef> loadMergedFromFiles(List<Path> files) throws Exception {
        List<String> texts = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (Path file : files) {
            texts.add(Files.readString(file, StandardCharsets.UTF_8));
            labels.add(file.toString());
        }
        return loadMergedFromJsonTexts(texts, labels);
    }

    public static List<Path> resolveDefaultManifestFiles(Path cwd) {
        List<Path> paths = new ArrayList<>();
        for (String relative : DEFAULT_MANIFESTS) {
            Path p1 = cwd.resolve("core/assets").resolve(relative);
            if (Files.exists(p1)) {
                paths.add(p1);
                continue;
            }
            Path p2 = cwd.resolve("assets").resolve(relative);
            if (Files.exists(p2)) {
                paths.add(p2);
                continue;
            }
            paths.add(p1);
        }
        return paths;
    }

    static void mergeInto(Map<String, CardDef> defs, String jsonText, String sourceLabel) {
        Json json = new Json();
        CardManifest manifest = json.fromJson(CardManifest.class, jsonText);
        if (manifest == null || manifest.cards == null) {
            throw new IllegalStateException("Invalid card manifest: " + sourceLabel);
        }
        for (CardDef def : manifest.cards) {
            if (def == null || def.id == null || def.id.isBlank()) {
                throw new IllegalStateException("Card with missing id in manifest: " + sourceLabel);
            }
            if (defs.put(def.id, def) != null) {
                throw new IllegalStateException("Duplicate card id across manifests: " + def.id);
            }
        }
    }

    static List<String> defaultManifestPaths() {
        return DEFAULT_MANIFESTS;
    }
}
