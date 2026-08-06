package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BuildingTreeGraph {

    public static final double GRID_STEP = 1.5;
    public static final double TIER_SPACING = GRID_STEP * 2.0;

    public enum NodeState {
        OWNED,
        AFFORDABLE,
        IN_PROGRESS,
        UNAVAILABLE
    }

    public static final class Node {
        private final Building building;
        private final NodeState state;
        private final int tierIndex;
        private final double posX;
        private final double posY;

        Node(Building building, NodeState state, int tierIndex, double posX, double posY) {
            this.building = building;
            this.state = state;
            this.tierIndex = tierIndex;
            this.posX = posX;
            this.posY = posY;
        }

        public Building getBuilding() {
            return building;
        }

        public NodeState getState() {
            return state;
        }

        public int getTierIndex() {
            return tierIndex;
        }

        public double getPosX() {
            return posX;
        }

        public double getPosY() {
            return posY;
        }
    }

    public static final class Edge {
        private final Building from;
        private final Building to;

        Edge(Building from, Building to) {
            this.from = from;
            this.to = to;
        }

        public Building getFrom() {
            return from;
        }

        public Building getTo() {
            return to;
        }
    }

    public static final class TierDivider {
        private final int upperTier;
        private final double posY;

        TierDivider(int upperTier, double posY) {
            this.upperTier = upperTier;
            this.posY = posY;
        }

        public int getUpperTier() {
            return upperTier;
        }

        public double getPosY() {
            return posY;
        }
    }

    public static final class Result {
        private final List<Node> nodes;
        private final List<Edge> edges;
        private final List<TierDivider> tierDividers;
        private final List<Building> roots;
        private final double minX;
        private final double maxX;
        private final double minY;
        private final double maxY;

        Result(
                List<Node> nodes,
                List<Edge> edges,
                List<TierDivider> tierDividers,
                List<Building> roots,
                double minX,
                double maxX,
                double minY,
                double maxY) {
            this.nodes = Collections.unmodifiableList(nodes);
            this.edges = Collections.unmodifiableList(edges);
            this.tierDividers = Collections.unmodifiableList(tierDividers);
            this.roots = Collections.unmodifiableList(roots);
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

        public List<TierDivider> getTierDividers() {
            return tierDividers;
        }

        public List<Building> getRoots() {
            return roots;
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

    private BuildingTreeGraph() {
    }

    public static NodeState stateFor(Colony colony, Building building) {
        if (colony == null || building == null) {
            return NodeState.UNAVAILABLE;
        }
        if (colony.hasBuilding(building)) {
            return NodeState.OWNED;
        }
        if (colony.getCurrentBuildingProject() == building) {
            return NodeState.IN_PROGRESS;
        }

        boolean reqMet = building.getRequirement() == null || colony.hasBuilding(building.getRequirement());
        boolean tierMet = building.isAvailableFor(colony.getDynasty());
        boolean unlockMet = GameUnlocks.meetsBuildingUnlockRequirement(colony, building);
        if (!reqMet || !tierMet || !unlockMet) {
            return NodeState.UNAVAILABLE;
        }

        int builders = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
        int cranes = colony.getAssignedRoleCount(GameConstants.ROLE_CRANE);
        if (builders <= 0 && cranes <= 0) {
            return NodeState.UNAVAILABLE;
        }
        if (colony.getMinerals() < building.getMineralCost() || colony.getResins() < building.getResinCost()) {
            return NodeState.UNAVAILABLE;
        }
        if (colony.getCurrentBuildingProject() != null) {
            return NodeState.UNAVAILABLE;
        }
        return NodeState.AFFORDABLE;
    }

    public static Result build(Colony colony) {
        List<Building> all = GameUnlocks.getBuildings();
        if (all.isEmpty()) {
            return emptyResult();
        }

        Map<Building, NodeState> states = new LinkedHashMap<>();
        for (Building building : all) {
            states.put(building, stateFor(colony, building));
        }

        Map<Building, List<Building>> children = new HashMap<>();
        List<Edge> edges = new ArrayList<>();
        List<Building> roots = new ArrayList<>();
        for (Building building : all) {
            children.put(building, new ArrayList<>());
        }
        for (Building building : all) {
            Building requirement = building.getRequirement();
            if (requirement == null) {
                roots.add(building);
            } else if (states.containsKey(requirement)) {
                children.get(requirement).add(building);
                edges.add(new Edge(requirement, building));
            } else {
                roots.add(building);
            }
        }
        roots.sort(Comparator.comparingInt(Building::getId));
        for (List<Building> kids : children.values()) {
            kids.sort(Comparator.comparingInt(Building::getId));
        }

        Map<Building, Integer> localX = new HashMap<>();
        Map<Building, Double> posX = new HashMap<>();
        Map<Building, Double> posY = new HashMap<>();
        double cursorX = 0;
        double gapBetweenTrees = GRID_STEP * 2.0;

        for (Building root : roots) {
            int[] nextLeaf = {0};
            assignLocalX(root, children, localX, nextLeaf);
            int width = Math.max(1, nextLeaf[0]);
            double treeOrigin = cursorX + (width - 1) * GRID_STEP / 2.0;
            placeTree(root, children, localX, treeOrigin, width, posX, posY);
            cursorX += width * GRID_STEP + gapBetweenTrees;
        }

        List<Node> nodes = new ArrayList<>();
        double minX = 0;
        double maxX = 0;
        double minY = 0;
        double maxY = 0;
        boolean first = true;
        for (Building building : all) {
            double x = posX.getOrDefault(building, 0.0);
            double y = posY.getOrDefault(building, 0.0);
            int tier = building.getTier().getId();
            if (first) {
                minX = maxX = x;
                minY = maxY = y;
                first = false;
            } else {
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
            nodes.add(new Node(building, states.get(building), tier, x, y));
        }
        nodes.sort(Comparator
                .comparingInt(Node::getTierIndex)
                .thenComparingDouble(Node::getPosX)
                .thenComparingInt(n -> n.getBuilding().getId()));

        List<TierDivider> dividers = new ArrayList<>();
        int maxTier = 0;
        for (Building building : all) {
            maxTier = Math.max(maxTier, building.getTier().getId());
        }
        for (int tier = 0; tier < maxTier; tier++) {
            double lowerY = -tier * TIER_SPACING;
            double upperY = -(tier + 1) * TIER_SPACING;
            dividers.add(new TierDivider(tier + 1, (lowerY + upperY) / 2.0));
        }

        return new Result(nodes, edges, dividers, roots, minX, maxX, minY, maxY);
    }

    private static Result emptyResult() {
        return new Result(List.of(), List.of(), List.of(), List.of(), 0, 0, 0, 0);
    }

    private static void assignLocalX(
            Building node,
            Map<Building, List<Building>> children,
            Map<Building, Integer> localX,
            int[] nextLeaf) {
        List<Building> kids = children.getOrDefault(node, List.of());
        if (kids.isEmpty()) {
            localX.put(node, nextLeaf[0]);
            nextLeaf[0] += 1;
            return;
        }
        for (Building kid : kids) {
            assignLocalX(kid, children, localX, nextLeaf);
        }
        int first = localX.get(kids.get(0));
        int last = localX.get(kids.get(kids.size() - 1));
        localX.put(node, (first + last) / 2);
    }

    private static void placeTree(
            Building node,
            Map<Building, List<Building>> children,
            Map<Building, Integer> localX,
            double treeOrigin,
            int treeWidth,
            Map<Building, Double> posX,
            Map<Building, Double> posY) {
        int local = localX.getOrDefault(node, 0);
        double centered = local - (treeWidth - 1) / 2.0;
        posX.put(node, treeOrigin + centered * GRID_STEP);
        posY.put(node, -node.getTier().getId() * TIER_SPACING);
        for (Building kid : children.getOrDefault(node, List.of())) {
            placeTree(kid, children, localX, treeOrigin, treeWidth, posX, posY);
        }
    }
}
