package com.desertadventure.presentation;

import com.desertadventure.combat.card.ActionCardInstance;
import com.desertadventure.combat.card.ActionCardType;

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
        appendDescriptionLines(lines, type.getDescription());
        lines.add(formatCooldown(type, instance));
        return lines;
    }

    private static void appendDescriptionLines(List<String> lines, String description) {
        if (description == null || description.isBlank()) {
            return;
        }
        for (String paragraph : description.split("\\n")) {
            String trimmed = paragraph.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
    }

    private static String formatCooldown(ActionCardType type, ActionCardInstance instance) {
        if (instance != null) {
            int remaining = instance.getCooldownRemaining();
            if (remaining > 0) {
                return "冷卻：剩餘 " + remaining + " 回合";
            }
            return "可出牌";
        }
        return "冷卻：" + type.getCooldownTurns() + " 回合";
    }
}
