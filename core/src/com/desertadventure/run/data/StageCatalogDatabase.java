package com.desertadventure.run.data;

import com.desertadventure.run.StageCatalog;

/** Global stage catalog (initialized at session bootstrap). */
public final class StageCatalogDatabase {
    private static volatile StageCatalog catalog;

    private StageCatalogDatabase() {
    }

    public static void initialize(StageCatalog stageCatalog) {
        if (stageCatalog == null) {
            throw new IllegalArgumentException("stageCatalog must not be null");
        }
        catalog = stageCatalog;
    }

    public static boolean isInitialized() {
        return catalog != null;
    }

    public static StageCatalog getRequired() {
        StageCatalog c = catalog;
        if (c == null) {
            throw new IllegalStateException("StageCatalogDatabase not initialized");
        }
        return c;
    }
}
