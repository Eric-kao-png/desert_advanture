package com.desertadventure.presentation;

import com.desertadventure.combat.card.ActionCardCategory;
import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardMechanic;
import com.desertadventure.combat.card.ActionCardTarget;
import com.desertadventure.combat.card.ActionCardType;
import com.desertadventure.config.GameMessages;

import java.util.ArrayList;
import java.util.List;

/** Builds info-panel text lines for a combat card. */
public final class CombatCardInfoFormatter {
    private CombatCardInfoFormatter() {
    }

    public static List<String> lines(ActionCardType type, ActionCardInstance instance) {
        List<String> lines = new ArrayList<>();
        if (type == null) {
            return lines;
        }
        lines.add(type.getDisplayName());
        lines.add(GameMessages.cardCategoryTooltip(type.getCategory()));
        lines.add(formatTarget(type.getTarget()));
        lines.add(formatCooldown(type, instance));
        String effect = describeEffect(type);
        if (!effect.isEmpty()) {
            lines.add(effect);
        }
        return lines;
    }

    private static String formatTarget(ActionCardTarget target) {
        return switch (target) {
            case SELF -> "Target: Self";
            case ENEMY -> "Target: Enemy";
        };
    }

    private static String formatCooldown(ActionCardType type, ActionCardInstance instance) {
        if (instance != null) {
            int remaining = instance.getCooldownRemaining();
            if (remaining > 0) {
                return "Cooldown: " + remaining + " turn(s) left";
            }
            return "Ready to play";
        }
        return "Cooldown: " + type.getCooldownTurns() + " turn(s)";
    }

    private static String describeEffect(ActionCardType type) {
        int primary = type.getPrimaryValue();
        int secondary = type.getSecondaryValue();
        return switch (type.getMechanic()) {
            case DAMAGE -> "Deal " + primary + " damage.";
            case HEAL -> "Heal " + primary + " HP.";
            case SHIELD -> "Gain " + primary + " shield.";
            case FULL_POWER_ATTACK -> "Deal " + secondary + " damage, or " + primary
                    + " if no Utility card resolved this round.";
            case HALVE_ENEMY_HP -> "Reduce enemy HP by half (floor).";
            case THRUST -> "Deal " + secondary + " on round 1, else " + primary + " damage.";
            case POISON -> "Apply Poison for " + primary + " turns (" + secondary + " damage per round end).";
            case PURIFY -> "Clear your debuff; if enemy has one, copy it to them for " + secondary + " turns.";
            case IGNORE_SHIELD_DAMAGE -> "Deal " + primary + " damage (ignores shield).";
            case RANDOM_POISON_DAMAGE -> "Deal " + primary + " or apply Poison for " + secondary + " turns.";
            case CHANCE_POISON_DAMAGE -> "Deal " + primary + " damage; " + secondary + "% chance to apply Poison.";
            case TRANSFER_DEBUFF -> "Transfer your debuff to the enemy.";
            case BONUS_DAMAGE_VS_DEBUFFED -> "Deal " + primary + "–" + secondary
                    + " damage (higher if enemy has a debuff).";
        };
    }
}
