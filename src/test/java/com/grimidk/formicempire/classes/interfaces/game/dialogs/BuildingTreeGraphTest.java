package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
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
    void buildIncludesEveryBuildingUnderBasicRoots() {
        BuildingTreeGraph.Result result = BuildingTreeGraph.build(colony);

        assertEquals(GameUnlocks.getBuildings().size(), result.getNodes().size());
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
    void edgesPointFromRequirementToChild() {
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
    void tierDividersSeparateConsecutiveTiers() {
        BuildingTreeGraph.Result result = BuildingTreeGraph.build(colony);
        assertFalse(result.getTierDividers().isEmpty());
        for (BuildingTreeGraph.TierDivider divider : result.getTierDividers()) {
            assertTrue(divider.getPosY() < 0);
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
        colony.unlockBuilding(GameUnlocks.ROYAL_CHAMBER_0);
        assertEquals(
                BuildingTreeGraph.NodeState.OWNED,
                BuildingTreeGraph.stateFor(colony, GameUnlocks.ROYAL_CHAMBER_0));
        assertEquals(
                BuildingTreeGraph.NodeState.UNAVAILABLE,
                BuildingTreeGraph.stateFor(colony, GameUnlocks.ROYAL_CHAMBER_2));
    }
}
