package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;

public final class HuntBattleFallenBody {
    private final AntClass antClass;
    private final AntModProfile modProfile;
    private final int orbitSlot;
    private final float bodyRotationDegrees;

    HuntBattleFallenBody(AntClass antClass, AntModProfile modProfile, int orbitSlot,
            float bodyRotationDegrees) {
        this.antClass = antClass;
        this.modProfile = modProfile != null ? modProfile : AntModProfile.standard();
        this.orbitSlot = orbitSlot;
        this.bodyRotationDegrees = bodyRotationDegrees;
    }

    public AntClass getAntClass() {
        return antClass;
    }

    public AntModProfile getModProfile() {
        return modProfile;
    }

    public int getOrbitSlot() {
        return orbitSlot;
    }

    public float getBodyRotationDegrees() {
        return bodyRotationDegrees;
    }
}
