package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyIntegrationServiceTest {

    private World world;
    private TradeManager tradeManager;
    private Dynasty overlord;
    private Dynasty target;

    @BeforeEach
    void setUp() {
        world = new World();
        tradeManager = new TradeManager();
        overlord = new Dynasty(1, "Overlord", true, GameConstants.SPECIES_OMNI);
        target = new Dynasty(2, "Target", false, GameConstants.SPECIES_LEAFCUTTER);
        world.getDynastys().add(overlord);
        world.getDynastys().add(target);

        Colony overlordColony = new Colony(1, "Capital", true);
        Colony targetColony = new Colony(2, "Neighbor", false);
        overlord.addColony(overlordColony);
        target.addColony(targetColony);
        overlord.setCapital(overlordColony);
        overlordColony.getWorkers().add(new Ant(overlordColony, GameConstants.TYPE_WORKER));
        targetColony.getWorkers().add(new Ant(targetColony, GameConstants.TYPE_WORKER));

        overlordColony.setAge(7);
        overlord.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        overlordColony.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 1);

        overlord.setMilitaryPower(500);
        target.setMilitaryPower(50);
        overlord.setDiplomaticReputation(target.getId(), GameConstants.REPUTATION_FRIENDLY.getMinScore());
        overlord.getDiplomacyService().formNonAggressionPact(target, world);
    }

    @Test
    void computeIntegrationMonthsPerColonyScalesWithDiplomats() {
        assertEquals(30.0, GameNumbers.computeIntegrationMonthsPerColony(1.0), 0.001);
        assertEquals(3.0, GameNumbers.computeIntegrationMonthsPerColony(100.0), 0.001);
        assertEquals(0.3, GameNumbers.computeIntegrationMonthsPerColony(10000.0), 0.001);
        assertTrue(GameNumbers.computeIntegrationMonthsPerColony(50.0) < 30.0);
        assertTrue(GameNumbers.computeIntegrationMonthsPerColony(200.0)
                < GameNumbers.computeIntegrationMonthsPerColony(100.0));
    }

    @Test
    void enoughDiplomatsCanCompleteIntegrationInOneDailyTick() {
        Hex overlordHex = new Hex();
        Hex targetHex = new Hex();
        overlordHex.setQ(0);
        overlordHex.setR(0);
        targetHex.setQ(1);
        targetHex.setR(0);
        overlordHex.setNorthEast(targetHex);
        targetHex.setSouthWest(overlordHex);
        overlordHex.setColony(overlord.getCapital());
        targetHex.setColony(target.getColonies().get(0));
        world.setHexes(new ArrayList<>(List.of(overlordHex, targetHex)));

        overlord.setIntegrationTargetDynastyId(target.getId());
        overlord.getColonies().get(0).setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 1_000_000);
        DynastyIntegrationService.assignIntegrationDiplomats(overlord, 1_000_000, true, world, tradeManager);
        assertTrue(DynastyIntegrationService.computeTotalIntegrationDays(overlord, target) < 1.0);

        DynastyIntegrationService.tickIntegrationsDaily(world, tradeManager);

        assertFalse(overlord.hasActiveIntegration());
        assertTrue(target.isDefeated());
    }

    @Test
    void meetsMilitaryRequirementNeedsFiveTimesPower() {
        assertTrue(DynastyIntegrationService.meetsMilitaryRequirement(overlord, target));
        target.setMilitaryPower(200);
        assertFalse(DynastyIntegrationService.meetsMilitaryRequirement(overlord, target));
    }

    @Test
    void totalMonthsScaleWithColonyCountAndDiplomats() {
        overlord.getColonies().get(0).setIntegrationDiplomatsDeployed(1);
        double oneDiplomat = DynastyIntegrationService.computeTotalIntegrationMonths(overlord, target);

        target.addColony(new Colony(3, "Far", false));
        double twoColoniesOneDiplomat = DynastyIntegrationService.computeTotalIntegrationMonths(overlord, target);
        assertTrue(twoColoniesOneDiplomat > oneDiplomat);

        overlord.getColonies().get(0).setIntegrationDiplomatsDeployed(100);
        double fast = DynastyIntegrationService.computeTotalIntegrationMonths(overlord, target);
        assertTrue(fast < twoColoniesOneDiplomat);
    }

    @Test
    void integratedColonyInheritsOverlordResearchAssimilationsAndBuildings() {
        Colony overlordColony = overlord.getColonies().get(0);
        Colony targetColony = target.getColonies().get(0);

        overlord.unlockUpgrade(GameUnlocks.ROLE_WARRIOR);
        overlord.completeAssimilation(GameUnlocks.ASSIMILATION_MARAUDER);
        overlordColony.unlockBuilding(GameUnlocks.PASSIVE_LAB);
        overlordColony.setHatchRateSoldier(0.25f);

        targetColony.setDynasty(overlord);
        targetColony.setNativeSpeciesId(GameConstants.SPECIES_LEAFCUTTER.getId());
        ColonyStarterService.shared().inheritIntegratedColonyFromOverlord(overlord, targetColony);

        assertTrue(targetColony.hasUpgrade(GameUnlocks.ROLE_WARRIOR));
        assertTrue(targetColony.getDynasty().isAssimilationCompleted(GameUnlocks.ASSIMILATION_MARAUDER));
        assertTrue(targetColony.hasBuilding(GameUnlocks.PASSIVE_LAB));
        assertEquals(0.25f, targetColony.getHatchRateSoldier(), 0.001f);
    }

    @Test
    void integrationProgressAdvancesDailyAndEtaUsesWorldDate() {
        Hex overlordHex = new Hex();
        Hex targetHex = new Hex();
        overlordHex.setQ(0);
        overlordHex.setR(0);
        targetHex.setQ(1);
        targetHex.setR(0);
        overlordHex.setNorthEast(targetHex);
        targetHex.setSouthWest(overlordHex);
        overlordHex.setColony(overlord.getCapital());
        targetHex.setColony(target.getColonies().get(0));
        world.setHexes(new ArrayList<>(List.of(overlordHex, targetHex)));
        world.setYear(1);
        world.setMonth(1);
        world.setDay(1);

        overlord.setIntegrationTargetDynastyId(target.getId());
        overlord.getColonies().get(0).setIntegrationDiplomatsDeployed(1);
        double totalDays = DynastyIntegrationService.computeTotalIntegrationDays(overlord, target);

        assertEquals(0, overlord.getIntegrationProgressDays(), 0.001);
        DynastyIntegrationService.tickIntegrationsDaily(world, tradeManager);
        assertEquals(1, overlord.getIntegrationProgressDays(), 0.001);
        assertEquals(
                (int) Math.round((1.0 / totalDays) * 100.0),
                (int) Math.round(DynastyIntegrationService.getIntegrationProgressPercent(overlord, target)));

        String eta = DynastyIntegrationService.getIntegrationEstimatedCompletionDate(world, overlord, target);
        int daysRemaining = DynastyIntegrationService.getIntegrationDaysRemaining(overlord, target);
        assertEquals(
                DynastyIntegrationService.formatWorldDate(DynastyIntegrationService.worldDayIndex(world) + daysRemaining),
                eta);
    }

    @Test
    void completingIntegrationQueuesPlayerPopupAlert() {
        Hex overlordHex = new Hex();
        Hex targetHex = new Hex();
        overlordHex.setQ(0);
        overlordHex.setR(0);
        targetHex.setQ(1);
        targetHex.setR(0);
        overlordHex.setNorthEast(targetHex);
        targetHex.setSouthWest(overlordHex);
        overlordHex.setColony(overlord.getCapital());
        targetHex.setColony(target.getColonies().get(0));
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(overlordHex);
        hexes.add(targetHex);
        world.setHexes(hexes);

        overlord.setIntegrationTargetDynastyId(target.getId());
        overlord.getColonies().get(0).setIntegrationDiplomatsDeployed(1);
        double total = DynastyIntegrationService.computeTotalIntegrationDays(overlord, target);
        overlord.setIntegrationProgressDays(total - 1);

        DynastyIntegrationService.tickIntegrationsDaily(world, tradeManager);

        assertFalse(overlord.hasActiveIntegration());
        assertTrue(target.isDefeated());
        assertEquals(1, overlord.copyPendingIntegrationCompletedTargetIds().size());
        assertEquals(target.getId(), overlord.copyPendingIntegrationCompletedTargetIds().get(0));
    }

    @Test
    void warOnIntegrationTargetRedirectsToOverlordAndAlertsPlayer() {
        Dynasty aggressor = new Dynasty(3, "Aggressor", false, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(aggressor);

        Hex aggressorHex = new Hex();
        Hex targetHex = new Hex();
        aggressorHex.setQ(0);
        aggressorHex.setR(0);
        targetHex.setQ(1);
        targetHex.setR(0);
        aggressorHex.setNorthEast(targetHex);
        targetHex.setSouthWest(aggressorHex);

        Colony aggressorColony = new Colony(10, "Aggressor Capital", false);
        Colony targetColony = target.getColonies().get(0);
        aggressor.addColony(aggressorColony);
        aggressorHex.setColony(aggressorColony);
        targetHex.setColony(targetColony);
        ensureWarPopulation(aggressor, aggressorColony);
        ensureWarPopulation(target, targetColony);
        ensureWarPopulation(overlord, overlord.getCapital());

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(aggressorHex);
        hexes.add(targetHex);
        world.setHexes(hexes);

        overlord.setIntegrationTargetDynastyId(target.getId());
        aggressor.setDiplomaticReputation(target.getId(), 30);
        target.setDiplomaticReputation(aggressor.getId(), 30);

        aggressor.getDiplomacyService().declareWar(target, world, tradeManager);

        assertFalse(aggressor.getDiplomacyService().isAtWarWith(target));
        assertTrue(aggressor.getDiplomacyService().isAtWarWith(overlord));
        assertTrue(overlord.getDiplomacyService().isAtWarWith(aggressor));
        assertEquals(1, overlord.copyPendingIntegrationVassalWarAlerts().size());
        assertEquals(aggressor.getId(), overlord.copyPendingIntegrationVassalWarAlerts().get(0).attackerId);
        assertEquals(target.getId(), overlord.copyPendingIntegrationVassalWarAlerts().get(0).vassalId);
        assertFalse(overlord.hasPendingWarDeclarationFrom(aggressor.getId()));
    }

    @Test
    void integrationDiplomatsDrawFromAnyColonyWithCapacity() {
        Colony satellite = new Colony(3, "Satellite", false);
        satellite.setAge(7);
        satellite.setLoyalty(GameNumbers.DEFAULT_COLONY_LOYALTY);
        satellite.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 2);
        overlord.addColony(satellite);

        Colony militant = new Colony(4, "Militant", false);
        militant.setAge(7);
        militant.setLoyalty(GameConstants.LOYALTY_MILITANT.getMinScore());
        militant.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 3);
        overlord.addColony(militant);

        assertEquals(6, DynastyIntegrationService.countMaxAssignableIntegrationDiplomats(
                overlord, world, tradeManager));

        DynastyIntegrationService.assignIntegrationDiplomats(overlord, 4, false, world, tradeManager);
        assertEquals(0, overlord.getCapital().getIntegrationDiplomatsDeployed());
        assertEquals(1, satellite.getIntegrationDiplomatsDeployed());
        assertEquals(3, militant.getIntegrationDiplomatsDeployed());
    }

    @Test
    void canStartIntegrationWhenBorderIsThroughNonCapitalColony() {
        Colony borderColony = new Colony(5, "Frontier", false);
        borderColony.setAge(7);
        overlord.addColony(borderColony);

        Hex capitalHex = new Hex();
        Hex borderHex = new Hex();
        Hex targetHex = new Hex();
        capitalHex.setQ(0);
        capitalHex.setR(0);
        borderHex.setQ(1);
        borderHex.setR(0);
        targetHex.setQ(2);
        targetHex.setR(0);
        capitalHex.setNorthEast(borderHex);
        borderHex.setSouthWest(capitalHex);
        borderHex.setNorthEast(targetHex);
        targetHex.setSouthWest(borderHex);
        capitalHex.setColony(overlord.getCapital());
        borderHex.setColony(borderColony);
        targetHex.setColony(target.getColonies().get(0));
        world.setHexes(new ArrayList<>(List.of(capitalHex, borderHex, targetHex)));

        assertTrue(overlord.getDiplomacyService().sharesBorderWith(target, world));
        assertTrue(DynastyIntegrationService.canStartIntegration(overlord, target, world, tradeManager));
    }

    @Test
    void manualIntegrationDiplomatAssignmentIsPreservedAcrossReconcile() {
        overlord.setIntegrationTargetDynastyId(target.getId());
        DynastyIntegrationService.assignIntegrationDiplomats(overlord, 1, true, world, tradeManager);
        assertEquals(1, DynastyIntegrationService.countIntegrationDiplomats(overlord));

        DynastyIntegrationService.reconcileIntegrationDiplomatDeployment(overlord, world, tradeManager);
        assertEquals(1, DynastyIntegrationService.countIntegrationDiplomats(overlord));
        assertTrue(overlord.isIntegrationDiplomatsManual());
    }

    private static void ensureWarPopulation(Dynasty dynasty, Colony colony) {
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        int needed = GameNumbers.WAR_DECLARATION_MIN_POPULATION
                - dynasty.getStatService().getTotalPopulation(dynasty);
        for (int i = 0; i < needed; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 5);
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_DEFENDER, 5);
    }
}
