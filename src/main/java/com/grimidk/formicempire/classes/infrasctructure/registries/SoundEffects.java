package com.grimidk.formicempire.classes.infrasctructure.registries;

import com.grimidk.formicempire.classes.infrasctructure.audio.SoundEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SoundEffects {
    private static final List<SoundEffect> EFFECTS = new ArrayList<>();

    public static final SoundEffect POPUP = effect("popup", "Popup");
    public static final SoundEffect SPIN_UP = effect("spinUp", "Spin Up");
    public static final SoundEffect SPIN_DOWN = effect("spinDown", "Spin Down");
    public static final SoundEffect BATTLE = effect("battle", "Battle");

    private SoundEffects() {}

    public static List<SoundEffect> getEffects() {
        return Collections.unmodifiableList(EFFECTS);
    }

    public static List<SoundEffect> getPlayableEffects() {
        List<SoundEffect> out = new ArrayList<>();
        for (SoundEffect effect : EFFECTS) {
            if (effect.isResourcePresent()) {
                out.add(effect);
            }
        }
        return Collections.unmodifiableList(out);
    }

    public static SoundEffect getById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (SoundEffect effect : EFFECTS) {
            if (effect.getId().equals(id)) {
                return effect;
            }
        }
        return null;
    }

    private static SoundEffect effect(String id, String displayName) {
        return register(new SoundEffect(id, displayName));
    }

    private static SoundEffect register(SoundEffect effect) {
        for (SoundEffect existing : EFFECTS) {
            if (existing.getId().equals(effect.getId())) {
                throw new IllegalStateException("Duplicate sound effect: " + effect.getId());
            }
        }
        EFFECTS.add(effect);
        return effect;
    }
}
