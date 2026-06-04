package com.desertadventure.combat.status.data;

import com.desertadventure.combat.model.NegativeStatusType;
import com.desertadventure.combat.model.PositiveStatusType;

import java.util.EnumMap;
import java.util.Map;

public final class InMemoryStatusEffectRepository implements StatusEffectRepository {
    private final Map<NegativeStatusType, StatusEffectDef> negativeByType;
    private final Map<PositiveStatusType, StatusEffectDef> positiveByType;

    public InMemoryStatusEffectRepository(
            Map<NegativeStatusType, StatusEffectDef> negativeByType,
            Map<PositiveStatusType, StatusEffectDef> positiveByType) {
        this.negativeByType = Map.copyOf(negativeByType);
        this.positiveByType = Map.copyOf(positiveByType);
    }

    @Override
    public StatusEffectDef getNegativeRequired(NegativeStatusType type) {
        StatusEffectDef def = negativeByType.get(type);
        if (def == null) {
            throw new IllegalStateException("Missing negative status def: " + type);
        }
        return def;
    }

    @Override
    public StatusEffectDef getPositiveRequired(PositiveStatusType type) {
        StatusEffectDef def = positiveByType.get(type);
        if (def == null) {
            throw new IllegalStateException("Missing positive status def: " + type);
        }
        return def;
    }

    @Override
    public boolean hasNegative(NegativeStatusType type) {
        return negativeByType.containsKey(type);
    }

    @Override
    public boolean hasPositive(PositiveStatusType type) {
        return positiveByType.containsKey(type);
    }

    static InMemoryStatusEffectRepository fromManifest(StatusEffectManifest manifest, String sourceLabel) {
        Map<NegativeStatusType, StatusEffectDef> negative = new EnumMap<>(NegativeStatusType.class);
        Map<PositiveStatusType, StatusEffectDef> positive = new EnumMap<>(PositiveStatusType.class);
        if (manifest.negative != null) {
            for (StatusEffectDef def : manifest.negative) {
                mergeNegative(negative, def, sourceLabel);
            }
        }
        if (manifest.positive != null) {
            for (StatusEffectDef def : manifest.positive) {
                mergePositive(positive, def, sourceLabel);
            }
        }
        for (NegativeStatusType required : NegativeStatusType.values()) {
            if (!negative.containsKey(required)) {
                throw new IllegalStateException(
                        "Manifest missing negative status for NegativeStatusType: "
                                + required + " (" + sourceLabel + ")");
            }
        }
        for (PositiveStatusType required : PositiveStatusType.values()) {
            if (!positive.containsKey(required)) {
                throw new IllegalStateException(
                        "Manifest missing positive status for PositiveStatusType: "
                                + required + " (" + sourceLabel + ")");
            }
        }
        return new InMemoryStatusEffectRepository(negative, positive);
    }

    private static void mergeNegative(
            Map<NegativeStatusType, StatusEffectDef> defs, StatusEffectDef def, String sourceLabel) {
        validateDef(def, sourceLabel);
        if (def.polarity != StatusPolarityId.NEGATIVE) {
            throw new IllegalStateException("Negative section entry must have polarity NEGATIVE: " + def.id);
        }
        NegativeStatusType type = parseNegativeId(def.id, sourceLabel);
        if (defs.put(type, def) != null) {
            throw new IllegalStateException("Duplicate negative status id: " + def.id);
        }
    }

    private static void mergePositive(
            Map<PositiveStatusType, StatusEffectDef> defs, StatusEffectDef def, String sourceLabel) {
        validateDef(def, sourceLabel);
        if (def.polarity != StatusPolarityId.POSITIVE) {
            throw new IllegalStateException("Positive section entry must have polarity POSITIVE: " + def.id);
        }
        PositiveStatusType type = parsePositiveId(def.id, sourceLabel);
        if (defs.put(type, def) != null) {
            throw new IllegalStateException("Duplicate positive status id: " + def.id);
        }
    }

    private static void validateDef(StatusEffectDef def, String sourceLabel) {
        if (def == null || def.id == null || def.id.isBlank()) {
            throw new IllegalStateException("Status with missing id in: " + sourceLabel);
        }
        if (def.name == null || def.name.isBlank()) {
            throw new IllegalStateException("Status " + def.id + " missing name in: " + sourceLabel);
        }
        if (def.polarity == null) {
            throw new IllegalStateException("Status " + def.id + " missing polarity in: " + sourceLabel);
        }
    }

    private static NegativeStatusType parseNegativeId(String id, String sourceLabel) {
        try {
            return NegativeStatusType.valueOf(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unknown negative status id in manifest: " + id + " (" + sourceLabel + ")");
        }
    }

    private static PositiveStatusType parsePositiveId(String id, String sourceLabel) {
        try {
            return PositiveStatusType.valueOf(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unknown positive status id in manifest: " + id + " (" + sourceLabel + ")");
        }
    }
}
