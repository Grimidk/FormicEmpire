package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildingTreeGraphTest {

    private Dynasty dynasty;
    private Colony colony;

    @BeforeEach
    void setUp() {
        dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        dynasty.setRank(GameConstants.RANK_DUCHY);
        colony.unlockUpgrade(GameUnlocks.ROLE_BUILDER);
        colony.unlockUpgrade(GameUnlocks.ABILITY_BUILD);
    }

    @Test
    void revealAllIncludesEveryBuildingUnderBasicRoots() {
        BuildingTreeGraph.Result result = BuildingTreeGraph.build(colony, true);

        assertEquals(GameUnlocks.getBuildings().size(), result.getNodes().size());
        assertTrue(result.getNodes().stream()
                .allMatch(node -> node.getState() == BuildingTreeGraph.NodeState.OWNED));
        assertFalse(result.getRoots().isEmpty());
        for (Building root : result.getRoots()) {
            assertEquals(null, root.getRequirement());
        }

        Set<Building> rooted = new HashSet<>();
        for (BuildingTreeGraph.Node node : result.getNodes()) {
            Building cursor = node.getBuilding();
            while (cursor.getRequirement() != null) {
                cursor = cursor.getRequirement();
            }
            assertTrue(result.getRoots().contains(cursor), node.getBuilding().getNameKey());
            rooted.add(node.getBuilding());
        }
        assertEquals(GameUnlocks.getBuildings().size(), rooted.size());
    }

    @Test
    void obscuredBuildShowsOwnedAndDirectChildrenOnly() {
        BuildingTreeGraph.Result result = BuildingTreeGraph.build(colony);

        Set<Building> visible = result.getNodes().stream()
                .map(BuildingTreeGraph.Node::getBuilding)
                .collect(Collectors.toSet());
        assertTrue(visible.contains(GameUnlocks.ROYAL_CHAMBER_0));
        assertTrue(visible.contains(GameUnlocks.ROYAL_CHAMBER_1));
        assertFalse(visible.contains(GameUnlocks.ROYAL_CHAMBER_2));
        assertFalse(visible.contains(GameUnlocks.PASSIVE_LAB));
        assertTrue(result.getNodes().size() < GameUnlocks.getBuildings().size());
        assertTrue(result.getEdges().stream().noneMatch(edge ->
                edge.getTo() == GameUnlocks.ROYAL_CHAMBER_2
                        || edge.getTo() == GameUnlocks.PASSIVE_LAB));
    }

    @Test
    void obscuredBuildKeepsStablePositionsMatchingFullLayout() {
        BuildingTreeGraph.Result obscured = BuildingTreeGraph.build(colony);
        BuildingTreeGraph.Result full = BuildingTreeGraph.build(colony, true);

        assertEquals(full.getMinX(), obscured.getMinX(), 1e-9);
        assertEquals(full.getMaxX(), obscured.getMaxX(), 1e-9);
        assertEquals(full.getMinY(), obscured.getMinY(), 1e-9);
        assertEquals(full.getMaxY(), obscured.getMaxY(), 1e-9);

        Map<Building, BuildingTreeGraph.Node> fullByBuilding = full.getNodes().stream()
                .collect(Collectors.toMap(BuildingTreeGraph.Node::getBuilding, n -> n));
        for (BuildingTreeGraph.Node node : obscured.getNodes()) {
            BuildingTreeGraph.Node match = fullByBuilding.get(node.getBuilding());
            assertEquals(match.getPosX(), node.getPosX(), 1e-9, node.getBuilding().getNameKey());
            assertEquals(match.getPosY(), node.getPosY(), 1e-9, node.getBuilding().getNameKey());
        }
    }

    @Test
    void unlockingDoesNotMoveExistingNodes() {
        BuildingTreeGraph.Result before = BuildingTreeGraph.build(colony);
        Map<Building, double[]> positions = before.getNodes().stream()
                .collect(Collectors.toMap(
                        BuildingTreeGraph.Node::getBuilding,
                        n -> new double[] {n.getPosX(), n.getPosY()}));

        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_1);
        BuildingTreeGraph.Result after = BuildingTreeGraph.build(colony);
        for (BuildingTreeGraph.Node node : after.getNodes()) {
            double[] prior = positions.get(node.getBuilding());
            if (prior == null) {
                continue;
            }
            assertEquals(prior[0], node.getPosX(), 1e-9, node.getBuilding().getNameKey());
            assertEquals(prior[1], node.getPosY(), 1e-9, node.getBuilding().getNameKey());
        }
    }

    @Test
    void unlockingRevealsDirectChildrenAndEdges() {
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_1);
        BuildingTreeGraph.Result result = BuildingTreeGraph.build(colony);

        Set<Building> visible = result.getNodes().stream()
                .map(BuildingTreeGraph.Node::getBuilding)
                .collect(Collectors.toSet());
        assertTrue(visible.contains(GameUnlocks.ROYAL_CHAMBER_2));
        assertTrue(visible.contains(GameUnlocks.PASSIVE_LAB));
        assertFalse(visible.contains(GameUnlocks.ROYAL_CHAMBER_3));

        assertTrue(result.getEdges().stream().anyMatch(edge ->
                edge.getFrom() == GameUnlocks.ROYAL_CHAMBER_1
                        && edge.getTo() == GameUnlocks.PASSIVE_LAB));
    }

    @Test
    void edgesPointFromRequirementToChild() {
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_1);
        BuildingTreeGraph.Result result = BuildingTreeGraph.build(colony);
        boolean royalEdge = result.getEdges().stream().anyMatch(edge ->
                edge.getFrom() == GameUnlocks.ROYAL_CHAMBER_0
                        && edge.getTo() == GameUnlocks.ROYAL_CHAMBER_1);
        boolean passiveEdge = result.getEdges().stream().anyMatch(edge ->
                edge.getFrom() == GameUnlocks.ROYAL_CHAMBER_1
                        && edge.getTo() == GameUnlocks.PASSIVE_LAB);
        assertTrue(royalEdge);
        assertTrue(passiveEdge);
    }

    @Test
    void higherTiersSitAboveBasics() {
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_1);
        BuildingTreeGraph.Result result = BuildingTreeGraph.build(colony);
        BuildingTreeGraph.Node basic = result.getNodes().stream()
                .filter(n -> n.getBuilding() == GameUnlocks.ROYAL_CHAMBER_0)
                .findFirst()
                .orElseThrow();
        BuildingTreeGraph.Node upstairs = result.getNodes().stream()
                .filter(n -> n.getBuilding() == GameUnlocks.ROYAL_CHAMBER_2)
                .findFirst()
                .orElseThrow();
        assertTrue(upstairs.getPosY() < basic.getPosY());
    }

    @Test
    void tierDividersIncludeTier0BelowBasicsAndBetweenOccupiedAdjacentTiers() {
        BuildingTreeGraph.Result result = BuildingTreeGraph.build(colony);
        assertFalse(result.getTierDividers().isEmpty());

        Set<Integer> occupied = result.getNodes().stream()
                .map(BuildingTreeGraph.Node::getTierIndex)
                .collect(Collectors.toSet());
        assertTrue(occupied.contains(0));
        assertTrue(occupied.contains(1));
        assertTrue(result.getTierDividers().stream().anyMatch(d -> d.getUpperTier() == 0));
        assertTrue(result.getTierDividers().stream().anyMatch(d -> d.getUpperTier() == 1));

        BuildingTreeGraph.TierDivider tier0Line = result.getTierDividers().stream()
                .filter(d -> d.getUpperTier() == 0)
                .findFirst()
                .orElseThrow();
        assertEquals(BuildingTreeGraph.TIER_SPACING / 2.0, tier0Line.getPosY(), 1e-9);
        assertTrue(result.getMaxY() >= tier0Line.getPosY());

        double tier0Y = 0.0;
        double tier1Y = -BuildingTreeGraph.TIER_SPACING;
        BuildingTreeGraph.TierDivider tier1Line = result.getTierDividers().stream()
                .filter(d -> d.getUpperTier() == 1)
                .findFirst()
                .orElseThrow();
        assertEquals((tier0Y + tier1Y) / 2.0, tier1Line.getPosY(), 1e-9);

        for (BuildingTreeGraph.TierDivider divider : result.getTierDividers()) {
            int upper = divider.getUpperTier();
            assertTrue(upper >= 0 && upper < GameConstants.getTiers().size());
            assertTrue(occupied.contains(upper));
            if (upper > 0) {
                assertTrue(occupied.contains(upper - 1));
                assertTrue(divider.getPosY() < 0);
            } else {
                assertTrue(divider.getPosY() > 0);
            }
        }

        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_1);
        BuildingTreeGraph.Result deeper = BuildingTreeGraph.build(colony);
        Set<Integer> deeperOccupied = deeper.getNodes().stream()
                .map(BuildingTreeGraph.Node::getTierIndex)
                .collect(Collectors.toSet());
        assertTrue(deeper.getTierDividers().stream().anyMatch(d -> d.getUpperTier() == 0));
        for (BuildingTreeGraph.TierDivider divider : deeper.getTierDividers()) {
            int upper = divider.getUpperTier();
            assertTrue(deeperOccupied.contains(upper));
            if (upper > 0) {
                assertTrue(deeperOccupied.contains(upper - 1));
            }
        }
    }

    @Test
    void nodesOccupyUniquePositions() {
        BuildingTreeGraph.Result result = BuildingTreeGraph.build(colony);
        Set<String> cells = result.getNodes().stream()
                .map(n -> Math.round(n.getPosX() * 1000) + "," + Math.round(n.getPosY() * 1000))
                .collect(Collectors.toSet());
        assertEquals(result.getNodes().size(), cells.size());
    }

    @Test
    void ownedAndUnavailableStates() {
        assertEquals(
                BuildingTreeGraph.NodeState.OWNED,
                BuildingTreeGraph.stateFor(colony, GameUnlocks.ROYAL_CHAMBER_0));
        assertEquals(
                BuildingTreeGraph.NodeState.UNAVAILABLE,
                BuildingTreeGraph.stateFor(colony, GameUnlocks.ROYAL_CHAMBER_2));
    }

    @Test
    void refreshStatesRebuildsFrontierAfterUnlock() {
        BuildingTreeGraph.Result before = BuildingTreeGraph.build(colony);
        assertFalse(before.getNodes().stream()
                .anyMatch(n -> n.getBuilding() == GameUnlocks.ROYAL_CHAMBER_2));

        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_1);
        BuildingTreeGraph.Result after = BuildingTreeGraph.refreshStates(before, colony);

        BuildingTreeGraph.Node owned = after.getNodes().stream()
                .filter(n -> n.getBuilding() == GameUnlocks.ROYAL_CHAMBER_1)
                .findFirst()
                .orElseThrow();
        assertEquals(BuildingTreeGraph.NodeState.OWNED, owned.getState());
        assertTrue(after.getNodes().stream()
                .anyMatch(n -> n.getBuilding() == GameUnlocks.ROYAL_CHAMBER_2));
    }

    @Test
    void instantBuildingsIgnoresTierRequirement() {
        dynasty.setRank(GameConstants.RANK_COLONY);
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_1);

        assertEquals(
                BuildingTreeGraph.NodeState.UNAVAILABLE,
                BuildingTreeGraph.stateFor(colony, GameUnlocks.ROYAL_CHAMBER_2));
        assertFalse(colony.startBuildingProject(GameUnlocks.ROYAL_CHAMBER_2));

        Engine engine = new Engine();
        engine.setInstantBuildings(true);
        World world = new World();
        engine.setWorld(world);
        world.registerDynasty(dynasty);

        assertEquals(
                BuildingTreeGraph.NodeState.AFFORDABLE,
                BuildingTreeGraph.stateFor(colony, GameUnlocks.ROYAL_CHAMBER_2));
        assertTrue(colony.startBuildingProject(GameUnlocks.ROYAL_CHAMBER_2));
        assertTrue(colony.hasBuilding(GameUnlocks.ROYAL_CHAMBER_2));
    }
}
