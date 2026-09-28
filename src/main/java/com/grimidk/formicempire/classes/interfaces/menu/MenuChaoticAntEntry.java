package com.grimidk.formicempire.classes.interfaces.menu;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

public record MenuChaoticAntEntry(
        AntClass type,
        AntSpecies species,
        int profileCode,
        boolean attackerSide,
        BattleLine battleLine,
        boolean reserve) {

    public MenuChaoticAntEntry {
        battleLine = battleLine != null ? battleLine : GameConstants.BATTLE_LINE_INFANTRY;
    }

    public static MenuChaoticAntEntry overworld(AntClass type, AntSpecies species, int profileCode) {
        return new MenuChaoticAntEntry(type, species, profileCode, true, GameConstants.BATTLE_LINE_INFANTRY, false);
    }

    public static MenuChaoticAntEntry colony(AntClass type, AntSpecies species, int profileCode) {
        return new MenuChaoticAntEntry(type, species, profileCode, true, GameConstants.BATTLE_LINE_INFANTRY, false);
    }

    public static MenuChaoticAntEntry battle(AntClass type, AntSpecies species, int profileCode, boolean attackerSide,
            BattleLine battleLine, boolean reserve) {
        return new MenuChaoticAntEntry(type, species, profileCode, attackerSide, battleLine, reserve);
    }

    public static MenuChaoticAntEntry convoy(AntClass type, AntSpecies species, int profileCode) {
        return new MenuChaoticAntEntry(type, species, profileCode, true, GameConstants.BATTLE_LINE_INFANTRY, false);
    }
}
