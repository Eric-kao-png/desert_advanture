package com.desertadventure.combat.status.data;

import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.model.PositiveStatusType;

/** Global status effect repository (initialized at session / test bootstrap). */
public final class StatusEffectDatabase {
    private static volatile StatusEffectRepository repository;

    private StatusEffectDatabase() {
    }

    public static void initialize(StatusEffectRepository repo) {
        if (repo == null) {
            throw new IllegalArgumentException("repo must not be null");
        }
        repository = repo;
    }

    public static boolean isInitialized() {
        return repository != null;
    }

    public static StatusEffectRepository getRequired() {
        StatusEffectRepository repo = repository;
        if (repo == null) {
            throw new IllegalStateException("StatusEffectDatabase not initialized");
        }
        return repo;
    }

    public static String displayName(NegativeStatusType type) {
        if (type == null) {
            return "";
        }
        if (isInitialized()) {
            return getRequired().getNegativeRequired(type).name;
        }
        return type.name();
    }

    public static String displayName(PositiveStatusType type) {
        if (type == null) {
            return "";
        }
        if (isInitialized() && repository.hasPositive(type)) {
            return getRequired().getPositiveRequired(type).name;
        }
        return type.name();
    }
}
