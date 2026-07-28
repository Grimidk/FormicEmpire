package com.grimidk.formicempire.classes.interfaces.menu;

import com.grimidk.formicempire.classes.constants.world.Biome;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuChaoticCatalogTest {

    @Test
    void catalogHasTwelveChaoticScenarios() {
        List<MenuChaoticDefinition> scenarios = MenuChaoticCatalog.getScenarios();
        assertEquals(12, scenarios.size());

        Set<Integer> biomeIds = new HashSet<>();
        int overworld = 0;
        int colony = 0;
        int battle = 0;
        int convoy = 0;
        for (MenuChaoticDefinition scenario : scenarios) {
            assertNotNull(scenario.primaryBiome());
            biomeIds.add(scenario.primaryBiome().getId());
            switch (scenario.kind()) {
                case BATTLE -> {
                    battle++;
                    assertNotNull(scenario.secondaryBiome());
                    biomeIds.add(scenario.secondaryBiome().getId());
                }
                case OVERWORLD -> overworld++;
                case COLONY -> colony++;
                case CONVOY -> {
                    convoy++;
                    assertNotNull(scenario.convoyBackground());
                }
            }
            assertTrue(scenario.antCount() > 0);
            assertTrue(scenario.antCount() <= MenuChaoticCatalog.MAX_ANTS);
            assertFalse(scenario.antTemplate().isEmpty());
        }
        assertEquals(3, overworld);
        assertEquals(3, colony);
        assertEquals(3, battle);
        assertEquals(3, convoy);
        assertTrue(biomeIds.size() >= 12);
    }

    @Test
    void worldsBuildFromDefinitionsWithoutAllocatingEachTick() {
        for (MenuChaoticDefinition definition : MenuChaoticCatalog.getScenarios()) {
            MenuChaoticWorld world = MenuChaoticWorld.fromDefinition(definition);
            assertEquals(definition.antCount(), world.getAnts().size());
            assertEquals(definition.critterCount(), world.getCritters().size());
            world.update(0.02f, 1280, 720);
            Biome primary = world.getPrimaryBiome();
            assertNotNull(primary);
        }
    }
}
