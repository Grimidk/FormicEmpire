package com.grimidk.formicempire.classes.interfaces.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class RouteViewVisualsCarryTest {

    @Test
    void gathererShowsCarryOnlyWhenReturningWithResource() {
        Colony colony = new Colony(1, "C", true);
        Ant forager = new Ant(colony, GameConstants.TYPE_WORKER);
        forager.setRole(GameConstants.ROLE_FORAGER);
        assertFalse(RouteViewVisuals.showsGathererCarry(forager));

        forager.setCarrying(GameConstants.RESOURCE_PLANT);
        assertTrue(RouteViewVisuals.showsGathererCarry(forager));
        assertEquals(2, RouteViewVisuals.jawFrameForCarry(1, true));
        ResourceType[] icons = RouteViewVisuals.gathererCarryIcons(forager);
        assertEquals(1, icons.length);
        assertEquals(GameConstants.RESOURCE_PLANT, icons[0]);
    }

    @Test
    void nurseBroodCarryDoesNotShowResourceIcon() {
        Colony colony = new Colony(1, "C", true);
        Ant nurse = new Ant(colony, GameConstants.TYPE_WORKER);
        nurse.setRole(GameConstants.ROLE_NURSE);
        nurse.setCarryingAnt(GameConstants.TYPE_EGG);
        assertFalse(RouteViewVisuals.showsGathererCarry(nurse));
        assertEquals(0, RouteViewVisuals.gathererCarryIcons(nurse).length);
    }

    @Test
    void secondaryResinAppearsBesidePrimaryPlant() {
        Colony colony = new Colony(1, "C", true);
        Ant forager = new Ant(colony, GameConstants.TYPE_WORKER);
        forager.setRole(GameConstants.ROLE_FORAGER);
        forager.setCarrying(GameConstants.RESOURCE_PLANT);
        forager.setCarryingSec(GameConstants.RESOURCE_RESIN);
        ResourceType[] icons = RouteViewVisuals.gathererCarryIcons(forager);
        assertEquals(2, icons.length);
        assertEquals(GameConstants.RESOURCE_PLANT, icons[0]);
        assertEquals(GameConstants.RESOURCE_RESIN, icons[1]);
    }

    @Test
    void hunterAndMinerUseSameCarryRule() {
        Colony colony = new Colony(1, "C", true);
        Ant hunter = new Ant(colony, GameConstants.TYPE_SOLDIER);
        hunter.setRole(GameConstants.ROLE_HUNTER);
        hunter.setCarrying(GameConstants.RESOURCE_MEAT);
        assertTrue(RouteViewVisuals.showsGathererCarry(hunter));

        Ant miner = new Ant(colony, GameConstants.TYPE_SOLDIER);
        miner.setRole(GameConstants.ROLE_MINER);
        miner.setCarrying(GameConstants.RESOURCE_ROCK);
        assertTrue(RouteViewVisuals.showsGathererCarry(miner));
    }

    @Test
    void cargoIconsSkipEmptyAmountsAndCapAtTwo() {
        Map<ResourceType, Double> cargo = new LinkedHashMap<>();
        cargo.put(GameConstants.RESOURCE_WATER, 0d);
        cargo.put(GameConstants.RESOURCE_PLANT, 12d);
        cargo.put(GameConstants.RESOURCE_ROCK, 3d);
        cargo.put(GameConstants.RESOURCE_MEAT, 1d);
        ResourceType[] icons = RouteViewVisuals.cargoIcons(cargo);
        assertEquals(2, icons.length);
        assertEquals(GameConstants.RESOURCE_PLANT.getId(), icons[0].getId());
        assertEquals(GameConstants.RESOURCE_MEAT.getId(), icons[1].getId());
    }

    @Test
    void dronesDoNotHoldJawCargo() {
        assertFalse(RouteViewVisuals.canHoldJawCargo(GameConstants.TYPE_DRONE));
        assertFalse(RouteViewVisuals.canHoldJawCargo(GameConstants.TYPE_PRINCESS));
        assertTrue(RouteViewVisuals.canHoldJawCargo(GameConstants.TYPE_WORKER));
    }
}
