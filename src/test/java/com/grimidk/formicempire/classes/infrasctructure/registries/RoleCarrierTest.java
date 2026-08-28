package com.grimidk.formicempire.classes.infrasctructure.registries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleCarrierTest {

    @Test
    void carrierIsObtainableHexDefenseOnlyMajorRole() {
        assertTrue(GameConstants.isObtainableRole(GameConstants.ROLE_CARRIER));
        assertTrue(GameConstants.ROLE_CARRIER.isActiveMilitary());
        assertTrue(GameConstants.ROLE_CARRIER.isHexDefenseOnly());
        assertFalse(GameConstants.ROLE_CARRIER.participatesInBorderBattle());
        assertTrue(GameUnlocks.getUpgrades().contains(GameUnlocks.ROLE_CARRIER));
    }
}
