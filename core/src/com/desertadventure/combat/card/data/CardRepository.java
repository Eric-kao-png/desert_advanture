package com.desertadventure.combat.card.data;

import java.util.Map;

public interface CardRepository {
    CardDef getRequired(String id);

    Map<String, CardDef> getAll();
}

