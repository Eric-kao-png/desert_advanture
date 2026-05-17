package com.desertadventure.combat.system;

public final class DamageCalculator {
    private DamageCalculator() {
    }

    public static float compute(int attack, int defense) {
        return Math.max(1f, attack - defense * 0.5f);
    }
}
