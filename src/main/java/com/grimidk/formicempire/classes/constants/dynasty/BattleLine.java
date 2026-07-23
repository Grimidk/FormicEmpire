package com.grimidk.formicempire.classes.constants.dynasty;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;

/**
 * War battle-line band (infantry / artillery / air). Scaffolding for future combat resolution.
 */
public class BattleLine extends Constant {
    private final float baseAccuracyPercent;
    private final Set<AntRole> allowedRoles;

    public BattleLine(int id, String nameKey, float baseAccuracyPercent, Set<AntRole> allowedRoles, ImageIcon icon) {
        super(id, nameKey, icon);
        this.baseAccuracyPercent = baseAccuracyPercent;
        this.allowedRoles = allowedRoles == null || allowedRoles.isEmpty()
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(allowedRoles));
    }

    /** Hit chance baseline as a percent (100 = always accurate at baseline). */
    public float getBaseAccuracyPercent() {
        return baseAccuracyPercent;
    }

    public Set<AntRole> getAllowedRoles() {
        return allowedRoles;
    }

    public boolean allowsRole(AntRole role) {
        return role != null && allowedRoles.contains(role);
    }
}
