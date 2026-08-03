package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResearchTreeGraphTest {

    private Dynasty dynasty;
    private Colony colony;

    @BeforeEach
    void setUp() {
        dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        dynasty.setRank(GameConstants.RANK_DUCHY);
        colony.unlockUpgrade(GameUnlocks.TYPE_EGG);
        colony.unlockUpgrade(GameUnlocks.TYPE_WORKER);
    }

    @Test
    void buildIncludesEveryUpgradeCenteredOnEgg() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null);

        assertEquals(GameUnlocks.TYPE_EGG, result.getCenter());
        assertEquals(GameUnlocks.getUpgrades().size(), result.getNodes().size());

        ResearchTreeGraph.Node egg = result.getNodes().stream()
                .filter(n -> n.getUpgrade() == GameUnlocks.TYPE_EGG)
                .findFirst()
                .orElseThrow();
        assertEquals(0, egg.getDepth());
        assertEquals(0.0, egg.getPosX(), 0.0001);
        assertEquals(0.0, egg.getPosY(), 0.0001);
        assertEquals(ResearchTreeGraph.NodeState.OWNED, egg.getState());
    }

    @Test
    void statesCoverOwnedAffordableUnavailableAndTriggerProgress() {
        colony.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        colony.setResearchPoints(0);

        assertEquals(ResearchTreeGraph.NodeState.OWNED,
                ResearchTreeGraph.stateFor(colony, null, GameUnlocks.TYPE_WORKER));
        assertEquals(ResearchTreeGraph.NodeState.UNAVAILABLE,
                ResearchTreeGraph.stateFor(colony, null, GameUnlocks.ROLE_BUILDER));
        assertEquals(ResearchTreeGraph.NodeState.UNAVAILABLE,
                ResearchTreeGraph.stateFor(colony, null, GameUnlocks.ROLE_TRANSPORT));

        colony.setResearchPoints(GameUnlocks.ROLE_BUILDER.getCost());
        assertEquals(ResearchTreeGraph.NodeState.AFFORDABLE,
                ResearchTreeGraph.stateFor(colony, null, GameUnlocks.ROLE_BUILDER));

        Ant corpse = new Ant(colony, GameConstants.TYPE_DEAD);
        colony.getDeadAnts().add(corpse);
        assertEquals(ResearchTreeGraph.NodeState.TRIGGER_PROGRESS,
                ResearchTreeGraph.stateFor(colony, null, GameUnlocks.ROLE_GRAVER));
    }

    @Test
    void edgesFollowDirectRequirements() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null);

        boolean workerEdge = result.getEdges().stream().anyMatch(edge ->
                edge.getFrom() == GameUnlocks.TYPE_EGG
                        && edge.getTo() == GameUnlocks.TYPE_WORKER);
        boolean researchEdge = result.getEdges().stream().anyMatch(edge ->
                edge.getFrom() == GameUnlocks.STAT_RESEARCH_1
                        && edge.getTo() == GameUnlocks.STAT_RESEARCH_2);
        assertTrue(workerEdge);
        assertTrue(researchEdge);
    }

    @Test
    void nodesOccupyUniquePositions() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null);

        Set<String> cells = result.getNodes().stream()
                .map(n -> Math.round(n.getPosX() * 1000) + "," + Math.round(n.getPosY() * 1000))
                .collect(Collectors.toSet());
        assertEquals(result.getNodes().size(), cells.size());
    }

    @Test
    void nodesSitOnGridWithStepOnePointFive() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null);
        List<ResearchTreeGraph.Node> nodes = result.getNodes();
        for (ResearchTreeGraph.Node node : nodes) {
            double gx = node.getPosX() / ResearchTreeGraph.GRID_STEP;
            double gy = node.getPosY() / ResearchTreeGraph.GRID_STEP;
            assertTrue(Math.abs(gx - Math.round(gx)) < 1e-9,
                    node.getUpgrade().getNameKey() + " x not on grid");
            assertTrue(Math.abs(gy - Math.round(gy)) < 1e-9,
                    node.getUpgrade().getNameKey() + " y not on grid");
        }
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                double dx = nodes.get(j).getPosX() - nodes.get(i).getPosX();
                double dy = nodes.get(j).getPosY() - nodes.get(i).getPosY();
                assertTrue(Math.hypot(dx, dy) >= ResearchTreeGraph.GRID_STEP - 1e-9,
                        nodes.get(i).getUpgrade().getNameKey() + " overlaps "
                                + nodes.get(j).getUpgrade().getNameKey());
            }
        }
    }

    @Test
    void eggCenteredAtOrigin() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null);
        ResearchTreeGraph.Node egg = result.getNodes().stream()
                .filter(n -> n.getUpgrade() == GameUnlocks.TYPE_EGG)
                .findFirst()
                .orElseThrow();
        assertEquals(0.0, egg.getPosX(), 1e-9);
        assertEquals(0.0, egg.getPosY(), 1e-9);
    }
}
