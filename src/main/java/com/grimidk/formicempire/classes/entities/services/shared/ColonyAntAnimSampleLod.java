package com.grimidk.formicempire.classes.entities.services.shared;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

public final class ColonyAntAnimSampleLod {

    private ColonyAntAnimSampleLod() {
    }

    public static boolean isSampleActive(int colonyAntTotal) {
        return colonyAntTotal > GameNumbers.SPRITE_MERGE_ANT_THRESHOLD;
    }

    public static int spatialCell(int coord) {
        return Math.floorDiv(coord, GameNumbers.SPRITE_MERGE_POSITION_CELL_PX);
    }

    public static long hourlyAnimSampleKey(Ant ant, AntType type) {
        int roleId = ant.getRole() != null ? ant.getRole().getId() : 0;
        int subtypeCode = ant.getSubtypeProfile().getCode();
        int cellX = spatialCell(ant.getX());
        int cellY = spatialCell(ant.getY());
        return ((long) type.getId() << 48)
                | ((long) (roleId & 0xFFFF) << 32)
                | ((long) (subtypeCode & 0xFFFF) << 16)
                | ((long) (cellX & 0xFF) << 8)
                | (long) (cellY & 0xFF);
    }
}
