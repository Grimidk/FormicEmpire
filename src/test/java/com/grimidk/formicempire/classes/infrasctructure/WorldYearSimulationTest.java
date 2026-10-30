package com.grimidk.formicempire.classes.infrasctructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyLabourService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

class WorldYearSimulationTest {

    private static final int MAP_WIDTH = 16;
    private static final int MAP_HEIGHT = 8;
    private static final int RIVAL_DYNASTY_COUNT = 4;
    private static final int SIM_YEARS = 2;
    private static final int SIM_MONTHS = SIM_YEARS * 12;
    private static final int HOURS_PER_DAY = 24;
    private static final int MIN_PEAK_POPULATION = 120;
    private static final int MIN_END_POPULATION = 80;
    private static final double MIN_MONTHLY_COLONY_GROWTH_RATE = 0.75;
    private static final double MIN_DYNASTIES_WITH_SECOND_COLONY = 0.75;

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
        GameRandom.getShuffleRandom().setSeed(20260419L);
        engine = new Engine();
        engine.setAutosaveFrequency(0);

        World world = buildSmallWorld(engine);
        List<Dynasty> dynasties = List.copyOf(world.getDynastys());
        assertEquals(RIVAL_DYNASTY_COUNT, dynasties.size(),
                "Rectangular globe world should spawn the configured number of AI dynasties");
        assertTrue(dynasties.stream().noneMatch(Dynasty::isPlayer),
                "All dynasties in this sim should run as AI (no player-controlled dynasty)");
        assertEquals(RIVAL_DYNASTY_COUNT, world.getColonizedHexes().size(),
                "Colonized hex index must contain all spawned capitals after populateDynasties");

        Map<Integer, Integer> startPop = new HashMap<>();
        Map<Integer, Integer> peakPop = new HashMap<>();
        Map<Integer, Integer> startBuildings = new HashMap<>();
        Map<Integer, Integer> peakBuildings = new HashMap<>();
        Map<Integer, Integer> startUpgrades = new HashMap<>();
        Map<Integer, Long> startMilitaryPower = new HashMap<>();
        Map<Integer, Long> peakMilitaryPower = new HashMap<>();
        Map<Integer, Boolean> sawNuptialReady = new HashMap<>();
        Map<Integer, List<Integer>> monthlyDynastyPop = new HashMap<>();
        Map<Integer, List<Integer>> monthlyColonyPop = new HashMap<>();

        for (Dynasty dynasty : dynasties) {
            assertTrue(dynasty.getAiPersonality() != null,
                    dynasty.getName() + " must have an AI personality");
            int id = dynasty.getId();
            startPop.put(id, population(dynasty));
            peakPop.put(id, population(dynasty));
            startBuildings.put(id, buildingCount(dynasty));
            peakBuildings.put(id, buildingCount(dynasty));
            startUpgrades.put(id, upgradeCount(dynasty));
            long power = dynastyMilitaryPower(dynasty);
            startMilitaryPower.put(id, power);
            peakMilitaryPower.put(id, power);
            sawNuptialReady.put(id, false);
            monthlyDynastyPop.put(id, new ArrayList<>());
            monthlyDynastyPop.get(id).add(population(dynasty));
        }
        snapshotColonyPops(world, monthlyColonyPop);

        int hours = SIM_MONTHS * GameNumbers.DAYS_PER_MONTH * HOURS_PER_DAY;
        int lastMonthIndex = -1;

        try {
            for (int h = 0; h < hours; h++) {
                world.runHour();

                int monthIndex = world.getYear() * 12 + world.getMonth();
                for (Dynasty dynasty : dynasties) {
                    int id = dynasty.getId();
                    peakPop.put(id, Math.max(peakPop.get(id), population(dynasty)));
                    peakBuildings.put(id, Math.max(peakBuildings.get(id), buildingCount(dynasty)));
                    peakMilitaryPower.put(id, Math.max(peakMilitaryPower.get(id), dynastyMilitaryPower(dynasty)));
                    Colony capital = dynasty.getCapital();
                    if ((capital != null && ColonyLabourService.meetsNuptialRequirements(capital))
                            || dynasty.getTotalNuptialFlights() > 0
                            || dynasty.getColonies().size() >= 2) {
                        sawNuptialReady.put(id, true);
                    }
                }

                if (monthIndex != lastMonthIndex && world.getDay() == 1 && world.getHour() == 0) {
                    lastMonthIndex = monthIndex;
                    for (Dynasty dynasty : dynasties) {
                        monthlyDynastyPop.get(dynasty.getId()).add(population(dynasty));
                    }
                    snapshotColonyPops(world, monthlyColonyPop);
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
            assertTrue(endPop > startPop.get(id) * 4,
                    label + " should grow substantially over two years; start=" + startPop.get(id)
                            + " end=" + endPop);

            int endUpgrades = upgradeCount(dynasty);
            assertTrue(endUpgrades >= startUpgrades.get(id) + 8,
                    label + " should research multiple upgrades; start=" + startUpgrades.get(id)
                            + " end=" + endUpgrades);
            assertTrue(dynasty.hasUpgrade(GameUnlocks.ROLE_BUILDER),
                    label + " should unlock ROLE_BUILDER");
            assertTrue(dynasty.hasUpgrade(GameUnlocks.TYPE_SOLDIER),
                    label + " should unlock TYPE_SOLDIER");
            assertTrue(dynasty.hasUpgrade(GameUnlocks.TYPE_MAJOR),
                    label + " should unlock TYPE_MAJOR");
            assertTrue(dynasty.hasUpgrade(GameUnlocks.TYPE_PRINCESS),
                    label + " should unlock TYPE_PRINCESS");
            assertTrue(dynasty.hasUpgrade(GameUnlocks.ROLE_BREEDER),
                    label + " should unlock ROLE_BREEDER");
            assertTrue(dynasty.hasUpgrade(GameUnlocks.ABILITY_SPREAD),
                    label + " should unlock ABILITY_SPREAD");

            assertTrue(peakBuildings.get(id) > startBuildings.get(id),
                    label + " should construct new buildings; start=" + startBuildings.get(id)
                            + " peak=" + peakBuildings.get(id));

            assertTrue(peakMilitaryPower.get(id) > startMilitaryPower.get(id),
                    label + " should become militarily stronger; startPower=" + startMilitaryPower.get(id)
                            + " peakPower=" + peakMilitaryPower.get(id));

            List<Integer> samples = monthlyDynastyPop.get(id);
            assertTrue(samples.size() >= 4, label + " should have monthly samples");
            double earlyAvg = average(samples.subList(0, Math.min(3, samples.size())));
            double lateAvg = average(samples.subList(Math.max(0, samples.size() - 3), samples.size()));
            assertTrue(lateAvg >= earlyAvg * 0.9,
                    label + " late-year population should not collapse vs early; earlyAvg="
                            + earlyAvg + " lateAvg=" + lateAvg + " samples=" + samples);

            assertTrue(Boolean.TRUE.equals(sawNuptialReady.get(id)),
                    label + " capital should become nuptial-ready (drones + breeders) during the sim");
        }

        int expandedCount = countDynastiesWithSecondColony(dynasties);
        assertTrue(expandedCount / (double) dynasties.size() >= MIN_DYNASTIES_WITH_SECOND_COLONY,
                "At least " + (int) (MIN_DYNASTIES_WITH_SECOND_COLONY * 100)
                        + "% of dynasties should expand to a second colony; got "
                        + expandedCount + "/" + dynasties.size());

        int occupied = 0;
        for (Hex hex : world.getHexes()) {
            if (hex.getColony() != null && hex.getColony().getAntTotal() > 0) {
                occupied++;
            }
        }
        assertTrue(occupied > RIVAL_DYNASTY_COUNT,
                "Map should have more living colonies than initial capitals after expansion; occupied=" + occupied);
    }

    private static World buildSmallWorld(Engine engine) {
        World world = new World();
        world.setEngine(engine);
        engine.setWorld(world);
        world.setSpectatorMode(true);
        world.setTargetRivalDynastyCount(RIVAL_DYNASTY_COUNT);
        world.setWorldWidth(MAP_WIDTH);
        world.setWorldHeight(MAP_HEIGHT);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                null,
                "Spectator",
                GameConstants.DYNASTY_TITLE_DYNASTY.getNameKey(),
                MAP_WIDTH,
                MAP_HEIGHT,
                false);
        world.populateDynasties(
                GameConstants.BIOME_PLAINS,
                null,
                "Spectator",
                GameConstants.DYNASTY_TITLE_DYNASTY.getNameKey());

        List<Dynasty> dynasties = world.getDynastys();
        for (int i = 0; i < dynasties.size(); i++) {
            Dynasty a = dynasties.get(i);
            Colony cap = a.getCapital();
            Hex capHex = cap != null ? world.getHexOfColony(cap) : null;
            if (capHex != null) {
                ensureExpandableNeighbor(capHex);
            }
            for (int j = i + 1; j < dynasties.size(); j++) {
                Dynasty b = dynasties.get(j);
                a.setDiplomaticReputation(b.getId(), 85);
                b.setDiplomaticReputation(a.getId(), 85);
                a.getDiplomacyService().applyNonAggressionPact(b);
            }
        }
        return world;
    }

    private static void ensureExpandableNeighbor(Hex capHex) {
        for (Hex neighbor : capHex.getAdjacentNeighbors()) {
            if (neighbor != null && neighbor.getColony() == null
                    && neighbor.getBiome() != null && neighbor.getBiome().canStart()) {
                return;
            }
        }
        for (Hex neighbor : capHex.getAdjacentNeighbors()) {
            if (neighbor != null && neighbor.getColony() == null) {
                neighbor.setBiome(GameConstants.BIOME_PLAINS);
                neighbor.setLocalWeather(GameConstants.WEATHER_CLEAR);
                neighbor.setIsland(false);
                return;
            }
        }
    }

    private static void snapshotColonyPops(World world, Map<Integer, List<Integer>> monthlyColonyPop) {
        for (Hex hex : world.getColonizedHexes()) {
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
            if (samples.size() < 6) {
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
                if (next >= (int) (prev * 0.85)) {
                    grew++;
                    grewTotal++;
                }
            }
            if (pairs == 0) {
                continue;
            }
            coloniesScored++;
            boolean isInitialCapital = samples.size() >= 18;
            boolean pass = (grew / (double) pairs >= MIN_MONTHLY_COLONY_GROWTH_RATE)
                    && (!isInitialCapital || samples.get(samples.size() - 1) > samples.get(0));
            if (pass) {
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

    private static int countDynastiesWithSecondColony(List<Dynasty> dynasties) {
        int count = 0;
        for (Dynasty dynasty : dynasties) {
            if (dynasty != null && !dynasty.isDefeated() && dynasty.getColonies().size() >= 2) {
                count++;
            }
        }
        return count;
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

    private static int upgradeCount(Dynasty dynasty) {
        return dynasty != null && dynasty.getUnlockedUpgrades() != null
                ? dynasty.getUnlockedUpgrades().size()
                : 0;
    }

    private static long dynastyMilitaryPower(Dynasty dynasty) {
        if (dynasty == null) {
            return 0L;
        }
        long total = 0L;
        for (Colony colony : dynasty.getColonies()) {
            if (colony != null) {
                ColonyMilitaryService.refreshColonyMilitaryPower(colony);
                total += colony.getMilitaryPower();
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
