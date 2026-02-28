package com.grimidk.formicempire.classes.infrasctructure.managers;

import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SaveManagerTest {

    private SaveManager saveManager;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        saveManager = new SaveManager();
    }

    @Test
    public void testSaveAndLoadManual() throws IOException {
        int slotId = 99;
        String saveName = "Test Save";
        Savefile original = new Savefile(slotId, saveName);
        original.setMinute(30);
        original.setHour(12);
        
        // Dynasty
        Savefile.SavedDynasty sd = new Savefile.SavedDynasty();
        sd.id = 1;
        sd.name = "Test Dynasty";
        List<Savefile.SavedDynasty> dynasties = new ArrayList<>();
        dynasties.add(sd);
        original.setDynastys(dynasties);

        // Action
        saveManager.saveUserSlot(original);

        // Verification
        Savefile loaded = saveManager.loadSlot(slotId);
        assertNotNull(loaded, "Loaded save should not be null");
        assertEquals(saveName, loaded.getName());
        assertEquals(30, loaded.getMinute());
        assertEquals(12, loaded.getHour());
        assertEquals(1, loaded.getDynastys().size());
        assertEquals("Test Dynasty", loaded.getDynastys().get(0).name);
        
        // Cleanup
        saveManager.deleteSlot(slotId);
    }
}