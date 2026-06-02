package com.desertadventure.combat.card.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class InMemoryCardRepository implements CardRepository {
    private final Map<String, CardDef> byId;

    public InMemoryCardRepository(Map<String, CardDef> byId) {
        this.byId = new HashMap<>(byId);
    }

    @Override
    public CardDef getRequired(String id) {
        CardDef def = byId.get(id);
        if (def == null) {
            throw new IllegalStateException("Missing card def: " + id);
        }
        return def;
    }

    @Override
    public Map<String, CardDef> getAll() {
        return Collections.unmodifiableMap(byId);
    }
}

