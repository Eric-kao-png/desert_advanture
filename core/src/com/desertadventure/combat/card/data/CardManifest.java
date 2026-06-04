package com.desertadventure.combat.card.data;

import java.util.ArrayList;
import java.util.List;

/** Root JSON file for a card manifest (`offense_cards.json` or `change_cards.json`). */
public final class CardManifest {
    public int version;
    public List<CardDef> cards = new ArrayList<>();
}

