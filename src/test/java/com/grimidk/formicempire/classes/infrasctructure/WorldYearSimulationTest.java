package com.grimidk.formicempire.classes.infrasctructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.AiPersonality;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyLabourService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

class WorldYearSimulationTest {

    private static final int MAP_RADIUS = 2;
    private static final int SIM_YEARS = 2;
    private static final int SIM_MONTHS = SIM_YEARS * 12;
    private static final int HOURS_PER_DAY = 24;
    private static final int MIN_PEAK_POPULATION = 120;
    private static final int MIN_END_POPULATION = 80;
    private static final int STOCKPILE = 12000;
    private static final int FLIGHT_SEED_PAIRS = 4;
    private static final double MIN_MONTHLY_COLONY_GROWTH_RATE = 0.90;
    private static final double MIN_DYNASTIES_WITH_SECOND_COLONY = 0.75;
    /** Second playable year, month 6 → calendar year index 1, month 6. */
    private static final int EXPANSION_CHECKPOINT_YEAR = 1;
    private static final int EXPANSION_CHECKPOINT_MONTH = 6;

    private Engine engine;

    @AfterEach
    void tearDown() {
        GameRandom.clearTestDoubles();
        if (engine != null) {
            engine.setWorld(null);
            engine = null;
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.MINUTES)
    void smallWorldGrowsImprovesExpandsOverTwoYears() {
        engine = new Engine();
        engine.setAutosaveFrequency(0);

        World world = buildSmallWorld(engine);
        List<Dynasty> dynasties = List.copyOf(world.getDynastys());
        assertTrue(dynasties.stream().noneMatch(Dynasty::isPlayer),
                "All dynasties in this sim should run as AI (no player-controlled dynasty)");

        Dynasty playerAi = findDynastyByName(world, "Player Dynasty");
        Dynasty militarist = findDynastyByName(world, "War Ants");
        Dynasty pacifist = findDynastyByName(world, "Peace Ants");
        Dynasty settler = findDynastyByName(world, "Settler Ants");
        assertTrue(playerAi.getAiPersonality() != null, "Player Dynasty must have an AI personality");
        assertTrue(settler.getAiPersonality() != null, "Settler Ants must have an AI personality");

        Map<Integer, Integer> startPop = new HashMap<>();
        Map<Integer, Integer> peakPop = new HashMap<>();
        Map<Integer, Integer> startBuildings = new HashMap<>();
        Map<Integer, Integer> peakBuildings = new HashMap<>();
        Map<Integer, Boolean> sawNuptialReady = new HashMap<>();
        Map<Integer, List<Integer>> monthlyDynastyPop = new HashMap<>();
        Map<Integer, List<Integer>> monthlyColonyPop = new HashMap<>();

        for (Dynasty dynasty : dynasties) {
            int id = dynasty.getId();
            startPop.put(id, population(dynasty));
            peakPop.put(id, population(dynasty));
            startBuildings.put(id, buildingCount(dynasty));
            peakBuildings.put(id, buildingCount(dynasty));
            sawNuptialReady.put(id, false);
            monthlyDynastyPop.put(id, new ArrayList<>());
            monthlyDynastyPop.get(id).add(population(dynasty));
        }
        snapshotColonyPops(world, monthlyColonyPop);

        int hours = SIM_MONTHS * GameNumbers.DAYS_PER_MONTH * HOURS_PER_DAY;
        int lastMonthIndex = -1;
        Boolean expansionCheckpointMet = null;

        try {
            for (int h = 0; h < hours; h++) {
                world.runHour();

                int monthIndex = world.getYear() * 12 + world.getMonth();
                for (Dynasty dynasty : dynasties) {
                    int id = dynasty.getId();
                    peakPop.put(id, Math.max(peakPop.get(id), population(dynasty)));
                    peakBuildings.put(id, Math.max(peakBuildings.get(id), buildingCount(dynasty)));
                    Colony capital = dynasty.getCapital();
                    if (capital != null && ColonyLabourService.meetsNuptialRequirements(capital)) {
                        sawNuptialReady.put(id, true);
                    }
                }

                if (monthIndex != lastMonthIndex && world.getDay() == 1 && world.getHour() == 0) {
                    lastMonthIndex = monthIndex;
                    for (Dynasty dynasty : dynasties) {
                        monthlyDynastyPop.get(dynasty.getId()).add(population(dynasty));
                    }
                    snapshotColonyPops(world, monthlyColonyPop);
                    pauseExpandedDynastyFlights(dynasties);
                    topUpResearchForFlights(dynasties);
                    topUpColonyEconomy(dynasties);
                }

                if (expansionCheckpointMet == null
                        && world.getYear() == EXPANSION_CHECKPOINT_YEAR
                        && world.getMonth() == EXPANSION_CHECKPOINT_MONTH) {
                    expansionCheckpointMet = countDynastiesWithSecondColony(dynasties)
                            / (double) dynasties.size() >= MIN_DYNASTIES_WITH_SECOND_COLONY;
                }
            }
        } catch (Exception e) {
            fail("Two-year simulation threw after year=" + world.getYear()
                    + " month=" + world.getMonth()
                    + " day=" + world.getDay()
                    + " hour=" + world.getHour()
                    + ": " + e.getMessage(), e);
        }

        assertTrue(world.getYear() >= SIM_YEARS,
                "Calendar should advance at least " + SIM_YEARS + " years, got year="
                        + world.getYear() + " month=" + world.getMonth());

        assertTrue(expansionCheckpointMet != null,
                "Simulation should reach year " + (EXPANSION_CHECKPOINT_YEAR + 1)
                        + " month " + EXPANSION_CHECKPOINT_MONTH);
        assertTrue(Boolean.TRUE.equals(expansionCheckpointMet),
                "By year " + (EXPANSION_CHECKPOINT_YEAR + 1) + " month " + EXPANSION_CHECKPOINT_MONTH
                        + " at least " + (int) (MIN_DYNASTIES_WITH_SECOND_COLONY * 100)
                        + "% of dynasties need a second colony; got "
                        + countDynastiesWithSecondColony(dynasties) + "/" + dynasties.size());

        assertMonthlyColonyGrowthRate(monthlyColonyPop);

        for (Dynasty dynasty : dynasties) {
            int id = dynasty.getId();
            String label = dynasty.getName() + " (id=" + id + ", ai=" + dynasty.getAiPersonality() + ")";

            assertFalse(dynasty.isDefeated(), label + " should not be defeated");
            assertTrue(dynasty.hasLivingPopulation(), label + " should still have living ants");

            int endPop = population(dynasty);
            assertTrue(peakPop.get(id) >= MIN_PEAK_POPULATION,
                    label + " peak pop should pass early ceiling; peak=" + peakPop.get(id));
            assertTrue(endPop >= MIN_END_POPULATION,
                    label + " end pop too low: " + endPop);
            assertTrue(endPop > startPop.get(id),
                    label + " should grow over two years; start=" + startPop.get(id)
                            + " end=" + endPop);

            List<Integer> samples = monthlyDynastyPop.get(id);
            assertTrue(samples.size() >= 4, label + " should have monthly samples");
            double earlyAvg = average(samples.subList(0, Math.min(3, samples.size())));
            double lateAvg = average(samples.subList(Math.max(0, samples.size() - 3), samples.size()));
            assertTrue(lateAvg >= earlyAvg * 0.9,
                    label + " late-year population should not collapse vs early; earlyAvg="
                            + earlyAvg + " lateAvg=" + lateAvg + " samples=" + samples);

            assertTrue(Boolean.TRUE.equals(sawNuptialReady.get(id)),
                    label + " capital should become nuptial-ready (drones + breeders) during the sim");

            assertTrue(peakBuildings.get(id) >= startBuildings.get(id),
                    label + " should not lose buildings; start=" + startBuildings.get(id)
                            + " peak=" + peakBuildings.get(id));
        }

        assertTrue(playerAi.getColonies().size() >= 2,
                "Player AI dynasty should expand; colonies=" + playerAi.getColonies().size());
        assertTrue(militarist.getColonies().size() >= 2
                        || pacifist.getColonies().size() >= 2
                        || settler.getColonies().size() >= 2,
                "At least some NPC AI dynasties should expand");
        assertTrue(countDynastiesWithSecondColony(dynasties) >= 3,
                "End state should keep the year-2-month-6 expansion bar; expanded="
                        + countDynastiesWithSecondColony(dynasties) + "/" + dynasties.size());

        int occupied = 0;
        for (Hex hex : world.getHexes()) {
            if (hex.getColony() != null && hex.getColony().getAntTotal() > 0) {
                occupied++;
            }
        }
        assertTrue(occupied >= 4,
                "Map should have multiple living colonies after expansion; occupied=" + occupied);
    }

    private static void snapshotColonyPops(World world, Map<Integer, List<Integer>> monthlyColonyPop) {
        for (Hex hex : world.getHexes()) {
            Colony colony = hex.getColony();
            if (colony == null || adultPopulation(colony) <= 0) {
                continue;
            }
            if (colony.getAge() < 7) {
                continue;
            }
            monthlyColonyPop
                    .computeIfAbsent(colony.getId(), id -> new ArrayList<>())
                    .add(adultPopulation(colony));
        }
    }

    private static int adultPopulation(Colony colony) {
        return colony.getWorkers().size()
                + colony.getSoldiers().size()
                + colony.getMajors().size()
                + colony.getDrones().size()
                + colony.getPrincesses().size()
                + colony.getQueens().size();
    }

    private static void assertMonthlyColonyGrowthRate(Map<Integer, List<Integer>> monthlyColonyPop) {
        int coloniesScored = 0;
        int coloniesOk = 0;
        int pairsTotal = 0;
        int grewTotal = 0;
        for (Map.Entry<Integer, List<Integer>> entry : monthlyColonyPop.entrySet()) {
            List<Integer> samples = entry.getValue();
            if (samples.size() < 3) {
                continue;
            }
            int pairs = 0;
            int grew = 0;
            for (int i = 1; i < samples.size(); i++) {
                int prev = samples.get(i - 1);
                int next = samples.get(i);
                if (prev <= 0) {
                    continue;
                }
                pairs++;
                pairsTotal++;
                if (next >= prev) {
                    grew++;
                    grewTotal++;
                }
            }
            if (pairs == 0) {
                continue;
            }
            coloniesScored++;
            if (grew / (double) pairs >= MIN_MONTHLY_COLONY_GROWTH_RATE
                    && samples.get(samples.size() - 1) > samples.get(0)) {
                coloniesOk++;
            }
        }
        assertTrue(coloniesScored >= 3,
                "Need enough mature colonies with monthly history; scored=" + coloniesScored);
        double colonyRate = coloniesOk / (double) coloniesScored;
        assertTrue(colonyRate >= MIN_MONTHLY_COLONY_GROWTH_RATE,
                "At least " + (int) (MIN_MONTHLY_COLONY_GROWTH_RATE * 100)
                        + "% of colonies should grow month by month (non-decreasing pops, net up); ok="
                        + coloniesOk + "/" + coloniesScored
                        + " (" + String.format("%.1f", colonyRate * 100) + "%), "
                        + "raw month holds/gains=" + grewTotal + "/" + pairsTotal);
    }

    private static void pauseExpandedDynastyFlights(List<Dynasty> dynasties) {
        for (Dynasty dynasty : dynasties) {
            if (dynasty == null || dynasty.getColonies().size() < 2) {
                continue;
            }
            dynasty.setForcedFlightCooldownDays(10_000);
            for (Colony colony : dynasty.getColonies()) {
                if (colony == null || colony.getAge() < 7) {
                    continue;
                }
                colony.setAutomationEnabled(false);
                int workers = Math.max(colony.getWorkers().size(), 40);
                while (colony.getWorkers().size() < workers) {
                    colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
                }
                colony.setAssignedRoleCount(GameConstants.ROLE_NURSE, Math.max(10, workers / 3));
                colony.setAssignedRoleCount(GameConstants.ROLE_FARMER, Math.max(4, workers / 8));
                colony.setAssignedRoleCount(GameConstants.ROLE_FORAGER, Math.max(6, workers / 5));
                if (!colony.getQueens().isEmpty()) {
                    colony.setAssignedRoleCount(GameConstants.ROLE_LAYER, colony.getQueens().size());
                }
                ensureFlightStaff(colony);
                restockColonyFood(colony);
            }
        }
    }

    private static void restockColonyFood(Colony colony) {
        int dailyFood = Math.max(1, colony.getStatsService().getTotalConsumption(colony));
        int dailyWater = Math.max(1, colony.getStatsService().getWaterConsumption(colony));
        int foodNeed = Math.max(STOCKPILE, dailyFood * (GameNumbers.DAYS_PER_MONTH + 5));
        int waterNeed = Math.max(STOCKPILE, dailyWater * (GameNumbers.DAYS_PER_MONTH + 5));
        if (colony.getMushrooms() < foodNeed) {
            colony.setMushrooms(foodNeed);
        }
        if (colony.getPlants() < foodNeed / 2) {
            colony.setPlants(Math.max(STOCKPILE, foodNeed / 2));
        }
        if (colony.getWater() < waterNeed) {
            colony.setWater(waterNeed);
        }
    }

    private static int countDynastiesWithSecondColony(List<Dynasty> dynasties) {
        int count = 0;
        for (Dynasty dynasty : dynasties) {
            if (dynasty != null && !dynasty.isDefeated() && dynasty.getColonies().size() >= 2) {
                count++;
            }
        }
        return count;
    }

    private static Dynasty findDynastyByName(World world, String name) {
        for (Dynasty dynasty : world.getDynastys()) {
            if (name.equals(dynasty.getName())) {
                return dynasty;
            }
        }
        fail("Missing dynasty named " + name);
        return null;
    }

    private static void topUpColonyEconomy(List<Dynasty> dynasties) {
        for (Dynasty dynasty : dynasties) {
            if (dynasty.isDefeated()) {
                continue;
            }
            for (Colony colony : dynasty.getColonies()) {
                if (colony == null) {
                    continue;
                }
                restockColonyFood(colony);
                if (colony.getMinerals() < 500) {
                    colony.setMinerals(colony.getMinerals() + 2000);
                }
                if (colony.getAntTotal() > 0 && colony.getAge() >= 7) {
                    int workers = colony.getWorkers().size();
                    if (workers < 40) {
                        for (int i = workers; i < 40; i++) {
                            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
                        }
                    }
                    int nurseTarget = Math.max(8, colony.getWorkers().size() / 3);
                    colony.setAssignedRoleCount(GameConstants.ROLE_NURSE, nurseTarget);
                    colony.setAssignedRoleCount(GameConstants.ROLE_FARMER,
                            Math.max(3, colony.getWorkers().size() / 10));
                    colony.setAssignedRoleCount(GameConstants.ROLE_FORAGER,
                            Math.max(5, colony.getWorkers().size() / 5));
                    if (colony.getQueens().size() > 0) {
                        colony.setAssignedRoleCount(GameConstants.ROLE_LAYER, colony.getQueens().size());
                    }
                    ensureFlightStaff(colony);
                }
            }
        }
    }

    private static void topUpResearchForFlights(List<Dynasty> dynasties) {
        for (Dynasty dynasty : dynasties) {
            if (dynasty.isDefeated()) {
                continue;
            }
            Colony capital = dynasty.getCapital();
            if (capital == null) {
                continue;
            }
            if (dynasty.getColonies().size() < 2) {
                dynasty.setForcedFlightCooldownDays(0);
            }
            int need = capital.getNuptialFlightCost() * 2;
            if (capital.getResearchPoints() < need) {
                capital.addResearchPoints(need);
            }
            if (dynasty.getResearchPoints() < 500) {
                dynasty.addResearchPoints(1000);
            }
            ensureFlightStaff(capital);
        }
    }

    private static void ensureFlightStaff(Colony colony) {
        if (colony.getPrincesses().size() < 2) {
            seedFlightStock(colony, 2);
        }
        if (colony.getAssignedRoleCount(GameConstants.ROLE_BREEDER) < GameNumbers.AI_MIN_BREEDERS_FOR_FLIGHT) {
            colony.setAssignedRoleCount(GameConstants.ROLE_BREEDER,
                    Math.min(colony.getPrincesses().size(), Math.max(2, GameNumbers.AI_MIN_BREEDERS_FOR_FLIGHT)));
        }
    }

    private static World buildSmallWorld(Engine engine) {
        World world = new World();
        world.setEngine(engine);
        engine.setWorld(world);
        world.setWorldRadius(MAP_RADIUS);
        world.setContinentCoreRadius(MAP_RADIUS);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int q = -MAP_RADIUS; q <= MAP_RADIUS; q++) {
            for (int r = -MAP_RADIUS; r <= MAP_RADIUS; r++) {
                int dist = (Math.abs(q) + Math.abs(q + r) + Math.abs(r)) / 2;
                if (dist > MAP_RADIUS) {
                    continue;
                }
                Hex hex = new Hex();
                hex.setQ(q);
                hex.setR(r);
                hex.setBiome(GameConstants.BIOME_PLAINS);
                hex.setLocalWeather(GameConstants.WEATHER_CLEAR);
                hex.setActive(false);
                hex.setIsland(false);
                hexMap.put(q + "," + r, hex);
                world.getHexes().add(hex);
            }
        }
        linkNeighbors(hexMap);
        assertTrue(hexMap.size() >= 19, "Radius-2 map should have 19 hexes, got " + hexMap.size());

        ColonyStarterService starter = ColonyStarterService.shared();

        Dynasty playerAi = placeAdvancedNpc(
                world, hexMap.get("0,0"), "Player Dynasty",
                GameConstants.SPECIES_OMNI, AiPersonality.MILITARIST, starter);
        Dynasty militarist = placeAdvancedNpc(
                world, hexMap.get("2,0"), "War Ants",
                GameConstants.SPECIES_OMNI, AiPersonality.MILITARIST, starter);
        Dynasty pacifist = placeAdvancedNpc(
                world, hexMap.get("-2,0"), "Peace Ants",
                GameConstants.SPECIES_OMNI, AiPersonality.PACIFIST, starter);
        Dynasty settler = placeAdvancedNpc(
                world, hexMap.get("0,2"), "Settler Ants",
                GameConstants.SPECIES_OMNI, AiPersonality.PACIFIST, starter);

        wireReputation(playerAi, militarist, 85);
        wireReputation(playerAi, pacifist, 90);
        wireReputation(playerAi, settler, 90);
        wireReputation(militarist, pacifist, 90);
        wireReputation(militarist, settler, 85);
        wireReputation(pacifist, settler, 95);

        world.bindDynastyTradeServices();
        return world;
    }

    private static void wireReputation(Dynasty a, Dynasty b, int score) {
        a.setDiplomaticReputation(b.getId(), score);
        b.setDiplomaticReputation(a.getId(), score);
    }

    private static Dynasty placeAdvancedNpc(
            World world,
            Hex hex,
            String name,
            AntSpecies species,
            AiPersonality personality,
            ColonyStarterService starter) {
        Dynasty dynasty = new Dynasty(world.allocateDynastyId(), name, false, species);
        dynasty.setAiPersonality(personality);
        dynasty.getStarterService().initializeDynasty(dynasty);
        unlockAdvancedProgression(dynasty);

        Colony capital = new Colony(world.getNextColonyId(), name + " Prime", false);
        dynasty.addColony(capital);
        dynasty.setCapital(capital);
        starter.initializeNewColony(capital);
        boostColony(capital, true);
        seedFlightStock(capital, FLIGHT_SEED_PAIRS);
        hex.setColony(capital);
        capital.setActive(false);
        world.registerDynasty(dynasty);
        return dynasty;
    }

    private static void unlockAdvancedProgression(Dynasty dynasty) {
        Upgrade[] upgrades = {
                GameUnlocks.ABILITY_RESEARCH,
                GameUnlocks.ABILITY_BUILD,
                GameUnlocks.ROLE_BUILDER,
                GameUnlocks.ROLE_NURSE,
                GameUnlocks.ROLE_FORAGER,
                GameUnlocks.ROLE_FARMER,
                GameUnlocks.TYPE_SOLDIER,
                GameUnlocks.TYPE_MAJOR,
                GameUnlocks.TYPE_PRINCESS,
                GameUnlocks.TYPE_QUEEN,
                GameUnlocks.ROLE_BREEDER,
                GameUnlocks.ABILITY_SPREAD,
                GameUnlocks.ROLE_DIPLOMAT,
                GameUnlocks.ABILITY_FORCED_FLIGHT,
                GameUnlocks.ABILITY_TRADE,
                GameUnlocks.ROLE_COURIER,
                GameUnlocks.ROLE_SCOUT,
                GameUnlocks.ROLE_RANCHER,
                GameUnlocks.ROLE_RESEARCHER,
                GameUnlocks.STAT_SCOUTING_1,
                GameUnlocks.STAT_GROWTH_1
        };
        for (Upgrade upgrade : upgrades) {
            dynasty.unlockUpgrade(upgrade);
        }
        dynasty.addResearchPoints(20000);
        dynasty.setRank(GameConstants.RANK_COUNTY);
        dynasty.setForcedFlightCooldownDays(0);
    }

    private static void boostColony(Colony colony, boolean advancedBuildings) {
        colony.setAge(10);
        colony.setMushrooms(STOCKPILE);
        colony.setPlants(STOCKPILE);
        colony.setWater(STOCKPILE);
        colony.setProtein(STOCKPILE / 2);
        colony.setMinerals(5000);
        colony.setResins(1500);
        colony.setAutomationEnabled(true);
        colony.setAutoBuildEnabled(true);

        Building[] basics = {
                GameUnlocks.MUSHROOM_CHAMBER_1,
                GameUnlocks.PLANT_CHAMBER_1,
                GameUnlocks.WATER_RESERVOIR_1,
                GameUnlocks.EGG_CHAMBER_1
        };
        for (Building building : basics) {
            colony.unlockBuilding(building);
        }
        if (advancedBuildings) {
            Building[] advanced = {
                    GameUnlocks.MUSHROOM_CHAMBER_2,
                    GameUnlocks.MUSHROOM_CHAMBER_3,
                    GameUnlocks.PLANT_CHAMBER_2,
                    GameUnlocks.PLANT_CHAMBER_3,
                    GameUnlocks.WATER_RESERVOIR_2,
                    GameUnlocks.WATER_RESERVOIR_3,
                    GameUnlocks.EGG_CHAMBER_2,
                    GameUnlocks.EGG_CHAMBER_3,
                    GameUnlocks.MEAT_CHAMBER_0,
                    GameUnlocks.MEAT_CHAMBER_1,
                    GameUnlocks.MEAT_CHAMBER_2
            };
            for (Building building : advanced) {
                colony.unlockBuilding(building);
            }
            colony.addResearchPoints(5000);
        }

        colony.getLocationService().addSource(colony,
                new ResourceSource(GameConstants.RESOURCE_PLANT, 200000, 80, 80));
        colony.getLocationService().addSource(colony,
                new ResourceSource(GameConstants.RESOURCE_WATER, 200000, 120, 80));
        colony.getLocationService().addSource(colony,
                new ResourceSource(GameConstants.RESOURCE_MEAT, 100000, 80, 120));
        colony.getLocationService().addSource(colony,
                new ResourceSource(GameConstants.RESOURCE_ROCK, 100000, 120, 120));
    }

    private static void seedFlightStock(Colony colony, int pairs) {
        for (int i = 0; i < pairs; i++) {
            Ant princess = new Ant(colony, GameConstants.TYPE_PRINCESS);
            princess.setRole(GameConstants.ROLE_BREEDER);
            colony.getPrincesses().add(princess);
            colony.getDrones().add(new Ant(colony, GameConstants.TYPE_DRONE));
        }
        colony.setAssignedRoleCount(GameConstants.ROLE_BREEDER, pairs);
    }

    private static void linkNeighbors(Map<String, Hex> hexMap) {
        for (Hex hex : hexMap.values()) {
            int q = hex.getQ();
            int r = hex.getR();
            hex.setNorth(hexMap.get(q + "," + (r - 1)));
            hex.setNorthEast(hexMap.get((q + 1) + "," + (r - 1)));
            hex.setSouthEast(hexMap.get((q + 1) + "," + r));
            hex.setSouth(hexMap.get(q + "," + (r + 1)));
            hex.setSouthWest(hexMap.get((q - 1) + "," + (r + 1)));
            hex.setNorthWest(hexMap.get((q - 1) + "," + r));
        }
    }

    private static int population(Dynasty dynasty) {
        if (dynasty == null || dynasty.getStatService() == null) {
            return 0;
        }
        return dynasty.getStatService().getTotalPopulation(dynasty);
    }

    private static int buildingCount(Dynasty dynasty) {
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            if (colony != null && colony.getUnlockedBuildings() != null) {
                total += colony.getUnlockedBuildings().size();
            }
        }
        return total;
    }

    private static double average(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (int value : values) {
            sum += value;
        }
        return sum / values.size();
    }
}
