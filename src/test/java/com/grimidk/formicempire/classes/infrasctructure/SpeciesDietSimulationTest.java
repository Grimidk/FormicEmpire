package com.grimidk.formicempire.classes.infrasctructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.critter.ant.SpeciesDiet;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyJobRules;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyAiPriorities;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyStarterService;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.managers.TriggerManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.ResearchTreeGraph;

class SpeciesDietSimulationTest {

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
    void speciesHaveAssignedDietConstants() {
        for (AntSpecies species : GameConstants.getSpecies()) {
            assertNotNull(species.getDiet(), species.getNameKey() + " must have a non-null SpeciesDiet");
            assertTrue(
                    species.getDiet() == GameConstants.DIET_FUNGIVOROUS
                            || species.getDiet() == GameConstants.DIET_CARNIVOROUS,
                    species.getNameKey() + " must have a fungivorous or carnivorous constant diet");
        }
        assertEquals(GameConstants.DIET_FUNGIVOROUS, GameConstants.SPECIES_OMNI.getDiet());
        assertEquals(GameConstants.DIET_FUNGIVOROUS, GameConstants.SPECIES_LEAFCUTTER.getDiet());
        assertEquals(GameConstants.DIET_CARNIVOROUS, GameConstants.SPECIES_ARMY.getDiet());
        assertEquals(GameConstants.DIET_CARNIVOROUS, GameConstants.SPECIES_MARAUDER.getDiet());
        assertEquals(GameConstants.DIET_CARNIVOROUS, GameConstants.SPECIES_TRAPJAW.getDiet());
        assertEquals(GameConstants.DIET_CARNIVOROUS, GameConstants.SPECIES_BULLET.getDiet());
        assertEquals(List.of(GameConstants.DIET_FUNGIVOROUS, GameConstants.DIET_CARNIVOROUS),
                GameConstants.getStartingSpeciesDiets());
    }

    @Test
    void playerStartDietChoiceConfiguresFungivorousOrCarnivorousColony() {
        engine = new Engine();
        engine.setAutosaveFrequency(0);

        Savefile carnSave = new Savefile(9901, "CarnTest");
        carnSave.setStarterDietId(GameConstants.DIET_CARNIVOROUS.getId());
        carnSave.setWorldWidth(8);
        carnSave.setWorldHeight(6);

        World carnWorld = new World();
        carnWorld.setEngine(engine);
        carnWorld.setTargetRivalDynastyCount(0);
        carnWorld.loadWorld(carnSave);

        Colony carnColony = carnWorld.getActiveHex() != null ? carnWorld.getActiveHex().getColony() : null;
        assertNotNull(carnColony);
        Dynasty carnDynasty = carnColony.getDynasty();
        assertNotNull(carnDynasty);
        assertEquals(GameConstants.DIET_CARNIVOROUS, carnDynasty.getStartingDiet());
        assertEquals(GameConstants.DIET_CARNIVOROUS, carnColony.getDiet());
        assertTrue(carnColony.isCarnivorous());
        assertFalse(carnColony.isFungivorous());
        assertTrue(carnColony.canEatMeat());
        assertFalse(carnColony.canEatFungi());

        assertFalse(carnColony.hasUpgrade(GameUnlocks.ROLE_FARMER));
        assertTrue(carnColony.hasUpgrade(GameUnlocks.TYPE_SOLDIER));
        assertTrue(carnColony.hasUpgrade(GameUnlocks.ROLE_HUNTER));
        assertTrue(carnColony.hasUpgrade(GameUnlocks.ROLE_FORAGER));
        assertTrue(carnColony.hasBuilding(GameUnlocks.MEAT_CHAMBER_0));
        assertTrue(carnColony.getProtein() > 0, "Carnivorous starter colony must have starting meat");
        assertEquals(3, carnColony.getAssignedRoleCount(GameConstants.ROLE_HUNTER));
        assertEquals(0, carnColony.getAssignedRoleCount(GameConstants.ROLE_FARMER));
        assertTrue(carnColony.getLocationService().getDiscoveredSources().stream()
                .anyMatch(s -> s.getResourceType() == GameConstants.RESOURCE_MEAT && s.getQuantity() >= 10000),
                "Carnivorous starter colony must have a 10,000 meat source");
    }

    @Test
    void carnivorousColonyEatsMeatAndSyrupsAndDoesNotFarmFungi() {
        Dynasty dynasty = new Dynasty(1, "CarnDynasty", true, GameConstants.SPECIES_ARMY);
        new DynastyStarterService().initializeDynasty(dynasty);
        Colony colony = new Colony(1, "CarnCapital", true);
        dynasty.addColony(colony);
        ColonyStarterService.shared().initializeNewColony(colony);
        ColonyStarterService.shared().matureColony(colony);

        colony.setMushrooms(500);
        colony.setProtein(10);
        colony.setSyrups(200);
        colony.setPlants(300);

        int mushroomsBefore = colony.getMushrooms();
        int proteinBefore = colony.getProtein();
        int syrupsBefore = colony.getSyrups();
        int plantsBefore = colony.getPlants();

        colony.getLabourService().runConverting(colony);
        assertEquals(mushroomsBefore, colony.getMushrooms(),
                "Carnivorous colony without farmers must not convert plants/meat into mushrooms");
        assertEquals(plantsBefore, colony.getPlants(),
                "Carnivorous colony must not consume plants for fungal farming");

        colony.getPopulationService().runEating(colony, null);
        assertEquals(mushroomsBefore, colony.getMushrooms(),
                "Carnivorous colony must not eat fungi");
        assertTrue(colony.getProtein() < proteinBefore || colony.getSyrups() < syrupsBefore,
                "Carnivorous colony must eat meat and syrups");
        assertTrue(colony.getProtein() < proteinBefore, "Carnivorous colony must consume meat");
        assertTrue(colony.getSyrups() < syrupsBefore, "Carnivorous colony must consume syrups when available");
    }

    @Test
    void omnivoreUpgradeUnlocksAfterThreeOppositeDietAssimilations() {
        Dynasty fungDynasty = new Dynasty(1, "FungDynasty", true, GameConstants.SPECIES_OMNI);
        fungDynasty.setStartingDiet(GameConstants.DIET_FUNGIVOROUS);
        new DynastyStarterService().initializeDynasty(fungDynasty);
        Colony fungColony = new Colony(1, "FungCapital", true);
        fungDynasty.addColony(fungColony);
        ColonyStarterService.shared().initializeNewColony(fungColony);

        fungDynasty.unlockUpgrade(GameUnlocks.ABILITY_ASSIMILATION);
        assertFalse(fungDynasty.meetsOmnivorePrerequisites());

        fungDynasty.completeAssimilation(GameConstants.SPECIES_LEAFCUTTER.getAssimilation());
        assertEquals(0, fungDynasty.countOppositeDietAssimilations(),
                "Same-diet assimilation should not count toward opposite-diet requirement");

        fungDynasty.completeAssimilation(GameConstants.SPECIES_ARMY.getAssimilation());
        fungDynasty.completeAssimilation(GameConstants.SPECIES_MARAUDER.getAssimilation());
        assertEquals(2, fungDynasty.countOppositeDietAssimilations());
        assertFalse(fungDynasty.meetsOmnivorePrerequisites());

        fungDynasty.completeAssimilation(GameConstants.SPECIES_TRAPJAW.getAssimilation());
        assertEquals(3, fungDynasty.countOppositeDietAssimilations());
        assertTrue(fungDynasty.meetsOmnivorePrerequisites());
        assertTrue(GameUnlocks.meetsExtraAutomationPrerequisites(fungDynasty, GameUnlocks.ABILITY_OMNIVORE));

        Engine localEngine = new Engine();
        World localWorld = new World();
        TriggerManager triggerManager = new TriggerManager(localWorld, fungColony, localEngine);
        triggerManager.registerListeners();
        localEngine.notifyHourListeners();
        triggerManager.unregisterListeners();

        assertTrue(fungDynasty.hasUpgrade(GameUnlocks.ABILITY_OMNIVORE));
        assertEquals(GameConstants.DIET_OMNIVOROUS, fungDynasty.getDiet());
        assertTrue(fungColony.isOmnivorous());
        assertTrue(fungColony.canEatFungi());
        assertTrue(fungColony.canEatMeat());
        assertTrue(fungColony.hasUpgrade(GameUnlocks.ROLE_FARMER));
        assertTrue(fungColony.hasUpgrade(GameUnlocks.TYPE_SOLDIER));
        assertTrue(fungColony.hasUpgrade(GameUnlocks.ROLE_HUNTER));
        assertTrue(fungColony.hasBuilding(GameUnlocks.MEAT_CHAMBER_0));

        ColonyStarterService.shared().matureColony(fungColony);
        fungColony.setMushrooms(10);
        fungColony.setProtein(500);
        fungColony.setSyrups(100);
        ColonyJobRules.applyDailyEating(fungColony);
        assertEquals(0, fungColony.getMushrooms(), "Omnivorous colony should eat fungi first");
        assertTrue(fungColony.getProtein() < 500, "Omnivorous colony should also eat meat for remaining deficit");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void bothFungivorousAndCarnivorousDynastiesProsperOverOneYear() {
        assertDietWorldProspersOverOneYear(GameConstants.DIET_FUNGIVOROUS, 20260419L);
        assertDietWorldProspersOverOneYear(GameConstants.DIET_CARNIVOROUS, 20260420L);
    }

    private void assertDietWorldProspersOverOneYear(SpeciesDiet playerStartDiet, long seed) {
        GameRandom.getShuffleRandom().setSeed(seed);
        engine = new Engine();
        engine.setAutosaveFrequency(0);

        World world = new World();
        world.setEngine(engine);
        engine.setWorld(world);
        world.setSpectatorMode(false);
        world.setStarterDiet(playerStartDiet);
        world.setTargetRivalDynastyCount(2);
        world.setWorldWidth(16);
        world.setWorldHeight(8);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                null,
                "Player",
                GameConstants.DYNASTY_TITLE_DYNASTY.getNameKey(),
                16,
                8,
                false);
        world.populateDynasties(
                GameConstants.BIOME_PLAINS,
                null,
                "Player",
                GameConstants.DYNASTY_TITLE_DYNASTY.getNameKey());

        List<Dynasty> dynasties = world.getDynastys();
        Dynasty playerDynasty = dynasties.stream().filter(Dynasty::isPlayer).findFirst().orElse(null);
        assertNotNull(playerDynasty);
        assertEquals(playerStartDiet, playerDynasty.getDiet());

        Colony playerCap = playerDynasty.getCapital();
        assertNotNull(playerCap);
        playerCap.setAutomationEnabled(true);
        playerCap.setAutoBuildEnabled(true);

        TriggerManager triggerManager = new TriggerManager(world, playerCap, engine);
        triggerManager.registerListeners();

        for (int i = 0; i < dynasties.size(); i++) {
            Dynasty a = dynasties.get(i);
            Colony cap = a.getCapital();
            Hex capHex = cap != null ? world.getHexOfColony(cap) : null;
            if (capHex != null) {
                for (Hex neighbor : capHex.getAdjacentNeighbors()) {
                    if (neighbor != null && neighbor.getColony() == null) {
                        neighbor.setBiome(GameConstants.BIOME_PLAINS);
                        neighbor.setLocalWeather(GameConstants.WEATHER_CLEAR);
                        neighbor.setIsland(false);
                        break;
                    }
                }
            }
            for (int j = i + 1; j < dynasties.size(); j++) {
                Dynasty b = dynasties.get(j);
                a.setDiplomaticReputation(b.getId(), 85);
                b.setDiplomaticReputation(a.getId(), 85);
                a.getDiplomacyService().applyNonAggressionPact(b);
            }
        }

        int hours = 12 * GameNumbers.DAYS_PER_MONTH * 24;
        for (int h = 0; h < hours; h++) {
            for (int m = 0; m < 60; m++) {
                playerCap.runConverting();
            }
            world.runHour();
            if (world.getHour() == 0) {
                purchaseNextPlayerUpgradeForSim(playerDynasty);
            }
        }
        triggerManager.unregisterListeners();

        for (Dynasty dynasty : dynasties) {
            SpeciesDiet diet = dynasty.getDiet();
            int totalPop = dynasty.getColonies().stream().mapToInt(Colony::getAntTotal).sum();
            assertFalse(dynasty.isDefeated(),
                    dynasty.getName() + " (" + diet.getNameKey() + ") must not be defeated");
            assertTrue(totalPop >= 80,
                    dynasty.getName() + " (" + diet.getNameKey() + ") should prosper to >= 80 ants, got " + totalPop);
            if (diet == GameConstants.DIET_CARNIVOROUS) {
                assertFalse(dynasty.hasUpgrade(GameUnlocks.ROLE_FARMER),
                        "Pure carnivorous dynasty must not have ROLE_FARMER");
                assertTrue(dynasty.hasUpgrade(GameUnlocks.ROLE_HUNTER),
                        "Carnivorous dynasty must have ROLE_HUNTER");
            } else if (diet == GameConstants.DIET_FUNGIVOROUS) {
                assertTrue(dynasty.hasUpgrade(GameUnlocks.ROLE_FARMER),
                        "Fungivorous dynasty must have ROLE_FARMER");
            }
        }
    }

    private static void purchaseNextPlayerUpgradeForSim(Dynasty dynasty) {
        if (dynasty == null) {
            return;
        }
        Colony cap = dynasty.getCapital();
        if (cap != null && !dynasty.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)
                && cap.getAntTotal() > GameNumbers.TRIGGER_NPC_RESEARCHER_MIN_ANTS) {
            dynasty.unlockUpgrade(GameUnlocks.ROLE_RESEARCHER);
        }
        if (dynasty.getResearchPoints() >= GameNumbers.TRIGGER_RESEARCH_MIN_RP
                && !dynasty.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) {
            dynasty.unlockUpgrade(GameUnlocks.ABILITY_RESEARCH);
        }
        if (!dynasty.hasUpgrade(GameUnlocks.ABILITY_RESEARCH)) {
            return;
        }
        for (Upgrade upgrade : DynastyAiPriorities.researchPriority(dynasty)) {
            if (upgrade == null || dynasty.hasUpgrade(upgrade)) {
                continue;
            }
            if (!GameUnlocks.isUpgradeRequirementMet(dynasty, upgrade)) {
                continue;
            }
            if (!GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, upgrade)) {
                continue;
            }
            if (!upgrade.isAvailableFor(dynasty)) {
                continue;
            }
            int cost = upgrade.getCost();
            if (cost <= 0 || dynasty.getResearchPoints() < cost) {
                return;
            }
            dynasty.addResearchPoints(-cost);
            dynasty.unlockUpgrade(upgrade);
            if (upgrade == GameUnlocks.TYPE_SOLDIER
                    || (dynasty.hasUpgrade(GameUnlocks.TYPE_SOLDIER) && !dynasty.hasUpgrade(GameUnlocks.TYPE_MAJOR))) {
                dynasty.unlockUpgrade(GameUnlocks.TYPE_MAJOR);
            }
            if (upgrade == GameUnlocks.TYPE_PRINCESS) {
                dynasty.unlockUpgrade(GameUnlocks.TYPE_QUEEN);
            }
            return;
        }
    }

    @Test
    void carnivoryUpgradesScaleMeatEatingEfficiencyAndUnlockHexSustain() {
        Dynasty dynasty = new Dynasty(1, "Trapjaw", true, GameConstants.SPECIES_TRAPJAW);
        new DynastyStarterService().initializeDynasty(dynasty);
        Colony colony = new Colony(1, "Trapjaw Prime", true);
        dynasty.addColony(colony);
        ColonyStarterService.shared().initializeNewColony(colony);
        ColonyStarterService.shared().matureColony(colony);

        assertEquals(1f, colony.getStatsService().getMeatEatingEfficiency(colony));
        int baseConsumption = colony.getTotalConsumption();
        assertTrue(baseConsumption > 0);
        assertEquals(baseConsumption, colony.getStatsService().getProteinConsumption(colony));

        colony.setProtein(1000);
        colony.setWater(1000);
        ColonyJobRules.applyDailyEating(colony);
        assertEquals(1000 - baseConsumption, colony.getProtein());

        colony.unlockUpgrade(GameUnlocks.STAT_CARNIVORY_1);
        assertEquals(2f, colony.getStatsService().getMeatEatingEfficiency(colony));
        int expectedCarnivory1 = (int) Math.ceil(baseConsumption / 2.0);
        assertEquals(expectedCarnivory1, colony.getStatsService().getProteinConsumption(colony));

        colony.setProtein(1000);
        colony.setWater(1000);
        colony.getPopulationService().runEating(colony, null);
        assertEquals(1000 - expectedCarnivory1, colony.getProtein());

        colony.unlockUpgrade(GameUnlocks.STAT_CARNIVORY_2);
        assertEquals(4f, colony.getStatsService().getMeatEatingEfficiency(colony));
        int expectedCarnivory2 = (int) Math.ceil(baseConsumption / 4.0);
        assertEquals(expectedCarnivory2, colony.getStatsService().getProteinConsumption(colony));

        colony.unlockUpgrade(GameUnlocks.STAT_CARNIVORY_3);
        assertEquals(8f, colony.getStatsService().getMeatEatingEfficiency(colony));
        int expectedCarnivory3 = (int) Math.ceil(baseConsumption / 8.0);
        assertEquals(expectedCarnivory3, colony.getStatsService().getProteinConsumption(colony));

        colony.setProtein(1000);
        colony.setWater(1000);
        ColonyJobRules.applyDailyEating(colony);
        assertEquals(1000 - expectedCarnivory3, colony.getProtein());

        assertFalse(colony.hasUpgrade(GameUnlocks.STAT_FARMING_3));
        assertTrue(GameUnlocks.isUpgradeRequirementMet(colony, GameUnlocks.STAT_HEX_SUSTAIN));
        dynasty.setRank(GameConstants.RANK_SUPER);
        colony.setResearchPoints(GameUnlocks.STAT_HEX_SUSTAIN.getCost());
        assertEquals(ResearchTreeGraph.NodeState.AFFORDABLE,
                ResearchTreeGraph.stateFor(colony, null, GameUnlocks.STAT_HEX_SUSTAIN));
        assertTrue(ResearchTreeGraph.isVisible(colony, null, GameUnlocks.STAT_HEX_SUSTAIN));
    }

    @Test
    void composterRecyclesIntoProteinForPureCarnivoresAndMushroomsForFungivores() {
        Dynasty carnDynasty = new Dynasty(1, "Army", true, GameConstants.SPECIES_ARMY);
        new DynastyStarterService().initializeDynasty(carnDynasty);
        Colony carnColony = new Colony(1, "Army Prime", true);
        carnDynasty.addColony(carnColony);
        ColonyStarterService.shared().initializeNewColony(carnColony);
        ColonyStarterService.shared().matureColony(carnColony);

        carnColony.unlockUpgrade(GameUnlocks.ROLE_GRAVER);
        carnColony.unlockBuilding(GameUnlocks.BUILDING_COMPOSTER);
        carnColony.setAssignedRoleCount(GameConstants.ROLE_GRAVER, 2);
        carnColony.setProtein(100);
        carnColony.setMushrooms(0);
        carnColony.getDeadAnts().add(new Ant(carnColony, GameConstants.CLASS_DEAD));
        carnColony.getDeadAnts().add(new Ant(carnColony, GameConstants.CLASS_DEAD));

        ColonyJobRules.applyDailyComposting(carnColony);
        assertEquals(0, carnColony.getDeadAnts().size());
        assertEquals(108, carnColony.getProtein());
        assertEquals(0, carnColony.getMushrooms());
    }

    @Test
    void dietAndCarnivoryIconsLoadFromClasspath() {
        assertNotNull(GameConstants.DIET_FUNGIVOROUS.getIcon());
        assertNotNull(GameConstants.DIET_CARNIVOROUS.getIcon());
        assertNotNull(GameConstants.DIET_OMNIVOROUS.getIcon());
        assertNotNull(GameUnlocks.ABILITY_OMNIVORE.getIcon());
        assertNotNull(GameUnlocks.STAT_CARNIVORY_1.getIcon());
        assertNotNull(GameUnlocks.STAT_CARNIVORY_2.getIcon());
        assertNotNull(GameUnlocks.STAT_CARNIVORY_3.getIcon());
    }
}
