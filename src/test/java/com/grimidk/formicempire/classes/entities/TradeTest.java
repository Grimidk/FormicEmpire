package com.grimidk.formicempire.classes.entities;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    }

    @Test
    public void testCapacityCheckFailsWhenFull() {
        Trade trade = new Trade(originHex, destHex, load, transport, false, GameConstants.METHOD_LAND);
        origin.setPlants(200); 
        destination.setPlants(destination.getPlantsCapacity());
        trade.startTrip();
        assertFalse(trade.isActive(), "Trade should be cancelled if destination is full/over capacity");
    }

    @Test
    public void testCapacityCheckSucceedsWhenNotFull() {
        destination.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0); 
        origin.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        origin.setPlants(200);
        Trade trade = new Trade(originHex, destHex, load, transport, false, GameConstants.METHOD_LAND);
        trade.startTrip();
        assertTrue(trade.isActive(), "Trade should be active if destination has capacity");
    }

    @Test
    public void testDurationCalculation() throws NoSuchFieldException, IllegalAccessException {
        Trade trade = new Trade(originHex, destHex, load, transport, false, GameConstants.METHOD_LAND);
        Field totalHoursField = Trade.class.getDeclaredField("totalHours");
        totalHoursField.setAccessible(true);
        int hours = (int) totalHoursField.get(trade);
        assertEquals(168, hours, "Duration should be 168 hours for base speed");
        
        Trade airTrade = new Trade(originHex, destHex, load, transport, false, GameConstants.METHOD_AIR);
        int airHours = (int) totalHoursField.get(airTrade);
        assertEquals(34, airHours, "Duration should be 34 hours for Air speed (5x)");
    }

    @Test
    public void testSecurityLossWithWorkers() throws NoSuchFieldException, IllegalAccessException {
        destination.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        origin.unlockBuilding(GameUnlocks.PLANT_CHAMBER_0);
        origin.setPlants(200);

        Trade trade = new Trade(originHex, destHex, load, transport, false, GameConstants.METHOD_LAND);
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

        Trade trade = new Trade(originHex, destHex, load, transport, false, GameConstants.METHOD_LAND);
        trade.startTrip();
        
        Field remainingHoursField = Trade.class.getDeclaredField("remainingHours");
        remainingHoursField.setAccessible(true);
        remainingHoursField.set(trade, 1);
        
        trade.tick();
        
        assertEquals(100.0, destination.getPlantsPrecise(), 0.01, "Destination should receive 100% of load with high security (50 soldiers)");
    }
}
