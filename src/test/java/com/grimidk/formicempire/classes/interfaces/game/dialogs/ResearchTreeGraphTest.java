package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void revealAllIncludesEveryUpgradeCenteredOnEgg() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null, true);

        assertEquals(GameUnlocks.TYPE_EGG, result.getCenter());
        assertEquals(GameUnlocks.getUpgrades().size(), result.getNodes().size());
        assertTrue(result.getNodes().stream()
                .allMatch(node -> node.getState() == ResearchTreeGraph.NodeState.OWNED));

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
    void revealAllWithNullColonyMarksEveryNodeOwned() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(null, null, true);

        assertEquals(GameUnlocks.getUpgrades().size(), result.getNodes().size());
        assertTrue(result.getNodes().stream()
                .allMatch(node -> node.getState() == ResearchTreeGraph.NodeState.OWNED));
    }

    @Test
    void buildWithNullColonyStillLayoutsFullEncyclopediaTree() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(null, null);

        assertEquals(GameUnlocks.TYPE_EGG, result.getCenter());
        assertEquals(GameUnlocks.getUpgrades().size(), result.getNodes().size());
        assertTrue(result.getNodes().stream()
                .allMatch(node -> node.getState() == ResearchTreeGraph.NodeState.UNAVAILABLE));
    }

    @Test
    void obscuredBuildKeepsStablePositionsMatchingFullLayout() {
        ResearchTreeGraph.Result obscured = ResearchTreeGraph.build(colony, null);
        ResearchTreeGraph.Result full = ResearchTreeGraph.build(colony, null, true);

        assertEquals(full.getMinX(), obscured.getMinX(), 1e-9);
        assertEquals(full.getMaxX(), obscured.getMaxX(), 1e-9);
        assertEquals(full.getMinY(), obscured.getMinY(), 1e-9);
        assertEquals(full.getMaxY(), obscured.getMaxY(), 1e-9);

        Map<Upgrade, ResearchTreeGraph.Node> fullByUpgrade = full.getNodes().stream()
                .collect(Collectors.toMap(ResearchTreeGraph.Node::getUpgrade, n -> n));
        for (ResearchTreeGraph.Node node : obscured.getNodes()) {
            ResearchTreeGraph.Node match = fullByUpgrade.get(node.getUpgrade());
            assertEquals(match.getPosX(), node.getPosX(), 1e-9, node.getUpgrade().getNameKey());
            assertEquals(match.getPosY(), node.getPosY(), 1e-9, node.getUpgrade().getNameKey());
        }
    }

    @Test
    void obscuredBuildShowsOwnedAndDirectChildrenOnly() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null);

        Set<Upgrade> visible = result.getNodes().stream()
                .map(ResearchTreeGraph.Node::getUpgrade)
                .collect(Collectors.toSet());
        assertTrue(visible.contains(GameUnlocks.TYPE_EGG));
        assertTrue(visible.contains(GameUnlocks.TYPE_WORKER));
        assertTrue(visible.contains(GameUnlocks.ROLE_BUILDER));
        assertFalse(visible.contains(GameUnlocks.ABILITY_BUILD));
        assertTrue(result.getNodes().size() < GameUnlocks.getUpgrades().size());

        assertFalse(result.getEdges().stream().anyMatch(edge ->
                edge.getTo() == GameUnlocks.ABILITY_BUILD));
    }

    @Test
    void triggerProgressShowsPastUnlockFrontier() {
        assertFalse(colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER));
        colony.setResearchPoints(GameNumbers.TRIGGER_RESEARCH_MIN_RP);

        assertEquals(
                ResearchTreeGraph.NodeState.TRIGGER_PROGRESS,
                ResearchTreeGraph.stateFor(colony, null, GameUnlocks.ABILITY_RESEARCH));
        assertTrue(ResearchTreeGraph.isVisible(colony, null, GameUnlocks.ABILITY_RESEARCH));

        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null);
        assertTrue(result.getNodes().stream()
                .anyMatch(n -> n.getUpgrade() == GameUnlocks.ABILITY_RESEARCH));
    }

    @Test
    void unlockingDoesNotMoveExistingNodes() {
        ResearchTreeGraph.Result before = ResearchTreeGraph.build(colony, null);
        Map<Upgrade, double[]> positions = before.getNodes().stream()
                .collect(Collectors.toMap(
                        ResearchTreeGraph.Node::getUpgrade,
                        n -> new double[] {n.getPosX(), n.getPosY()}));

        colony.unlockUpgrade(GameUnlocks.ROLE_BUILDER);
        ResearchTreeGraph.Result after = ResearchTreeGraph.build(colony, null);
        for (ResearchTreeGraph.Node node : after.getNodes()) {
            double[] prior = positions.get(node.getUpgrade());
            if (prior == null) {
                continue;
            }
            assertEquals(prior[0], node.getPosX(), 1e-9, node.getUpgrade().getNameKey());
            assertEquals(prior[1], node.getPosY(), 1e-9, node.getUpgrade().getNameKey());
        }
    }

    @Test
    void assimilationProgressShowsPastUnlockFrontier() {
        assertFalse(colony.hasUpgrade(GameUnlocks.ABILITY_ASSIMILATION));
        dynasty.setCurrentAssimilation(GameUnlocks.ASSIMILATION_LEAFCUTTER);

        assertEquals(
                ResearchTreeGraph.NodeState.SPECIAL_PROGRESS,
                ResearchTreeGraph.stateFor(colony, null, GameUnlocks.ASSIMILATED_FARMING));
        assertTrue(ResearchTreeGraph.isVisible(colony, null, GameUnlocks.ASSIMILATED_FARMING));

        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null);
        assertTrue(result.getNodes().stream()
                .anyMatch(n -> n.getUpgrade() == GameUnlocks.ASSIMILATED_FARMING));
    }

    @Test
    void unlockingRevealsNextFrontier() {
        colony.unlockUpgrade(GameUnlocks.ROLE_BUILDER);
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null);

        Set<Upgrade> visible = result.getNodes().stream()
                .map(ResearchTreeGraph.Node::getUpgrade)
                .collect(Collectors.toSet());
        assertTrue(visible.contains(GameUnlocks.ABILITY_BUILD));
        assertTrue(result.getEdges().stream().anyMatch(edge ->
                edge.getFrom() == GameUnlocks.ROLE_BUILDER
                        && edge.getTo() == GameUnlocks.ABILITY_BUILD));
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
    void princessTypeIsTier1AndStillRequiresThatTier() {
        dynasty.setRank(GameConstants.RANK_COLONY);
        colony.setResearchPoints(GameUnlocks.TYPE_PRINCESS.getCost());

        assertEquals(GameConstants.TIER_1, GameUnlocks.TYPE_PRINCESS.getTier());
        assertEquals(ResearchTreeGraph.NodeState.UNAVAILABLE,
                ResearchTreeGraph.stateFor(colony, null, GameUnlocks.TYPE_PRINCESS));

        dynasty.setRank(GameConstants.RANK_COUNTY);
        assertEquals(ResearchTreeGraph.NodeState.AFFORDABLE,
                ResearchTreeGraph.stateFor(colony, null, GameUnlocks.TYPE_PRINCESS));

        colony.setResearchPoints(GameUnlocks.TYPE_PRINCESS.getCost() - 1);
        assertEquals(ResearchTreeGraph.NodeState.UNAVAILABLE,
                ResearchTreeGraph.stateFor(colony, null, GameUnlocks.TYPE_PRINCESS));
    }

    @Test
    void edgesFollowDirectRequirementsAmongVisibleNodes() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null);

        boolean workerEdge = result.getEdges().stream().anyMatch(edge ->
                edge.getFrom() == GameUnlocks.TYPE_EGG
                        && edge.getTo() == GameUnlocks.TYPE_WORKER);
        assertTrue(workerEdge);

        ResearchTreeGraph.Result revealed = ResearchTreeGraph.build(colony, null, true);
        boolean researchEdge = revealed.getEdges().stream().anyMatch(edge ->
                edge.getFrom() == GameUnlocks.STAT_RESEARCH_1
                        && edge.getTo() == GameUnlocks.STAT_RESEARCH_2);
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
    void nodesStayNearTheirDepthRing() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null, true);
        Set<Upgrade> manual = Set.of(
                GameUnlocks.STAT_RESEARCH_2,
                GameUnlocks.STAT_RESEARCH_3,
                GameUnlocks.STAT_PASSIVE_1,
                GameUnlocks.ROLE_BUILDER,
                GameUnlocks.ROLE_DIPLOMAT,
                GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_2,
                GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_3,
                GameUnlocks.TYPE_SOLDIER,
                GameUnlocks.ROLE_CATCHER,
                GameUnlocks.ROLE_CRANE,
                GameUnlocks.STAT_SKELETON,
                GameUnlocks.STAT_ATTACK_2,
                GameUnlocks.STAT_ATTACK_SPEED_1,
                GameUnlocks.STAT_DEFENSE_1,
                GameUnlocks.STAT_DEFENSE_2,
                GameUnlocks.ROLE_LAYER,
                GameUnlocks.ROLE_POTTER);
        double maxRingError = ResearchTreeGraph.GRID_STEP * 3.0;
        for (ResearchTreeGraph.Node node : result.getNodes()) {
            if (node.getDepth() == 0 || manual.contains(node.getUpgrade())) {
                continue;
            }
            double radius = Math.hypot(node.getPosX(), node.getPosY());
            double expected = node.getDepth() * ResearchTreeGraph.RING_SPACING;
            assertTrue(Math.abs(radius - expected) <= maxRingError,
                    node.getUpgrade().getNameKey()
                            + " radius " + radius
                            + " expected near " + expected);
        }
    }

    @Test
    void manualPlacementsFollowRequestedOffsets() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null, true);
        Map<Upgrade, ResearchTreeGraph.Node> byUpgrade = result.getNodes().stream()
                .collect(Collectors.toMap(ResearchTreeGraph.Node::getUpgrade, n -> n));

        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_RESEARCH_1).getPosX(),
                byUpgrade.get(GameUnlocks.STAT_RESEARCH_2).getPosX(),
                1e-9);
        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_RESEARCH_1).getPosY() - ResearchTreeGraph.GRID_STEP,
                byUpgrade.get(GameUnlocks.STAT_RESEARCH_2).getPosY(),
                1e-9);
        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_RESEARCH_2).getPosY() - ResearchTreeGraph.GRID_STEP,
                byUpgrade.get(GameUnlocks.STAT_RESEARCH_3).getPosY(),
                1e-9);
        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_RESEARCH_3).getPosX() + ResearchTreeGraph.GRID_STEP,
                byUpgrade.get(GameUnlocks.STAT_PASSIVE_1).getPosX(),
                1e-9);
        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_RESEARCH_3).getPosY(),
                byUpgrade.get(GameUnlocks.STAT_PASSIVE_1).getPosY(),
                1e-9);

        assertEquals(
                byUpgrade.get(GameUnlocks.ROLE_DIPLOMAT).getPosY(),
                byUpgrade.get(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_2).getPosY(),
                1e-9);
        assertEquals(
                byUpgrade.get(GameUnlocks.ROLE_DIPLOMAT).getPosX() + ResearchTreeGraph.GRID_STEP,
                byUpgrade.get(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_2).getPosX(),
                1e-9);
        assertEquals(
                byUpgrade.get(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_2).getPosX()
                        + ResearchTreeGraph.GRID_STEP,
                byUpgrade.get(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_3).getPosX(),
                1e-9);

        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_ATTACK_1).getPosX(),
                byUpgrade.get(GameUnlocks.STAT_ATTACK_SPEED_1).getPosX(),
                1e-9);
        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_ATTACK_1).getPosY() + ResearchTreeGraph.GRID_STEP,
                byUpgrade.get(GameUnlocks.STAT_ATTACK_SPEED_1).getPosY(),
                1e-9);

        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_ATTACK_1).getPosX() - ResearchTreeGraph.GRID_STEP,
                byUpgrade.get(GameUnlocks.STAT_ATTACK_2).getPosX(),
                1e-9);
        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_ATTACK_1).getPosY(),
                byUpgrade.get(GameUnlocks.STAT_ATTACK_2).getPosY(),
                1e-9);

        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_HEALTH_1).getPosX(),
                byUpgrade.get(GameUnlocks.STAT_SKELETON).getPosX(),
                1e-9);
        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_HEALTH_1).getPosY() - ResearchTreeGraph.GRID_STEP,
                byUpgrade.get(GameUnlocks.STAT_SKELETON).getPosY(),
                1e-9);

        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_DEFENSE_1).getPosX(),
                byUpgrade.get(GameUnlocks.STAT_DEFENSE_2).getPosX(),
                1e-9);
        assertEquals(
                byUpgrade.get(GameUnlocks.STAT_DEFENSE_1).getPosY() + ResearchTreeGraph.GRID_STEP,
                byUpgrade.get(GameUnlocks.STAT_DEFENSE_2).getPosY(),
                1e-9);

        assertEquals(
                byUpgrade.get(GameUnlocks.TYPE_WORKER).getPosX() + ResearchTreeGraph.GRID_STEP,
                byUpgrade.get(GameUnlocks.ROLE_POTTER).getPosX(),
                1e-9);
        assertEquals(
                byUpgrade.get(GameUnlocks.TYPE_WORKER).getPosY() - ResearchTreeGraph.GRID_STEP,
                byUpgrade.get(GameUnlocks.ROLE_POTTER).getPosY(),
                1e-9);
    }

    @Test
    void fullTreeKeepsEdgeCrossingsLow() {
        ResearchTreeGraph.Result result = ResearchTreeGraph.build(colony, null, true);
        Map<Upgrade, double[]> positions = result.getNodes().stream()
                .collect(Collectors.toMap(
                        ResearchTreeGraph.Node::getUpgrade,
                        n -> new double[] {n.getPosX(), n.getPosY()}));
        int crossings = ResearchTreeGraph.countEdgeCrossings(positions, result.getEdges());
        assertTrue(crossings <= 12, "too many crossings: " + crossings);
    }

    @Test
    void refreshStatesRebuildsFrontierAfterUnlock() {
        ResearchTreeGraph.Result before = ResearchTreeGraph.build(colony, null);
        assertFalse(before.getNodes().stream()
                .anyMatch(n -> n.getUpgrade() == GameUnlocks.ABILITY_BUILD));

        colony.unlockUpgrade(GameUnlocks.ROLE_BUILDER);
        ResearchTreeGraph.Result after = ResearchTreeGraph.refreshStates(before, colony, null);

        ResearchTreeGraph.Node builder = after.getNodes().stream()
                .filter(n -> n.getUpgrade() == GameUnlocks.ROLE_BUILDER)
                .findFirst()
                .orElseThrow();
        assertEquals(ResearchTreeGraph.NodeState.OWNED, builder.getState());
        assertTrue(after.getNodes().stream()
                .anyMatch(n -> n.getUpgrade() == GameUnlocks.ABILITY_BUILD));
    }

    @Test
    void buyButtonTooltipNamesTierWithAntCount() {
        dynasty.setRank(GameConstants.RANK_COLONY);
        colony.unlockUpgrade(GameUnlocks.TYPE_EGG);
        colony.setResearchPoints(GameUnlocks.TYPE_PRINCESS.getCost());

        String tip = ResearchTreeGraph.buyButtonTooltip(colony, null, GameUnlocks.TYPE_PRINCESS);

        assertTrue(tip.contains(GameConstants.TIER_1.getName()), tip);
        assertTrue(tip.contains(AssetStyles.formatNumber(GameConstants.RANK_COUNTY.getPopulation())), tip);
    }

    @Test
    void buyButtonTooltipNamesAssimilationReward() {
        String tip = ResearchTreeGraph.buyButtonTooltip(
                colony, null, GameUnlocks.ASSIMILATED_FIREVENOM);

        assertTrue(tip.contains(GameUnlocks.ASSIMILATION_FIREVENOM.getName()), tip);
        assertEquals(
                GameUnlocks.ASSIMILATION_FIREVENOM,
                ResearchTreeGraph.assimilationForReward(GameUnlocks.ASSIMILATED_FIREVENOM));
    }

    @Test
    void buyButtonTooltipNamesSynergyReward() {
        String tip = ResearchTreeGraph.buyButtonTooltip(colony, null, GameUnlocks.SYNERGY_SUPER_VENOM);

        assertTrue(tip.contains(GameUnlocks.SUPER_VENOM_SYNERGY.getName()), tip);
        assertEquals(
                GameUnlocks.SUPER_VENOM_SYNERGY,
                ResearchTreeGraph.synergyForReward(GameUnlocks.SYNERGY_SUPER_VENOM));
    }

    @Test
    void buyButtonTooltipNamesAssimilationForSubtypeRoles() {
        dynasty.setRank(GameConstants.RANK_COLONY);
        colony.unlockUpgrade(GameUnlocks.TYPE_WORKER);
        colony.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);

        String potterTip = ResearchTreeGraph.buyButtonTooltip(colony, null, GameUnlocks.ROLE_POTTER);
        assertNotNull(potterTip);
        assertTrue(potterTip.contains(GameUnlocks.ASSIMILATION_HONEYPOT.getName()), potterTip);

        String defenderTip = ResearchTreeGraph.buyButtonTooltip(colony, null, GameUnlocks.ROLE_DEFENDER);
        assertNotNull(defenderTip);
        assertTrue(defenderTip.contains(GameUnlocks.ASSIMILATION_DOORHEAD.getName()), defenderTip);

        assertEquals(GameUnlocks.ASSIMILATION_HONEYPOT,
                GameUnlocks.getAssimilationRequiredForSubtypeRoleUpgrade(GameUnlocks.ROLE_POTTER));
        assertEquals(GameUnlocks.ASSIMILATION_DOORHEAD,
                GameUnlocks.getAssimilationRequiredForSubtypeRoleUpgrade(GameUnlocks.ROLE_DEFENDER));
        assertFalse(GameUnlocks.meetsSubtypeRoleAssimilationRequirement(dynasty, GameUnlocks.ROLE_POTTER));

        colony.unlockUpgrade(GameUnlocks.ASSIMILATED_HONEYPOT);
        assertTrue(GameUnlocks.meetsSubtypeRoleAssimilationRequirement(dynasty, GameUnlocks.ROLE_POTTER));
    }

    @Test
    void buyButtonTooltipReportsMissingParentAndNotEnoughRp() {
        dynasty.setRank(GameConstants.RANK_DUCHY);
        colony.setResearchPoints(0);

        String missingParent = ResearchTreeGraph.buyButtonTooltip(
                colony, null, GameUnlocks.ROLE_ASSISTANT);
        assertTrue(missingParent.contains(GameUnlocks.TYPE_PRINCESS.getDisplayName()), missingParent);

        colony.unlockUpgrade(GameUnlocks.TYPE_PRINCESS);
        String missingRp = ResearchTreeGraph.buyButtonTooltip(colony, null, GameUnlocks.ROLE_ASSISTANT);
        assertTrue(missingRp.contains(
                LanguageStrings.get(LanguageStrings.UPGRADE_NOT_ENOUGH_RP)), missingRp);

        colony.setResearchPoints(GameUnlocks.ROLE_ASSISTANT.getCost());
        assertEquals(null, ResearchTreeGraph.buyButtonTooltip(colony, null, GameUnlocks.ROLE_ASSISTANT));
    }
}
