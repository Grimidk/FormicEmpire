package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.dynasty.PactRequestIncomingPolicy;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.CrossDynastyTradeProposal;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyDiplomacyServiceTest {

    private Dynasty player;
    private Dynasty neighbor;

    @BeforeEach
    void setUp() {
        player = new Dynasty(1, "Player Dynasty", true, GameConstants.SPECIES_OMNI);
        neighbor = new Dynasty(2, "Wild Dynasty", false, GameConstants.SPECIES_OMNI);
        seedDiplomaticPopulation(player);
        seedDiplomaticPopulation(neighbor);
    }

    private static void seedDiplomaticPopulation(Dynasty dynasty) {
        Colony colony = new Colony(dynasty.getId() * 100, "Capital", dynasty.isPlayer());
        colony.setCapital(true);
        colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        dynasty.addColony(colony);
    }

    private static void addSatelliteColonies(Dynasty dynasty, int count) {
        for (int i = 0; i < count; i++) {
            Colony satellite = new Colony(dynasty.getId() * 1000 + i, "Satellite " + i, dynasty.isPlayer());
            satellite.getWorkers().add(new Ant(satellite, GameConstants.TYPE_WORKER));
            dynasty.addColony(satellite);
        }
    }

    @Test
    void geneticIntegrityStartsAtFullAndScalesWithSatellitesAndAssimilations() {
        assertEquals(100.0, player.getGeneticIntegrity(), 0.001);

        addSatelliteColonies(player, 5);
        assertEquals(95.0, player.getGeneticIntegrity(), 0.001);

        player.completeAssimilation(GameUnlocks.ASSIMILATION_LEAFCUTTER);
        assertEquals(0.0, player.getMinGeneticIntegrity(), 0.001);
        assertEquals(95.0, player.getGeneticIntegrity(), 0.001);

        player.unlockUpgrade(GameUnlocks.ABILITY_CLONING);
        assertEquals(5.0, player.getMinGeneticIntegrity(), 0.001);
        assertEquals(95.0, player.getGeneticIntegrity(), 0.001);

        addSatelliteColonies(player, 86);
        assertEquals(9.0, player.getGeneticIntegrity(), 0.001);

        player.completeAssimilation(GameUnlocks.ASSIMILATION_MARAUDER);
        assertEquals(10.0, player.getMinGeneticIntegrity(), 0.001);
        assertEquals(10.0, player.getGeneticIntegrity(), 0.001);
    }

    private static World emptyWorld() {
        return new World();
    }

    @Test
    void formPactRequiresCordialReputation() {
        World world = emptyWorld();
        player.setDiplomaticReputation(neighbor.getId(), 59);
        assertFalse(player.getDiplomacyService().canFormNonAggressionPact(neighbor, world));

        player.setDiplomaticReputation(neighbor.getId(), 60);
        assertTrue(player.getDiplomacyService().canFormNonAggressionPact(neighbor, world));
    }

    @Test
    void formPactUsesEffectiveReputationWhenWorldProvided() {
        player.setMilitaryPower(10);
        neighbor.setMilitaryPower(110);
        player.setDiplomaticReputation(neighbor.getId(), 55);
        World world = emptyWorld();

        assertTrue(player.getDiplomacyService().canFormNonAggressionPact(neighbor, world));
    }

    @Test
    void pactAppliesReputationBonusBothWaysAndBlocksReformWhileActive() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);

        player.getDiplomacyService().formNonAggressionPact(neighbor, emptyWorld());

        assertTrue(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertTrue(neighbor.getDiplomacyService().hasNonAggressionPact(player));
        assertEquals(80, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(80, neighbor.getDiplomaticReputation(player.getId()));
        assertFalse(player.getDiplomacyService().canFormNonAggressionPact(neighbor, emptyWorld()));
    }

    @Test
    void breakingPactAppliesPenaltyAndReplacesModifier() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor, emptyWorld());

        player.getDiplomacyService().breakNonAggressionPact(neighbor);

        assertFalse(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertEquals(GameConstants.DIPLO_MODIFIER_BROKEN_PACT.getNameKey(),
                player.getDiplomaticModifierKey(neighbor.getId()));
        assertEquals(50, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(50, neighbor.getDiplomaticReputation(player.getId()));
    }

    @Test
    void formingPactAfterBrokenPactReplacesModifier() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor, emptyWorld());
        player.getDiplomacyService().breakNonAggressionPact(neighbor);

        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor, emptyWorld());

        assertTrue(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertEquals(GameConstants.DIPLO_MODIFIER_PACT.getNameKey(),
                player.getDiplomaticModifierKey(neighbor.getId()));
        assertEquals(80, player.getDiplomaticReputation(neighbor.getId()));
    }

    @Test
    void pactAddsGeneticIntegrityBonusWhileActive() {
        addSatelliteColonies(player, 10);
        addSatelliteColonies(neighbor, 10);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);

        assertEquals(90.0, player.getBaseGeneticIntegrity(), 0.001);
        assertEquals(90.0, player.getGeneticIntegrity(), 0.001);
        assertEquals(0.0, player.getDiplomaticGeneticIntegrityBonus(), 0.001);

        player.getDiplomacyService().formNonAggressionPact(neighbor, emptyWorld());

        assertEquals(10.0, player.getDiplomaticGeneticIntegrityBonus(), 0.001);
        assertEquals(100.0, player.getGeneticIntegrity(), 0.001);
        assertEquals(100.0, neighbor.getGeneticIntegrity(), 0.001);

        player.getDiplomacyService().breakNonAggressionPact(neighbor);

        assertEquals(0.0, player.getDiplomaticGeneticIntegrityBonus(), 0.001);
        assertEquals(90.0, player.getGeneticIntegrity(), 0.001);
    }

    @Test
    void warModifierReplacesPactAndAppliesReputationPenalty() {
        addSatelliteColonies(player, 10);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor, emptyWorld());

        player.getDiplomacyService().applyWar(neighbor);

        assertTrue(player.getDiplomacyService().isAtWarWith(neighbor));
        assertFalse(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertEquals(GameConstants.DIPLO_MODIFIER_WAR.getNameKey(),
                player.getDiplomaticModifierKey(neighbor.getId()));
        assertEquals(0, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(90.0, player.getGeneticIntegrity(), 0.001);
    }

    @Test
    void declareWarBlockedByActivePact() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor, emptyWorld());

        assertFalse(player.getDiplomacyService().canDeclareWar(neighbor, null));
    }

    @Test
    void declareWarRequiresActiveMilitaryRoles() {
        World world = buildBorderWorld();
        Colony playerBorderColony = world.getHexes().get(0).getColony();
        Colony neighborBorderColony = world.getHexes().get(1).getColony();
        for (Colony colony : player.getColonies()) {
            colony.getWarAssignedRoleCounts().clear();
            colony.getWorkers().clear();
            colony.getSoldiers().clear();
        }
        for (Colony colony : neighbor.getColonies()) {
            colony.getWarAssignedRoleCounts().clear();
            colony.getWorkers().clear();
            colony.getSoldiers().clear();
        }

        assertFalse(DynastyDiplomacyService.meetsWarActiveMilitaryRequirement(player));
        assertFalse(player.getDiplomacyService().canDeclareWar(neighbor, world));

        playerBorderColony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 5);
        ensureWarPopulation(player, playerBorderColony);
        ensureWarPopulation(neighbor, neighborBorderColony);
        assertTrue(player.getDiplomacyService().canDeclareWar(neighbor, world));
    }

    @Test
    void declareWarAllowedWhenTargetHasNoActiveMilitaryRoles() {
        World world = buildBorderWorld();
        Colony playerBorderColony = world.getHexes().get(0).getColony();
        Colony neighborBorderColony = world.getHexes().get(1).getColony();
        ensureWarPopulation(player, playerBorderColony);
        ensureWarPopulation(neighbor, neighborBorderColony);
        neighborBorderColony.getWarAssignedRoleCounts().clear();
        playerBorderColony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 5);

        assertEquals(0, DynastyDiplomacyService.countAssignedActiveMilitaryRoles(neighbor));
        assertTrue(player.getDiplomacyService().canDeclareWar(neighbor, world));
    }

    @Test
    void declareWarRequiresMinimumPopulation() {
        Colony smallColony = new Colony(30, "Small", true);
        player.addColony(smallColony);
        player.setCapital(smallColony);
        assertFalse(DynastyDiplomacyService.meetsWarDeclarationPopulationRequirement(player));
        assertFalse(player.getDiplomacyService().canDeclareWar(neighbor, null));

        for (int i = 0; i < GameNumbers.WAR_DECLARATION_MIN_POPULATION; i++) {
            smallColony.getWorkers().add(
                    new com.grimidk.formicempire.classes.entities.critter.Ant(smallColony, GameConstants.TYPE_WORKER));
        }
        assertTrue(DynastyDiplomacyService.meetsWarDeclarationPopulationRequirement(player));
    }

    @Test
    void warServiceListsEachActiveWarOnce() {
        World world = buildBorderWorld();
        player.getDiplomacyService().applyWar(neighbor, null, world);

        List<com.grimidk.formicempire.classes.entities.dynasty.War> wars = world.getWarService().getActiveWars();
        assertEquals(1, wars.size());
        assertTrue(wars.get(0).involves(player.getId()));
    }

    @Test
    void declareWarBlockedUntilPactBreakCooldownExpires() {
        World world = buildBorderWorld();
        world.setYear(1);
        world.setMonth(6);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor, emptyWorld());
        player.getDiplomacyService().breakNonAggressionPact(neighbor, world);

        assertFalse(player.getDiplomacyService().canDeclareWar(neighbor, world));
        assertEquals(6, player.getDiplomacyService().getWarDeclarationCooldownMonthsRemaining(neighbor, world));

        world.setMonth(12);
        assertTrue(player.getDiplomacyService().canDeclareWar(neighbor, world));
    }

    @Test
    void declareWarNotifiesPlayerDynasty() {
        World world = buildBorderWorld();
        player.setDiplomaticReputation(neighbor.getId(), 30);
        neighbor.setDiplomaticReputation(player.getId(), 30);

        neighbor.getDiplomacyService().declareWar(player, world, null);

        assertTrue(neighbor.getDiplomacyService().isAtWarWith(player));
        assertTrue(player.hasPendingWarDeclarationFrom(neighbor.getId()));
    }

    @Test
    void npcWarQueuesAlertOnPlayerDynasty() {
        World world = new World();
        Dynasty aggressor = new Dynasty(3, "Aggressor", false, GameConstants.SPECIES_OMNI);
        Dynasty rival = new Dynasty(4, "Rival", false, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(aggressor);
        world.getDynastys().add(rival);
        world.getDynastys().add(player);

        Hex aggressorHex = new Hex();
        Hex rivalHex = new Hex();
        aggressorHex.setQ(2);
        aggressorHex.setR(0);
        rivalHex.setQ(3);
        rivalHex.setR(0);
        aggressorHex.setNorthEast(rivalHex);
        rivalHex.setSouthWest(aggressorHex);

        Colony aggressorColony = new Colony(20, "Aggressor Capital", false);
        Colony rivalColony = new Colony(21, "Rival Capital", false);
        aggressor.addColony(aggressorColony);
        rival.addColony(rivalColony);
        aggressorColony.setDynasty(aggressor);
        rivalColony.setDynasty(rival);
        aggressorHex.setColony(aggressorColony);
        rivalHex.setColony(rivalColony);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(aggressorHex);
        hexes.add(rivalHex);
        world.setHexes(hexes);

        ensureWarPopulation(aggressor, aggressorColony);
        ensureWarPopulation(rival, rivalColony);

        Colony playerColony = new Colony(22, "Player Capital", true);
        player.addColony(playerColony);
        player.setCapital(playerColony);

        aggressor.getDiplomacyService().declareWar(rival, world, null);

        assertTrue(aggressor.getDiplomacyService().isAtWarWith(rival));
        assertEquals(1, player.copyPendingNpcWarAlerts().size());
        assertEquals(aggressor.getId(), player.copyPendingNpcWarAlerts().get(0).attackerId);
        assertEquals(rival.getId(), player.copyPendingNpcWarAlerts().get(0).defenderId);
    }

    private World buildBorderWorld() {
        World world = new World();
        Hex playerHex = new Hex();
        Hex neighborHex = new Hex();
        playerHex.setQ(0);
        playerHex.setR(0);
        neighborHex.setQ(1);
        neighborHex.setR(0);
        playerHex.setNorthEast(neighborHex);
        neighborHex.setSouthWest(playerHex);

        Colony playerColony = new Colony(10, "Capital", true);
        Colony neighborColony = new Colony(11, "Border", false);
        player.addColony(playerColony);
        neighbor.addColony(neighborColony);
        playerColony.setDynasty(player);
        neighborColony.setDynasty(neighbor);
        playerHex.setColony(playerColony);
        neighborHex.setColony(neighborColony);
        ensureWarPopulation(player, playerColony);
        ensureWarPopulation(neighbor, neighborColony);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(playerHex);
        hexes.add(neighborHex);
        world.setHexes(hexes);
        world.getDynastys().add(player);
        world.getDynastys().add(neighbor);
        return world;
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

    @Test
    void diplomatPressureUpgradesIncreaseStabilityGain() {
        assertEquals(1, player.getDiplomacyService().getDiplomatStabilityGainPerAnt());
        player.unlockUpgrade(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_2);
        assertEquals(3, player.getDiplomacyService().getDiplomatStabilityGainPerAnt());
        player.unlockUpgrade(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_3);
        assertEquals(5, player.getDiplomacyService().getDiplomatStabilityGainPerAnt());
    }

    @Test
    void requestTradeRequiresNeutralReputation() {
        player.unlockUpgrade(GameUnlocks.ABILITY_TRADE);
        player.setDiplomaticReputation(neighbor.getId(), 39);
        neighbor.setDiplomaticReputation(player.getId(), 39);
        assertFalse(player.getDiplomacyService().canRequestTrade(neighbor, null));

        player.setDiplomaticReputation(neighbor.getId(), 40);
        assertTrue(player.getDiplomacyService().canRequestTrade(neighbor, null));
    }

    @Test
    void offerTradeRequiresWaryReputation() {
        player.unlockUpgrade(GameUnlocks.ABILITY_TRADE);
        player.setDiplomaticReputation(neighbor.getId(), 19);
        assertFalse(player.getDiplomacyService().canOfferTrade(neighbor, null));

        player.setDiplomaticReputation(neighbor.getId(), 20);
        assertTrue(player.getDiplomacyService().canOfferTrade(neighbor, null));
    }

    @Test
    void declineTradeProposalAppliesPenaltyBothWays() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        CrossDynastyTradeProposal proposal = new CrossDynastyTradeProposal(
                neighbor.getId(), 1, 2, CrossDynastyTradeProposal.Kind.REQUEST, Map.of());
        player.getDiplomacyService().declineTradeProposal(neighbor, proposal, null);
        assertEquals(55, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(55, neighbor.getDiplomaticReputation(player.getId()));
    }

    @Test
    void tradeOfferAcceptedAppliesSeparateReceiverAndSenderModifiers() {
        player.setDiplomaticReputation(neighbor.getId(), 50);
        neighbor.setDiplomaticReputation(player.getId(), 50);

        player.getDiplomacyService().applyCrossDynastyTradeReputation(
                player, neighbor, CrossDynastyTradeProposal.Kind.OFFER);

        assertEquals(55, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(70, neighbor.getDiplomaticReputation(player.getId()));
    }

    @Test
    void tradeRequestAcceptedAppliesSeparateReceiverAndSenderModifiers() {
        player.setDiplomaticReputation(neighbor.getId(), 50);
        neighbor.setDiplomaticReputation(player.getId(), 50);

        neighbor.getDiplomacyService().applyCrossDynastyTradeReputation(
                neighbor, player, CrossDynastyTradeProposal.Kind.REQUEST);

        assertEquals(70, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(45, neighbor.getDiplomaticReputation(player.getId()));
    }

    @Test
    void tradeAcceptanceChanceScalesFromNeutral() {
        assertEquals(0.0, DynastyDiplomacyService.computeTradeAcceptanceChance(39), 0.001);
        assertEquals(0.5, DynastyDiplomacyService.computeTradeAcceptanceChance(40), 0.001);
        assertEquals(1.0, DynastyDiplomacyService.computeTradeAcceptanceChance(100), 0.001);
    }

    @Test
    void pactAcceptanceChanceScalesFromFiftyFiftyToGuaranteed() {
        assertEquals(0.0, DynastyDiplomacyService.computePactAcceptanceChance(59), 0.001);
        assertEquals(0.5, DynastyDiplomacyService.computePactAcceptanceChance(60), 0.001);
        assertEquals(0.75, DynastyDiplomacyService.computePactAcceptanceChance(80), 0.001);
        assertEquals(1.0, DynastyDiplomacyService.computePactAcceptanceChance(100), 0.001);
    }

    @Test
    void decliningPactAppliesSmallerPenaltyThanBreaking() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);

        neighbor.getDiplomacyService().declineNonAggressionPact(player, null);

        assertEquals(GameConstants.DIPLO_MODIFIER_DECLINED_PACT.getNameKey(),
                player.getDiplomaticModifierKey(neighbor.getId()));
        assertEquals(50, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(50, neighbor.getDiplomaticReputation(player.getId()));
        assertTrue(Math.abs(GameConstants.DIPLO_MODIFIER_DECLINED_PACT.getReputationDelta())
                < Math.abs(GameConstants.DIPLO_MODIFIER_BROKEN_PACT.getReputationDelta()));
    }

    @Test
    void requestToPlayerQueuesPendingPact() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);

        neighbor.getDiplomacyService().requestNonAggressionPact(player, emptyWorld());

        assertTrue(player.hasPendingPactRequestFrom(neighbor.getId()));
        assertFalse(neighbor.getDiplomacyService().hasNonAggressionPact(player));
    }

    @Test
    void acceptPendingPactFormsPact() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.addPendingPactRequest(neighbor.getId());

        player.getDiplomacyService().acceptNonAggressionPact(neighbor, emptyWorld());

        assertTrue(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertFalse(player.hasPendingPactRequestFrom(neighbor.getId()));
    }

    @Test
    void autoAcceptIncomingPactFormsPactWithoutPendingQueue() {
        World world = emptyWorld();
        world.setYear(1);
        world.setMonth(1);
        world.setDay(1);
        player.setPactRequestIncomingPolicy(PactRequestIncomingPolicy.AUTO_ACCEPT);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);

        neighbor.getDiplomacyService().requestNonAggressionPact(player, world);

        assertTrue(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertFalse(player.hasPendingPactRequestFrom(neighbor.getId()));
    }

    @Test
    void autoDeclineIncomingPactAppliesDeclineModifier() {
        World world = emptyWorld();
        world.setYear(1);
        world.setMonth(1);
        world.setDay(1);
        player.setPactRequestIncomingPolicy(PactRequestIncomingPolicy.AUTO_DECLINE);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);

        neighbor.getDiplomacyService().requestNonAggressionPact(player, world);

        assertFalse(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertFalse(player.hasPendingPactRequestFrom(neighbor.getId()));
        assertEquals(GameConstants.DIPLO_MODIFIER_DECLINED_PACT.getNameKey(),
                player.getDiplomaticModifierKey(neighbor.getId()));
    }

    @Test
    void incomingPactLimitedToOnePerDay() {
        World world = emptyWorld();
        world.setYear(1);
        world.setMonth(1);
        world.setDay(1);
        Dynasty neighbor2 = new Dynasty(3, "Wild Dynasty 2", false, GameConstants.SPECIES_OMNI);
        seedDiplomaticPopulation(neighbor2);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        player.setDiplomaticReputation(neighbor2.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        neighbor2.setDiplomaticReputation(player.getId(), 60);

        neighbor.getDiplomacyService().requestNonAggressionPact(player, world);
        neighbor2.getDiplomacyService().requestNonAggressionPact(player, world);

        assertTrue(player.hasPendingPactRequestFrom(neighbor.getId()));
        assertFalse(player.hasPendingPactRequestFrom(neighbor2.getId()));
        assertFalse(player.hasDiplomaticModifierKey(
                neighbor2.getId(), GameConstants.DIPLO_MODIFIER_DECLINED_PACT.getNameKey()));
    }

    @Test
    void blockedIncomingPactWhilePendingDoesNotDecline() {
        World world = emptyWorld();
        world.setYear(1);
        world.setMonth(1);
        world.setDay(1);
        Dynasty neighbor2 = new Dynasty(3, "Wild Dynasty 2", false, GameConstants.SPECIES_OMNI);
        seedDiplomaticPopulation(neighbor2);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        player.setDiplomaticReputation(neighbor2.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        neighbor2.setDiplomaticReputation(player.getId(), 60);
        player.addPendingPactRequest(neighbor.getId());

        neighbor2.getDiplomacyService().requestNonAggressionPact(player, world);

        assertFalse(player.hasPendingPactRequestFrom(neighbor2.getId()));
        assertFalse(player.hasDiplomaticModifierKey(
                neighbor2.getId(), GameConstants.DIPLO_MODIFIER_DECLINED_PACT.getNameKey()));
    }

    @Test
    void blockedIncomingPactWhilePromptOpenDoesNotDecline() {
        World world = emptyWorld();
        world.setYear(1);
        world.setMonth(1);
        world.setDay(1);
        Dynasty neighbor2 = new Dynasty(3, "Wild Dynasty 2", false, GameConstants.SPECIES_OMNI);
        seedDiplomaticPopulation(neighbor2);
        player.setDiplomaticReputation(neighbor2.getId(), 60);
        neighbor2.setDiplomaticReputation(player.getId(), 60);
        player.setPactRequestPromptOpen(true);

        neighbor2.getDiplomacyService().requestNonAggressionPact(player, world);

        assertFalse(player.hasPendingPactRequestFrom(neighbor2.getId()));
        assertFalse(player.hasDiplomaticModifierKey(
                neighbor2.getId(), GameConstants.DIPLO_MODIFIER_DECLINED_PACT.getNameKey()));
    }

    @Test
    void pendingPactQueueRejectsAdditionalRequestersBeyondMax() {
        assertEquals(1, GameNumbers.DIPLO_PENDING_PACT_REQUEST_QUEUE_MAX);
        player.addPendingPactRequest(neighbor.getId());
        player.addPendingPactRequest(3);
        assertTrue(player.hasPendingPactRequestFrom(neighbor.getId()));
        assertFalse(player.hasPendingPactRequestFrom(3));
        assertEquals(1, player.copyPendingPactRequestFromIds().size());
    }

    @Test
    void switchingToAutoDeclineAppliesToQueuedPactRequests() {
        World world = emptyWorld();
        world.getDynastys().add(player);
        world.getDynastys().add(neighbor);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.addPendingPactRequest(neighbor.getId());

        player.setPactRequestIncomingPolicy(PactRequestIncomingPolicy.AUTO_DECLINE);
        player.getDiplomacyService().applyIncomingPactPolicyToPendingRequests(world);

        assertFalse(player.hasPendingPactRequestFrom(neighbor.getId()));
        assertFalse(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertEquals(GameConstants.DIPLO_MODIFIER_DECLINED_PACT.getNameKey(),
                player.getDiplomaticModifierKey(neighbor.getId()));
    }

    @Test
    void switchingToAutoAcceptAppliesToQueuedPactRequests() {
        World world = emptyWorld();
        world.getDynastys().add(player);
        world.getDynastys().add(neighbor);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.addPendingPactRequest(neighbor.getId());

        player.setPactRequestIncomingPolicy(PactRequestIncomingPolicy.AUTO_ACCEPT);
        player.getDiplomacyService().applyIncomingPactPolicyToPendingRequests(world);

        assertFalse(player.hasPendingPactRequestFrom(neighbor.getId()));
        assertTrue(player.getDiplomacyService().hasNonAggressionPact(neighbor));
    }

    @Test
    void meetsTradeLoyaltyRequirementUsesDisloyalThreshold() {
        Colony colony = new Colony(1, "Test", true);
        colony.setLoyalty(19);
        assertFalse(DynastyDiplomacyService.meetsTradeLoyaltyRequirement(colony));
        colony.setLoyalty(20);
        assertTrue(DynastyDiplomacyService.meetsTradeLoyaltyRequirement(colony));
    }

    @Test
    void borderFrictionReducesEffectiveReputation() {
        player.setDiplomaticReputation(neighbor.getId(), 50);
        neighbor.setDiplomaticReputation(player.getId(), 50);

        World world = new World();
        Hex playerHex = new Hex();
        Hex neighborHex = new Hex();
        playerHex.setQ(0);
        playerHex.setR(0);
        neighborHex.setQ(1);
        neighborHex.setR(0);
        playerHex.setNorthEast(neighborHex);
        neighborHex.setSouthWest(playerHex);

        Colony playerColony = new Colony(10, "Capital", true);
        Colony neighborColony = new Colony(11, "Border", false);
        player.addColony(playerColony);
        neighbor.addColony(neighborColony);
        playerHex.setColony(playerColony);
        neighborHex.setColony(neighborColony);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(playerHex);
        hexes.add(neighborHex);
        world.setHexes(hexes);
        world.getDynastys().add(player);
        world.getDynastys().add(neighbor);

        assertTrue(player.getDiplomacyService().sharesBorderWith(neighbor, world));
        assertEquals(-10, player.getDiplomacyService().getBorderFrictionAdjustment(neighbor, world));
        assertEquals(40, player.getDiplomacyService().getEffectiveDiplomaticReputation(neighbor, world));
    }

    @Test
    void capitalColonyClampsToMaximumEffectiveLoyalty() {
        Colony capital = new Colony(1, "Capital", true);
        capital.setCapital(true);
        capital.setLoyalty(0);
        assertEquals(GameNumbers.COLONY_LOYALTY_MAX, capital.getEffectiveLoyalty(null, null));
    }

    @Test
    void applyWarCancelsCrossDynastyTrades() {
        TradeManager tradeManager = new TradeManager();
        player.bindTradeManager(tradeManager);
        neighbor.bindTradeManager(tradeManager);

        Hex playerHex = new Hex();
        Hex neighborHex = new Hex();
        playerHex.setQ(0);
        playerHex.setR(0);
        neighborHex.setQ(1);
        neighborHex.setR(0);

        Colony playerColony = new Colony(10, "Alpha", true);
        Colony neighborColony = new Colony(11, "Beta", false);
        player.addColony(playerColony);
        neighbor.addColony(neighborColony);
        playerColony.setDynasty(player);
        neighborColony.setDynasty(neighbor);
        playerHex.setColony(playerColony);
        neighborHex.setColony(neighborColony);

        Trade trade = new Trade(playerHex, neighborHex, Map.of(), Map.of(), Map.of(), true, false,
                GameConstants.METHOD_LAND);
        trade.startTrip();
        tradeManager.addTrade(trade);

        player.getDiplomacyService().applyWar(neighbor, tradeManager);

        assertTrue(player.getDiplomacyService().isAtWarWith(neighbor));
        assertEquals(0, tradeManager.getActiveTrades().size());
    }

    @Test
    void declinedPactBlocksRetryUntilNextMonth() {
        World world = new World();
        world.setYear(1);
        world.setMonth(3);

        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().declineNonAggressionPact(neighbor, world);

        assertFalse(neighbor.getDiplomacyService().canRequestNonAggressionPact(player, world));
        assertEquals(1, neighbor.getDiplomacyService().getPactRequestDeclineCooldownMonthsRemaining(player, world));

        advanceWorldDays(world, GameConstants.DIPLO_MODIFIER_DECLINED_PACT.getDurationDays());
        world.setMonth(4);
        assertTrue(neighbor.getDiplomacyService().canRequestNonAggressionPact(player, world));
        assertEquals(60, neighbor.getDiplomaticReputation(player.getId()));
    }

    @Test
    void declinedTradeBlocksRetryUntilNextMonth() {
        World world = new World();
        world.setYear(0);
        world.setMonth(5);

        player.unlockUpgrade(GameUnlocks.ABILITY_TRADE);
        neighbor.unlockUpgrade(GameUnlocks.ABILITY_TRADE);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        CrossDynastyTradeProposal proposal = new CrossDynastyTradeProposal(
                neighbor.getId(), 1, 2, CrossDynastyTradeProposal.Kind.REQUEST, Map.of());

        player.getDiplomacyService().declineTradeProposal(neighbor, proposal, world);

        assertFalse(neighbor.getDiplomacyService().canRequestTrade(player, world));
        assertEquals(1, neighbor.getDiplomacyService().getTradeRequestDeclineCooldownMonthsRemaining(player, world));

        advanceWorldDays(world, GameConstants.DIPLO_MODIFIER_TRADE_REQUEST.getDurationDays());
        world.setMonth(6);
        assertTrue(neighbor.getDiplomacyService().canRequestTrade(player, world));
        assertEquals(60, neighbor.getDiplomaticReputation(player.getId()));
    }

    @Test
    void applyWasAtWarModifierAdjustsReputation() {
        player.setDiplomaticReputation(neighbor.getId(), 50);
        neighbor.setDiplomaticReputation(player.getId(), 50);

        player.getDiplomacyService().applyWasAtWarModifier(neighbor, null);

        assertEquals(30, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(30, neighbor.getDiplomaticReputation(player.getId()));
        assertEquals(GameConstants.DIPLO_MODIFIER_WAS_AT_WAR.getNameKey(),
                player.getDiplomaticModifierKey(neighbor.getId()));
    }

    @Test
    void reputationModifierTooltipUsesSignedDeltasSortedPositiveFirst() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor, emptyWorld());

        var lines = player.getDiplomacyService().collectVisibleReputationModifiers(neighbor, null);
        assertTrue(lines.stream().anyMatch(line -> line.delta() == 20));

        String tooltip = player.getDiplomacyService().buildStanceIconTooltip(neighbor, null);
        assertTrue(tooltip.contains("Base: 50") || tooltip.contains("Base: 50<br>"));
        assertTrue(tooltip.contains("Pact: +20"));
        assertTrue(tooltip.contains("Effective:"));
    }

    @Test
    void reputationTooltipListsEverySavedModifierKey() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor, emptyWorld());
        player.markCrossDynastyTradeRepBonus(neighbor.getId());
        player.addDiplomaticModifierKey(neighbor.getId(), GameConstants.DIPLO_MODIFIER_TRADE.getNameKey());

        var lines = player.getDiplomacyService().collectVisibleReputationModifiers(neighbor, null);
        assertTrue(lines.stream().anyMatch(line ->
                line.delta() == GameConstants.DIPLO_MODIFIER_PACT.getReputationDelta()));
        assertTrue(lines.stream().anyMatch(line ->
                line.delta() == GameConstants.DIPLO_MODIFIER_TRADE.getReputationDelta()));

        String tooltip = player.getDiplomacyService().buildStanceIconTooltip(neighbor, null);
        assertTrue(tooltip.contains("Base: 50") || tooltip.contains("Base: 50<br>"));
        assertTrue(tooltip.contains("Pact: +20"));
        assertTrue(tooltip.contains("Trade Route: +20"));
        assertFalse(tooltip.contains("Prior relations"));
        assertTrue(tooltip.contains("Effective:"));
    }

    @Test
    void assignDynastyMissionDiplomatsSetsAbsoluteCountAndRecallsToZero() {
        World world = emptyWorld();
        Colony capital = player.getCapital();
        capital.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        capital.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 5);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);

        player.getDiplomacyService().assignDynastyMissionDiplomats(neighbor, 3, world);
        assertEquals(3, player.getDiplomacyService().countDynastyMissionDiplomatsToward(neighbor));
        assertEquals(3, player.getDiplomatSupportTo(neighbor.getId()));

        player.getDiplomacyService().assignDynastyMissionDiplomats(neighbor, 0, world);
        assertEquals(0, player.getDiplomacyService().countDynastyMissionDiplomatsToward(neighbor));
        assertEquals(0, player.getDiplomatSupportTo(neighbor.getId()));
    }

    @Test
    void assignColonyMissionDiplomatsSetsAbsoluteCount() {
        World world = emptyWorld();
        TradeManager tradeManager = new TradeManager();
        Colony capital = player.getCapital();
        Colony satellite = new Colony(102, "Satellite", true);
        player.addColony(satellite);
        capital.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        capital.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 3);
        satellite.setLoyalty(10);

        player.getDiplomacyService().assignColonyMissionDiplomats(satellite, 2, tradeManager, world);
        assertEquals(2, player.getDiplomacyService().countColonyMissionDiplomatsOn(satellite));

        player.getDiplomacyService().assignColonyMissionDiplomats(satellite, 0, tradeManager, world);
        assertEquals(0, player.getDiplomacyService().countColonyMissionDiplomatsOn(satellite));
    }

    @Test
    void diplomatMissionRecalledWhenPrincessDiplomatDies() {
        World world = new World();
        Hex playerHex = new Hex();
        Hex neighborHex = new Hex();
        playerHex.setQ(0);
        playerHex.setR(0);
        neighborHex.setQ(1);
        neighborHex.setR(0);
        playerHex.setNorthEast(neighborHex);
        neighborHex.setSouthWest(playerHex);

        Colony capital = player.getCapital();
        capital.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        capital.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 1);
        Ant princess = new Ant(capital, GameConstants.TYPE_PRINCESS);
        princess.setRole(GameConstants.ROLE_DIPLOMAT);
        capital.getPrincesses().add(princess);
        playerHex.setColony(capital);
        neighborHex.setColony(neighbor.getCapital());

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(playerHex);
        hexes.add(neighborHex);
        world.setHexes(hexes);
        world.getDynastys().add(player);
        world.getDynastys().add(neighbor);

        int sent = player.getDiplomacyService().sendDiplomatsToDynasty(capital, neighbor, 1, world);
        assertEquals(1, sent);
        assertEquals(1, player.getDiplomatSupportTo(neighbor.getId()));

        princess.goDie(capital, "Test");
        capital.getPrincesses().remove(princess);
        player.getDiplomacyService().reconcileDiplomatDeploymentsAfterCasualty(capital, GameConstants.ROLE_DIPLOMAT);

        assertEquals(0, player.getDiplomatSupportTo(neighbor.getId()));
        assertEquals(0, player.getDiplomacyService().getDiplomatReputationAdjustment(neighbor));
    }

    @Test
    void geneticExchangeGrantsTargetReputationAndIntegrityModifier() {
        Colony capital = player.getCapital();
        player.setDiplomaticReputation(neighbor.getId(), 70);
        neighbor.setDiplomaticReputation(player.getId(), 50);
        for (int i = 0; i < GameNumbers.GENETIC_EXCHANGE_DRONE_COST; i++) {
            capital.getDrones().add(new Ant(capital, GameConstants.TYPE_DRONE));
        }

        World world = emptyWorld();
        world.getDynastys().add(player);
        world.getDynastys().add(neighbor);

        assertTrue(player.getDiplomacyService().offerGeneticExchange(neighbor, world));
        assertEquals(60, neighbor.getDiplomaticReputation(player.getId()));
        assertTrue(neighbor.hasDiplomaticModifierKey(
                player.getId(), GameConstants.DIPLO_MODIFIER_GENETIC_EXCHANGE.getNameKey()));
        assertEquals(10.0, neighbor.getDiplomaticGeneticIntegrityBonus(), 0.001);
        assertFalse(player.getDiplomacyService().canOfferGeneticExchange(neighbor, world));
    }

    @Test
    void geneticExchangePullsDronesFromAllColonies() {
        Colony capital = player.getCapital();
        Colony interior = new Colony(102, "Interior", true);
        player.addColony(interior);
        player.setDiplomaticReputation(neighbor.getId(), 70);
        for (int i = 0; i < 10; i++) {
            capital.getDrones().add(new Ant(capital, GameConstants.TYPE_DRONE));
        }
        for (int i = 0; i < 15; i++) {
            interior.getDrones().add(new Ant(interior, GameConstants.TYPE_DRONE));
        }

        World world = emptyWorld();
        world.getDynastys().add(player);
        world.getDynastys().add(neighbor);

        assertEquals(25, player.getDiplomacyService().countDynastyLiveDrones());
        assertTrue(player.getDiplomacyService().canOfferGeneticExchange(neighbor, world));
        assertTrue(player.getDiplomacyService().offerGeneticExchange(neighbor, world));
        assertEquals(0, player.getDiplomacyService().countDynastyLiveDrones());
    }

    @Test
    void warmongerAppliesAfterFiveDeclaredWarsWithAllDynasties() {
        World world = emptyWorld();
        world.getDynastys().add(player);
        world.getDynastys().add(neighbor);
        List<Dynasty> victims = new ArrayList<>();
        for (int i = 0; i < GameNumbers.WARMONGER_DECLARED_WARS_THRESHOLD; i++) {
            Dynasty victim = new Dynasty(100 + i, "Victim " + i, false, GameConstants.SPECIES_OMNI);
            seedDiplomaticPopulation(victim);
            world.getDynastys().add(victim);
            victims.add(victim);
        }
        Dynasty bystander = new Dynasty(200, "Bystander", false, GameConstants.SPECIES_OMNI);
        seedDiplomaticPopulation(bystander);
        world.getDynastys().add(bystander);

        player.setDiplomaticReputation(bystander.getId(), GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION);
        bystander.setDiplomaticReputation(player.getId(), GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION);

        assertEquals(0, player.getDiplomacyService().getWarmongerAdjustment(bystander, world));
        assertFalse(world.getWarService().isWarmonger(player.getId()));

        for (int i = 0; i < GameNumbers.WARMONGER_DECLARED_WARS_THRESHOLD - 1; i++) {
            assertNotNull(world.getWarService().beginWar(player, victims.get(i)));
        }
        assertFalse(world.getWarService().isWarmonger(player.getId()));
        assertEquals(0, player.getDiplomacyService().getWarmongerAdjustment(bystander, world));

        assertNotNull(world.getWarService().beginWar(player, victims.get(GameNumbers.WARMONGER_DECLARED_WARS_THRESHOLD - 1)));
        assertTrue(world.getWarService().isWarmonger(player.getId()));
        assertEquals(GameConstants.DIPLO_MODIFIER_WARMONGER.getReputationDelta(),
                player.getDiplomacyService().getWarmongerAdjustment(bystander, world));
        assertEquals(GameConstants.DIPLO_MODIFIER_WARMONGER.getReputationDelta(),
                bystander.getDiplomacyService().getWarmongerAdjustment(player, world));
        assertEquals(
                GameNumbers.clampDiplomaticReputation(
                        GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION
                                + GameConstants.DIPLO_MODIFIER_WARMONGER.getReputationDelta()),
                player.getDiplomacyService().getEffectiveDiplomaticReputation(bystander, world));
        assertTrue(player.getDiplomacyService().collectVisibleReputationModifiers(bystander, world).stream()
                .anyMatch(line -> line.delta() == GameConstants.DIPLO_MODIFIER_WARMONGER.getReputationDelta()));
    }

    @Test
    void autoDiplomacyDoesNotExceedPerTargetDiplomatLimit() {
        World world = emptyWorld();
        TradeManager tradeManager = new TradeManager();
        Colony capital = player.getCapital();
        capital.setAge(7);
        capital.setLoyalty(GameConstants.LOYALTY_MILITANT.getMinScore());
        capital.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        capital.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 5);

        Colony militant = new Colony(103, "Militant", true);
        player.addColony(militant);
        militant.setAge(7);
        militant.setLoyalty(GameConstants.LOYALTY_MILITANT.getMinScore());
        militant.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        militant.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 5);

        Colony target = new Colony(104, "Unstable", true);
        player.addColony(target);
        target.setAge(7);
        target.setLoyalty(GameConstants.LOYALTY_DISLOYAL.getMinScore());

        player.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        player.unlockUpgrade(GameUnlocks.ABILITY_AUTO_DIPLOMACY);
        player.setAutoDiplomacyEnabled(true);

        player.getDiplomacyService().runAutomatedColonyLoyalty(player, world, tradeManager);

        assertEquals(GameNumbers.DIPLOMAT_MAX_PER_DYNASTY_MISSION,
                player.getDiplomacyService().countColonyMissionDiplomatsOn(target));
    }

    private void advanceWorldDays(World world, int days) {
        TradeManager tradeManager = new TradeManager();
        for (int i = 0; i < days; i++) {
            player.runDailyJobs(world, tradeManager);
            neighbor.runDailyJobs(world, tradeManager);
        }
    }
}
