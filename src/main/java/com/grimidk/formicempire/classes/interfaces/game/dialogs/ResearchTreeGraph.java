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
        private final double posX;
        private final double posY;

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
        Upgrade center = GameUnlocks.TYPE_EGG;
        Map<Upgrade, NodeState> nodeStates = new LinkedHashMap<>();
        for (Upgrade upgrade : GameUnlocks.getUpgrades()) {
            nodeStates.put(upgrade, NodeState.UNAVAILABLE);
        }
        if (!nodeStates.containsKey(center)) {
            return new LayoutCache(
                    Map.of(),
                    Map.of(),
                    List.of(),
                    null,
                    -1,
                    0,
                    0,
                    0,
                    0);
        }

        Map<Upgrade, Upgrade> parentByUpgrade = new HashMap<>();
        List<Edge> allEdges = new ArrayList<>();
        for (Upgrade upgrade : nodeStates.keySet()) {
            if (upgrade == center) {
                continue;
            }
            Upgrade requirement = upgrade.getRequirement();
            if (requirement != null && nodeStates.containsKey(requirement)) {
                parentByUpgrade.put(upgrade, requirement);
                allEdges.add(new Edge(requirement, upgrade));
            } else {
                parentByUpgrade.put(upgrade, center);
            }
        }

        Result laidOut = layoutRadial(nodeStates, parentByUpgrade, allEdges, center);
        Map<Upgrade, double[]> positions = new HashMap<>();
        Map<Upgrade, Integer> depths = new HashMap<>();
        for (Node node : laidOut.getNodes()) {
            positions.put(node.getUpgrade(), new double[] {node.getPosX(), node.getPosY()});
            depths.put(node.getUpgrade(), node.getDepth());
        }
        return new LayoutCache(
                Collections.unmodifiableMap(positions),
                Collections.unmodifiableMap(depths),
                Collections.unmodifiableList(new ArrayList<>(laidOut.getEdges())),
                center,
                laidOut.getMaxDepth(),
                laidOut.getMinX(),
                laidOut.getMaxX(),
                laidOut.getMinY(),
                laidOut.getMaxY());
    }

    private static Result layoutRadial(
            Map<Upgrade, NodeState> nodeStates,
            Map<Upgrade, Upgrade> parentByUpgrade,
            List<Edge> edges,
            Upgrade center) {
        Map<Upgrade, List<Upgrade>> children = new HashMap<>();
        for (Upgrade upgrade : nodeStates.keySet()) {
            children.put(upgrade, new ArrayList<>());
        }
        for (Map.Entry<Upgrade, Upgrade> entry : parentByUpgrade.entrySet()) {
            children.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }
        for (List<Upgrade> kids : children.values()) {
            kids.sort(Comparator.comparingInt(Upgrade::getCost).thenComparingInt(Upgrade::getId));
        }

        Map<Upgrade, Integer> subtreeSizes = new HashMap<>();
        subtreeSize(center, children, subtreeSizes);

        Map<Upgrade, Integer> depthByUpgrade = new HashMap<>();
        Map<Upgrade, double[]> roughPos = new HashMap<>();
        placeRadial(center, 0.0, 360.0, 0, children, subtreeSizes, depthByUpgrade, roughPos);

        Map<Upgrade, double[]> posByUpgrade = snapToFreeGridCells(
                center, roughPos, depthByUpgrade, parentByUpgrade);
        resolveEdgeCrossings(posByUpgrade, edges, depthByUpgrade, parentByUpgrade);
        applyManualPlacements(posByUpgrade);

        List<Node> nodes = new ArrayList<>();
        int maxDepth = 0;
        double minX = 0;
        double maxX = 0;
        double minY = 0;
        double maxY = 0;
        for (Upgrade upgrade : nodeStates.keySet()) {
            int depth = depthByUpgrade.getOrDefault(upgrade, 0);
            double[] pos = posByUpgrade.get(upgrade);
            if (pos == null) {
                pos = new double[] {0, 0};
            }
            maxDepth = Math.max(maxDepth, depth);
            minX = Math.min(minX, pos[0]);
            maxX = Math.max(maxX, pos[0]);
            minY = Math.min(minY, pos[1]);
            maxY = Math.max(maxY, pos[1]);
            nodes.add(new Node(upgrade, nodeStates.get(upgrade), depth, pos[0], pos[1]));
        }
        nodes.sort(Comparator
                .comparingInt(Node::getDepth)
                .thenComparingDouble(Node::getPosY)
                .thenComparingDouble(Node::getPosX)
                .thenComparingInt(n -> n.getUpgrade().getId()));

        return new Result(nodes, edges, maxDepth, center, minX, maxX, minY, maxY);
    }

    private static int subtreeSize(
            Upgrade node,
            Map<Upgrade, List<Upgrade>> children,
            Map<Upgrade, Integer> memo) {
        Integer cached = memo.get(node);
        if (cached != null) {
            return cached;
        }
        int size = 1;
        for (Upgrade child : children.getOrDefault(node, List.of())) {
            size += subtreeSize(child, children, memo);
        }
        memo.put(node, size);
        return size;
    }

    private static void placeRadial(
            Upgrade node,
            double startDegrees,
            double endDegrees,
            int depth,
            Map<Upgrade, List<Upgrade>> children,
            Map<Upgrade, Integer> subtreeSizes,
            Map<Upgrade, Integer> depthByUpgrade,
            Map<Upgrade, double[]> posByUpgrade) {
        depthByUpgrade.put(node, depth);
        if (depth == 0) {
            posByUpgrade.put(node, new double[] {0, 0});
        } else {
            double mid = (startDegrees + endDegrees) / 2.0;
            double radians = Math.toRadians(mid);
            double radius = depth * RING_SPACING;
            posByUpgrade.put(node, new double[] {
                    Math.cos(radians) * radius,
                    Math.sin(radians) * radius
            });
        }

        List<Upgrade> kids = children.getOrDefault(node, List.of());
        if (kids.isEmpty()) {
            return;
        }
        int total = 0;
        for (Upgrade kid : kids) {
            total += subtreeSizes.getOrDefault(kid, 1);
        }
        double cursor = startDegrees;
        double spanTotal = endDegrees - startDegrees;
        if (spanTotal <= 0) {
            spanTotal = 360.0;
            cursor = 0.0;
        }
        for (Upgrade kid : kids) {
            double share = spanTotal * subtreeSizes.getOrDefault(kid, 1) / (double) Math.max(1, total);
            placeRadial(
                    kid,
                    cursor,
                    cursor + share,
                    depth + 1,
                    children,
                    subtreeSizes,
                    depthByUpgrade,
                    posByUpgrade);
            cursor += share;
        }
    }

    private static Map<Upgrade, double[]> snapToFreeGridCells(
            Upgrade center,
            Map<Upgrade, double[]> roughPos,
            Map<Upgrade, Integer> depthByUpgrade,
            Map<Upgrade, Upgrade> parentByUpgrade) {
        List<Upgrade> order = new ArrayList<>(roughPos.keySet());
        order.sort(Comparator
                .comparingInt((Upgrade u) -> depthByUpgrade.getOrDefault(u, 0))
                .thenComparingInt(Upgrade::getId));

        Set<Long> occupied = new HashSet<>();
        Map<Upgrade, double[]> snapped = new HashMap<>();
        occupied.add(cellKey(0, 0));
        snapped.put(center, new double[] {0, 0});

        for (Upgrade upgrade : order) {
            if (upgrade == center) {
                continue;
            }
            double[] rough = roughPos.get(upgrade);
            int gx = (int) Math.round(rough[0] / GRID_STEP);
            int gy = (int) Math.round(rough[1] / GRID_STEP);
            if (gx == 0 && gy == 0) {
                gx = 1;
            }
            int depth = Math.max(1, depthByUpgrade.getOrDefault(upgrade, 1));
            double[] parentPos = snapped.get(parentByUpgrade.get(upgrade));
            int[] free = nearestFreeCell(gx, gy, depth, parentPos, occupied);
            occupied.add(cellKey(free[0], free[1]));
            snapped.put(upgrade, new double[] {free[0] * GRID_STEP, free[1] * GRID_STEP});
        }
        return snapped;
    }

    private static int[] nearestFreeCell(
            int startX,
            int startY,
            int depth,
            double[] parentPos,
            Set<Long> occupied) {
        double targetRadius = depth * (RING_SPACING / GRID_STEP);
        double parentAngle = parentPos == null
                ? Math.atan2(startY, startX)
                : Math.atan2(startY * GRID_STEP - parentPos[1], startX * GRID_STEP - parentPos[0]);

        int bestX = startX;
        int bestY = startY;
        double bestScore = Double.POSITIVE_INFINITY;
        boolean found = false;

        int maxRadius = Math.max(48, depth * 8);
        for (int radius = 0; radius <= maxRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int dyAbs = radius - Math.abs(dx);
                int[] dys = dyAbs == 0 ? new int[] {0} : new int[] {dyAbs, -dyAbs};
                for (int dy : dys) {
                    int x = startX + dx;
                    int y = startY + dy;
                    if (x == 0 && y == 0) {
                        continue;
                    }
                    if (occupied.contains(cellKey(x, y))) {
                        continue;
                    }
                    double cellRadius = Math.hypot(x, y);
                    double ringError = Math.abs(cellRadius - targetRadius);
                    double angle = Math.atan2(y, x);
                    double angleError = Math.abs(normalizeAngle(angle - parentAngle));
                    double moveCost = Math.hypot(dx, dy);
                    double score = moveCost + ringError * 4.0 + angleError * 2.0;
                    if (score < bestScore) {
                        bestScore = score;
                        bestX = x;
                        bestY = y;
                        found = true;
                    }
                }
            }
            if (found && radius >= 2) {
                break;
            }
        }
        if (!found) {
            return new int[] {startX + maxRadius, startY};
        }
        return new int[] {bestX, bestY};
    }

    private static void applyManualPlacements(Map<Upgrade, double[]> posByUpgrade) {
        Set<Long> occupied = new HashSet<>();
        for (Map.Entry<Upgrade, double[]> entry : posByUpgrade.entrySet()) {
            double[] pos = entry.getValue();
            occupied.add(cellKey(
                    (int) Math.round(pos[0] / GRID_STEP),
                    (int) Math.round(pos[1] / GRID_STEP)));
        }

        placeRelative(posByUpgrade, occupied, GameUnlocks.STAT_RESEARCH_2, GameUnlocks.STAT_RESEARCH_1, 0, -1);
        placeRelative(posByUpgrade, occupied, GameUnlocks.STAT_RESEARCH_3, GameUnlocks.STAT_RESEARCH_2, 0, -1);
        placeRelative(posByUpgrade, occupied, GameUnlocks.STAT_PASSIVE_1, GameUnlocks.STAT_RESEARCH_3, 1, 0);

        nudge(posByUpgrade, occupied, GameUnlocks.ROLE_BUILDER, -1, 0);

        nudge(posByUpgrade, occupied, GameUnlocks.ROLE_DIPLOMAT, -2, 0);
        placeRelative(posByUpgrade, occupied, GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_2, GameUnlocks.ROLE_DIPLOMAT, 1, 0);
        placeRelative(posByUpgrade, occupied, GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_3, GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_2, 1, 0);

        nudgeSubtree(posByUpgrade, occupied, GameUnlocks.TYPE_SOLDIER, -1, 0);
        nudge(posByUpgrade, occupied, GameUnlocks.ROLE_CATCHER, 0, -1);
        nudge(posByUpgrade, occupied, GameUnlocks.ROLE_CRANE, 1, 0);

        placeRelative(posByUpgrade, occupied, GameUnlocks.STAT_SKELETON, GameUnlocks.STAT_HEALTH_1, 0, -1);
        placeRelative(posByUpgrade, occupied, GameUnlocks.STAT_ATTACK_2, GameUnlocks.STAT_ATTACK_1, -1, 0);
        placeRelative(posByUpgrade, occupied, GameUnlocks.STAT_ATTACK_SPEED_1, GameUnlocks.STAT_ATTACK_1, 0, 1);
        nudge(posByUpgrade, occupied, GameUnlocks.STAT_DEFENSE_1, -1, 0);
        placeRelative(posByUpgrade, occupied, GameUnlocks.STAT_DEFENSE_2, GameUnlocks.STAT_DEFENSE_1, 0, 1);

        nudge(posByUpgrade, occupied, GameUnlocks.ROLE_LAYER, -1, 0);

        placeRelative(posByUpgrade, occupied, GameUnlocks.ROLE_POTTER, GameUnlocks.TYPE_WORKER, 1, -1);
    }

    private static void nudgeSubtree(
            Map<Upgrade, double[]> posByUpgrade,
            Set<Long> occupied,
            Upgrade root,
            int dxCells,
            int dyCells) {
        List<Upgrade> members = new ArrayList<>();
        for (Upgrade upgrade : posByUpgrade.keySet()) {
            if (isInRequirementSubtree(upgrade, root)) {
                members.add(upgrade);
            }
        }
        members.sort(Comparator
                .comparingDouble((Upgrade u) -> {
                    double[] pos = posByUpgrade.get(u);
                    return dxCells <= 0 ? pos[0] : -pos[0];
                })
                .thenComparingDouble(u -> {
                    double[] pos = posByUpgrade.get(u);
                    return dyCells <= 0 ? pos[1] : -pos[1];
                })
                .thenComparingInt(Upgrade::getId));
        for (Upgrade upgrade : members) {
            nudge(posByUpgrade, occupied, upgrade, dxCells, dyCells);
        }
    }

    private static boolean isInRequirementSubtree(Upgrade upgrade, Upgrade root) {
        Upgrade cursor = upgrade;
        while (cursor != null) {
            if (cursor == root) {
                return true;
            }
            cursor = cursor.getRequirement();
        }
        return false;
    }

    private static void nudge(
            Map<Upgrade, double[]> posByUpgrade,
            Set<Long> occupied,
            Upgrade upgrade,
            int dxCells,
            int dyCells) {
        double[] pos = posByUpgrade.get(upgrade);
        if (pos == null) {
            return;
        }
        int gx = (int) Math.round(pos[0] / GRID_STEP) + dxCells;
        int gy = (int) Math.round(pos[1] / GRID_STEP) + dyCells;
        moveToCell(posByUpgrade, occupied, upgrade, gx, gy);
    }

    private static void placeRelative(
            Map<Upgrade, double[]> posByUpgrade,
            Set<Long> occupied,
            Upgrade upgrade,
            Upgrade anchor,
            int dxCells,
            int dyCells) {
        double[] anchorPos = posByUpgrade.get(anchor);
        double[] selfPos = posByUpgrade.get(upgrade);
        if (anchorPos == null || selfPos == null) {
            return;
        }
        int gx = (int) Math.round(anchorPos[0] / GRID_STEP) + dxCells;
        int gy = (int) Math.round(anchorPos[1] / GRID_STEP) + dyCells;
        moveToCell(posByUpgrade, occupied, upgrade, gx, gy);
    }

    private static void moveToCell(
            Map<Upgrade, double[]> posByUpgrade,
            Set<Long> occupied,
            Upgrade upgrade,
            int gx,
            int gy) {
        if (gx == 0 && gy == 0) {
            gx = 1;
        }
        double[] old = posByUpgrade.get(upgrade);
        if (old != null) {
            occupied.remove(cellKey(
                    (int) Math.round(old[0] / GRID_STEP),
                    (int) Math.round(old[1] / GRID_STEP)));
        }

        long key = cellKey(gx, gy);
        if (occupied.contains(key)) {
            Upgrade occupant = null;
            for (Map.Entry<Upgrade, double[]> entry : posByUpgrade.entrySet()) {
                if (entry.getKey() == upgrade) {
                    continue;
                }
                double[] pos = entry.getValue();
                if ((int) Math.round(pos[0] / GRID_STEP) == gx
                        && (int) Math.round(pos[1] / GRID_STEP) == gy) {
                    occupant = entry.getKey();
                    break;
                }
            }
            if (occupant != null) {
                occupied.remove(key);
                int[] free = nearestVacantCell(gx, gy, occupied);
                posByUpgrade.put(occupant, new double[] {free[0] * GRID_STEP, free[1] * GRID_STEP});
                occupied.add(cellKey(free[0], free[1]));
            }
        }

        posByUpgrade.put(upgrade, new double[] {gx * GRID_STEP, gy * GRID_STEP});
        occupied.add(cellKey(gx, gy));
    }

    private static int[] nearestVacantCell(int startX, int startY, Set<Long> occupied) {
        for (int radius = 1; radius < 64; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int dyAbs = radius - Math.abs(dx);
                int[] dys = dyAbs == 0 ? new int[] {0} : new int[] {dyAbs, -dyAbs};
                for (int dy : dys) {
                    int x = startX + dx;
                    int y = startY + dy;
                    if (x == 0 && y == 0) {
                        continue;
                    }
                    if (!occupied.contains(cellKey(x, y))) {
                        return new int[] {x, y};
                    }
                }
            }
        }
        return new int[] {startX + 64, startY};
    }

    private static void resolveEdgeCrossings(
            Map<Upgrade, double[]> posByUpgrade,
            List<Edge> edges,
            Map<Upgrade, Integer> depthByUpgrade,
            Map<Upgrade, Upgrade> parentByUpgrade) {
        Set<Long> occupied = new HashSet<>();
        for (double[] pos : posByUpgrade.values()) {
            occupied.add(cellKey(
                    (int) Math.round(pos[0] / GRID_STEP),
                    (int) Math.round(pos[1] / GRID_STEP)));
        }

        for (int pass = 0; pass < 4; pass++) {
            boolean improved = false;
            int crossings = countEdgeCrossings(posByUpgrade, edges);
            if (crossings == 0) {
                return;
            }
            for (Edge edge : edges) {
                Upgrade child = edge.getTo();
                Upgrade parent = edge.getFrom();
                if (parentByUpgrade.get(child) != parent) {
                    continue;
                }
                double[] childPos = posByUpgrade.get(child);
                double[] parentPos = posByUpgrade.get(parent);
                if (childPos == null || parentPos == null) {
                    continue;
                }
                int depth = Math.max(1, depthByUpgrade.getOrDefault(child, 1));
                int gx = (int) Math.round(childPos[0] / GRID_STEP);
                int gy = (int) Math.round(childPos[1] / GRID_STEP);
                occupied.remove(cellKey(gx, gy));

                int[] best = null;
                int bestCrossings = crossings;
                double targetRadius = depth * (RING_SPACING / GRID_STEP);
                for (int radius = 1; radius <= 6; radius++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int dyAbs = radius - Math.abs(dx);
                        int[] dys = dyAbs == 0 ? new int[] {0} : new int[] {dyAbs, -dyAbs};
                        for (int dy : dys) {
                            int x = gx + dx;
                            int y = gy + dy;
                            if ((x == 0 && y == 0) || occupied.contains(cellKey(x, y))) {
                                continue;
                            }
                            if (Math.abs(Math.hypot(x, y) - targetRadius) > 2.5) {
                                continue;
                            }
                            double[] trial = {x * GRID_STEP, y * GRID_STEP};
                            posByUpgrade.put(child, trial);
                            int next = countEdgeCrossings(posByUpgrade, edges);
                            if (next < bestCrossings) {
                                bestCrossings = next;
                                best = new int[] {x, y};
                            }
                        }
                    }
                }

                if (best != null) {
                    posByUpgrade.put(child, new double[] {best[0] * GRID_STEP, best[1] * GRID_STEP});
                    occupied.add(cellKey(best[0], best[1]));
                    crossings = bestCrossings;
                    improved = true;
                } else {
                    posByUpgrade.put(child, childPos);
                    occupied.add(cellKey(gx, gy));
                }
            }
            if (!improved) {
                return;
            }
        }
    }

    static int countEdgeCrossings(Map<Upgrade, double[]> posByUpgrade, List<Edge> edges) {
        int crossings = 0;
        for (int i = 0; i < edges.size(); i++) {
            Edge a = edges.get(i);
            double[] a1 = posByUpgrade.get(a.getFrom());
            double[] a2 = posByUpgrade.get(a.getTo());
            if (a1 == null || a2 == null) {
                continue;
            }
            for (int j = i + 1; j < edges.size(); j++) {
                Edge b = edges.get(j);
                if (sharesEndpoint(a, b)) {
                    continue;
                }
                double[] b1 = posByUpgrade.get(b.getFrom());
                double[] b2 = posByUpgrade.get(b.getTo());
                if (b1 == null || b2 == null) {
                    continue;
                }
                if (segmentsCross(a1[0], a1[1], a2[0], a2[1], b1[0], b1[1], b2[0], b2[1])) {
                    crossings++;
                }
            }
        }
        return crossings;
    }

    private static boolean sharesEndpoint(Edge a, Edge b) {
        return a.getFrom() == b.getFrom()
                || a.getFrom() == b.getTo()
                || a.getTo() == b.getFrom()
                || a.getTo() == b.getTo();
    }

    private static boolean segmentsCross(
            double ax1, double ay1, double ax2, double ay2,
            double bx1, double by1, double bx2, double by2) {
        double d1 = cross(bx2 - bx1, by2 - by1, ax1 - bx1, ay1 - by1);
        double d2 = cross(bx2 - bx1, by2 - by1, ax2 - bx1, ay2 - by1);
        double d3 = cross(ax2 - ax1, ay2 - ay1, bx1 - ax1, by1 - ay1);
        double d4 = cross(ax2 - ax1, ay2 - ay1, bx2 - ax1, by2 - ay1);
        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0))
                && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }
        return false;
    }

    private static double cross(double ax, double ay, double bx, double by) {
        return ax * by - ay * bx;
    }

    private static double normalizeAngle(double radians) {
        double value = radians;
        while (value > Math.PI) {
            value -= Math.PI * 2.0;
        }
        while (value < -Math.PI) {
            value += Math.PI * 2.0;
        }
        return value;
    }

    private static long cellKey(int gx, int gy) {
        return (((long) gx) << 32) ^ (gy & 0xffffffffL);
    }
}
