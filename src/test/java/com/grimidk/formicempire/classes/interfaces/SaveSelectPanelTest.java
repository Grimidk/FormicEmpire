package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveSelectPanelTest {

    @Test
    void resolveStarterBiomeIdHandlesRandomAndSpecificSelection() {
        Set<Integer> validIds = GameConstants.getStartingBiomes().stream()
                .map(Biome::getId)
                .collect(Collectors.toSet());
        assertFalse(validIds.isEmpty());

        for (int i = 0; i < 20; i++) {
            int randomId = SaveSelectPanel.resolveStarterBiomeId(null);
            assertTrue(validIds.contains(randomId));
        }

        assertEquals(GameConstants.BIOME_DESERT.getId(),
                SaveSelectPanel.resolveStarterBiomeId(GameConstants.BIOME_DESERT));
    }

    @Test
    void pickRandomDynastyBaseNameReturnsValidNonBlankName() {
        for (int i = 0; i < 10; i++) {
            String picked = SaveSelectPanel.pickRandomDynastyBaseName("Solari");
            assertNotNull(picked);
            assertFalse(picked.isBlank());
        }
    }
}
