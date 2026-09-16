package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.dynasty.Rank;
import com.grimidk.formicempire.classes.constants.misc.Tier;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastySynergyService;
import com.grimidk.formicempire.classes.entities.services.shared.TriggerProgressService;
import com.grimidk.formicempire.classes.entities.services.shared.TriggerProgressService.TriggerProgress;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ResearchTreeGraph {

    public static final double GRID_STEP = 1.5;
    public static final double RING_SPACING = GRID_STEP * 2.5;

    public enum NodeState {
        OWNED,
        AFFORDABLE,
        TRIGGER_PROGRESS,
        SPECIAL_PROGRESS,
        UNAVAILABLE
    }

    public enum EdgeShape {
        AXIS,
        DIAGONAL,
        SKEWED
    }

    public static final class Node {
        private final Upgrade upgrade;
        private final NodeState state;
        private final int depth;
        private double posX;
        private double posY;

        Node(Upgrade upgrade, NodeState state, int depth, double posX, double posY) {
            this.upgrade = upgrade;
            this.state = state;
            this.depth = depth;
            this.posX = posX;
            this.posY = posY;
        }

        public Upgrade getUpgrade() {
            return upgrade;
        }

        public NodeState getState() {
            return state;
        }

        public int getDepth() {
            return depth;
        }

        public double getPosX() {
            return posX;
        }

        public double getPosY() {
            return posY;
        }

        public void setPosX(double posX) {
            this.posX = posX;
        }

        public void setPosY(double posY) {
            this.posY = posY;
        }
    }

    public static final class Edge {
        private final Upgrade from;
        private final Upgrade to;

        Edge(Upgrade from, Upgrade to) {
            this.from = from;
            this.to = to;
        }

        public Upgrade getFrom() {
            return from;
        }

        public Upgrade getTo() {
            return to;
        }
    }

    public static final class Result {
        private final List<Node> nodes;
        private final List<Edge> edges;
        private final int maxDepth;
        private final Upgrade center;
        private final double minX;
        private final double maxX;
        private final double minY;
        private final double maxY;

        Result(
                List<Node> nodes,
                List<Edge> edges,
                int maxDepth,
                Upgrade center,
                double minX,
                double maxX,
                double minY,
                double maxY) {
            this.nodes = Collections.unmodifiableList(nodes);
            this.edges = Collections.unmodifiableList(edges);
            this.maxDepth = maxDepth;
            this.center = center;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public List<Edge> getEdges() {
            return edges;
        }

        public int getMaxDepth() {
            return maxDepth;
        }

        public Upgrade getCenter() {
            return center;
        }

        public double getMinX() {
            return minX;
        }

        public double getMaxX() {
            return maxX;
        }

        public double getMinY() {
            return minY;
        }

        public double getMaxY() {
            return maxY;
        }
    }

    private ResearchTreeGraph() {
    }

    private static final class LayoutCache {
        private final Map<Upgrade, double[]> positions;
        private final Map<Upgrade, Integer> depths;
        private final List<Edge> edges;
        private final Upgrade center;
        private final int maxDepth;
        private final double minX;
        private final double maxX;
        private final double minY;
        private final double maxY;

        private LayoutCache(
                Map<Upgrade, double[]> positions,
                Map<Upgrade, Integer> depths,
                List<Edge> edges,
                Upgrade center,
                int maxDepth,
                double minX,
                double maxX,
                double minY,
                double maxY) {
            this.positions = positions;
            this.depths = depths;
            this.edges = edges;
            this.center = center;
            this.maxDepth = maxDepth;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }
    }

    private static volatile LayoutCache layoutCache;
    private static volatile Map<Upgrade, Assimilation> assimilationByReward;
    private static volatile Map<Upgrade, Synergy> synergyByReward;

    public static NodeState stateFor(Colony colony, Engine engine, Upgrade upgrade) {
        if (colony == null || upgrade == null) {
            return NodeState.UNAVAILABLE;
        }
        if (colony.hasUpgrade(upgrade)) {
            return NodeState.OWNED;
        }
        Map<Upgrade, TriggerProgress> triggers = TriggerProgressService.indexByUpgrade(colony, engine);
        return stateFor(
                colony,
                engine,
                upgrade,
                triggers,
                assimilationRewardIndex(),
                synergyRewardIndex());
    }

    public static Assimilation assimilationForReward(Upgrade upgrade) {
        if (upgrade == null) {
            return null;
        }
        return assimilationRewardIndex().get(upgrade);
    }

    public static Synergy synergyForReward(Upgrade upgrade) {
        if (upgrade == null) {
            return null;
        }
        return synergyRewardIndex().get(upgrade);
    }

    public static String formatTierRequirement(Tier tier) {
        if (tier == null) {
            return "";
        }
        Rank rank = tier.getRankRequirement();
        long ants = rank != null ? rank.getPopulation() : 0L;
        return LanguageStrings.format(
                LanguageStrings.UPGRADE_REQUIRES_TIER_ANTS_FMT,
                tier.getName(),
                AssetStyles.formatNumber(ants));
    }

    public static String buyButtonTooltip(Colony colony, Engine engine, Upgrade upgrade) {
        if (colony == null || upgrade == null || colony.hasUpgrade(upgrade)) {
            return null;
        }
        if (stateFor(colony, engine, upgrade) == NodeState.AFFORDABLE) {
            return null;
        }

        List<String> parts = new ArrayList<>();
        Assimilation assimilation = assimilationForReward(upgrade);
        if (assimilation != null) {
            parts.add(LanguageStrings.format(
                    LanguageStrings.UPGRADE_REQUIRES_ASSIMILATION_FMT, assimilation.getName()));
        }
        Synergy synergy = synergyForReward(upgrade);
        if (synergy != null) {
            parts.add(LanguageStrings.format(
                    LanguageStrings.UPGRADE_REQUIRES_SYNERGY_FMT, synergy.getName()));
        }

        Assimilation subtypeAssimilation = GameUnlocks.getAssimilationRequiredForSubtypeRoleUpgrade(upgrade);
        if (subtypeAssimilation != null
                && (colony.getDynasty() == null
                        || subtypeAssimilation.getReward() == null
                        || !colony.hasUpgrade(subtypeAssimilation.getReward()))) {
            String assimilationPart = LanguageStrings.format(
                    LanguageStrings.UPGRADE_REQUIRES_ASSIMILATION_FMT, subtypeAssimilation.getName());
            if (!parts.contains(assimilationPart)) {
                parts.add(assimilationPart);
            }
        }

        if (assimilation == null && synergy == null) {
            Upgrade requirement = upgrade.getRequirement();
            Dynasty dynasty = colony.getDynasty();
            boolean infinite = engine != null && engine.isInfiniteResearch();
            boolean requirementIsSubtypeAssimilationReward = subtypeAssimilation != null
                    && requirement != null
                    && requirement == subtypeAssimilation.getReward();
            if (!infinite
                    && requirement != null
                    && !colony.hasUpgrade(requirement)
                    && !requirementIsSubtypeAssimilationReward) {
                parts.add(requirement.getDisplayName());
            }
            if (!infinite && !upgrade.isAvailableFor(dynasty)) {
                parts.add(formatTierRequirement(upgrade.getTier()));
            }
            if (!infinite && !GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, upgrade)) {
                TriggerProgress progress = TriggerProgressService.find(colony, engine, upgrade);
                if (progress != null) {
                    String metric = progress.getMetricLabel();
                    if (metric != null && !metric.isBlank()) {
                        parts.add(metric);
                    } else {
                        parts.add(progress.getHint());
                    }
                }
            }
            boolean gatesMet = (requirement == null || colony.hasUpgrade(requirement))
                    && (infinite || upgrade.isAvailableFor(dynasty))
                    && (infinite || GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, upgrade))
                    && GameUnlocks.meetsSubtypeRoleAssimilationRequirement(dynasty, upgrade);
            if (!infinite && gatesMet && upgrade.getCost() > 0 && colony.getResearchPoints() < upgrade.getCost()) {
                parts.add(LanguageStrings.get(LanguageStrings.UPGRADE_NOT_ENOUGH_RP));
            } else if (!infinite && parts.isEmpty() && upgrade.getCost() <= 0) {
                TriggerProgress progress = TriggerProgressService.find(colony, engine, upgrade);
                if (progress != null) {
                    String metric = progress.getMetricLabel();
                    if (metric != null && !metric.isBlank()) {
                        parts.add(metric);
                    } else {
                        parts.add(progress.getHint());
                    }
                }
            }
        }

        if (parts.isEmpty()) {
            return null;
        }
        return LanguageStrings.format(
                LanguageStrings.UPGRADE_REQUIRES_FMT,
                String.join(LanguageStrings.get(LanguageStrings.UPGRADE_REQUIRES_LIST_SEPARATOR), parts));
    }

    private static NodeState stateFor(
            Colony colony,
            Engine engine,
            Upgrade upgrade,
            Map<Upgrade, TriggerProgress> triggers,
            Map<Upgrade, Assimilation> assimilations,
            Map<Upgrade, Synergy> synergies) {
        if (colony == null || upgrade == null) {
            return NodeState.UNAVAILABLE;
        }
        if (colony.hasUpgrade(upgrade)) {
            return NodeState.OWNED;
        }

        Dynasty dynasty = colony.getDynasty();
        boolean infinite = engine != null && engine.isInfiniteResearch();
        boolean reqMet = infinite
                || upgrade.getRequirement() == null
                || colony.hasUpgrade(upgrade.getRequirement());
        boolean extraMet = infinite || GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, upgrade);
        boolean tierMet = infinite || upgrade.isAvailableFor(dynasty);
        boolean subtypeAssimMet = GameUnlocks.meetsSubtypeRoleAssimilationRequirement(dynasty, upgrade);
        boolean gatesMet = reqMet && extraMet && tierMet && subtypeAssimMet;

        if (gatesMet && upgrade.getCost() > 0 && colony.getResearchPoints() >= upgrade.getCost()) {
            return NodeState.AFFORDABLE;
        }
        if (gatesMet && infinite && assimilations.get(upgrade) == null && synergies.get(upgrade) == null) {
            return NodeState.AFFORDABLE;
        }

        TriggerProgress trigger = triggers.get(upgrade);
        if (trigger != null && trigger.isGateMet() && trigger.getCurrent() > 0) {
            return NodeState.TRIGGER_PROGRESS;
        }

        if (hasSpecialProgress(dynasty, upgrade, gatesMet, assimilations, synergies)) {
            return NodeState.SPECIAL_PROGRESS;
        }

        return NodeState.UNAVAILABLE;
    }

    private static boolean hasSpecialProgress(
            Dynasty dynasty,
            Upgrade upgrade,
            boolean gatesMet,
            Map<Upgrade, Assimilation> assimilations,
            Map<Upgrade, Synergy> synergies) {
        if (dynasty == null || upgrade == null) {
            return false;
        }

        Assimilation assimilation = assimilations.get(upgrade);
        if (assimilation != null) {
            if (dynasty.getCurrentAssimilation() == assimilation) {
                return true;
            }
            if (gatesMet && isAssimilationAvailableNow(dynasty, assimilation)) {
                return true;
            }
        }

        Synergy synergy = synergies.get(upgrade);
        return synergy != null && DynastySynergyService.isPartiallyComplete(dynasty, synergy);
    }

    private static Map<Upgrade, Assimilation> assimilationRewardIndex() {
        Map<Upgrade, Assimilation> cached = assimilationByReward;
        if (cached != null) {
            return cached;
        }
        synchronized (ResearchTreeGraph.class) {
            if (assimilationByReward == null) {
                Map<Upgrade, Assimilation> map = new HashMap<>();
                for (Assimilation assimilation : GameUnlocks.getAssimilations()) {
                    if (assimilation.getReward() != null) {
                        map.put(assimilation.getReward(), assimilation);
                    }
                }
                assimilationByReward = map;
            }
            return assimilationByReward;
        }
    }

    private static Map<Upgrade, Synergy> synergyRewardIndex() {
        Map<Upgrade, Synergy> cached = synergyByReward;
        if (cached != null) {
            return cached;
        }
        synchronized (ResearchTreeGraph.class) {
            if (synergyByReward == null) {
                Map<Upgrade, Synergy> map = new HashMap<>();
                for (Synergy synergy : GameUnlocks.getSynergies()) {
                    if (synergy.getReward() != null) {
                        map.put(synergy.getReward(), synergy);
                    }
                }
                synergyByReward = map;
            }
            return synergyByReward;
        }
    }

    private static boolean isAssimilationAvailableNow(Dynasty dynasty, Assimilation assimilation) {
        return GameUnlocks.isAssimilationAvailable(dynasty, assimilation);
    }

    public static EdgeShape classifyEdge(double fromX, double fromY, double toX, double toY) {
        double dx = Math.abs(toX - fromX);
        double dy = Math.abs(toY - fromY);
        if (dx < 1e-9 || dy < 1e-9) {
            return EdgeShape.AXIS;
        }
        if (Math.abs(dx - dy) < 1e-9) {
            return EdgeShape.DIAGONAL;
        }
        return EdgeShape.SKEWED;
    }

    public static boolean isVisible(Colony colony, Engine engine, Upgrade upgrade) {
        if (colony == null || upgrade == null) {
            return false;
        }
        if (colony.hasUpgrade(upgrade)) {
            return true;
        }
        Upgrade requirement = upgrade.getRequirement();
        if (requirement == null) {
            return true;
        }
        if (colony.hasUpgrade(requirement)) {
            return true;
        }
        NodeState state = stateFor(colony, engine, upgrade);
        return state == NodeState.AFFORDABLE
                || state == NodeState.TRIGGER_PROGRESS
                || state == NodeState.SPECIAL_PROGRESS;
    }

    private static boolean isVisible(Upgrade upgrade, Colony colony, Map<Upgrade, NodeState> nodeStates) {
        if (colony == null || upgrade == null) {
            return false;
        }
        if (colony.hasUpgrade(upgrade)) {
            return true;
        }
        Upgrade requirement = upgrade.getRequirement();
        if (requirement == null) {
            return true;
        }
        if (colony.hasUpgrade(requirement)) {
            return true;
        }
        NodeState state = nodeStates.get(upgrade);
        return state == NodeState.AFFORDABLE
                || state == NodeState.TRIGGER_PROGRESS
                || state == NodeState.SPECIAL_PROGRESS;
    }

    public static Result build(Colony colony, Engine engine) {
        return build(colony, engine, false);
    }

    public static Result build(Colony colony, Engine engine, boolean revealAll) {
        LayoutCache layout = ensureLayoutCache();
        if (layout == null || layout.center == null) {
            return emptyResult();
        }

        Map<Upgrade, Assimilation> assimilations = assimilationRewardIndex();
        Map<Upgrade, Synergy> synergies = synergyRewardIndex();
        Map<Upgrade, TriggerProgress> triggers = (!revealAll && colony != null)
                ? TriggerProgressService.indexByUpgrade(colony, engine)
                : Map.of();

        Map<Upgrade, NodeState> nodeStates = new LinkedHashMap<>();
        for (Upgrade upgrade : layout.positions.keySet()) {
            NodeState state;
            if (revealAll) {
                state = NodeState.OWNED;
            } else if (colony == null) {
                state = NodeState.UNAVAILABLE;
            } else {
                state = stateFor(colony, engine, upgrade, triggers, assimilations, synergies);
            }
            nodeStates.put(upgrade, state);
        }

        boolean obscure = !revealAll && colony != null;
        Set<Upgrade> visible = new HashSet<>();
        if (obscure) {
            for (Upgrade upgrade : nodeStates.keySet()) {
                if (isVisible(upgrade, colony, nodeStates)) {
                    visible.add(upgrade);
                }
            }
            visible.add(layout.center);
        } else {
            visible.addAll(nodeStates.keySet());
        }

        List<Node> nodes = new ArrayList<>();
        for (Map.Entry<Upgrade, NodeState> entry : nodeStates.entrySet()) {
            Upgrade upgrade = entry.getKey();
            if (!visible.contains(upgrade)) {
                continue;
            }
            double[] pos = layout.positions.get(upgrade);
            if (pos == null) {
                continue;
            }
            nodes.add(new Node(
                    upgrade,
                    entry.getValue(),
                    layout.depths.getOrDefault(upgrade, 0),
                    pos[0],
                    pos[1]));
        }
        nodes.sort(Comparator
                .comparingInt(Node::getDepth)
                .thenComparingDouble(Node::getPosY)
                .thenComparingDouble(Node::getPosX)
                .thenComparingInt(n -> n.getUpgrade().getId()));

        List<Edge> edges = new ArrayList<>();
        for (Edge edge : layout.edges) {
            if (visible.contains(edge.getFrom()) && visible.contains(edge.getTo())) {
                edges.add(edge);
            }
        }
        return new Result(
                nodes,
                edges,
                layout.maxDepth,
                layout.center,
                layout.minX,
                layout.maxX,
                layout.minY,
                layout.maxY);
    }

    public static Result refreshStates(Result previous, Colony colony, Engine engine) {
        return build(colony, engine);
    }

    private static Result emptyResult() {
        return new Result(List.of(), List.of(), -1, null, 0, 0, 0, 0);
    }

    private static LayoutCache ensureLayoutCache() {
        LayoutCache cached = layoutCache;
        if (cached != null) {
            return cached;
        }
        synchronized (ResearchTreeGraph.class) {
            if (layoutCache != null) {
                return layoutCache;
            }
            layoutCache = computeLayoutCache();
            return layoutCache;
        }
    }

    private static LayoutCache computeLayoutCache() {
        com.grimidk.formicempire.classes.constants.unlocks.Upgrade center = com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks.TYPE_EGG;
        java.util.Map<com.grimidk.formicempire.classes.constants.unlocks.Upgrade, NodeState> nodeStates = new java.util.LinkedHashMap<>();
        for (com.grimidk.formicempire.classes.constants.unlocks.Upgrade upgrade : com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks.getUpgrades()) {
            nodeStates.put(upgrade, NodeState.UNAVAILABLE);
        }

        java.util.List<Edge> edges = new java.util.ArrayList<>();
        for (com.grimidk.formicempire.classes.constants.unlocks.Upgrade child : com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks.getUpgrades()) {
            com.grimidk.formicempire.classes.constants.unlocks.Upgrade parent = child.getRequirement();
            if (parent != null) {
                edges.add(new Edge(parent, child));
            }
        }

        java.util.Map<com.grimidk.formicempire.classes.constants.unlocks.Upgrade, double[]> posByUpgrade = new java.util.HashMap<>();
        for (com.grimidk.formicempire.classes.constants.unlocks.Upgrade u : com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks.getUpgrades()) {
            posByUpgrade.put(u, new double[]{u.getGridX() * GRID_STEP, u.getGridY() * GRID_STEP});
        }

        double minX = 0, maxX = 0, minY = 0, maxY = 0;
        for (double[] pos : posByUpgrade.values()) {
            minX = Math.min(minX, pos[0]);
            maxX = Math.max(maxX, pos[0]);
            minY = Math.min(minY, pos[1]);
            maxY = Math.max(maxY, pos[1]);
        }

        return new LayoutCache(
                java.util.Collections.unmodifiableMap(posByUpgrade),
                java.util.Collections.emptyMap(),
                java.util.Collections.unmodifiableList(edges),
                center,
                0,
                minX,
                maxX,
                minY,
                maxY);
    }
}
