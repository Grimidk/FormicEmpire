package com.grimidk.formicempire.classes.infrasctructure.registries;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UnlockTierLineTest {

    private static final Pattern SERIES = Pattern.compile("^(.*_)(\\d+)$");

    @Test
    void numberedUpgradeSeriesDoNotShareTiers() {
        for (Upgrade upgrade : GameUnlocks.getUpgrades()) {
            Upgrade req = upgrade.getRequirement();
            if (req == null) {
                continue;
            }
            Matcher child = SERIES.matcher(upgrade.getNameKey());
            Matcher parent = SERIES.matcher(req.getNameKey());
            if (child.matches() && parent.matches() && child.group(1).equals(parent.group(1))) {
                assertTrue(upgrade.getTier().getId() > req.getTier().getId(),
                        upgrade.getNameKey() + " must outrank " + req.getNameKey());
            }
            assertTrue(upgrade.getTier().getId() >= req.getTier().getId() - 1,
                    upgrade.getNameKey() + " cannot be more than one tier below " + req.getNameKey());
        }
    }

    @Test
    void buildingRequirementChainsDoNotShareTiers() {
        for (Building building : GameUnlocks.getBuildings()) {
            Building req = building.getRequirement();
            if (req == null) {
                continue;
            }
            assertTrue(building.getTier().getId() > req.getTier().getId(),
                    building.getNameKey() + " must outrank " + req.getNameKey());
        }
    }

    @Test
    void tiersExposeMinimumCosts() {
        assertTrue(GameConstants.TIER_0.getMinimumRp() == 10L);
        assertTrue(GameConstants.TIER_1.getMinimumRp() == 100L);
        assertTrue(GameConstants.TIER_0.getMinimumConstructionCost() == 0);
        assertTrue(GameConstants.TIER_2.getMinimumConstructionCost() == 200);
        assertTrue(GameConstants.TIER_3.getMinimumConstructionCost() == 500);
        assertTrue(GameConstants.getTiers().size() == 12);
    }
}
