package com.grimidk.formicempire.classes.interfaces.menu;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public record MenuChaoticAntEntry(
        AntType type,
        AntSpecies species,
        int profileCode,
        boolean attackerSide,
        BattleLine battleLine,
        boolean reserve) {

    public MenuChaoticAntEntry {
        battleLine = battleLine != null ? battleLine : GameConstants.BATTLE_LINE_INFANTRY;
    }

    public static MenuChaoticAntEntry overworld(AntType type, AntSpecies species, int profileCode) {
        return new MenuChaoticAntEntry(type, species, profileCode, true, GameConstants.BATTLE_LINE_INFANTRY, false);
    }

    public static MenuChaoticAntEntry colony(AntType type, AntSpecies species, int profileCode) {
        return new MenuChaoticAntEntry(type, species, profileCode, true, GameConstants.BATTLE_LINE_INFANTRY, false);
    }

    public static MenuChaoticAntEntry battle(AntType type, AntSpecies species, int profileCode, boolean attackerSide,
            BattleLine battleLine, boolean reserve) {
        return new MenuChaoticAntEntry(type, species, profileCode, attackerSide, battleLine, reserve);
    }

    public static MenuChaoticAntEntry convoy(AntType type, AntSpecies species, int profileCode) {
        return new MenuChaoticAntEntry(type, species, profileCode, true, GameConstants.BATTLE_LINE_INFANTRY, false);
    }
}
