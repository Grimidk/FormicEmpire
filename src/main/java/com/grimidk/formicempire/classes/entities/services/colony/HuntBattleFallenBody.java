package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public final class HuntBattleFallenBody {
    private final AntClass antClass;
    private final AntModProfile modProfile;
    private final int orbitSlot;
    private final float bodyRotationDegrees;
    private final BattleLine battleLine;
    private final int lineSlot;

    HuntBattleFallenBody(AntClass antClass, AntModProfile modProfile, int orbitSlot,
            float bodyRotationDegrees, BattleLine battleLine, int lineSlot) {
        this.antClass = antClass;
        this.modProfile = modProfile != null ? modProfile : AntModProfile.standard();
        this.orbitSlot = orbitSlot;
        this.bodyRotationDegrees = bodyRotationDegrees;
        this.battleLine = battleLine != null ? battleLine : GameConstants.BATTLE_LINE_INFANTRY;
        this.lineSlot = lineSlot;
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

    public BattleLine getBattleLine() {
        return battleLine;
    }

    public int getLineSlot() {
        return lineSlot;
    }
}
