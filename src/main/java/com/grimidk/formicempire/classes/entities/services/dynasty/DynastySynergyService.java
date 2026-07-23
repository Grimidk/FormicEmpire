package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DynastySynergyService {

    private DynastySynergyService() {
    }

    public static boolean isSynergyAbilityUnlocked(Dynasty dynasty) {
        return dynasty != null && dynasty.hasUpgrade(GameUnlocks.ABILITY_SYNERGY);
    }

    public static boolean isActive(Dynasty dynasty, Synergy synergy) {
        return meetsRequirements(dynasty, synergy);
    }

    public static boolean isUnlocked(Dynasty dynasty, Synergy synergy) {
        return isActive(dynasty, synergy);
    }

    public static boolean hasAchieved(Dynasty dynasty, Synergy synergy) {
        return dynasty != null && synergy != null && dynasty.hasUpgrade(synergy.getReward());
    }

    public static boolean hasAnyActive(Dynasty dynasty) {
        if (dynasty == null) {
            return false;
        }
        for (Synergy synergy : GameUnlocks.getSynergies()) {
            if (isActive(dynasty, synergy)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnyUnlocked(Dynasty dynasty) {
        return hasAnyActive(dynasty);
    }

    public static int countRequirementsMet(Dynasty dynasty, Synergy synergy) {
        if (dynasty == null || synergy == null) {
            return 0;
        }
        int met = 0;
        for (Upgrade requirement : synergy.getRequirements()) {
            if (requirement != null && dynasty.hasUpgrade(requirement)) {
                met++;
            }
        }
        return met;
    }

    public static boolean isPartiallyComplete(Dynasty dynasty, Synergy synergy) {
        if (!isSynergyAbilityUnlocked(dynasty) || isActive(dynasty, synergy)) {
            return false;
        }
        int met = countRequirementsMet(dynasty, synergy);
        return met > 0 && met < synergy.getRequirementCount();
    }

    public static boolean isVisibleInPanel(Dynasty dynasty, Synergy synergy) {
        if (!isSynergyAbilityUnlocked(dynasty) || synergy == null) {
            return false;
        }
        return isActive(dynasty, synergy)
                || isPartiallyComplete(dynasty, synergy)
                || hasAchieved(dynasty, synergy);
    }

    public static List<Synergy> getVisibleSynergies(Dynasty dynasty) {
        if (!isSynergyAbilityUnlocked(dynasty)) {
            return Collections.emptyList();
        }
        List<Synergy> visible = new ArrayList<>();
        for (Synergy synergy : GameUnlocks.getSynergies()) {
            if (isVisibleInPanel(dynasty, synergy)) {
                visible.add(synergy);
            }
        }
        return visible;
    }

    public static boolean isUpgradeSuperseded(Dynasty dynasty, Upgrade upgrade) {
        if (dynasty == null || upgrade == null) {
            return false;
        }
        for (Synergy synergy : GameUnlocks.getSynergies()) {
            if (!isActive(dynasty, synergy)) {
                continue;
            }
            for (Upgrade requirement : synergy.getRequirements()) {
                if (upgrade == requirement) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void refreshUnlocked(Dynasty dynasty) {
        if (dynasty == null) {
            return;
        }
        for (Synergy synergy : GameUnlocks.getSynergies()) {
            Upgrade reward = synergy.getReward();
            if (reward == null || synergy.getRequirementCount() < 2) {
                continue;
            }
            if (meetsRequirements(dynasty, synergy) && !dynasty.hasUpgrade(reward)) {
                dynasty.applySynergyReward(reward, synergy);
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

    private static boolean meetsRequirements(Dynasty dynasty, Synergy synergy) {
        if (dynasty == null || synergy == null || !isSynergyAbilityUnlocked(dynasty)) {
            return false;
        }
        if (synergy.getRequirementCount() < 2) {
            return false;
        }
        for (Upgrade requirement : synergy.getRequirements()) {
            if (requirement == null || !dynasty.hasUpgrade(requirement)) {
                return false;
            }
        }
        return true;
    }
}
