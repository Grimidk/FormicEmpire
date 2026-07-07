package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

public final class DynastySynergyService {

    private DynastySynergyService() {
    }

    public static boolean isUnlocked(Dynasty dynasty, Synergy synergy) {
        return dynasty != null && synergy != null && dynasty.hasUpgrade(synergy.getReward());
    }

    public static boolean hasAnyUnlocked(Dynasty dynasty) {
        if (dynasty == null) {
            return false;
        }
        for (Synergy synergy : GameUnlocks.getSynergies()) {
            if (isUnlocked(dynasty, synergy)) {
                return true;
            }
        }
        return false;
    }

    /** Grants or revokes synergy reward upgrades when requirements change. */
    public static void refreshUnlocked(Dynasty dynasty) {
        if (dynasty == null) {
            return;
        }
        boolean synergyEnabled = dynasty.hasUpgrade(GameUnlocks.ABILITY_SYNERGY);
        for (Synergy synergy : GameUnlocks.getSynergies()) {
            Upgrade first = synergy.getRequirement1();
            Upgrade second = synergy.getRequirement2();
            Upgrade reward = synergy.getReward();
            if (first == null || second == null || reward == null) {
                continue;
            }
            boolean shouldHave = synergyEnabled
                    && dynasty.hasUpgrade(first)
                    && dynasty.hasUpgrade(second);
            if (shouldHave) {
                dynasty.unlockUpgrade(reward);
            } else {
                dynasty.revokeUpgrade(reward);
            }
        }
    }

    public static void refreshAll(Iterable<Dynasty> dynasties) {
        if (dynasties == null) {
            return;
        }
        for (Dynasty dynasty : dynasties) {
            refreshUnlocked(dynasty);
        }
    }
}
