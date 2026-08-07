package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastySynergyService;
import com.grimidk.formicempire.classes.entities.services.shared.TriggerProgressService;
import com.grimidk.formicempire.classes.entities.services.shared.TriggerProgressService.TriggerProgress;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

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
    private static final double RING_SPACING = GRID_STEP * 2.0;

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

    public static NodeState stateFor(Colony colony, Engine engine, Upgrade upgrade) {
        if (colony == null || upgrade == null) {
            return NodeState.UNAVAILABLE;
        }
        if (colony.hasUpgrade(upgrade)) {
            return NodeState.OWNED;
        }

        Dynasty dynasty = colony.getDynasty();
        boolean reqMet = upgrade.getRequirement() == null || colony.hasUpgrade(upgrade.getRequirement());
        boolean extraMet = GameUnlocks.meetsExtraAutomationPrerequisites(dynasty, upgrade);
        boolean tierMet = upgrade.isAvailableFor(dynasty);
        boolean gatesMet = reqMet && extraMet && tierMet;

        if (gatesMet && upgrade.getCost() > 0 && colony.getResearchPoints() >= upgrade.getCost()) {
            return NodeState.AFFORDABLE;
        }

        TriggerProgress trigger = TriggerProgressService.find(colony, engine, upgrade);
        if (trigger != null && trigger.isGateMet() && trigger.getCurrent() > 0) {
            return NodeState.TRIGGER_PROGRESS;
        }

        if (hasSpecialProgress(dynasty, upgrade, gatesMet)) {
            return NodeState.SPECIAL_PROGRESS;
        }

        return NodeState.UNAVAILABLE;
    }

    private static boolean hasSpecialProgress(Dynasty dynasty, Upgrade upgrade, boolean gatesMet) {
        if (dynasty == null || upgrade == null) {
            return false;
        }

        Assimilation assimilation = assimilationForReward(upgrade);
        if (assimilation != null) {
            if (dynasty.getCurrentAssimilation() == assimilation) {
                return true;
            }
            if (gatesMet && isAssimilationAvailableNow(dynasty, assimilation)) {
                return true;
            }
        }

        Synergy synergy = synergyForReward(upgrade);
        return synergy != null && DynastySynergyService.isPartiallyComplete(dynasty, synergy);
    }

    private static Assimilation assimilationForReward(Upgrade upgrade) {
        for (Assimilation assimilation : GameUnlocks.getAssimilations()) {
            if (assimilation.getReward() == upgrade) {
                return assimilation;
            }
        }
        return null;
    }

    private static Synergy synergyForReward(Upgrade upgrade) {
        for (Synergy synergy : GameUnlocks.getSynergies()) {
            if (synergy.getReward() == upgrade) {
                return synergy;
            }
        }
        return null;
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
        return state == NodeState.TRIGGER_PROGRESS || state == NodeState.SPECIAL_PROGRESS;
    }

    public static Result build(Colony colony, Engine engine) {
        return build(colony, engine, false);
    }

    public static Result build(Colony colony, Engine engine, boolean revealAll) {
        Upgrade center = GameUnlocks.TYPE_EGG;
        Map<Upgrade, NodeState> nodeStates = new LinkedHashMap<>();
        for (Upgrade upgrade : GameUnlocks.getUpgrades()) {
            nodeStates.put(upgrade, colony == null
                    ? NodeState.UNAVAILABLE
                    : stateFor(colony, engine, upgrade));
        }
        if (!nodeStates.containsKey(center)) {
            return emptyResult();
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
        boolean obscure = !revealAll && colony != null;
        if (!obscure) {
            return laidOut;
        }

        Set<Upgrade> visible = new HashSet<>();
        for (Upgrade upgrade : nodeStates.keySet()) {
            if (isVisible(colony, engine, upgrade)) {
                visible.add(upgrade);
            }
        }
        if (!visible.contains(center)) {
            visible.add(center);
        }

        List<Node> nodes = new ArrayList<>();
        for (Node node : laidOut.getNodes()) {
            if (visible.contains(node.getUpgrade())) {
                nodes.add(node);
            }
        }
        List<Edge> edges = new ArrayList<>();
        for (Edge edge : laidOut.getEdges()) {
            if (visible.contains(edge.getFrom()) && visible.contains(edge.getTo())) {
                edges.add(edge);
            }
        }
        return new Result(
                nodes,
                edges,
                laidOut.getMaxDepth(),
                laidOut.getCenter(),
                laidOut.getMinX(),
                laidOut.getMaxX(),
                laidOut.getMinY(),
                laidOut.getMaxY());
    }

    public static Result refreshStates(Result previous, Colony colony, Engine engine) {
        return build(colony, engine);
    }

    private static Result emptyResult() {
        return new Result(List.of(), List.of(), -1, null, 0, 0, 0, 0);
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

        Map<Upgrade, double[]> posByUpgrade = snapToFreeGridCells(center, roughPos, depthByUpgrade);

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
            Map<Upgrade, Integer> depthByUpgrade) {
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
            int[] free = nearestFreeCell(gx, gy, occupied);
            occupied.add(cellKey(free[0], free[1]));
            snapped.put(upgrade, new double[] {free[0] * GRID_STEP, free[1] * GRID_STEP});
        }
        return snapped;
    }

    private static int[] nearestFreeCell(int startX, int startY, Set<Long> occupied) {
        if (!occupied.contains(cellKey(startX, startY))) {
            return new int[] {startX, startY};
        }
        for (int radius = 1; radius < 64; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int dy = radius - Math.abs(dx);
                int[][] candidates = dy == 0
                        ? new int[][] {{startX + dx, startY}}
                        : new int[][] {{startX + dx, startY + dy}, {startX + dx, startY - dy}};
                for (int[] cell : candidates) {
                    if (cell[0] == 0 && cell[1] == 0) {
                        continue;
                    }
                    if (!occupied.contains(cellKey(cell[0], cell[1]))) {
                        return cell;
                    }
                }
            }
        }
        return new int[] {startX + 64, startY};
    }

    private static long cellKey(int gx, int gy) {
        return (((long) gx) << 32) ^ (gy & 0xffffffffL);
    }
}
