package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.CrossDynastyTradeProposal;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.entities.services.world.WarService;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyRebellionServiceTest {

    @AfterEach
    void clearRandomQueue() {
        GameRandom.clearTestDoubles();
    }

    @Test
    void monthlyChanceIsFivePercentAtBarelyRebellious() {
        assertEquals(0.05, DynastyRebellionService.computeMonthlyRebellionChance(19), 0.0001);
    }

    @Test
    void monthlyChanceRisesAsLoyaltyFalls() {
        assertTrue(DynastyRebellionService.computeMonthlyRebellionChance(0)
                > DynastyRebellionService.computeMonthlyRebellionChance(19));
        assertEquals(GameConstants.REBELLION_MONTHLY_CHANCE_MAX,
                DynastyRebellionService.computeMonthlyRebellionChance(0), 0.0001);
    }

    @Test
    void monthlyChanceIsZeroAboveRebelliousThreshold() {
        assertEquals(0.0, DynastyRebellionService.computeMonthlyRebellionChance(20));
    }

    @Test
    void capitalCannotBeRebellionRisk() {
        Colony capital = new Colony(1, "Prime", false);
        capital.setCapital(true);
        assertFalse(DynastyRebellionService.isRebellionRisk(capital, null, new World()));
    }

    @Test
    void rebelliousColonyCanJoinRebellion() {
        Colony colony = new Colony(2, "Satellite", false);
        colony.setLoyalty(15);
        assertTrue(DynastyRebellionService.canColonyJoinRebellion(colony, null, new World()));
    }

    @Test
    void disloyalColonyCanJoinRebellion() {
        Colony colony = new Colony(2, "Satellite", false);
        colony.setLoyalty(25);
        assertTrue(DynastyRebellionService.canColonyJoinRebellion(colony, null, new World()));
    }

    @Test
    void complacentColonyCannotJoinRebellion() {
        Colony colony = new Colony(3, "Satellite", false);
        colony.setLoyalty(45);
        assertFalse(DynastyRebellionService.canColonyJoinRebellion(colony, null, new World()));
    }

    @Test
    void joinChanceIncreasesWithProximityAndLowerLoyalty() {
        World world = new World();
        Colony seed = colonyOnHex(world, 1, "Seed", 0, 0);
        Colony near = colonyOnHex(world, 2, "Near", 1, 0);
        Colony far = colonyOnHex(world, 3, "Far", 4, 0);
        linkAdjacent(world, seed, near);
        near.setLoyalty(10);
        far.setLoyalty(10);

        double nearChance = DynastyRebellionService.computeJoinChance(seed, near, null, world);
        double farChance = DynastyRebellionService.computeJoinChance(seed, far, null, world);
        assertTrue(nearChance > farChance);

        near.setLoyalty(35);
        double loyalChance = DynastyRebellionService.computeJoinChance(seed, near, null, world);
        assertTrue(nearChance > loyalChance);
    }

    @Test
    void blockingRebellionWhileActiveRebellionExists() {
        World world = new World();
        Dynasty parent = new Dynasty(1, "Meat Dynasty", false, GameConstants.SPECIES_OMNI);
        Dynasty rebellion = new Dynasty(2, "Meat Rebellion", false, GameConstants.SPECIES_OMNI);
        rebellion.setOriginDynastyId(parent.getId());
        world.getDynastys().add(parent);
        world.getDynastys().add(rebellion);
        parent.setActiveRebellionDynastyId(rebellion.getId());
        assertTrue(DynastyRebellionService.hasBlockingRebellion(world, parent));
    }

    @Test
    void blockingRebellionWhilePlayerResponsePending() {
        World world = new World();
        Dynasty parent = new Dynasty(1, "Meat Dynasty", true, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(parent);
        parent.setPendingRebellionResponseFromId(99);
        assertTrue(DynastyRebellionService.hasBlockingRebellion(world, parent));
    }

    @Test
    void blockingRebellionClearsWhenRebelDynastyDefeated() {
        World world = new World();
        Dynasty parent = new Dynasty(1, "Meat Dynasty", false, GameConstants.SPECIES_OMNI);
        Dynasty rebellion = new Dynasty(2, "Meat Rebellion", false, GameConstants.SPECIES_OMNI);
        rebellion.setOriginDynastyId(parent.getId());
        world.getDynastys().add(parent);
        world.getDynastys().add(rebellion);
        parent.setActiveRebellionDynastyId(rebellion.getId());

        assertTrue(DynastyRebellionService.hasBlockingRebellion(world, parent));

        rebellion.setDefeated(true);
        assertFalse(DynastyRebellionService.hasBlockingRebellion(world, parent));
    }

    @Test
    void triggerRebellionCreatesDynastyAndQueuesPlayerResponse() {
        World world = buildParentWorld(true);
        Dynasty parent = world.getDynastys().get(0);
        Colony seed = parent.getColonies().stream().filter(c -> c.getId() == 2).findFirst().orElseThrow();

        DynastyRebellionService.triggerRebellion(world, new TradeManager(), parent, seed);

        assertEquals(1, parent.getColonies().size());
        assertTrue(parent.getPendingRebellionResponseFromId() > 0);
        Dynasty rebellion = world.findDynastyById(parent.getPendingRebellionResponseFromId());
        assertNotNull(rebellion);
        assertEquals(parent.getId(), rebellion.getOriginDynastyId());
        assertEquals(seed, rebellion.getCapital());
        assertEquals(1, rebellion.getColonies().size());
        assertTrue(rebellion.getName().toLowerCase().contains("rebellion")
                || rebellion.getName().toLowerCase().contains("rebelión")
                || rebellion.getName().toLowerCase().contains("rebelião")
                || rebellion.getName().toLowerCase().contains("rébellion"));
        assertTrue(parent.hasUpgrade(GameUnlocks.TYPE_WORKER));
        assertTrue(rebellion.hasUpgrade(GameUnlocks.TYPE_WORKER));
    }

    @Test
    void triggerRebellionRecallsColonyDiplomatsTargetingRebelColonies() {
        World world = buildParentWorld(false);
        Dynasty parent = world.getDynastys().get(0);
        Colony capital = parent.getCapital();
        Colony seed = parent.getColonies().stream().filter(c -> c.getId() == 2).findFirst().orElseThrow();
        capital.getOutgoingColonyDiplomatMissions().put(seed.getId(), 3);
        seed.getIncomingColonyDiplomatSupport().put(capital.getId(), 3);

        DynastyRebellionService.triggerRebellion(world, new TradeManager(), parent, seed);

        assertFalse(capital.getOutgoingColonyDiplomatMissions().containsKey(seed.getId()));
        assertFalse(seed.getIncomingColonyDiplomatSupport().containsKey(capital.getId()));
    }

    @Test
    void grantIndependenceAppliesPactAndReputationModifier() {
        World world = new World();
        Dynasty parent = new Dynasty(1, "Crystal Dynasty", false, GameConstants.SPECIES_OMNI);
        Dynasty rebellion = new Dynasty(2, "Crystal Rebellion", false, GameConstants.SPECIES_OMNI);
        rebellion.setOriginDynastyId(parent.getId());
        world.getDynastys().add(parent);
        world.getDynastys().add(rebellion);
        parent.setActiveRebellionDynastyId(rebellion.getId());

        DynastyRebellionService.grantIndependence(world, parent, rebellion);

        assertTrue(parent.getDiplomacyService().hasNonAggressionPact(rebellion));
        assertEquals(
                GameConstants.DIPLO_MODIFIER_GRANTED_INDEPENDENCE.getNameKey(),
                rebellion.getDiplomaticModifierKey(parent.getId()));
        assertEquals(
                GameConstants.DEFAULT_DIPLOMATIC_REPUTATION
                        + GameConstants.DIPLO_MODIFIER_PACT.getReputationDelta()
                        + GameConstants.DIPLO_MODIFIER_GRANTED_INDEPENDENCE.getReputationDelta(),
                rebellion.getDiplomaticReputation(parent.getId()));
        assertEquals(0, parent.getActiveRebellionDynastyId());
    }

    @Test
    void rebellionWarBlocksPeaceAndUsesRebellionName() {
        World world = buildParentWorld(false);
        Dynasty parent = world.getDynastys().get(0);
        Dynasty rebellion = new Dynasty(world.allocateDynastyId(), "Crystal Rebellion", false, GameConstants.SPECIES_OMNI);
        rebellion.setOriginDynastyId(parent.getId());
        rebellion.inheritProgressFrom(parent);
        world.getDynastys().add(rebellion);
        seedWarPopulation(parent);
        seedWarPopulation(rebellion);

        War war = world.getWarService().beginRebellionWar(parent, rebellion);
        assertNotNull(war);
        assertTrue(war.isRebellionWar());
        assertTrue(war.getDisplayName().toLowerCase().contains("rebellion")
                || war.getDisplayName().toLowerCase().contains("rebelión")
                || war.getDisplayName().toLowerCase().contains("rebelião")
                || war.getDisplayName().toLowerCase().contains("rébellion"));
        WarService warService = world.getWarService();
        assertFalse(warService.canOfferPeace(war, parent));
        assertFalse(warService.canAcceptPeaceOffer(war, rebellion));
    }

    @Test
    void onRebellionWarConcludedParentVictoryDefeatsRebellion() {
        World world = new World();
        Dynasty parent = new Dynasty(1, "Crystal Dynasty", false, GameConstants.SPECIES_OMNI);
        Dynasty rebellion = new Dynasty(2, "Crystal Rebellion", false, GameConstants.SPECIES_OMNI);
        rebellion.setOriginDynastyId(parent.getId());
        world.getDynastys().add(parent);
        world.getDynastys().add(rebellion);
        parent.setActiveRebellionDynastyId(rebellion.getId());

        War war = new War(1, parent.getId(), rebellion.getId(), 1, parent.getId(),
                "First Crystal Rebellion", 100, 80);
        war.setRebellionWar(true);

        DynastyRebellionService.onRebellionWarConcluded(world, war, parent.getId());

        assertTrue(rebellion.isDefeated());
        assertEquals(0, parent.getActiveRebellionDynastyId());
    }

    @Test
    void onRebellionWarConcludedRebellionVictoryDefeatsParent() {
        World world = new World();
        Dynasty parent = new Dynasty(1, "Crystal Dynasty", false, GameConstants.SPECIES_OMNI);
        Dynasty rebellion = new Dynasty(2, "Crystal Rebellion", false, GameConstants.SPECIES_OMNI);
        rebellion.setOriginDynastyId(parent.getId());
        world.getDynastys().add(parent);
        world.getDynastys().add(rebellion);
        parent.setActiveRebellionDynastyId(rebellion.getId());

        War war = new War(1, parent.getId(), rebellion.getId(), 1, parent.getId(),
                "First Crystal Rebellion", 100, 80);
        war.setRebellionWar(true);

        DynastyRebellionService.onRebellionWarConcluded(world, war, rebellion.getId());

        assertTrue(parent.isDefeated());
        assertEquals(0, parent.getActiveRebellionDynastyId());
    }

    @Test
    void npcChoosesFightWhenStronger() {
        Dynasty parent = new Dynasty(1, "Parent", false, GameConstants.SPECIES_OMNI);
        Dynasty rebellion = new Dynasty(2, "Rebellion", false, GameConstants.SPECIES_OMNI);
        parent.setMilitaryPower(200);
        rebellion.setMilitaryPower(50);
        for (int i = 0; i < 20; i++) {
            GameRandom.enqueueTestDoubles(0.0);
            assertTrue(DynastyRebellionService.npcChoosesFight(parent, rebellion));
        }
    }

    @Test
    void npcChoosesIndependenceWhenWeaker() {
        Dynasty parent = new Dynasty(1, "Parent", false, GameConstants.SPECIES_OMNI);
        Dynasty rebellion = new Dynasty(2, "Rebellion", false, GameConstants.SPECIES_OMNI);
        parent.setMilitaryPower(50);
        rebellion.setMilitaryPower(200);
        for (int i = 0; i < 20; i++) {
            GameRandom.enqueueTestDoubles(1.0);
            assertFalse(DynastyRebellionService.npcChoosesFight(parent, rebellion));
        }
    }

    @Test
    void expandRebellionStopsCascadeOnFirstDecline() {
        World world = buildCascadeWorld(false);
        Dynasty parent = world.getDynastys().get(0);
        Colony seed = colonyById(parent, 2);
        GameRandom.enqueueTestDoubles(0.0, 1.0);

        Set<Colony> joined = DynastyRebellionService.expandRebellion(seed, parent, null, world);

        assertEquals(2, joined.size());
        assertTrue(joined.contains(seed));
        assertTrue(joined.contains(colonyById(parent, 3)));
        assertFalse(joined.contains(colonyById(parent, 4)));
    }

    @Test
    void expandRebellionJoinsMultipleConnectedColonies() {
        World world = buildCascadeWorld(false);
        Dynasty parent = world.getDynastys().get(0);
        Colony seed = colonyById(parent, 2);
        GameRandom.enqueueTestDoubles(0.0, 0.0);

        Set<Colony> joined = DynastyRebellionService.expandRebellion(seed, parent, null, world);

        assertEquals(3, joined.size());
        assertTrue(joined.contains(colonyById(parent, 3)));
        assertTrue(joined.contains(colonyById(parent, 4)));
    }

    @Test
    void triggerRebellionCancelsCrossColonyTrade() {
        World world = buildParentWorld(false);
        Dynasty parent = world.getDynastys().get(0);
        Colony capital = parent.getCapital();
        Colony seed = colonyById(parent, 2);
        TradeManager tradeManager = new TradeManager();
        Trade crossTrade = new Trade(
                findHex(world, capital),
                findHex(world, seed),
                Map.of(),
                Map.of(),
                Map.of(GameConstants.TYPE_WORKER, 1),
                false,
                false,
                GameConstants.METHOD_LAND);
        tradeManager.addTrade(crossTrade);

        DynastyRebellionService.triggerRebellion(world, tradeManager, parent, seed);

        assertTrue(tradeManager.getActiveTrades().isEmpty());
    }

    @Test
    void triggerRebellionKeepsInternalTradeBetweenRebelColonies() {
        World world = buildCascadeWorld(true);
        Dynasty parent = world.getDynastys().get(0);
        Colony seed = colonyById(parent, 2);
        Colony joiner = colonyById(parent, 3);
        TradeManager tradeManager = new TradeManager();
        Trade internalTrade = new Trade(
                findHex(world, seed),
                findHex(world, joiner),
                Map.of(),
                Map.of(),
                Map.of(GameConstants.TYPE_WORKER, 1),
                false,
                false,
                GameConstants.METHOD_LAND);
        tradeManager.addTrade(internalTrade);
        GameRandom.enqueueTestDoubles(0.0);

        DynastyRebellionService.triggerRebellion(world, tradeManager, parent, seed);

        assertEquals(1, tradeManager.getActiveTrades().size());
        assertTrue(tradeManager.getActiveTrades().contains(internalTrade));
    }

    @Test
    void triggerRebellionClearsPendingTradeProposalsBetweenDynasties() {
        World world = new World();
        int parentId = world.allocateDynastyId();
        int reservedRebelId = parentId + 1;
        Dynasty parent = new Dynasty(parentId, "Crystal Dynasty", true, GameConstants.SPECIES_OMNI);
        Colony capital = colonyOnHex(world, 1, "Crystal Prime", 0, 0);
        Colony seed = colonyOnHex(world, 2, "Crystal Secundus", 1, 0);
        seed.setLoyalty(5);
        linkAdjacent(world, capital, seed);
        parent.addColony(capital);
        parent.addColony(seed);
        parent.setCapital(capital);
        world.getDynastys().add(parent);
        parent.addPendingTradeProposal(new CrossDynastyTradeProposal(
                reservedRebelId,
                capital.getId(),
                seed.getId(),
                CrossDynastyTradeProposal.Kind.REQUEST,
                Map.of()));

        DynastyRebellionService.triggerRebellion(world, new TradeManager(), parent, seed);

        Dynasty rebellion = world.findDynastyById(reservedRebelId);
        assertNotNull(rebellion);
        assertFalse(parent.hasPendingTradeProposalFrom(reservedRebelId));
        assertFalse(rebellion.hasPendingTradeProposalFrom(parentId));
    }

    @Test
    void triggerRebellionRemovesCrossDynastyTunnel() {
        World world = buildParentWorld(false);
        Dynasty parent = world.getDynastys().get(0);
        Colony capital = parent.getCapital();
        Colony seed = colonyById(parent, 2);
        Hex capitalHex = findHex(world, capital);
        Hex seedHex = findHex(world, seed);
        Tunnel crossTunnel = new Tunnel(capitalHex, seedHex, 1000);
        crossTunnel.restoreState(1000, true);
        parent.addTunnel(crossTunnel);

        DynastyRebellionService.triggerRebellion(world, new TradeManager(), parent, seed);

        assertFalse(parent.getTunnels().contains(crossTunnel));
    }

    @Test
    void triggerRebellionMovesInternalTunnelToRebellionDynasty() {
        World world = buildCascadeWorld(true);
        Dynasty parent = world.getDynastys().get(0);
        Colony seed = colonyById(parent, 2);
        Colony joiner = colonyById(parent, 3);
        Hex seedHex = findHex(world, seed);
        Hex joinerHex = findHex(world, joiner);
        Tunnel internalTunnel = new Tunnel(seedHex, joinerHex, 500);
        internalTunnel.restoreState(500, true);
        parent.addTunnel(internalTunnel);
        GameRandom.enqueueTestDoubles(0.0);

        DynastyRebellionService.triggerRebellion(world, new TradeManager(), parent, seed);

        Dynasty rebellion = world.findDynastyById(parent.getPendingRebellionResponseFromId());
        assertNotNull(rebellion);
        assertTrue(rebellion.getTunnels().contains(internalTunnel));
        assertFalse(parent.getTunnels().contains(internalTunnel));
    }

    @Test
    void runMonthlyChecksTriggersRebellionOnSuccessfulRoll() {
        World world = buildParentWorld(true);
        Dynasty parent = world.getDynastys().get(0);
        int dynastiesBefore = world.getDynastys().size();
        GameRandom.enqueueTestDoubles(0.0);

        DynastyRebellionService.runMonthlyChecks(world, new TradeManager());

        assertEquals(dynastiesBefore + 1, world.getDynastys().size());
        assertEquals(1, parent.getColonies().size());
        assertTrue(parent.getPendingRebellionResponseFromId() > 0);
    }

    @Test
    void runMonthlyChecksSkipsWhenRebellionAlreadyActive() {
        World world = buildParentWorld(false);
        Dynasty parent = world.getDynastys().get(0);
        Dynasty existing = new Dynasty(world.allocateDynastyId(), "Crystal Rebellion", false, GameConstants.SPECIES_OMNI);
        existing.setOriginDynastyId(parent.getId());
        world.getDynastys().add(existing);
        parent.setActiveRebellionDynastyId(existing.getId());
        GameRandom.enqueueTestDoubles(0.0);

        DynastyRebellionService.runMonthlyChecks(world, new TradeManager());

        assertEquals(2, parent.getColonies().size());
        assertEquals(existing.getId(), parent.getActiveRebellionDynastyId());
    }

    @Test
    void respondToRebellionClearsPendingPlayerResponse() {
        World world = buildParentWorld(true);
        Dynasty parent = world.getDynastys().get(0);
        Dynasty rebellion = new Dynasty(world.allocateDynastyId(), "Crystal Rebellion", false, GameConstants.SPECIES_OMNI);
        rebellion.setOriginDynastyId(parent.getId());
        world.getDynastys().add(rebellion);
        parent.setPendingRebellionResponseFromId(rebellion.getId());
        seedWarPopulation(parent);
        seedWarPopulation(rebellion);

        DynastyRebellionService.respondToRebellion(world, new TradeManager(), parent, rebellion, false);

        assertEquals(0, parent.getPendingRebellionResponseFromId());
    }

    @Test
    void rebellionDynastySavedOriginDynastyIdLoads() {
        Savefile.SavedDynasty saved = new Savefile.SavedDynasty();
        saved.id = 12;
        saved.name = "Crystal Rebellion";
        saved.titleKey = "DYNASTY_TITLE_DYNASTY";
        saved.speciesId = GameConstants.SPECIES_OMNI.getId();
        saved.originDynastyId = 7;

        Dynasty rebellion = new Dynasty(saved);

        assertEquals(7, rebellion.getOriginDynastyId());
    }

    private static World buildCascadeWorld(boolean playerParent) {
        World world = new World();
        int parentId = world.allocateDynastyId();
        Dynasty parent = new Dynasty(parentId, "Crystal Dynasty", playerParent, GameConstants.SPECIES_OMNI);
        Colony capital = colonyOnHex(world, 1, "Crystal Prime", 0, 0);
        Colony seed = colonyOnHex(world, 2, "Crystal Secundus", 1, 0);
        Colony joiner = colonyOnHex(world, 3, "Crystal Tertius", 2, 0);
        Colony beyond = colonyOnHex(world, 4, "Crystal Quartus", 3, 0);
        seed.setLoyalty(5);
        joiner.setLoyalty(15);
        beyond.setLoyalty(10);
        linkAdjacent(world, capital, seed);
        linkAdjacent(world, seed, joiner);
        linkAdjacent(world, joiner, beyond);
        parent.addColony(capital);
        parent.addColony(seed);
        parent.addColony(joiner);
        parent.addColony(beyond);
        parent.setCapital(capital);
        world.getDynastys().add(parent);
        return world;
    }

    private static Colony colonyById(Dynasty dynasty, int id) {
        return dynasty.getColonies().stream().filter(c -> c.getId() == id).findFirst().orElseThrow();
    }

    private static World buildParentWorld(boolean playerParent) {
        World world = new World();
        int parentId = world.allocateDynastyId();
        Dynasty parent = new Dynasty(parentId, "Crystal Dynasty", playerParent, GameConstants.SPECIES_OMNI);
        parent.unlockUpgrade(GameUnlocks.TYPE_WORKER);
        Colony capital = colonyOnHex(world, 1, "Crystal Prime", 0, 0);
        Colony seed = colonyOnHex(world, 2, "Crystal Secundus", 3, 0);
        seed.setLoyalty(5);
        parent.addColony(capital);
        parent.addColony(seed);
        parent.setCapital(capital);
        capital.setCapital(true);
        world.getDynastys().add(parent);
        return world;
    }

    private static Colony colonyOnHex(World world, int id, String name, int q, int r) {
        Hex hex = new Hex();
        hex.setQ(q);
        hex.setR(r);
        Colony colony = new Colony(id, name, false);
        hex.setColony(colony);
        if (world.getHexes() == null) {
            world.setHexes(new ArrayList<>());
        }
        world.getHexes().add(hex);
        return colony;
    }

    private static void linkAdjacent(World world, Colony a, Colony b) {
        Hex hexA = findHex(world, a);
        Hex hexB = findHex(world, b);
        hexA.setNorthEast(hexB);
        hexB.setSouthWest(hexA);
    }

    private static Hex findHex(World world, Colony colony) {
        for (Hex hex : world.getHexes()) {
            if (hex.getColony() == colony) {
                return hex;
            }
        }
        return null;
    }

    private static void seedWarPopulation(Dynasty dynasty) {
        if (dynasty.getColonies().isEmpty()) {
            Colony colony = new Colony(dynasty.getId() * 100, "Capital", false);
            colony.setCapital(true);
            dynasty.addColony(colony);
        }
        Colony colony = dynasty.getCapital();
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        int needed = GameConstants.WAR_DECLARATION_MIN_POPULATION
                - dynasty.getStatService().getTotalPopulation(dynasty);
        for (int i = 0; i < needed; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_WARRIOR, 5);
        colony.getWarAssignedRoleCounts().put(GameConstants.ROLE_DEFENDER, 5);
    }
}

