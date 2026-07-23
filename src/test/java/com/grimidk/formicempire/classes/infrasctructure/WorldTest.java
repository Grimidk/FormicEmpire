package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        // Incomplete tunnels resume on a single sponsor (hex A preferred) so both
        // endpoints do not dig the same project and progress without diggers.
        Tunnel tA = cA.getCurrentTunnelProject();
        Tunnel tB = cB.getCurrentTunnelProject();

        assertNotNull(tA, "Colony A should have resumed tunnel project");
        assertNull(tB, "Colony B should not also sponsor the same tunnel");

        assertEquals(50.0, tA.getProgress(), 0.001);
        assertFalse(tA.isComplete());
    }

    @Test
    void generateWorldIncludesEachNonOmniSpecies() {
        World world = new World();
        Colony colony = new Colony(1, "Test Prime", true);
        world.generateWorld(
                GameConstants.BIOME_PLAINS,
                8,
                colony,
                "Test",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        Set<Integer> npcSpeciesIds = new HashSet<>();
        for (Dynasty dynasty : world.getDynastys()) {
            if (!dynasty.isPlayer() && dynasty.getSpecies() != null) {
                npcSpeciesIds.add(dynasty.getSpecies().getId());
            }
        }

        for (var species : GameConstants.getWorldSpawnableNpcSpecies()) {
            assertTrue(npcSpeciesIds.contains(species.getId()),
                    "Missing NPC dynasty for species id " + species.getId());
        }
    }

    @Test
    void minDynastyHexDistanceUsesClosestColonyPair() {
        World world = new World();
        ArrayList<Hex> hexes = new ArrayList<>();

        Dynasty dynastyA = new Dynasty(1, "Alpha", true, GameConstants.SPECIES_OMNI);
        Dynasty dynastyB = new Dynasty(2, "Beta", false, GameConstants.SPECIES_OMNI);

        Colony aFar = new Colony(1, "A Far", true);
        Colony aNear = new Colony(2, "A Near", true);
        Colony bOnly = new Colony(3, "B Only", false);
        aFar.setDynasty(dynastyA);
        aNear.setDynasty(dynastyA);
        bOnly.setDynasty(dynastyB);

        Hex hexAFar = new Hex();
        hexAFar.setQ(0);
        hexAFar.setR(0);
        hexAFar.setColony(aFar);

        Hex hexANear = new Hex();
        hexANear.setQ(3);
        hexANear.setR(0);
        hexANear.setColony(aNear);

        Hex hexB = new Hex();
        hexB.setQ(4);
        hexB.setR(0);
        hexB.setColony(bOnly);

        hexes.add(hexAFar);
        hexes.add(hexANear);
        hexes.add(hexB);
        world.setHexes(hexes);

        assertEquals(1, world.minDynastyHexDistance(dynastyA, dynastyB));
        assertEquals(Integer.MAX_VALUE, world.minDynastyHexDistance(dynastyA, null));
    }
}
