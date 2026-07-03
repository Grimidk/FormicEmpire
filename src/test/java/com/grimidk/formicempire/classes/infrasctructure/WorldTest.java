package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WorldTest {

    @Test
    public void testTunnelProjectResumedOnLoad() {
        // 1. Setup Savefile
        Savefile savefile = new Savefile(1, "TestSave");
        savefile.setWorldRadius(2);
        
        // Create Hexes (q,r): (0,0) and (0,1)
        List<Savefile.SavedHex> hexes = new ArrayList<>();
        hexes.add(new Savefile.SavedHex(0, 0, GameConstants.BIOME_PLAINS.getId(), true, 0, 1));
        hexes.add(new Savefile.SavedHex(0, 1, GameConstants.BIOME_PLAINS.getId(), true, 0, 1));
        savefile.setWorldHexes(hexes);

        // Create Colonies
        List<Savefile.SavedColony> colonies = new ArrayList<>();
        
        // Colony A at 0,0
        Savefile.SavedColony colA = new Savefile.SavedColony();
        colA.id = 1; colA.dynastyId = 1; colA.name = "Colony A"; colA.q = 0; colA.r = 0;
        colA.isPlayer = true;
        colonies.add(colA);

        // Colony B at 0,1
        Savefile.SavedColony colB = new Savefile.SavedColony();
        colB.id = 2; colB.dynastyId = 1; colB.name = "Colony B"; colB.q = 0; colB.r = 1;
        colB.isPlayer = true;
        colonies.add(colB);
        
        savefile.setColonies(colonies);

        // Create Dynasty
        List<Savefile.SavedDynasty> dynastys = new ArrayList<>();
        Savefile.SavedDynasty dynasty = new Savefile.SavedDynasty();
        dynasty.id = 1;
        dynasty.name = "Test Dynasty";
        dynasty.isPlayer = true;
        
        // Create Incomplete Tunnel between 0,0 and 0,1
        Savefile.SavedTunnel tunnel = new Savefile.SavedTunnel();
        tunnel.qA = 0; tunnel.rA = 0;
        tunnel.qB = 0; tunnel.rB = 1;
        tunnel.progress = 50.0;
        tunnel.totalCost = 100.0;
        tunnel.isComplete = false;
        
        dynasty.tunnels.add(tunnel);
        dynastys.add(dynasty);
        
        savefile.setDynastys(dynastys);

        // 2. Load World
        World world = new World();
        world.setEngine(null); // No engine needed for tunnel test
        world.loadWorld(savefile);

        // 3. Verify
        Hex hA = world.getHexAt(0, 0);
        Hex hB = world.getHexAt(0, 1);
        
        assertNotNull(hA);
        assertNotNull(hB);
        
        Colony cA = hA.getColony();
        Colony cB = hB.getColony();
        
        assertNotNull(cA);
        assertNotNull(cB);
        
        // The fix should ensure BOTH colonies get the project if they belong to the dynasty
        // because we don't know which one started it, so we assign to both to be safe/efficient.
        // Wait, did I implement "assign to both"? Yes:
        // if (hA... && hA...getDynasty() == d) ...
        // if (hB... && hB...getDynasty() == d) ...
        
        Tunnel tA = cA.getCurrentTunnelProject();
        Tunnel tB = cB.getCurrentTunnelProject();
        
        assertNotNull(tA, "Colony A should have resumed tunnel project");
        assertNotNull(tB, "Colony B should have resumed tunnel project");
        
        assertSame(tA, tB, "Both colonies should be working on the SAME tunnel instance");
        assertEquals(50.0, tA.getProgress(), 0.001);
        assertFalse(tA.isComplete());
    }
}
