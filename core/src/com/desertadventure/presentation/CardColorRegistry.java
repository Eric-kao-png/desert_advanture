package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.desertadventure.combat.card.ActionCardCategory;
import com.desertadventure.combat.card.ActionCardType;

import java.util.EnumMap;
import java.util.Map;

/** Combat card face fill colors by {@link ActionCardType}. */
public final class CardColorRegistry {
    private static final Color CARD_ATTACK = new Color(0.75f, 0.28f, 0.22f, 1f);
    private static final Color CARD_SHIELD = new Color(0.35f, 0.55f, 0.85f, 1f);
    private static final Color CARD_FULL_POWER = new Color(0.85f, 0.35f, 0.20f, 1f);
    private static final Color CARD_LIFE_MAGIC = new Color(0.55f, 0.25f, 0.75f, 1f);
    private static final Color CARD_THRUST = new Color(0.70f, 0.45f, 0.22f, 1f);
    private static final Color CARD_POISON = new Color(0.35f, 0.72f, 0.32f, 1f);
    private static final Color CARD_STRONG = new Color(0.85f, 0.4f, 0.15f, 1f);
    private static final Color CARD_HEAL = new Color(0.25f, 0.65f, 0.35f, 1f);

    private static final Map<ActionCardType, Color> BY_TYPE = new EnumMap<>(ActionCardType.class);

    static {
        BY_TYPE.put(ActionCardType.ATTACK, CARD_ATTACK);
        BY_TYPE.put(ActionCardType.STRIKE, CARD_STRONG);
        BY_TYPE.put(ActionCardType.HEAL, CARD_HEAL);
        BY_TYPE.put(ActionCardType.SHIELD, CARD_SHIELD);
        BY_TYPE.put(ActionCardType.ASSAULT, CARD_FULL_POWER);
        BY_TYPE.put(ActionCardType.LIFE_MAGIC, CARD_LIFE_MAGIC);
        BY_TYPE.put(ActionCardType.AMBUSH, CARD_THRUST);
        BY_TYPE.put(ActionCardType.POISON_MAGIC, CARD_POISON);
        BY_TYPE.put(ActionCardType.HEAVY_STRIKE, CARD_STRONG);
        BY_TYPE.put(ActionCardType.SWIFT_STRIKE, CARD_STRONG);
        BY_TYPE.put(ActionCardType.SPELLBLADE, CARD_STRONG);
        BY_TYPE.put(ActionCardType.CHASE_ATTACK, CARD_STRONG);
        BY_TYPE.put(ActionCardType.DOUBLE_BLADE, CARD_STRONG);
        BY_TYPE.put(ActionCardType.CLAW, CARD_STRONG);
        BY_TYPE.put(ActionCardType.CHARGED_SLASH, CARD_STRONG);
        BY_TYPE.put(ActionCardType.BLADE, CARD_STRONG);
        BY_TYPE.put(ActionCardType.GREAT_BLADE, CARD_STRONG);
        BY_TYPE.put(ActionCardType.VAMPIRISM, CARD_STRONG);
        BY_TYPE.put(ActionCardType.MAGIC_BOLT, CARD_STRONG);
        BY_TYPE.put(ActionCardType.POISON_BOLT, CARD_STRONG);
        BY_TYPE.put(ActionCardType.MAGIC_ARROW, CARD_STRONG);
        BY_TYPE.put(ActionCardType.ARROW, CARD_STRONG);
        BY_TYPE.put(ActionCardType.POISON_ARROW, CARD_STRONG);
        BY_TYPE.put(ActionCardType.PURIFY, CARD_HEAL);
        BY_TYPE.put(ActionCardType.MAGIC_MIRROR, CARD_HEAL);
    }

    private CardColorRegistry() {
    }

    public static Color colorFor(ActionCardType type) {
        Color mapped = BY_TYPE.get(type);
        return mapped != null ? mapped : defaultForCategory(type.getCategory());
    }

    public static Color defaultForCategory(ActionCardCategory category) {
        return switch (category) {
            case ATTACK -> CARD_STRONG;
            case CHANGE -> CARD_HEAL;
        };
    }
}
