package com.desertadventure.combat.system;

final class CombatPerspectives {
    private CombatPerspectives() {
    }

    static CombatPerspective forCaster(CombatController combat, EffectCaster caster) {
        if (caster == EffectCaster.ENEMY) {
            return new EnemyCombatPerspective(combat);
        }
        return new PlayerCombatPerspective(combat);
    }
}
