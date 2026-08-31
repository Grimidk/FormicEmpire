package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;

public final class HuntBattleFallenBody {
    private final AntType antType;
    private final AntSubtypeProfile subtypeProfile;
    private final int orbitSlot;
    private final float bodyRotationDegrees;

    HuntBattleFallenBody(AntType antType, AntSubtypeProfile subtypeProfile, int orbitSlot,
            float bodyRotationDegrees) {
        this.antType = antType;
        this.subtypeProfile = subtypeProfile != null ? subtypeProfile : AntSubtypeProfile.standard();
        this.orbitSlot = orbitSlot;
        this.bodyRotationDegrees = bodyRotationDegrees;
    }

    public AntType getAntType() {
        return antType;
    }

    public AntSubtypeProfile getSubtypeProfile() {
        return subtypeProfile;
    }

    public int getOrbitSlot() {
        return orbitSlot;
    }

    public float getBodyRotationDegrees() {
        return bodyRotationDegrees;
    }
}
