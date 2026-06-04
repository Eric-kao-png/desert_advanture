package com.desertadventure.combat.system;

import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.model.PositiveStatusType;

/** Directional combat operations from the resolving card's point of view. */
interface CombatPerspective {
    EffectCaster effectCaster();

    void dealDamageToEnemies(float amount);

    void dealDamageToEnemiesIgnoringShield(float amount);

    void clearCasterNegativeStatus();

    void transferCasterNegativeToOpponent();

    boolean casterHasNegativeStatus();

    boolean opponentHasNegativeStatus();

    void healPlayer(float amount);

    void addPlayerShield(int amount);

    void halveEnemyHp();

    void applyNegativeStatusToEnemies(NegativeStatusType type, int turns);

    void applyPositiveStatusToCaster(PositiveStatusType type, int turns);
}
