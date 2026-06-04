package com.desertadventure.run.data;

import com.badlogic.gdx.utils.Json;
import com.desertadventure.combat.enemy.EnemyArchetypeId;
import com.desertadventure.run.StageCatalog;
import com.desertadventure.run.StageDef;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Parses stage progression JSON into a {@link StageCatalog}. */
public final class StageManifestLoader {
    private StageManifestLoader() {
    }

    public static StageCatalog loadFromJson(String jsonText, String sourceLabel) {
        StageManifest manifest = new Json().fromJson(StageManifest.class, jsonText);
        if (manifest == null || manifest.stages == null || manifest.stages.isEmpty()) {
            throw new IllegalStateException("Invalid stage manifest: " + sourceLabel);
        }
        List<StageDef> stages = new ArrayList<>();
        for (StageJsonDef jsonDef : manifest.stages) {
            if (jsonDef == null || jsonDef.id == null || jsonDef.id.isBlank()) {
                throw new IllegalStateException("Stage with missing id in: " + sourceLabel);
            }
            boolean boss = jsonDef.boss;
            EnemyArchetypeId archetype = parseArchetype(jsonDef.enemyArchetype, sourceLabel, boss);
            if (boss && archetype != null) {
                throw new IllegalStateException("Boss stage must not set enemyArchetype: " + jsonDef.id);
            }
            if (!boss && archetype == null) {
                throw new IllegalStateException("Non-boss stage requires enemyArchetype: " + jsonDef.id);
            }
            stages.add(new StageDef(jsonDef.id, jsonDef.displayName, archetype, boss));
        }
        long bossCount = stages.stream().filter(StageDef::boss).count();
        if (bossCount != 1) {
            throw new IllegalStateException("Stage manifest must contain exactly one boss stage: " + sourceLabel);
        }
        StageDef last = stages.get(stages.size() - 1);
        if (!last.boss()) {
            throw new IllegalStateException("Final stage must be boss: " + sourceLabel);
        }
        return new StageCatalog(stages);
    }

    public static Path resolveManifestPath() {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path p1 = cwd.resolve("core/assets/stages/stages.json");
        if (Files.exists(p1)) {
            return p1;
        }
        Path p2 = cwd.resolve("assets/stages/stages.json");
        if (Files.exists(p2)) {
            return p2;
        }
        return p1;
    }

    private static EnemyArchetypeId parseArchetype(String raw, String sourceLabel, boolean boss) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        if (boss) {
            throw new IllegalStateException("Boss stage cannot specify enemyArchetype in: " + sourceLabel);
        }
        try {
            return EnemyArchetypeId.valueOf(raw.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unknown enemyArchetype: " + raw + " (" + sourceLabel + ")", e);
        }
    }

    static final class StageManifest {
        public int version;
        public List<StageJsonDef> stages;
    }

    static final class StageJsonDef {
        public String id;
        public String displayName;
        public String enemyArchetype;
        public boolean boss;
    }
}
