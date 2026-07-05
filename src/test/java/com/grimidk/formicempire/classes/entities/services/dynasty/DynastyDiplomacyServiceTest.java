package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.CrossDynastyTradeProposal;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyDiplomacyServiceTest {

    private Dynasty player;
    private Dynasty neighbor;

    @BeforeEach
    void setUp() {
        player = new Dynasty(1, "Player Dynasty", true, GameConstants.SPECIES_OMNI);
        neighbor = new Dynasty(2, "Wild Dynasty", false, GameConstants.SPECIES_OMNI);
    }

    @Test
    void formPactRequiresCordialReputation() {
        player.setDiplomaticReputation(neighbor.getId(), 59);
        assertFalse(player.getDiplomacyService().canFormNonAggressionPact(neighbor));

        player.setDiplomaticReputation(neighbor.getId(), 60);
        assertTrue(player.getDiplomacyService().canFormNonAggressionPact(neighbor));
    }

    @Test
    void pactAppliesReputationBonusBothWaysAndBlocksReformWhileActive() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);

        player.getDiplomacyService().formNonAggressionPact(neighbor);

        assertTrue(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertTrue(neighbor.getDiplomacyService().hasNonAggressionPact(player));
        assertEquals(80, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(80, neighbor.getDiplomaticReputation(player.getId()));
        assertFalse(player.getDiplomacyService().canFormNonAggressionPact(neighbor));
    }

    @Test
    void breakingPactAppliesPenaltyAndReplacesModifier() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor);

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
        player.getDiplomacyService().formNonAggressionPact(neighbor);
        player.getDiplomacyService().breakNonAggressionPact(neighbor);

        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor);

        assertTrue(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertEquals(GameConstants.DIPLO_MODIFIER_PACT.getNameKey(),
                player.getDiplomaticModifierKey(neighbor.getId()));
        assertEquals(80, player.getDiplomaticReputation(neighbor.getId()));
    }

    @Test
    void pactAddsGeneticIntegrityBonusWhileActive() {
        player.setGeneticIntegrity(85.0);
        neighbor.setGeneticIntegrity(85.0);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);

        assertEquals(85.0, player.getBaseGeneticIntegrity(), 0.001);
        assertEquals(85.0, player.getGeneticIntegrity(), 0.001);
        assertEquals(0.0, player.getDiplomaticGeneticIntegrityBonus(), 0.001);

        player.getDiplomacyService().formNonAggressionPact(neighbor);

        assertEquals(10.0, player.getDiplomaticGeneticIntegrityBonus(), 0.001);
        assertEquals(95.0, player.getGeneticIntegrity(), 0.001);
        assertEquals(95.0, neighbor.getGeneticIntegrity(), 0.001);

        player.getDiplomacyService().breakNonAggressionPact(neighbor);

        assertEquals(0.0, player.getDiplomaticGeneticIntegrityBonus(), 0.001);
        assertEquals(85.0, player.getGeneticIntegrity(), 0.001);
    }

    @Test
    void warModifierReplacesPactAndAppliesReputationPenalty() {
        player.setGeneticIntegrity(85.0);
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor);

        player.getDiplomacyService().applyWar(neighbor);

        assertTrue(player.getDiplomacyService().isAtWarWith(neighbor));
        assertFalse(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertEquals(GameConstants.DIPLO_MODIFIER_WAR.getNameKey(),
                player.getDiplomaticModifierKey(neighbor.getId()));
        assertEquals(0, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(85.0, player.getGeneticIntegrity(), 0.001);
    }

    @Test
    void declareWarBlockedByActivePact() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.getDiplomacyService().formNonAggressionPact(neighbor);

        assertFalse(player.getDiplomacyService().canDeclareWar(neighbor, null));
    }

    @Test
    void declareWarRequiresMinimumPopulation() {
        Colony smallColony = new Colony(30, "Small", true);
        player.addColony(smallColony);
        player.setCapital(smallColony);
        assertFalse(DynastyDiplomacyService.meetsWarDeclarationPopulationRequirement(player));
        assertFalse(player.getDiplomacyService().canDeclareWar(neighbor, null));

        for (int i = 0; i < GameConstants.WAR_DECLARATION_MIN_POPULATION; i++) {
            smallColony.getWorkers().add(
                    new com.grimidk.formicempire.classes.entities.Ant(smallColony, GameConstants.TYPE_WORKER));
        }
        assertTrue(DynastyDiplomacyService.meetsWarDeclarationPopulationRequirement(player));
    }

    @Test
    void warServiceListsEachActiveWarOnce() {
        World world = buildBorderWorld();
        player.getDiplomacyService().applyWar(neighbor, null, world);

        List<com.grimidk.formicempire.classes.entities.War> wars = world.getWarService().getActiveWars();
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
        player.getDiplomacyService().formNonAggressionPact(neighbor);
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
        int needed = GameConstants.WAR_DECLARATION_MIN_POPULATION
                - dynasty.getStatService().getTotalPopulation(dynasty);
        for (int i = 0; i < needed; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
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

        neighbor.getDiplomacyService().requestNonAggressionPact(player, null);

        assertTrue(player.hasPendingPactRequestFrom(neighbor.getId()));
        assertFalse(neighbor.getDiplomacyService().hasNonAggressionPact(player));
    }

    @Test
    void acceptPendingPactFormsPact() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);
        player.addPendingPactRequest(neighbor.getId());

        player.getDiplomacyService().acceptNonAggressionPact(neighbor);

        assertTrue(player.getDiplomacyService().hasNonAggressionPact(neighbor));
        assertFalse(player.hasPendingPactRequestFrom(neighbor.getId()));
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
        assertEquals(GameConstants.COLONY_LOYALTY_MAX, capital.getEffectiveLoyalty(null, null));
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

        world.setMonth(6);
        assertTrue(neighbor.getDiplomacyService().canRequestTrade(player, world));
        assertEquals(60, neighbor.getDiplomaticReputation(player.getId()));
    }
}
