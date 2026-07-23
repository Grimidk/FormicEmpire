package com.grimidk.formicempire.classes.constants.critter.ant;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;

public class AntRole extends Constant {
    private final AntType antType;
    private final Set<AntSubtype> requiredSubtypes;
    private final Set<AntSubtype> forcedAllowedSubtypes;
    private final boolean isActiveMilitary;

    public AntRole(int id, AntType antType, String name, ImageIcon icon) {
        this(id, antType, name, icon, Set.of(), Set.of(), false);
    }

    public AntRole(int id, AntType antType, String name, ImageIcon icon, boolean isActiveMilitary) {
        this(id, antType, name, icon, Set.of(), Set.of(), isActiveMilitary);
    }

    public AntRole(int id, AntType antType, String name, ImageIcon icon,
            Set<AntSubtype> requiredSubtypes, Set<AntSubtype> forcedAllowedSubtypes) {
        this(id, antType, name, icon, requiredSubtypes, forcedAllowedSubtypes, false);
    }

    public AntRole(int id, AntType antType, String name, ImageIcon icon,
            Set<AntSubtype> requiredSubtypes, Set<AntSubtype> forcedAllowedSubtypes,
            boolean isActiveMilitary) {
        super(id, name, icon);
        this.antType = antType;
        this.requiredSubtypes = copySubtypeSet(requiredSubtypes);
        LinkedHashSet<AntSubtype> forced = copySubtypeSet(forcedAllowedSubtypes);
        forced.addAll(this.requiredSubtypes);
        this.forcedAllowedSubtypes = Collections.unmodifiableSet(forced);
        this.isActiveMilitary = isActiveMilitary;
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

    public AntType getAntType() {
        return antType;
    }

    /**
     * Subtypes an ant must possess to fill this role. Empty means no subtype requirement
     * (standard / "nothing" ants remain eligible when no special subtypes are allowed).
     */
    public Set<AntSubtype> getRequiredSubtypes() {
        return requiredSubtypes;
    }

    /**
     * Special subtypes that are always allowed for this role (checkboxes stay on).
     * Required subtypes are always included here.
     */
    public Set<AntSubtype> getForcedAllowedSubtypes() {
        return forcedAllowedSubtypes;
    }

    /**
     * War-economy combat roles (Warrior, Militia, Brute, …). Quotas live only on the war
     * distribution and the role UI shows them only when war economy mode is enabled.
     */
    public boolean isActiveMilitary() {
        return isActiveMilitary;
    }

    public boolean requiresSubtypes() {
        return !requiredSubtypes.isEmpty();
    }

    public boolean isSubtypeForcedAllowed(AntSubtype subtype) {
        return subtype != null && forcedAllowedSubtypes.contains(subtype);
    }

    public boolean isSubtypeRequired(AntSubtype subtype) {
        return subtype != null && requiredSubtypes.contains(subtype);
    }

    @Override
    public String toString() {
        return getName();
    }
}
