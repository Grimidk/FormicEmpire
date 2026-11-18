package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SaveCodecTest {

    @TempDir
    Path tempDir;

    private Savefile buildRichSavefile() {
        Savefile save = new Savefile(1, "TestEmpire");
        save.setPlayTime(12345);
        save.setMinute(30);
        save.setHour(14);
        save.setDay(5);
        save.setMonth(3);
        save.setYear(2);
        save.setWorldRadius(6);
        save.setContinentCoreRadius(3);
        save.setColonyId(1);
        save.setColonyName("Capital");
        save.setTotalAnts(250);
        save.setWorkers(200);
        save.setQueens(1);
        save.setDeadAnts(12);
        save.setAllowsAchievements(true);

        for (int q = -3; q <= 3; q++) {
            for (int r = -3; r <= 3; r++) {
                Savefile.SavedHex hx = new Savefile.SavedHex(
                        q, r, GameConstants.BIOME_PLAINS.getId(), false, 2, 0);
                save.getWorldHexes().add(hx);
            }
        }

        for (int i = 1; i <= 15; i++) {
            Savefile.SavedColony sc = new Savefile.SavedColony();
            sc.id = i;
            sc.name = "Colony-" + i;
            sc.dynastyId = (i % 3) + 1;
            sc.isPlayer = (sc.dynastyId == 1);
            sc.isCapital = (i == 1);
            sc.q = i - 8;
            sc.r = 0;
            sc.plants = 100 + i;
            sc.assignedRoleCounts.put("ROLE_FORAGER", 25);
            sc.assignedRoleCounts.put("ROLE_BUILDER", 0);
            sc.assignedRoleCounts.put("ROLE_NURSE", 0);
            sc.warAssignedRoleCounts.put("ROLE_SOLDIER", 0);
            sc.localDeathStatistics.put("OLD_AGE", 0);
            sc.localDeathStatistics.put("STARVATION", 3);
            save.getColonies().add(sc);
        }

        for (int d = 1; d <= 3; d++) {
            Savefile.SavedDynasty sd = new Savefile.SavedDynasty();
            sd.id = d;
            sd.name = "Dynasty-" + d;
            sd.isPlayer = (d == 1);
            sd.speciesId = GameConstants.SPECIES_OMNI.getId();
            sd.deathStatistics.put("OLD_AGE", 0);
            sd.deathStatistics.put("COMBAT", 7);
            Savefile.SavedTunnel tunnel = new Savefile.SavedTunnel();
            tunnel.qA = 0; tunnel.rA = 0; tunnel.qB = 1; tunnel.rB = 0;
            tunnel.progress = 5000000.0; tunnel.totalCost = 5000000.0; tunnel.isComplete = true;
            sd.tunnels.add(tunnel);
            Savefile.SavedTunnel bridge = new Savefile.SavedTunnel();
            bridge.qA = 0; bridge.rA = 0; bridge.qB = 2; bridge.rB = 0;
            bridge.progress = 1000000.0; bridge.totalCost = 5000000.0; bridge.isComplete = false;
            sd.bridges.add(bridge);
            save.getDynastys().add(sd);
        }

        Savefile.SavedTrade tr = new Savefile.SavedTrade();
        tr.qOrigin = 0; tr.rOrigin = 0; tr.qDest = 1; tr.rDest = 0;
        tr.isRecurrent = true; tr.isBilateral = true; tr.methodId = GameConstants.METHOD_LAND.getId();
        tr.isActive = true; tr.totalHours = 48; tr.remainingHours = 12;
        tr.load.put(GameConstants.RESOURCE_PLANT.getId(), 50.0);
        save.getTrades().add(tr);

        return save;
    }

    @Test
    void compressedCodecRoundTripsPrunesZerosAndWritesMetadata() throws Exception {
        Savefile original = buildRichSavefile();
        String uncompressedJson = SaveCodec.toJson(original);

        File compressedFile = tempDir.resolve("slot1.manual.json").toFile();
        SaveCodec.writeCompressed(original, compressedFile);

        assertTrue(compressedFile.length() < uncompressedJson.length() / 3,
                "Compressed save (" + compressedFile.length() + " B) should be much smaller than raw JSON ("
                        + uncompressedJson.length() + " B)");

        Savefile loaded = SaveCodec.read(compressedFile);
        assertNotNull(loaded);
        assertEquals(Savefile.CURRENT_FORMAT_VERSION, loaded.getFormatVersion());
        assertEquals("TestEmpire", loaded.getName());
        assertEquals(original.getWorldHexes().size(), loaded.getWorldHexes().size());
        assertEquals(original.getColonies().size(), loaded.getColonies().size());
        assertEquals(original.getDynastys().size(), loaded.getDynastys().size());
        assertEquals(original.getTrades().size(), loaded.getTrades().size());

        Savefile.SavedColony c0 = loaded.getColonies().get(0);
        assertEquals(25, c0.assignedRoleCounts.get("ROLE_FORAGER"));
        assertFalse(c0.assignedRoleCounts.containsKey("ROLE_BUILDER"), "Zero entries should be pruned");
        assertEquals(3, c0.localDeathStatistics.get("STARVATION"));
        assertFalse(c0.localDeathStatistics.containsKey("OLD_AGE"), "Zero entries should be pruned");

        Savefile.SavedDynasty d0 = loaded.getDynastys().get(0);
        assertEquals(1, d0.tunnels.size());
        assertEquals(1, d0.bridges.size());
        assertFalse(d0.bridges.get(0).isComplete);

        Savefile metaPreview = SaveCodec.readMetadata(compressedFile);
        assertNotNull(metaPreview);
        assertEquals("TestEmpire", metaPreview.getName());
        assertEquals(12345, metaPreview.getPlayTime());
        assertEquals(250, metaPreview.getTotalAnts());
    }

    @Test
    void schemaEvolutionAndMigrationHandleUpdatesCleanly() throws Exception {
        String olderOrPartialJson = "{\"formatVersion\":1,\"id\":7,\"name\":\"Evolved\",\"futureRemovedField\":123,"
                + "\"colonies\":[{\"id\":1,\"name\":\"C1\",\"removedColonyProp\":\"abc\"}],"
                + "\"dynastys\":[{\"id\":1,\"name\":\"D1\"}],"
                + "\"trades\":[{\"qOrigin\":0,\"rOrigin\":0,\"qDest\":1,\"rDest\":0}],"
                + "\"wars\":[{\"id\":9}],"
                + "\"worldHistory\":[{\"type\":\"TEST\"}]}";
        File f = tempDir.resolve("evolved.json").toFile();
        Files.writeString(f.toPath(), olderOrPartialJson, StandardCharsets.UTF_8);

        Savefile loaded = SaveCodec.read(f);
        assertNotNull(loaded);
        assertEquals(Savefile.CURRENT_FORMAT_VERSION, loaded.getFormatVersion());
        assertEquals(7, loaded.getId());
        assertEquals("Evolved", loaded.getName());
        assertNotNull(loaded.getWorldHexes());
        assertNotNull(loaded.getColonies());
        assertNotNull(loaded.getDynastys());
        assertNotNull(loaded.getTrades());
        assertNotNull(loaded.getWars());
        assertNotNull(loaded.getWorldHistory());

        assertNotNull(loaded.getColonies().get(0).assignedRoleCounts);
        assertNotNull(loaded.getColonies().get(0).unlockedBuildingIds);
        assertNotNull(loaded.getDynastys().get(0).bridges);
        assertNotNull(loaded.getDynastys().get(0).tunnels);
        assertNotNull(loaded.getTrades().get(0).load);
        assertNotNull(loaded.getTrades().get(0).pendingLoad);
        assertNotNull(loaded.getWars().get(0).capturedColonyIds);
        assertNotNull(loaded.getWorldHistory().get(0).args);
    }
}
