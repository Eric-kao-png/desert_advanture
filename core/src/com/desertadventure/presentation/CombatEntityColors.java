package com.desertadventure.presentation;

import com.badlogic.gdx.graphics.Color;
import com.desertadventure.combat.model.CombatEntity;

/** Combat placeholder shape colors. */
public final class CombatEntityColors {
    private static final Color PLAYER = new Color(0.2f, 0.5f, 0.95f, 1f);
    private static final Color ENEMY = new Color(0.9f, 0.25f, 0.2f, 1f);
    private static final Color BOSS = new Color(0.55f, 0.2f, 0.85f, 1f);

    private CombatEntityColors() {
    }

    public static Color forEntity(CombatEntity entity, boolean hurt) {
        if (hurt) {
            return Color.WHITE;
        }
        return switch (entity.getKind()) {
            case PLAYER -> PLAYER;
            case ENEMY -> ENEMY;
            case BOSS -> BOSS;
        };
    }
}
