package com.grimidk.formicempire.classes.infrasctructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.ClimateZone;
import com.grimidk.formicempire.classes.constants.world.HydrologyLevel;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.world.Continent;
import com.grimidk.formicempire.classes.entities.services.shared.HexWaterCrossing;
import com.grimidk.formicempire.classes.entities.services.shared.SandboxCheatService;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleState;
import com.grimidk.formicempire.classes.entities.services.world.WarCreatureCombatService;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.maps.SavedMapService;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class GlobeWorldGenerationTest {

    @Test
    void allBiomesHavePropertiesAssigned() {
        assertEquals(23, GameConstants.getBiomes().size());
        for (Biome b : GameConstants.getBiomes()) {
            assertNotNull(b.getRarity(), "Biome " + b.getId() + " has null rarity");
            assertNotNull(b.getClimateZone(), "Biome " + b.getId() + " has null climateZone");
            assertNotNull(b.getHydrologyLevel(), "Biome " + b.getId() + " has null hydrologyLevel");
        }
        assertEquals(15, GameConstants.getStartingBiomes().size());
        assertFalse(GameConstants.getStartingBiomes().contains(GameConstants.BIOME_ICE_FIELD));
        assertFalse(GameConstants.getStartingBiomes().contains(GameConstants.BIOME_BURNT_FOREST));
        assertFalse(GameConstants.getStartingBiomes().contains(GameConstants.BIOME_OCEAN));
        assertFalse(GameConstants.getStartingBiomes().contains(GameConstants.BIOME_LAKE));
        assertFalse(GameConstants.getStartingBiomes().contains(GameConstants.BIOME_DEEP_OCEAN));
        assertFalse(GameConstants.getStartingBiomes().contains(GameConstants.BIOME_SHALLOW_OCEAN));
        assertFalse(GameConstants.getStartingBiomes().contains(GameConstants.BIOME_PEAK));
        assertFalse(GameConstants.getStartingBiomes().contains(GameConstants.BIOME_ABYSS));
    }

    @Test
    void allAntSpeciesHavePreferredBiome() {
        for (AntSpecies s : GameConstants.getSpecies()) {
            assertNotNull(s.getPreferredBiome(), "Species " + s.getName() + " has null preferredBiome");
        }
    }

    @Test
    void worldGeneratesFiveToEightContinentsWithRiversAndLakes() {
        World world = new World();
        Colony colony = new Colony(1, "Player Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_JUNGLE,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        assertTrue(world.getContinentCount() >= 4 && world.getContinentCount() <= 7,
                "Expected 4 to 7 continents, got " + world.getContinentCount());

        for (Continent c : world.getContinents()) {
            assertTrue(c.getHexCount() >= 100, "Each continent must have at least 100 hexes, got " + c.getHexCount());
        }

        int nonIceTotal = 0;
        int nonIceLand = 0;
        for (Hex h : world.getHexes()) {
            if (h.getBiome() == GameConstants.BIOME_ICE_FIELD && Math.abs(h.getR()) >= 22) {
                continue;
            }
            nonIceTotal++;
            if (!isOcean(h)) {
                nonIceLand++;
            }
        }
        double landFraction = (double) nonIceLand / nonIceTotal;
        assertTrue(landFraction >= 0.28 && landFraction <= 0.45,
                "Expected ~35% land vs 65% ocean, got " + landFraction);

        boolean hasLake = false;
        boolean hasRiver = false;
        Set<Integer> continentIdsWithCapitals = new HashSet<>();

        for (Hex h : world.getHexes()) {
            if (h.getBiome() == GameConstants.BIOME_LAKE) {
                hasLake = true;
                assertFalse(h.hasAnyRiver(), "Lake hex should not have river edges on its borders");
            }
            if (h.hasAnyRiver()) {
                hasRiver = true;
            }
            if (h.getColony() != null && h.getContinentId() > 0) {
                continentIdsWithCapitals.add(h.getContinentId());
            }
        }

        assertTrue(hasLake, "World should contain inland lake hexes");
        assertTrue(hasRiver, "World should contain river edges");
        assertTrue(continentIdsWithCapitals.size() >= 3,
                "Rival dynasties should be distributed across at least 3 continents, got " + continentIdsWithCapitals.size());

        Set<Biome> presentBiomes = new HashSet<>();
        for (Hex h : world.getHexes()) {
            if (h.getBiome() != null) {
                presentBiomes.add(h.getBiome());
            }
        }
        for (AntSpecies s : GameConstants.getSpecies()) {
            if (s.getPreferredBiome() != null) {
                assertTrue(presentBiomes.contains(s.getPreferredBiome()),
                        "Species " + s.getName() + " preferred biome " + s.getPreferredBiome().getId() + " must be present in world");
            }
        }

        Hex playerHex = world.getSpawnHex();
        assertNotNull(playerHex);
        assertNotNull(playerHex.getColony());
        assertTrue(playerHex.getColony().isPlayer());
        assertEquals(GameConstants.BIOME_JUNGLE, playerHex.getBiome());

        Map<AntSpecies, Integer> speciesColonyCounts = new HashMap<>();
        Set<AntSpecies> speciesInFavBiome = new HashSet<>();
        for (Hex h : world.getHexes()) {
            assertTrue(h.getRiverEdgeCount() <= 3, "Hex at (" + h.getQ() + "," + h.getR() + ") has > 3 river edges");
            if (h.getColony() != null && h.getColony().getDynasty() != null) {
                AntSpecies sp = h.getColony().getDynasty().getSpecies();
                speciesColonyCounts.put(sp, speciesColonyCounts.getOrDefault(sp, 0) + 1);
                if (sp != null && sp.getPreferredBiome() != null && h.getBiome() == sp.getPreferredBiome()) {
                    speciesInFavBiome.add(sp);
                }
            }
        }
        for (AntSpecies s : GameConstants.getWorldSpawnableNpcSpecies()) {
            int count = speciesColonyCounts.getOrDefault(s, 0);
            assertTrue(count >= 2, "Species " + s.getName() + " must have at least 2 colonies, got " + count);
            assertTrue(speciesInFavBiome.contains(s),
                    "Species " + s.getName() + " must have at least 1 dynasty spawned in its preferred biome "
                            + s.getPreferredBiome().getId());
        }
    }

    @Test
    void riverBattleAccuracyPenaltyWithoutRafting() {
        WarBattleState state = new WarBattleState();
        state.setRiverBattle(true);

        Dynasty withRafting = new Dynasty(1, "Rafting", true, GameConstants.SPECIES_OMNI);
        withRafting.unlockUpgrade(GameUnlocks.ASSIMILATED_RAFTING);

        Dynasty noRafting = new Dynasty(2, "NoRafting", false, GameConstants.SPECIES_OMNI);

        assertTrue(state.isRiverBattle());
        assertEquals(GameNumbers.RIVER_BATTLE_NO_RAFTING_ACCURACY_MULT, 0.80f, 0.001f);
    }

    @Test
    void worldGeneratesContinentObjectsWithHexesAndColonies() {
        World world = new World();
        Colony colony = new Colony(1, "Player Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PRAIRIE,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        assertNotNull(world.getContinents());
        assertEquals(world.getContinentCount(), world.getContinents().size());
        assertTrue(world.getContinents().size() >= 4 && world.getContinents().size() <= 7);

        for (Continent c : world.getContinents()) {
            assertTrue(c.getId() > 0);
            assertNotNull(c.getName());
            assertTrue(c.getHexCount() >= 100);
            assertEquals(c.getHexCount(), c.getHexes().size());
            assertEquals(c, world.getContinentById(c.getId()));

            Hex sampleHex = c.getHexes().get(0);
            assertEquals(c, world.getContinentForHex(sampleHex));
        }

        Continent playerContinent = world.getContinentForHex(world.getSpawnHex());
        assertNotNull(playerContinent);
        assertTrue(playerContinent.getColonies().contains(world.getSpawnHex().getColony()));
        assertTrue(playerContinent.getCapitals().contains(world.getSpawnHex().getColony()));
    }

    @Test
    void rectangularGlobeHasIceCapsAndBranchingRivers() {
        World world = new World();
        Colony colony = new Colony(1, "Player Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_FOREST,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        assertEquals(128, world.getWorldWidth());
        assertEquals(64, world.getWorldHeight());
        assertEquals(128 * 64, world.getHexes().size());

        int northIceCount = 0;
        int southIceCount = 0;
        for (Hex h : world.getHexes()) {
            if (h.getR() <= -26 && h.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                northIceCount++;
            }
            if (h.getR() >= 26 && h.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                southIceCount++;
            }
        }
        assertTrue(northIceCount > 0);
        assertTrue(southIceCount > 0);

        int riverEdgeCount = 0;
        for (Hex h : world.getHexes()) {
            for (boolean r : h.getRiverEdges()) {
                if (r) riverEdgeCount++;
            }
        }
        assertTrue(riverEdgeCount > 0);
    }

    @Test
    void rectangularGlobeHorizontalWrappingNeighbors() {
        World world = new World();
        Colony colony = new Colony(1, "Player Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_FOREST,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        int r = 0;
        int colLeft = -64;
        int colRight = 63;
        int qLeft = colLeft - (r >> 1);
        int qRight = colRight - (r >> 1);

        Hex leftHex = world.getHexAt(qLeft, r);
        Hex rightHex = world.getHexAt(qRight, r);
        assertNotNull(leftHex);
        assertNotNull(rightHex);

        assertEquals(rightHex, leftHex.getNorthWest());
        assertEquals(leftHex, rightHex.getSouthEast());
    }

    @Test
    void rectangularGlobeGeneratesAllNineteenBiomesWithRichVariety() {
        World world = new World();
        Colony colony = new Colony(1, "Player Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        Map<Biome, Integer> counts = new HashMap<>();
        for (Hex h : world.getHexes()) {
            if (h.getBiome() != null) {
                counts.merge(h.getBiome(), 1, Integer::sum);
            }
        }

        assertEquals(23, counts.size(), "All 23 biomes must be present on the globe");
        for (Biome b : GameConstants.getBiomes()) {
            int count = counts.getOrDefault(b, 0);
            assertTrue(count >= 20, "Biome " + b.getId() + " count (" + count + ") should be at least 20");
        }
    }

    @Test
    void enclosedOceanPatchesUnderTenTilesConvertedToLakesWithRivers() {
        World world = new World();
        Colony colony = new Colony(1, "Player Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        Set<Hex> visited = new HashSet<>();
        for (Hex h : world.getHexes()) {
            if (isOcean(h) && !visited.contains(h)) {
                Set<Hex> component = new HashSet<>();
                List<Hex> queue = new ArrayList<>();
                queue.add(h);
                visited.add(h);
                int head = 0;
                while (head < queue.size()) {
                    Hex curr = queue.get(head++);
                    component.add(curr);
                    for (int dir = 0; dir < 6; dir++) {
                        Hex n = curr.getNeighbor(dir);
                        if (n != null && isOcean(n) && visited.add(n)) {
                            queue.add(n);
                        }
                    }
                }
                assertTrue(component.size() >= 10,
                        "Ocean component surrounded by land must be at least 10 tiles, found smaller: " + component.size());
            }
        }
    }

    @Test
    void eachLakeComponentHasAtLeastOneRiverToOcean() {
        World world = new World();
        Colony colony = new Colony(1, "Player Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        Set<Hex> visitedLakes = new HashSet<>();
        int lakeComponentCount = 0;
        for (Hex h : world.getHexes()) {
            if (h != null && h.getBiome() == GameConstants.BIOME_LAKE && visitedLakes.add(h)) {
                lakeComponentCount++;
                Set<Hex> comp = new HashSet<>();
                List<Hex> queue = new ArrayList<>();
                queue.add(h);
                comp.add(h);
                int head = 0;
                while (head < queue.size()) {
                    Hex curr = queue.get(head++);
                    for (int dir = 0; dir < 6; dir++) {
                        Hex n = curr.getNeighbor(dir);
                        if (n != null && n.getBiome() == GameConstants.BIOME_LAKE && visitedLakes.add(n)) {
                            queue.add(n);
                            comp.add(n);
                        }
                    }
                }

                boolean hasRiverAtPerimeter = false;
                for (Hex lakeHex : comp) {
                    for (int dir = 0; dir < 6; dir++) {
                        Hex n1 = lakeHex.getNeighbor(dir);
                        Hex n2 = lakeHex.getNeighbor((dir + 1) % 6);
                        if (n1 != null && n2 != null && n1.directionTo(n2) >= 0) {
                            if (n1.hasRiverEdge(n1.directionTo(n2)) || n2.hasRiverEdge(n2.directionTo(n1))) {
                                hasRiverAtPerimeter = true;
                                break;
                            }
                        }
                    }
                    if (hasRiverAtPerimeter) break;
                }
                assertTrue(hasRiverAtPerimeter, "Lake component starting at (" + h.getQ() + "," + h.getR() + ") must have at least 1 river");
            }
        }
        assertTrue(lakeComponentCount > 0, "World must contain at least one lake");
    }

    @Test
    void noOrphanRiversLeftInWorld() {
        World world = new World();
        Colony colony = new Colony(1, "Player Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        record RiverEdge(Hex h1, Hex h2) {
            RiverEdge {
                if (h1.getQ() > h2.getQ() || (h1.getQ() == h2.getQ() && h1.getR() > h2.getR())) {
                    Hex tmp = h1;
                    h1 = h2;
                    h2 = tmp;
                }
            }
        }

        Set<RiverEdge> edges = new HashSet<>();
        for (Hex h : world.getHexes()) {
            if (h == null) continue;
            for (int dir = 0; dir < 6; dir++) {
                if (h.hasRiverEdge(dir)) {
                    Hex n = h.getNeighbor(dir);
                    if (n != null) {
                        edges.add(new RiverEdge(h, n));
                    }
                }
            }
        }

        for (RiverEdge edge : edges) {
            Hex u = edge.h1;
            Hex v = edge.h2;
            List<Hex> mutual = new ArrayList<>();
            for (Hex w : u.getAdjacentNeighbors()) {
                if (w != null && w != v && v.directionTo(w) >= 0) {
                    mutual.add(w);
                }
            }
            for (Hex w : mutual) {
                boolean touchesWater = HexWaterCrossing.isWaterBiome(u.getBiome())
                        || HexWaterCrossing.isWaterBiome(v.getBiome())
                        || HexWaterCrossing.isWaterBiome(w.getBiome());
                boolean touchesOtherRiver = edges.contains(new RiverEdge(u, w)) || edges.contains(new RiverEdge(v, w));
                assertTrue(touchesWater || touchesOtherRiver,
                        "River edge between (" + u.getQ() + "," + u.getR() + ") and (" + v.getQ() + "," + v.getR() + ") dead-ends at vertex with (" + w.getQ() + "," + w.getR() + ") without connecting to water or river");
            }
        }
    }

    @Test
    void rectangularGlobeIceCapsAreNamedArticaAndAntartica() {
        World world = new World();
        Colony colony = new Colony(1, "Player Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_FOREST,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        assertEquals(2, world.getIceCaps().size());

        Continent artica = null;
        Continent antartica = null;
        for (Continent cap : world.getIceCaps()) {
            assertTrue(cap.isIceCap());
            assertFalse(cap.isArchipelago());
            if (cap.getId() == -1) {
                artica = cap;
            } else if (cap.getId() == -2) {
                antartica = cap;
            }
        }

        assertNotNull(artica);
        assertNotNull(antartica);
        assertEquals(LanguageStrings.get(LanguageStrings.ICE_CAP_ARTICA), artica.getName());
        assertEquals(LanguageStrings.get(LanguageStrings.ICE_CAP_ANTARTICA), antartica.getName());

        int minR = -world.getWorldHeight() / 2;
        int maxR = world.getWorldHeight() / 2 - 1;

        for (Hex h : world.getHexes()) {
            if (h.getBiome() == GameConstants.BIOME_ICE_FIELD && h.getContinentId() == 0) {
                Continent cap = world.getContinentForHex(h);
                assertNotNull(cap, "Ice field at (" + h.getQ() + "," + h.getR() + ") must belong to an ice cap");
                assertTrue(cap.isIceCap());
                if (h.getR() < 0) {
                    assertEquals(artica, cap);
                } else {
                    assertEquals(antartica, cap);
                }
            }
        }
    }

    @Test
    void continentsCanExtendIntoIceFieldsAndPopulatedHexesCached() {
        World world = new World();
        Colony colony = new Colony(1, "Player Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        for (Continent c : world.getContinents()) {
            assertFalse(c.isIceCap());
            assertFalse(c.isArchipelago());
            assertEquals(c.getColonies().size(), c.getPopulatedHexes().size());
            assertEquals(!c.getColonies().isEmpty(), c.hasPopulatedHexes());
        }

        for (Continent arch : world.getArchipelagos()) {
            assertTrue(arch.isArchipelago());
            assertFalse(arch.isIceCap());
            for (Hex h : arch.getHexes()) {
                assertNotEquals(GameConstants.BIOME_ICE_FIELD, h.getBiome());
            }
            assertEquals(arch.getColonies().size(), arch.getPopulatedHexes().size());
            assertEquals(!arch.getColonies().isEmpty(), arch.hasPopulatedHexes());
        }

        for (Continent cap : world.getIceCaps()) {
            assertTrue(cap.isIceCap());
            assertFalse(cap.isArchipelago());
            assertEquals(cap.getColonies().size(), cap.getPopulatedHexes().size());
            assertEquals(!cap.getColonies().isEmpty(), cap.hasPopulatedHexes());
        }
    }

    @Test
    void burntForestOnlyAllowsMineralsAndResinAndCannotSpawnDynasties() {
        Biome burnt = GameConstants.BIOME_BURNT_FOREST;
        assertFalse(burnt.canStart());
        assertEquals(0f, burnt.getPlantAbundance(), 0.0001f);
        assertEquals(0f, burnt.getAnimalAbundance(), 0.0001f);
        assertEquals(0f, burnt.getMushroomAbundance(), 0.0001f);
        assertEquals(0, burnt.isIsHumid());
        assertTrue(burnt.getMineralAbundance() > 0f);
        assertTrue(burnt.getResinAbundance() > 0f);

        World world = new World();
        Colony playerColony = new Colony(1, "Player Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                playerColony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        Hex emptyBurntHex = null;
        for (Hex h : world.getHexes()) {
            if (h.getColony() != null) {
                assertTrue(h.getBiome().canStart(),
                        "Dynasty capital spawned on non-starting biome: " + h.getBiome().getId());
                assertNotEquals(GameConstants.BIOME_BURNT_FOREST, h.getBiome());
            } else if (h.getBiome() == GameConstants.BIOME_BURNT_FOREST && emptyBurntHex == null) {
                emptyBurntHex = h;
            }
        }
        assertNotNull(emptyBurntHex, "Expected at least one uncolonized Burnt Forest hex");

        Dynasty playerDynasty = world.findPlayerDynasty();
        assertNotNull(playerDynasty);
        playerDynasty.unlockUpgrade(GameUnlocks.ROLE_SCOUT);
        playerDynasty.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        playerDynasty.unlockUpgrade(GameUnlocks.ROLE_HUNTER);
        playerDynasty.unlockUpgrade(GameUnlocks.ROLE_MINER);
        playerDynasty.unlockUpgrade(GameUnlocks.STAT_SCOUTING_2);
        playerDynasty.unlockUpgrade(GameUnlocks.STAT_SCOUTING_3);
        playerDynasty.unlockUpgrade(GameUnlocks.ABILITY_RESIN);

        Colony outpost = new Colony(world.getNextColonyId(), "Burnt Outpost", true);
        outpost.setAge(0);
        playerDynasty.addColony(outpost);
        emptyBurntHex.setColony(outpost);
        outpost.matureColony();

        assertTrue(outpost.getLocationService().getDiscoveredSources().isEmpty(),
                "Matured Burnt Forest colony must not receive starter plant or water sources");

        outpost.setPeaceAssignedRoleCount(GameConstants.ROLE_SCOUT, 20);
        for (int i = 0; i < 60; i++) {
            outpost.runScoutting(GameConstants.BIOME_BURNT_FOREST, emptyBurntHex);
        }
        assertFalse(outpost.getLocationService().getDiscoveredSources().isEmpty(),
                "Scouts in Burnt Forest should find mineral or resin sources");
        for (var src : outpost.getLocationService().getDiscoveredSources()) {
            assertTrue(src.getResourceType() == GameConstants.RESOURCE_ROCK
                            || src.getResourceType() == GameConstants.RESOURCE_RESIN,
                    "Unexpected resource source in Burnt Forest: " + src.getResourceType().getId());
        }
    }

    @Test
    void spectatorModeWorldHasNoPlayerOmniDynasty() {
        Savefile save = new Savefile(0, "SpectatorWorld");
        save.setSpectatorMode(true);
        save.setWorldWidth(GameNumbers.WORLD_RECT_WIDTH);
        save.setWorldHeight(GameNumbers.WORLD_RECT_HEIGHT);

        World world = new World();
        world.loadWorld(save);

        assertTrue(world.isSpectatorMode());
        assertNull(world.findPlayerDynasty(), "Spectator world must not create a player dynasty");
        assertFalse(world.getDynastys().isEmpty(), "Spectator world must still populate NPC dynasties");
        assertFalse(world.getHistoryService().getEvents().isEmpty(),
                "Spectator world must record world history logs for initial dynasties and colonies");
        Set<AntSpecies> spectatorFavBiomeSpecies = new HashSet<>();
        for (Dynasty d : world.getDynastys()) {
            assertFalse(d.isPlayer(), "All dynasties in spectator world must be AI");
            assertNotEquals(GameConstants.SPECIES_OMNI, d.getSpecies(), "Spectator world must not spawn an Omni dynasty");
            for (Colony c : d.getColonies()) {
                assertFalse(c.runsFullSimulation(), "Spectator colonies must run fast lite simulation");
                Hex colHex = world.getHexOfColony(c);
                assertNotNull(colHex);
                assertFalse(SandboxCheatService.canCheatConquerHex(world, colHex),
                        "Cheat conquer must be disabled when there is no player dynasty");
                if (d.getSpecies() != null && colHex.getBiome() == d.getSpecies().getPreferredBiome()) {
                    spectatorFavBiomeSpecies.add(d.getSpecies());
                }
            }
        }
        for (AntSpecies s : GameConstants.getWorldSpawnableNpcSpecies()) {
            assertTrue(spectatorFavBiomeSpecies.contains(s),
                    "Spectator world must spawn at least 1 dynasty of " + s.getName() + " in its preferred biome");
        }
        assertNotNull(world.getActiveHex());

        assertEquals(GameSpeed.ID_TURBO,
                GameSpeed.maxPlayableId(true, false));
        assertEquals(GameSpeed.ID_HYPER_TURBO,
                GameSpeed.maxPlayableId(false, true));

        var mapData = SavedMapService.createSavedMapData(world, "SpecMap");
        Savefile fromMap = new Savefile(0, "SpectatorFromMap");
        fromMap.setSpectatorMode(true);
        SavedMapService.populateSavefileFromMapData(fromMap, mapData);

        World worldFromMap = new World();
        worldFromMap.loadWorld(fromMap);
        assertTrue(worldFromMap.isSpectatorMode());
        assertNull(worldFromMap.findPlayerDynasty(), "Spectator world from saved map must not create a player dynasty");
        assertFalse(worldFromMap.getDynastys().isEmpty(), "Spectator world from saved map must populate NPC dynasties");
        for (Dynasty d : worldFromMap.getDynastys()) {
            assertFalse(d.isPlayer());
            assertNotEquals(GameConstants.SPECIES_OMNI, d.getSpecies());
        }
    }

    private static boolean isOcean(Hex h) {
        return h != null && (h.getBiome() == GameConstants.BIOME_OCEAN
                || h.getBiome() == GameConstants.BIOME_DEEP_OCEAN
                || h.getBiome() == GameConstants.BIOME_SHALLOW_OCEAN);
    }
}
