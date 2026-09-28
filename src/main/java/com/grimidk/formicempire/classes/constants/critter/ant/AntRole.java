package com.grimidk.formicempire.classes.constants.critter.ant;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class AntRole extends Constant {
    private final AntClass antClass;
    private final Set<AntMod> requiredMods;
    private final Set<AntMod> forcedAllowedMods;
    private final boolean isActiveMilitary;
    private final boolean hexDefenseOnly;

    public AntRole(int id, AntClass antClass, String name, ImageIcon icon) {
        this(id, antClass, name, icon, Set.of(), Set.of(), false, false);
    }

    public AntRole(int id, AntClass antClass, String name, ImageIcon icon, boolean isActiveMilitary) {
        this(id, antClass, name, icon, Set.of(), Set.of(), isActiveMilitary, false);
    }

    public AntRole(int id, AntClass antClass, String name, ImageIcon icon,
            Set<AntMod> requiredMods, Set<AntMod> forcedAllowedMods) {
        this(id, antClass, name, icon, requiredMods, forcedAllowedMods, false, false);
    }

    public AntRole(int id, AntClass antClass, String name, ImageIcon icon,
            Set<AntMod> requiredMods, Set<AntMod> forcedAllowedMods,
            boolean isActiveMilitary) {
        this(id, antClass, name, icon, requiredMods, forcedAllowedMods, isActiveMilitary, false);
    }

    public AntRole(int id, AntClass antClass, String name, ImageIcon icon,
            Set<AntMod> requiredMods, Set<AntMod> forcedAllowedMods,
            boolean isActiveMilitary, boolean hexDefenseOnly) {
        super(id, name, icon);
        this.antClass = antClass;
        this.requiredMods = copyModSet(requiredMods);
        LinkedHashSet<AntMod> forced = copyModSet(forcedAllowedMods);
        forced.addAll(this.requiredMods);
        this.forcedAllowedMods = Collections.unmodifiableSet(forced);
        this.isActiveMilitary = isActiveMilitary;
        this.hexDefenseOnly = isActiveMilitary && hexDefenseOnly;
    }

    private static LinkedHashSet<AntMod> copyModSet(Set<AntMod> source) {
        LinkedHashSet<AntMod> copy = new LinkedHashSet<>();
        if (source != null) {
            for (AntMod mod : source) {
                if (mod != null && !mod.isNone()) {
                    copy.add(mod);
                }
            }
        }
        return copy;
    }

    public AntClass getAntClass() {
        return antClass;
    }

    public Set<AntMod> getRequiredMods() {
        return requiredMods;
    }

    public Set<AntMod> getForcedAllowedMods() {
        return forcedAllowedMods;
    }

    public boolean isActiveMilitary() {
        return isActiveMilitary;
    }

    public boolean isHexDefenseOnly() {
        return hexDefenseOnly;
    }

    public boolean participatesInBorderBattle() {
        return isActiveMilitary && !hexDefenseOnly;
    }

    public boolean requiresMods() {
        return !requiredMods.isEmpty();
    }

    public boolean isModForcedAllowed(AntMod mod) {
        return mod != null && forcedAllowedMods.contains(mod);
    }

    public boolean isModRequired(AntMod mod) {
        return mod != null && requiredMods.contains(mod);
    }

    @Override
    public String toString() {
        return getName();
    }
}
