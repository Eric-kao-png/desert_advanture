package com.desertadventure.combat.card.data;

import java.util.ArrayList;
import java.util.List;

/** Root JSON file for all action cards. */
public final class CardManifest {
    public int version;
    public List<CardDef> cards = new ArrayList<>();
}

