package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
    void requestTradeRequiresCordialReputation() {
        player.setDiplomaticReputation(neighbor.getId(), 59);
        neighbor.setDiplomaticReputation(player.getId(), 59);

        player.getDiplomacyService().requestTrade(neighbor, null);

        assertEquals(59, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(59, neighbor.getDiplomaticReputation(player.getId()));
    }

    @Test
    void requestTradeAppliesReputationPenaltyBothWays() {
        player.setDiplomaticReputation(neighbor.getId(), 60);
        neighbor.setDiplomaticReputation(player.getId(), 60);

        player.getDiplomacyService().requestTrade(neighbor, null);

        assertEquals(55, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(55, neighbor.getDiplomaticReputation(player.getId()));
    }

    @Test
    void crossDynastyTradeBonusAppliesOncePerPair() {
        player.setDiplomaticReputation(neighbor.getId(), 50);
        neighbor.setDiplomaticReputation(player.getId(), 50);

        player.getDiplomacyService().onCrossDynastyTradeEstablished(neighbor);
        player.getDiplomacyService().onCrossDynastyTradeEstablished(neighbor);

        assertEquals(70, player.getDiplomaticReputation(neighbor.getId()));
        assertEquals(70, neighbor.getDiplomaticReputation(player.getId()));
        assertTrue(player.hasCrossDynastyTradeRepBonus(neighbor.getId()));
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
}
