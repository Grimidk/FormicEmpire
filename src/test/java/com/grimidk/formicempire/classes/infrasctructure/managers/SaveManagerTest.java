package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SaveManagerTest {

    @Test
    public void testTradeSerializationDeserialization() throws Exception {
        // 1. Create a SavedTrade manually with all fields including new ones
        Savefile.SavedTrade st = new Savefile.SavedTrade();
        st.qOrigin = 1; st.rOrigin = 1;
        st.qDest = 2; st.rDest = 2;
        st.isRecurrent = true;
        st.isBilateral = true; // New field
        st.methodId = GameConstants.METHOD_AIR.getId();
        st.isActive = true;
        st.totalHours = 100;
        st.remainingHours = 50;
        st.isReturning = true;

        st.load.put(GameConstants.RESOURCE_PLANT.getId(), 10.0);
        st.returnLoad.put(GameConstants.RESOURCE_MEAT.getId(), 20.0); // New field
        st.transport.put(GameConstants.TYPE_WORKER.getId(), 5);

        // Pending fields
        st.hasPendingUpdate = true;
        st.pendingRecurrent = false;
        st.pendingIsBilateral = false; // New field
        st.pendingMethodId = GameConstants.METHOD_LAND.getId();
        st.pendingLoad.put(GameConstants.RESOURCE_WATER.getId(), 30.0);
        st.pendingReturnLoad.put(GameConstants.RESOURCE_FUNGI.getId(), 40.0); // New field
        st.pendingTransport.put(GameConstants.TYPE_SOLDIER.getId(), 10);

        List<Savefile.SavedTrade> list = new ArrayList<>();
        list.add(st);

        // 2. Serialize using reflection
        SaveManager saveManager = new SaveManager();
        Method serializeMethod = SaveManager.class.getDeclaredMethod("serializeTradesToJson", List.class);
        serializeMethod.setAccessible(true);
        String json = (String) serializeMethod.invoke(saveManager, list);

        System.out.println("Serialized JSON: " + json);

        // 3. Verify JSON contains new fields
        assertTrue(json.contains("\"ib\":true"), "JSON should contain isBilateral (ib)");
        assertTrue(json.contains("\"rload\":"), "JSON should contain returnLoad (rload)");
        assertTrue(json.contains("\"pib\":false"), "JSON should contain pendingIsBilateral (pib)");
        assertTrue(json.contains("\"prload\":"), "JSON should contain pendingReturnLoad (prload)");

        // 4. Deserialize using reflection
        Method deserializeMethod = SaveManager.class.getDeclaredMethod("deserializeJsonToTrades", String.class);
        deserializeMethod.setAccessible(true);
        List<Savefile.SavedTrade> deserializedList = (List<Savefile.SavedTrade>) deserializeMethod.invoke(saveManager, json);

        assertEquals(1, deserializedList.size());
        Savefile.SavedTrade result = deserializedList.get(0);

        // 5. Verify values
        assertTrue(result.isBilateral);
        assertEquals(20.0, result.returnLoad.get(GameConstants.RESOURCE_MEAT.getId()));
        
        assertTrue(result.hasPendingUpdate);
        assertFalse(result.pendingIsBilateral);
        assertEquals(40.0, result.pendingReturnLoad.get(GameConstants.RESOURCE_FUNGI.getId()));
    }
}
