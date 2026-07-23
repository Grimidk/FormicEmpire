package com.grimidk.formicempire.classes.entities;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;

public class TradeTest {

    private Colony origin;
    private Colony destination;
    private Hex originHex;
    private Hex destHex;
    private Map<ResourceType, Double> load;
    private Map<AntType, Integer> transport;

    @BeforeEach
    public void setup() {
        origin = new Colony(1, "Origin", true);
        destination = new Colony(2, "Destination", true);
        originHex = new Hex();
        originHex.setColony(origin);
        destHex = new Hex();
        destHex.setColony(destination);

        load = new HashMap<>();
        load.put(GameConstants.RESOURCE_PLANT, 100.0);
        
        transport = new HashMap<>();
        transport.put(GameConstants.TYPE_WORKER, 10);
        
        origin.addAnts(GameConstants.TYPE_WORKER, 100);
        origin.addAnts(GameConstants.TYPE_SOLDIER, 100);
    }

    @Test
    public void testCapacityCheckNowSucceedsWhenFull() {
        Trade trade = new Trade(originHex, destHex, load, null, transport, false, false, GameConstants.METHOD_LAND);
        origin.setPlants(200); 
        destination.setPlants(destination.getPlantsCapacity());
        boolean started = trade.startTrip();
        assertTrue(started, "Trade should now start even if destination is full (will deliver partial/zero later)");
        assertTrue(trade.isActive(), "Trade should be active even if destination is full");
    }

    @Test
    public void testCapacityCheckSucceedsWhenNotFull() {
        destination.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0); 
        origin.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        origin.setPlants(200);
        Trade trade = new Trade(originHex, destHex, load, null, transport, false, false, GameConstants.METHOD_LAND);
        boolean started = trade.startTrip();
        assertTrue(started, "Trade should start if destination has capacity");
        assertTrue(trade.isActive(), "Trade should be active if destination has capacity");
    }

    @Test
    public void testDurationCalculation() throws NoSuchFieldException, IllegalAccessException {
        Trade trade = new Trade(originHex, destHex, load, null, transport, false, false, GameConstants.METHOD_LAND);
        Field totalHoursField = Trade.class.getDeclaredField("totalHours");
        totalHoursField.setAccessible(true);
        int hours = (int) totalHoursField.get(trade);
        assertEquals(168, hours, "Duration should be 168 hours for base speed");
        
        Trade airTrade = new Trade(originHex, destHex, load, null, transport, false, false, GameConstants.METHOD_AIR);
        int airHours = (int) totalHoursField.get(airTrade);
        assertEquals(34, airHours, "Duration should be 34 hours for Air speed (5x)");
    }

    @Test
    public void testSecurityLossWithWorkers() throws NoSuchFieldException, IllegalAccessException {
        destination.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        origin.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        origin.setPlants(200);

        Trade trade = new Trade(originHex, destHex, load, null, transport, false, false, GameConstants.METHOD_LAND);
        trade.startTrip();
        
        Field remainingHoursField = Trade.class.getDeclaredField("remainingHours");
        remainingHoursField.setAccessible(true);
        remainingHoursField.set(trade, 1);
        
        trade.tick();
        
        assertEquals(18.18, destination.getPlantsPrecise(), 0.01, "Destination should receive ~18% of load with low security (10 workers)");
    }

    @Test
    public void testSecurityLossWithSoldiers() throws NoSuchFieldException, IllegalAccessException {
        destination.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        origin.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        origin.setPlants(200);

        transport.clear();
        transport.put(GameConstants.TYPE_SOLDIER, 50);

        Trade trade = new Trade(originHex, destHex, load, null, transport, false, false, GameConstants.METHOD_LAND);
        trade.startTrip();

        Field remainingHoursField = Trade.class.getDeclaredField("remainingHours");
        remainingHoursField.setAccessible(true);
        remainingHoursField.set(trade, 1);

        trade.tick();

        assertEquals(100.0, destination.getPlantsPrecise(), 0.01, "Destination should receive 100% of load with high security (50 soldiers)");
    }

    @Test
    public void testAntsNotRemovedDuringTrade() {
        int initialCount = origin.getAntTotal();
        
        Trade trade = new Trade(originHex, destHex, load, null, transport, false, false, GameConstants.METHOD_LAND);
        trade.startTrip();

        assertEquals(initialCount, origin.getAntTotal(), "Ants should NOT be removed from colony during trade");

        long antsOnTradeCount = origin.getWorkers().stream().filter(Ant::isOnTrade).count();
        assertEquals(10, antsOnTradeCount, "10 ants should be marked as on trade");
    }

    @Test
    public void testDeferredUpdate() throws NoSuchFieldException, IllegalAccessException {
        origin.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        destination.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        origin.setPlants(500);

        Trade trade = new Trade(originHex, destHex, load, null, transport, true, false, GameConstants.METHOD_LAND);
        trade.startTrip();

        Map<ResourceType, Double> newLoad = new HashMap<>();
        newLoad.put(GameConstants.RESOURCE_PLANT, 200.0);
        Map<AntType, Integer> newTransport = new HashMap<>();
        newTransport.put(GameConstants.TYPE_WORKER, 15);

        trade.setPendingUpdate(newLoad, null, newTransport, true, false, GameConstants.METHOD_AIR);
        assertTrue(trade.hasPendingUpdate());

        Field remainingHoursField = Trade.class.getDeclaredField("remainingHours");
        remainingHoursField.setAccessible(true);
        remainingHoursField.set(trade, 1);
        trade.tick(); 
        assertTrue(trade.isReturning());
        
        assertEquals(100.0, trade.getLoad().get(GameConstants.RESOURCE_PLANT), "Load should still be 100 during return trip of first leg");

        remainingHoursField.set(trade, 1);
        trade.tick(); 

        assertFalse(trade.hasPendingUpdate());
        assertEquals(200.0, trade.getLoad().get(GameConstants.RESOURCE_PLANT), "New load should be applied after return");
        assertEquals(15, trade.getTransport().get(GameConstants.TYPE_WORKER), "New transport should be applied after return");
        assertEquals(GameConstants.METHOD_AIR, trade.getMethod());
    }

    @Test
    public void testAntAvailability() {
        origin.getWorkers().clear();
        origin.addAnts(GameConstants.TYPE_WORKER, 10);

        for (int i = 0; i < 5; i++) {
            origin.getWorkers().get(i).setOnTrade(true);
        }

        transport.put(GameConstants.TYPE_WORKER, 10);
        Trade trade = new Trade(originHex, destHex, load, null, transport, false, false, GameConstants.METHOD_LAND);
        boolean started = trade.startTrip();

        assertFalse(started, "Trade should not start if not enough ants are available");
        
        try {
            Field antsOnTripField = Trade.class.getDeclaredField("antsOnTrip");
            antsOnTripField.setAccessible(true);
            java.util.List<?> antsOnTrip = (java.util.List<?>) antsOnTripField.get(trade);
            assertEquals(0, antsOnTrip.size(), "No ants should have been grabbed if requirements weren't met");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testStartTripFailsIfNoAntsFound() {
        origin.getWorkers().clear();
        
        transport.put(GameConstants.TYPE_WORKER, 1);
        Trade trade = new Trade(originHex, destHex, load, null, transport, false, false, GameConstants.METHOD_LAND);

        boolean started = trade.startTrip();
        assertFalse(started, "Trade should not start if no ants are found");

        try {
            Field antsOnTripField = Trade.class.getDeclaredField("antsOnTrip");
            antsOnTripField.setAccessible(true);
            java.util.List<?> antsOnTrip = (java.util.List<?>) antsOnTripField.get(trade);
            assertEquals(0, antsOnTrip.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCancelDueToEscortLossWhenAntsDie() {
        destination.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        origin.setPlants(200);
        Trade trade = new Trade(originHex, destHex, load, null, transport, false, false, GameConstants.METHOD_LAND);
        assertTrue(trade.startTrip());
        assertEquals(10, trade.getAntsOnTrip().size());

        for (int i = 0; i < 5; i++) {
            Ant ant = trade.getAntsOnTrip().get(i);
            ant.goDie(origin, "Test");
        }

        assertFalse(trade.hasSufficientLiveEscorts());
        trade.cancelDueToEscortLoss();
        assertFalse(trade.isActive());
    }
}
