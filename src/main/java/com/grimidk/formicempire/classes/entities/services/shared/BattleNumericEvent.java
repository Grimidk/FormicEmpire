package com.grimidk.formicempire.classes.entities.services.shared;

import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;

import javax.swing.ImageIcon;

public record BattleNumericEvent(
        long sequence,
        int tickIndex,
        boolean attackerSide,
        int d6Roll,
        int d6Target,
        boolean hit,
        boolean dodged,
        float damage,
        boolean kill,
        String skillName,
        ImageIcon skillIcon,
        BattleLine battleLine) {
}
