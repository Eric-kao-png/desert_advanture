package com.desertadventure.combat.status.data;

import com.badlogic.gdx.utils.Json;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Loads combat status effect definitions from JSON. */
public final class StatusEffectManifestLoader {
    public static final String MANIFEST_PATH = "combat/status_effects.json";

    private StatusEffectManifestLoader() {
    }

    public static StatusEffectRepository loadFromJson(String jsonText, String sourceLabel) {
        Json json = new Json();
        StatusEffectManifest manifest = json.fromJson(StatusEffectManifest.class, jsonText);
        if (manifest == null) {
            throw new IllegalStateException("Invalid status effect manifest: " + sourceLabel);
        }
        return InMemoryStatusEffectRepository.fromManifest(manifest, sourceLabel);
    }

    public static Path resolveManifestPath() {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path p1 = cwd.resolve("core/assets").resolve(MANIFEST_PATH);
        if (Files.exists(p1)) {
            return p1;
        }
        Path p2 = cwd.resolve("assets").resolve(MANIFEST_PATH);
        if (Files.exists(p2)) {
            return p2;
        }
        return p1;
    }

    public static StatusEffectRepository loadFromProjectAssets() {
        try {
            Path path = resolveManifestPath();
            String jsonText = Files.readString(path, StandardCharsets.UTF_8);
            return loadFromJson(jsonText, path.toString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load status effect manifest from project assets", e);
        }
    }
}
