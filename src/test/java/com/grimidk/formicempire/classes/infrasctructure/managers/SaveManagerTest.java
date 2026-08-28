package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
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
    void subtypeHatchRatesFlatRoundTripThroughColonyJson() throws Exception {
        Savefile.SavedColony sc = new Savefile.SavedColony();
        sc.id = 1;
        sc.name = "SubtypeRates";
        Colony live = new Colony(1, "SubtypeRates", true);
        live.setSubtypeHatchRate(GameConstants.TYPE_WORKER, AntSubtypeSlot.HEAD, 2, 25f);
        live.setSubtypeHatchRate(GameConstants.TYPE_WORKER, AntSubtypeSlot.HEAD, AntSubtype.DIGIT_NONE, 75f);
        live.setSubtypeHatchRate(GameConstants.TYPE_SOLDIER, AntSubtypeSlot.ABDOMEN, 3, 40f);
        live.setSubtypeHatchRate(GameConstants.TYPE_SOLDIER, AntSubtypeSlot.ABDOMEN, AntSubtype.DIGIT_NONE, 60f);
        sc.subtypeHatchRatesFlat = AntSubtypeService.flattenSubtypeRates(live.getSubtypeHatchRates());

        SaveManager saveManager = new SaveManager();
        StringWriter writer = new StringWriter();
        try (java.io.BufferedWriter w = new java.io.BufferedWriter(writer)) {
            Method writeColony = SaveManager.class.getDeclaredMethod(
                    "writeSavedColony", java.io.BufferedWriter.class, Savefile.SavedColony.class, boolean.class);
            writeColony.setAccessible(true);
            writeColony.invoke(saveManager, w, sc, true);
        }

        String json = writer.toString();
        String workerHeadKey = GameConstants.TYPE_WORKER.getNameKey() + "|HEAD|2";
        assertTrue(json.contains("\"" + workerHeadKey + "\":"),
                "serialized colony should keep pipe keys in subtypeHatchRatesFlat");

        Method parseColony = SaveManager.class.getDeclaredMethod("parseColonyObject", String.class);
        parseColony.setAccessible(true);
        Savefile.SavedColony loaded = (Savefile.SavedColony) parseColony.invoke(saveManager, json);
        assertEquals(25.0, loaded.subtypeHatchRatesFlat.get(workerHeadKey));
        assertEquals(40.0, loaded.subtypeHatchRatesFlat.get(
                GameConstants.TYPE_SOLDIER.getNameKey() + "|ABDOMEN|3"));

        Colony colony = new Colony(loaded);
        assertEquals(25f, colony.getSubtypeHatchRate(GameConstants.TYPE_WORKER, AntSubtypeSlot.HEAD, 2));
        assertEquals(75f, colony.getSubtypeHatchRate(
                GameConstants.TYPE_WORKER, AntSubtypeSlot.HEAD, AntSubtype.DIGIT_NONE));
        assertEquals(40f, colony.getSubtypeHatchRate(GameConstants.TYPE_SOLDIER, AntSubtypeSlot.ABDOMEN, 3));
        assertEquals(60f, colony.getSubtypeHatchRate(
                GameConstants.TYPE_SOLDIER, AntSubtypeSlot.ABDOMEN, AntSubtype.DIGIT_NONE));
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

    @Test
    void warArrayDeserializationPreservesSecondRecord() throws Exception {
        Savefile.SavedWar first = new Savefile.SavedWar();
        first.id = 1;
        first.dynastyIdA = 1;
        first.dynastyIdB = 2;
        first.startedWorldMonth = 10;
        first.declaredByDynastyId = 1;
        first.displayName = "First War";
        first.endedWorldMonth = 12;
        first.winnerDynastyId = 1;
        first.conclusionKey = "WAR_CONCLUSION_PEACE_TREATY";
        first.dynastyNameA = "Alpha Dynasty";
        first.dynastyNameB = "Beta Dynasty";
        first.winnerDynastyName = "Alpha Dynasty";

        Savefile.SavedWar second = new Savefile.SavedWar();
        second.id = 2;
        second.dynastyIdA = 1;
        second.dynastyIdB = 15;
        second.startedWorldMonth = 47;
        second.declaredByDynastyId = 1;
        second.displayName = "First Grim - Vine War";
        second.endedWorldMonth = 47;
        second.winnerDynastyId = 1;
        second.conclusionKey = "WAR_CONCLUSION_ABSOLUTE_VICTORY";
        second.dynastyNameA = "Grim Dynasty";
        second.dynastyNameB = "Vine Dynasty";
        second.winnerDynastyName = "Grim Dynasty";

        SaveManager saveManager = new SaveManager();
        Method serializeMethod = SaveManager.class.getDeclaredMethod("serializeWarsToJson", List.class);
        serializeMethod.setAccessible(true);
        String json = (String) serializeMethod.invoke(saveManager, List.of(first, second));

        Method deserializeMethod = SaveManager.class.getDeclaredMethod("deserializeJsonToWars", String.class);
        deserializeMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Savefile.SavedWar> loaded = (List<Savefile.SavedWar>) deserializeMethod.invoke(saveManager, json);

        assertEquals(2, loaded.size());
        assertEquals("First Grim - Vine War", loaded.get(1).displayName);
        assertEquals(15, loaded.get(1).dynastyIdB);
        assertEquals(1, loaded.get(1).winnerDynastyId);
        assertFalse(json.contains("dynastyNameA"));
        assertFalse(json.contains("winnerDynastyName"));
    }

    @Test
    void rebellionWarFlagSurvivesWarJsonRoundTrip() throws Exception {
        Savefile.SavedWar war = new Savefile.SavedWar();
        war.id = 9;
        war.dynastyIdA = 1;
        war.dynastyIdB = 2;
        war.startedWorldMonth = 3;
        war.declaredByDynastyId = 1;
        war.displayName = "First Crystal Rebellion";
        war.militaryPowerAtStartA = 100;
        war.militaryPowerAtStartB = 80;
        war.rebellionWar = true;

        SaveManager saveManager = new SaveManager();
        Method serializeMethod = SaveManager.class.getDeclaredMethod("serializeWarsToJson", List.class);
        serializeMethod.setAccessible(true);
        String json = (String) serializeMethod.invoke(saveManager, List.of(war));

        Method deserializeMethod = SaveManager.class.getDeclaredMethod("deserializeJsonToWars", String.class);
        deserializeMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Savefile.SavedWar> loaded = (List<Savefile.SavedWar>) deserializeMethod.invoke(saveManager, json);

        assertEquals(1, loaded.size());
        assertTrue(loaded.get(0).rebellionWar);
        assertEquals("First Crystal Rebellion", loaded.get(0).displayName);
    }

    @Test
    void rebellionDynastyFieldsSurviveDynastyJsonRoundTrip() throws Exception {
        String json = "{"
                + "\"id\":5,"
                + "\"name\":\"Crystal Dynasty\","
                + "\"titleKey\":\"DYNASTY_TITLE_DYNASTY\","
                + "\"isPlayer\":true,"
                + "\"isDefeated\":false,"
                + "\"rank\":\"Ant\","
                + "\"speciesId\":1,"
                + "\"originDynastyId\":0,"
                + "\"activeRebellionDynastyId\":12,"
                + "\"pendingRebellionResponseFromId\":12"
                + "}";

        SaveManager saveManager = new SaveManager();
        Method parseMethod = SaveManager.class.getDeclaredMethod("parseDynastyObject", String.class);
        parseMethod.setAccessible(true);
        Savefile.SavedDynasty loaded = (Savefile.SavedDynasty) parseMethod.invoke(saveManager, json);

        assertEquals(12, loaded.activeRebellionDynastyId);
        assertEquals(12, loaded.pendingRebellionResponseFromId);
        assertEquals(0, loaded.originDynastyId);
    }

    @Test
    void automatedSubtypeRatesAppearInColonyJson() throws Exception {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_WORKER);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_MAJOR);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_PRINCESS);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HONEYPOT);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DOORHEAD);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_STINGING);
        Colony colony = new Colony(1, "C", true);
        dynasty.addColony(colony);
        colony.setAutomationEnabled(true);
        colony.setMushrooms(15000);
        colony.setWater(2500);
        colony.getPopulationService().runHatching(colony);

        assertTrue(colony.getSubtypeHatchRate(GameConstants.TYPE_WORKER, AntSubtypeSlot.ABDOMEN, 3) > 0f,
                "automation should set honeypot worker rate before save");

        Savefile.SavedColony sc = new Savefile.SavedColony();
        sc.id = 1;
        sc.name = "C";
        sc.subtypeHatchRatesFlat = AntSubtypeService.flattenSubtypeRates(colony.getSubtypeHatchRates());

        SaveManager saveManager = new SaveManager();
        StringWriter writer = new StringWriter();
        try (java.io.BufferedWriter w = new java.io.BufferedWriter(writer)) {
            Method writeColony = SaveManager.class.getDeclaredMethod(
                    "writeSavedColony", java.io.BufferedWriter.class, Savefile.SavedColony.class, boolean.class);
            writeColony.setAccessible(true);
            writeColony.invoke(saveManager, w, sc, true);
        }
        String json = writer.toString();
        String honeypotKey = GameConstants.TYPE_WORKER.getNameKey() + "|ABDOMEN|3";
        assertTrue(json.contains("\"" + honeypotKey + "\":"), json);

        Method parseColony = SaveManager.class.getDeclaredMethod("parseColonyObject", String.class);
        parseColony.setAccessible(true);
        Savefile.SavedColony loaded = (Savefile.SavedColony) parseColony.invoke(saveManager, json);
        Colony restored = new Colony(loaded);
        assertEquals(50f, restored.getSubtypeHatchRate(GameConstants.TYPE_WORKER, AntSubtypeSlot.ABDOMEN, 3));
    }

    @Test
    void playerSaveSummaryUsesActiveOrCapitalNotLastSatellite() throws Exception {
        World world = new World();
        Engine engine = new Engine();
        world.setEngine(engine);

        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.getStarterService().initializeDynasty(dynasty);

        Colony capital = new Colony(1, "Capital", true);
        capital.setCapital(true);
        dynasty.setCapital(capital);
        dynasty.addColony(capital);
        capital.setDynasty(dynasty);
        capital.setActive(true);
        for (int i = 0; i < 50; i++) {
            capital.getWorkers().add(new Ant(capital, GameConstants.TYPE_WORKER));
        }

        Colony satellite = new Colony(2, "Satellite", true);
        dynasty.addColony(satellite);
        satellite.setDynasty(dynasty);
        satellite.setActive(false);
        satellite.getQueens().add(new Ant(satellite, GameConstants.TYPE_QUEEN));

        Hex capitalHex = new Hex();
        capitalHex.setQ(0);
        capitalHex.setR(0);
        capitalHex.setColony(capital);
        Hex satelliteHex = new Hex();
        satelliteHex.setQ(1);
        satelliteHex.setR(0);
        satelliteHex.setColony(satellite);

        world.registerDynasty(dynasty);
        world.getHexes().add(capitalHex);
        world.getHexes().add(satelliteHex);

        Savefile save = new Savefile(1, "test");
        SaveManager saveManager = new SaveManager();
        Method populate = SaveManager.class.getDeclaredMethod(
                "populateSavefileFromGame", Savefile.class, World.class, Engine.class);
        populate.setAccessible(true);
        populate.invoke(saveManager, save, world, engine);

        assertEquals(capital.getId(), save.getColonyId());
        assertEquals(capital.getName(), save.getColonyName());
        assertTrue(save.getTotalAnts() > satellite.getAntTotal());
        assertTrue(save.getWorkers() > 0);
    }
}
