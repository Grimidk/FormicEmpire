package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SaveManagerTest {

    @Test
    public void testTradeSerializationDeserialization() throws Exception {
        Savefile.SavedTrade st = new Savefile.SavedTrade();
        st.qOrigin = 1; st.rOrigin = 1;
        st.qDest = 2; st.rDest = 2;
        st.isRecurrent = true;
        st.isBilateral = true;
        st.methodId = GameConstants.METHOD_AIR.getId();
        st.isActive = true;
        st.totalHours = 100;
        st.remainingHours = 50;
        st.isReturning = true;

        st.load.put(GameConstants.RESOURCE_PLANT.getId(), 10.0);
        st.returnLoad.put(GameConstants.RESOURCE_MEAT.getId(), 20.0);
        st.transport.put(GameConstants.TYPE_WORKER.getId(), 5);

        // Pending fields
        st.hasPendingUpdate = true;
        st.pendingRecurrent = false;
        st.pendingIsBilateral = false;
        st.pendingMethodId = GameConstants.METHOD_LAND.getId();
        st.pendingLoad.put(GameConstants.RESOURCE_WATER.getId(), 30.0);
        st.pendingReturnLoad.put(GameConstants.RESOURCE_FUNGI.getId(), 40.0); 
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

    @Test
    void colonySymbioticMitesRoundTripThroughJson() throws Exception {
        Savefile.SavedColony sc = new Savefile.SavedColony();
        sc.id = 1;
        sc.name = "Test";
        sc.symbioticMites = 7;
        sc.aphids = 3;
        sc.dermestids = 2;

        SaveManager saveManager = new SaveManager();
        StringWriter writer = new StringWriter();
        try (java.io.BufferedWriter w = new java.io.BufferedWriter(writer)) {
            Method writeColony = SaveManager.class.getDeclaredMethod(
                    "writeSavedColony", java.io.BufferedWriter.class, Savefile.SavedColony.class, boolean.class);
            writeColony.setAccessible(true);
            writeColony.invoke(saveManager, w, sc, true);
        }

        String json = writer.toString();
        assertTrue(json.contains("\"symbioticMites\": 7"));

        Method parseColony = SaveManager.class.getDeclaredMethod("parseColonyObject", String.class);
        parseColony.setAccessible(true);
        Savefile.SavedColony loaded = (Savefile.SavedColony) parseColony.invoke(saveManager, json);
        assertEquals(7, loaded.symbioticMites);

        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_CATCHER);
        Colony colony = new Colony(loaded);
        dynasty.addColony(colony);
        assertEquals(7, colony.getSymbioticMites());
    }

    @Test
    void colonySymbioticMitesLoadLegacySoilMitesKey() throws Exception {
        String json = """
                {
                "id": 1,
                "name": "Legacy",
                "soilMites": 4
                }
                """;
        SaveManager saveManager = new SaveManager();
        Method parseColony = SaveManager.class.getDeclaredMethod("parseColonyObject", String.class);
        parseColony.setAccessible(true);
        Savefile.SavedColony loaded = (Savefile.SavedColony) parseColony.invoke(saveManager, json);
        assertEquals(4, loaded.symbioticMites);
    }

    @Test
    void colonySymbioticMitesLoadLegacyBullMitesKey() throws Exception {
        String json = """
                {
                "id": 1,
                "name": "Legacy",
                "bullMites": 4
                }
                """;
        SaveManager saveManager = new SaveManager();
        Method parseColony = SaveManager.class.getDeclaredMethod("parseColonyObject", String.class);
        parseColony.setAccessible(true);
        Savefile.SavedColony loaded = (Savefile.SavedColony) parseColony.invoke(saveManager, json);
        assertEquals(4, loaded.symbioticMites);
    }
}
