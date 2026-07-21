package com.grimidk.formicempire.classes.entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class DynastyAutoUpgradePrerequisitesTest {

    private Dynasty dynasty;

    @BeforeEach
    void setUp() {
        dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
    }

    @Test
    void autoTunnelsRequiresOnlyCompletedTunnels() {
        assertFalse(dynasty.meetsAutoTunnelsPrerequisites());
        assertFalse(GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, GameUnlocks.ABILITY_AUTO_TUNNELS));

        for (int i = 0; i < GameNumbers.AUTO_UPGRADE_MIN_COMPLETE_TUNNELS; i++) {
            Tunnel tunnel = new Tunnel(new Hex(), new Hex(), 100);
            tunnel.restoreState(100, true);
            dynasty.addTunnel(tunnel);
        }

        assertTrue(dynasty.meetsAutoTunnelsPrerequisites());
        assertTrue(GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, GameUnlocks.ABILITY_AUTO_TUNNELS));
        assertFalse(dynasty.meetsAutoDiplomacyPrerequisites());
        assertFalse(GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, GameUnlocks.ABILITY_AUTO_DIPLOMACY));
    }

    @Test
    void autoDiplomacyRequiresOnlyDiplomatsSent() {
        assertFalse(dynasty.meetsAutoDiplomacyPrerequisites());
        assertFalse(GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, GameUnlocks.ABILITY_AUTO_DIPLOMACY));

        dynasty.recordDiplomatsSent(GameNumbers.AUTO_UPGRADE_MIN_DIPLOMATS_SENT);

        assertTrue(dynasty.meetsAutoDiplomacyPrerequisites());
        assertTrue(GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, GameUnlocks.ABILITY_AUTO_DIPLOMACY));
        assertFalse(dynasty.meetsAutoTunnelsPrerequisites());
        assertFalse(GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, GameUnlocks.ABILITY_AUTO_TUNNELS));
    }
}
