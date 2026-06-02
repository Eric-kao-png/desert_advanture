package com.desertadventure.combat.card.data;

/** Global card repository access for the current run (initialized at session start). */
public final class CardDatabase {
    private static volatile CardRepository repository;

    private CardDatabase() {
    }

    public static void initialize(CardRepository repo) {
        repository = repo;
    }

    public static boolean isInitialized() {
        return repository != null;
    }

    public static CardRepository getRequired() {
        CardRepository repo = repository;
        if (repo == null) {
            throw new IllegalStateException("CardDatabase not initialized");
        }
        return repo;
    }
}

