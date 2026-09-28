package com.grimidk.formicempire.classes.constants.critter.ant;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class AntRole extends Constant {
    private final AntClass antClass;
    private final Set<AntSubtype> requiredSubtypes;
    private final Set<AntSubtype> forcedAllowedSubtypes;
    private final boolean isActiveMilitary;
    private final boolean hexDefenseOnly;

    public AntRole(int id, AntClass antClass, String name, ImageIcon icon) {
        this(id, antClass, name, icon, Set.of(), Set.of(), false, false);
    }

    public AntRole(int id, AntClass antClass, String name, ImageIcon icon, boolean isActiveMilitary) {
        this(id, antClass, name, icon, Set.of(), Set.of(), isActiveMilitary, false);
    }

    public AntRole(int id, AntClass antClass, String name, ImageIcon icon,
            Set<AntSubtype> requiredSubtypes, Set<AntSubtype> forcedAllowedSubtypes) {
        this(id, antClass, name, icon, requiredSubtypes, forcedAllowedSubtypes, false, false);
    }

    public AntRole(int id, AntClass antClass, String name, ImageIcon icon,
            Set<AntSubtype> requiredSubtypes, Set<AntSubtype> forcedAllowedSubtypes,
            boolean isActiveMilitary) {
        this(id, antClass, name, icon, requiredSubtypes, forcedAllowedSubtypes, isActiveMilitary, false);
    }

    public AntRole(int id, AntClass antClass, String name, ImageIcon icon,
            Set<AntSubtype> requiredSubtypes, Set<AntSubtype> forcedAllowedSubtypes,
            boolean isActiveMilitary, boolean hexDefenseOnly) {
        super(id, name, icon);
        this.antClass = antClass;
        this.requiredSubtypes = copySubtypeSet(requiredSubtypes);
        LinkedHashSet<AntSubtype> forced = copySubtypeSet(forcedAllowedSubtypes);
        forced.addAll(this.requiredSubtypes);
        this.forcedAllowedSubtypes = Collections.unmodifiableSet(forced);
        this.isActiveMilitary = isActiveMilitary;
        this.hexDefenseOnly = isActiveMilitary && hexDefenseOnly;
    }

    private static LinkedHashSet<AntSubtype> copySubtypeSet(Set<AntSubtype> source) {
        LinkedHashSet<AntSubtype> copy = new LinkedHashSet<>();
        if (source != null) {
            for (AntSubtype subtype : source) {
                if (subtype != null && !subtype.isNone()) {
                    copy.add(subtype);
                }
            }
        }
        return copy;
    }

    public AntClass getAntClass() {
        return antClass;
    }

    public AntType getAntType() {
        return antClass instanceof AntType t ? t : null;
    }

    public Set<AntSubtype> getRequiredSubtypes() {
        return requiredSubtypes;
    }

    public Set<AntSubtype> getForcedAllowedSubtypes() {
        return forcedAllowedSubtypes;
    }

    public Set<AntMod> getRequiredMods() {
        LinkedHashSet<AntMod> set = new LinkedHashSet<>();
        for (AntSubtype st : requiredSubtypes) {
            AntMod mod = st.toMod();
            if (mod != null) {
                set.add(mod);
            }
        }
        return Collections.unmodifiableSet(set);
    }

    public Set<AntMod> getForcedAllowedMods() {
        LinkedHashSet<AntMod> set = new LinkedHashSet<>();
        for (AntSubtype st : forcedAllowedSubtypes) {
            AntMod mod = st.toMod();
            if (mod != null) {
                set.add(mod);
            }
        }
        return Collections.unmodifiableSet(set);
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

    public boolean requiresSubtypes() {
        return !requiredSubtypes.isEmpty();
    }

    public boolean requiresMods() {
        return !requiredSubtypes.isEmpty();
    }

    public boolean isSubtypeForcedAllowed(AntSubtype subtype) {
        return subtype != null && forcedAllowedSubtypes.contains(subtype);
    }

    public boolean isModForcedAllowed(AntMod mod) {
        return mod != null && forcedAllowedSubtypes.stream().anyMatch(s -> s.getId() == mod.getId());
    }

    public boolean isSubtypeRequired(AntSubtype subtype) {
        return subtype != null && requiredSubtypes.contains(subtype);
    }

    public boolean isModRequired(AntMod mod) {
        return mod != null && requiredSubtypes.stream().anyMatch(s -> s.getId() == mod.getId());
    }

    @Override
    public String toString() {
        return getName();
    }
}
