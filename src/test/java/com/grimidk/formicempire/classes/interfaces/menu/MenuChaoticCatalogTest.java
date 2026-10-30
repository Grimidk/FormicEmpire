package com.grimidk.formicempire.classes.interfaces.menu;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.services.colony.ConvoyScene;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.game.rendering.RouteViewVisuals;
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
            int expectedAnts = Math.min(definition.antCount(), MenuChaoticCatalog.MAX_ANTS);
            assertEquals(expectedAnts, world.getAnts().size());
            assertEquals(definition.critterCount(), world.getCritters().size());
            world.update(0.02f, 1280, 720);
            Biome primary = world.getPrimaryBiome();
            assertNotNull(primary);
        }
    }

    @Test
    void colonyGatherersPickUpAtTheEdgeAndDropAtTheHill() {
        MenuChaoticDefinition colony = MenuChaoticCatalog.getScenarios().stream()
                .filter(scenario -> scenario.kind() == MenuChaoticKind.COLONY)
                .findFirst()
                .orElseThrow();
        MenuChaoticWorld world = MenuChaoticWorld.fromDefinition(colony);
        MenuChaoticWorld.ShowcaseAnt gatherer = world.getAnts().stream()
                .filter(ant -> RouteViewVisuals.canHoldJawCargo(ant.type))
                .findFirst()
                .orElseThrow();
        assertEquals(null, gatherer.carrying);

        gatherer.xNorm = 0.02f;
        gatherer.yNorm = 0.5f;
        gatherer.vx = -0.2f;
        gatherer.vy = 0f;
        world.update(0.05f, 1280, 720);
        assertNotNull(gatherer.carrying);
        assertTrue(gatherer.vx > 0f);

        gatherer.xNorm = MenuChaoticWorld.colonyEntranceXNorm();
        gatherer.yNorm = MenuChaoticWorld.colonyEntranceYNorm();
        world.update(0.05f, 1280, 720);
        assertEquals(null, gatherer.carrying);
    }

    @Test
    void landConvoyWorkersCarryCargoOnTheOutboundLeg() {
        MenuChaoticDefinition land = MenuChaoticCatalog.getScenarios().stream()
                .filter(scenario -> scenario.kind() == MenuChaoticKind.CONVOY
                        && scenario.convoyBackground() == ConvoyScene.BackgroundKind.LAND_BIOME)
                .findFirst()
                .orElseThrow();
        MenuChaoticWorld world = MenuChaoticWorld.fromDefinition(land);
        assertTrue(world.isConvoyTravelingRight());
        assertTrue(world.getAnts().stream().anyMatch(ant -> ant.carrying != null));
    }
}
