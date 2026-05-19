package com.desertadventure.screen.input;

import com.badlogic.gdx.Gdx;
import com.desertadventure.config.GameInputBindings;
import com.desertadventure.state.GameSession;
import com.desertadventure.state.GameplayMode;

/** Combat movement and ability hotkeys. */
public final class CombatKeyboardInput {
    public void handle(GameSession session, float delta) {
        if (!session.getMode().isCombat()) {
            return;
        }
        float move = 0f;
        if (GameInputBindings.moveLeft()) {
            move -= 1f;
        }
        if (GameInputBindings.moveRight()) {
            move += 1f;
        }
        session.getCombatController().update(delta, move);
        if (Gdx.input.isKeyJustPressed(GameInputBindings.ATTACK)) {
            session.getCombatController().tryBasicAttack();
        }
        if (Gdx.input.isKeyJustPressed(GameInputBindings.SKILL)) {
            session.getCombatController().trySkill();
        }
        if (Gdx.input.isKeyJustPressed(GameInputBindings.ULTIMATE)) {
            session.getCombatController().tryUltimate();
        }
    }
}
