package com.grimidk.formicempire.classes.interfaces.game.rendering;

import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class HexGridMesh {
    public static final float DIVIDER_STROKE = 2f;
    public static final int RIVAL_BORDER_SEGMENTS = 10;

    public static final class Vertex {
        public final double x;
        public final double y;
        private final List<Edge> edges = new ArrayList<>(3);

        private Vertex(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public List<Edge> edges() {
            return edges;
        }
    }

    public static final class Edge {
        public final Vertex a;
        public final Vertex b;
        public final Face faceA;
        public final Face faceB;
        public final int directionInFaceA;

        private Edge(Vertex a, Vertex b, Face faceA, Face faceB, int directionInFaceA) {
            this.a = a;
            this.b = b;
            this.faceA = faceA;
            this.faceB = faceB;
            this.directionInFaceA = directionInFaceA;
            a.edges.add(this);
            b.edges.add(this);
        }

        public boolean isRiver() {
            if (faceA != null && faceA.hex != null && faceA.hex.hasRiverEdge(directionInFaceA)) {
                return true;
            }
            if (faceB != null && faceB.hex != null && faceB.hex.hasRiverEdge((directionInFaceA + 3) % 6)) {
                return true;
            }
            return false;
        }

        public boolean isDynastyFrontier() {
            Dynasty dynastyA = HexMapGeometry.dynastyOf(faceA != null ? faceA.hex : null);
            Dynasty dynastyB = HexMapGeometry.dynastyOf(faceB != null ? faceB.hex : null);
            if (dynastyA == null && dynastyB == null) {
                return false;
            }
            if (dynastyA != null && dynastyB != null) {
                return dynastyA.getId() != dynastyB.getId();
            }
            return true;
        }

        public boolean isRivalDynastyFrontier() {
            Dynasty dynastyA = HexMapGeometry.dynastyOf(faceA != null ? faceA.hex : null);
            Dynasty dynastyB = HexMapGeometry.dynastyOf(faceB != null ? faceB.hex : null);
            return dynastyA != null
                    && dynastyB != null
                    && dynastyA.getId() != dynastyB.getId();
        }

        public Color dynastyBorderColor(double mapCenterX, double mapCenterY, Color fallback) {
            Face preferred = closerFace(mapCenterX, mapCenterY);
            Color preferredColor = dynastyColorOf(preferred, null);
            if (preferredColor != null) {
                return preferredColor;
            }
            Face other = preferred == faceA ? faceB : faceA;
            Color otherColor = dynastyColorOf(other, null);
            if (otherColor != null) {
                return otherColor;
            }
            return fallback;
        }

        private Face closerFace(double mapCenterX, double mapCenterY) {
            if (faceA == null) {
                return faceB;
            }
            if (faceB == null) {
                return faceA;
            }
            double distA = distanceSquared(faceA, mapCenterX, mapCenterY);
            double distB = distanceSquared(faceB, mapCenterX, mapCenterY);
            if (distA < distB) {
                return faceA;
            }
            if (distB < distA) {
                return faceB;
            }
            return faceA;
        }
    }

    public static final class Face {
        public final Hex hex;
        public final int q;
        public final int r;
        public final Vertex[] corners = new Vertex[6];
        public final double centerX;
        public final double centerY;
        public final int minX;
        public final int maxX;
        public final int minY;
        public final int maxY;
        private Polygon cachedPolygon;

        private Face(Hex hex, double centerX, double centerY, double layoutSize) {
            this.hex = hex;
            this.q = hex.getQ();
            this.r = hex.getR();
            this.centerX = centerX;
            this.centerY = centerY;
            this.minX = (int) Math.floor(centerX - layoutSize);
            this.maxX = (int) Math.ceil(centerX + layoutSize);
            this.minY = (int) Math.floor(centerY - layoutSize);
            this.maxY = (int) Math.ceil(centerY + layoutSize);
        }

        public boolean intersects(Rectangle clip) {
            if (clip == null) return true;
            return !(maxX < clip.x || minX > clip.x + clip.width || maxY < clip.y || minY > clip.y + clip.height);
        }

        public Polygon toPolygon() {
            if (cachedPolygon == null) {
                Polygon poly = new Polygon();
                for (Vertex corner : corners) {
                    poly.addPoint((int) Math.round(corner.x), (int) Math.round(corner.y));
                }
                cachedPolygon = poly;
            }
            return cachedPolygon;
        }
    }

    private final List<Face> faces;
    private final Map<Long, Face> facesByKey;
    private final List<Edge> edges;
    private final List<Edge> riverEdges;
    private final List<Vertex> vertices;
    private final double layoutSize;
    private final double mapCenterX;
    private final double mapCenterY;
    private final int boundsMinX;
    private final int boundsMaxX;
    private final int boundsMinY;
    private final int boundsMaxY;

    private HexGridMesh(
            List<Face> faces,
            Map<Long, Face> facesByKey,
            List<Edge> edges,
            List<Edge> riverEdges,
            List<Vertex> vertices,
            double layoutSize,
            double mapCenterX,
            double mapCenterY,
            int boundsMinX,
            int boundsMaxX,
            int boundsMinY,
            int boundsMaxY) {
        this.faces = List.copyOf(faces);
        this.facesByKey = Map.copyOf(facesByKey);
        this.edges = List.copyOf(edges);
        this.riverEdges = List.copyOf(riverEdges);
        this.vertices = List.copyOf(vertices);
        this.layoutSize = layoutSize;
        this.mapCenterX = mapCenterX;
        this.mapCenterY = mapCenterY;
        this.boundsMinX = boundsMinX;
        this.boundsMaxX = boundsMaxX;
        this.boundsMinY = boundsMinY;
        this.boundsMaxY = boundsMaxY;
    }

    public List<Face> faces() {
        return faces;
    }

    public List<Edge> edges() {
        return edges;
    }

    public List<Vertex> vertices() {
        return vertices;
    }

    public double layoutSize() {
        return layoutSize;
    }

    public double mapCenterX() {
        return mapCenterX;
    }

    public double mapCenterY() {
        return mapCenterY;
    }

    public int boundsMinX() {
        return boundsMinX;
    }

    public int boundsMaxX() {
        return boundsMaxX;
    }

    public int boundsMinY() {
        return boundsMinY;
    }

    public int boundsMaxY() {
        return boundsMaxY;
    }

    public Face faceAt(int q, int r) {
        return facesByKey.get(packFaceKey(q, r));
    }

    public static HexGridMesh build(
            Collection<Hex> hexes,
            double originX,
            double originY,
            double layoutSize,
            BiFunction<Integer, Integer, Hex> neighborAt) {
        Objects.requireNonNull(hexes, "hexes");
        Objects.requireNonNull(neighborAt, "neighborAt");

        Map<Long, Face> facesByKey = new HashMap<>();
        Map<Long, Vertex> verticesByKey = new HashMap<>();
        List<Face> faces = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        List<Edge> riverEdges = new ArrayList<>();
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Hex hex : hexes) {
            if (hex == null) {
                continue;
            }
            double cx = HexMapGeometry.flatTopCenterXAbs(hex.getQ(), hex.getR(), originX, layoutSize);
            double cy = HexMapGeometry.flatTopCenterYAbs(hex.getQ(), hex.getR(), originY, layoutSize);
            Face face = new Face(hex, cx, cy, layoutSize);
            if (face.minX < minX) minX = face.minX;
            if (face.maxX > maxX) maxX = face.maxX;
            if (face.minY < minY) minY = face.minY;
            if (face.maxY > maxY) maxY = face.maxY;
            Polygon poly = new Polygon();
            for (int i = 0; i < 6; i++) {
                double angleRad = Math.PI / 180.0 * (60 * i - 30);
                double vx = cx + layoutSize * Math.cos(angleRad);
                double vy = cy + layoutSize * Math.sin(angleRad);
                face.corners[i] = getOrCreateVertex(verticesByKey, vx, vy);
                poly.addPoint((int) Math.round(vx), (int) Math.round(vy));
            }
            face.cachedPolygon = poly;
            faces.add(face);
            facesByKey.put(packFaceKey(hex.getQ(), hex.getR()), face);
        }

        if (faces.isEmpty()) {
            minX = 0;
            maxX = 0;
            minY = 0;
            maxY = 0;
        }

        for (Face face : faces) {
            for (int i = 0; i < 6; i++) {
                int[] offset = FLAT_TOP_NEIGHBOR_OFFSET_FOR(i);
                int nq = face.q + offset[0];
                int nr = face.r + offset[1];
                Hex neighborHex = neighborAt.apply(nq, nr);
                if (!HexMapGeometry.ownsSharedEdge(face.q, face.r, neighborHex != null ? nq : null, neighborHex != null ? nr : null)) {
                    continue;
                }
                Face neighborFace = neighborHex != null ? facesByKey.get(packFaceKey(nq, nr)) : null;
                Vertex a = face.corners[i];
                Vertex b = face.corners[(i + 1) % 6];
                Edge edge = new Edge(a, b, face, neighborFace, i);
                edges.add(edge);
                if (edge.isRiver()) {
                    riverEdges.add(edge);
                }
            }
        }

        return new HexGridMesh(faces, facesByKey, edges, riverEdges, new ArrayList<>(verticesByKey.values()), layoutSize, originX, originY, minX, maxX, minY, maxY);
    }

    private static int[] FLAT_TOP_NEIGHBOR_OFFSET_FOR(int i) {
        return HexMapGeometry.FLAT_TOP_NEIGHBOR_OFFSETS[i];
    }

    public void paintFills(Graphics2D g2d, Function<Face, Color> fillColor) {
        Objects.requireNonNull(g2d, "g2d");
        Objects.requireNonNull(fillColor, "fillColor");
        Object previousAa = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        Rectangle clip = g2d.getClipBounds();
        for (Face face : faces) {
            if (clip != null && !face.intersects(clip)) {
                continue;
            }
            Color color = fillColor.apply(face);
            if (color == null) {
                continue;
            }
            g2d.setColor(color);
            g2d.fillPolygon(face.toPolygon());
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, previousAa);
    }

    public void paintDividers(Graphics2D g2d, boolean dynastyBorderMode, Function<Face, Color> plainEdgeColor, Color dynastyFallback) {
        Objects.requireNonNull(g2d, "g2d");
        Objects.requireNonNull(plainEdgeColor, "plainEdgeColor");
        float dividerStroke = (float) Math.max(1.0, Math.min(2.0, layoutSize * 0.08));
        BasicStroke stroke = new BasicStroke(dividerStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(stroke);
        Line2D.Double edgeLine = new Line2D.Double();
        Rectangle clip = g2d.getClipBounds();
        for (Edge edge : edges) {
            if (clip != null && !edgeIntersects(edge, clip)) {
                continue;
            }
            Color color = null;
            Color plain = plainColor(edge, plainEdgeColor);
            if (dynastyBorderMode && edge.isRivalDynastyFrontier()) {
                paintAlternatingRivalEdge(g2d, edge, dynastyFallback != null ? dynastyFallback : plain, edgeLine);
                continue;
            }
            if (dynastyBorderMode && edge.isDynastyFrontier()) {
                color = edge.dynastyBorderColor(mapCenterX, mapCenterY, dynastyFallback != null ? dynastyFallback : plain);
            } else {
                Color ocean = oceanBorderColor(edge, plainEdgeColor);
                color = ocean != null ? ocean : plain;
            }
            if (color == null) {
                continue;
            }
            g2d.setColor(color);
            edgeLine.setLine(edge.a.x, edge.a.y, edge.b.x, edge.b.y);
            g2d.draw(edgeLine);
        }

        if (dividerStroke >= 1.5f) {
            double nodeRadius = dividerStroke * 0.5;
            Ellipse2D.Double nodeEllipse = new Ellipse2D.Double();
            for (Vertex vertex : vertices) {
                if (clip != null && !clip.intersects(vertex.x - nodeRadius, vertex.y - nodeRadius, nodeRadius * 2.0, nodeRadius * 2.0)) {
                    continue;
                }
                Color nodeColor = nodeColor(vertex, dynastyBorderMode, plainEdgeColor, dynastyFallback);
                if (nodeColor == null) {
                    continue;
                }
                g2d.setColor(nodeColor);
                nodeEllipse.setFrame(vertex.x - nodeRadius, vertex.y - nodeRadius, nodeRadius * 2.0, nodeRadius * 2.0);
                g2d.fill(nodeEllipse);
            }
        }
    }

    private static boolean edgeIntersects(Edge edge, Rectangle clip) {
        double minX = Math.min(edge.a.x, edge.b.x);
        double maxX = Math.max(edge.a.x, edge.b.x);
        double minY = Math.min(edge.a.y, edge.b.y);
        double maxY = Math.max(edge.a.y, edge.b.y);
        return !(maxX < clip.x || minX > clip.x + clip.width || maxY < clip.y || minY > clip.y + clip.height);
    }

    public static final class FaultStyle {
        public final Color color;
        public final int intensity;

        public FaultStyle(Color color, int intensity) {
            this.color = color;
            this.intensity = intensity;
        }
    }

    public void paintFaultBoundaries(Graphics2D g2d, Function<Edge, Color> faultColorProvider) {
        paintFaultBoundariesWithStyles(g2d, edge -> {
            Color c = faultColorProvider.apply(edge);
            return c != null ? new FaultStyle(c, 1) : null;
        });
    }

    public void paintFaultBoundariesWithStyles(Graphics2D g2d, Function<Edge, FaultStyle> faultStyleProvider) {
        Objects.requireNonNull(g2d, "g2d");
        Objects.requireNonNull(faultStyleProvider, "faultStyleProvider");
        float baseStrokeWidth = (float) Math.max(1.8, Math.min(3.0, layoutSize * 0.12));
        float strongStrokeWidth = (float) Math.max(3.2, Math.min(5.4, layoutSize * 0.22));
        BasicStroke baseStroke = new BasicStroke(baseStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke strongStroke = new BasicStroke(strongStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Line2D.Double edgeLine = new Line2D.Double();
        Rectangle clip = g2d.getClipBounds();

        for (int pass = 1; pass <= 2; pass++) {
            g2d.setStroke(pass == 1 ? baseStroke : strongStroke);
            for (Edge edge : edges) {
                if (clip != null && !edgeIntersects(edge, clip)) {
                    continue;
                }
                FaultStyle style = faultStyleProvider.apply(edge);
                if (style == null || style.color == null) {
                    continue;
                }
                int intn = Math.max(1, Math.min(2, style.intensity));
                if (intn != pass) {
                    continue;
                }
                g2d.setColor(style.color);
                edgeLine.setLine(edge.a.x, edge.a.y, edge.b.x, edge.b.y);
                g2d.draw(edgeLine);
            }
        }
    }

    public void paintRivers(Graphics2D g2d) {
        paintRivers(g2d, 1.0f);
    }

    public void paintRivers(Graphics2D g2d, float strokeMultiplier) {
        Objects.requireNonNull(g2d, "g2d");
        Object previousAa = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Line2D.Double line = new Line2D.Double();
        Rectangle clip = g2d.getClipBounds();

        if (layoutSize <= 4.0) {
            float width = (float) Math.max(0.4, Math.min(0.9, layoutSize * 0.30)) * strokeMultiplier;
            BasicStroke stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2d.setStroke(stroke);
            g2d.setColor(new Color(0, 180, 240, 230));
            for (Edge edge : riverEdges) {
                if (clip != null && !edgeIntersects(edge, clip)) {
                    continue;
                }
                line.setLine(edge.a.x, edge.a.y, edge.b.x, edge.b.y);
                g2d.draw(line);
            }
        } else {
            float outerWidth = (float) Math.max(2.2, layoutSize * 0.18) * strokeMultiplier;
            float innerWidth = (float) Math.max(1.2, outerWidth * 0.50);

            BasicStroke outerStroke = new BasicStroke(outerWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2d.setStroke(outerStroke);
            g2d.setColor(new Color(0, 50, 140, 240));
            for (Edge edge : riverEdges) {
                if (clip != null && !edgeIntersects(edge, clip)) {
                    continue;
                }
                line.setLine(edge.a.x, edge.a.y, edge.b.x, edge.b.y);
                g2d.draw(line);
            }

            BasicStroke innerStroke = new BasicStroke(innerWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2d.setStroke(innerStroke);
            g2d.setColor(new Color(0, 225, 255, 255));
            for (Edge edge : riverEdges) {
                if (clip != null && !edgeIntersects(edge, clip)) {
                    continue;
                }
                line.setLine(edge.a.x, edge.a.y, edge.b.x, edge.b.y);
                g2d.draw(line);
            }
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, previousAa);
    }

    private void paintAlternatingRivalEdge(Graphics2D g2d, Edge edge, Color fallback, Line2D.Double line) {
        Face closer = edge.closerFace(mapCenterX, mapCenterY);
        Face farther = closer == edge.faceA ? edge.faceB : edge.faceA;
        Color first = dynastyColorOf(closer, fallback);
        Color second = dynastyColorOf(farther, fallback);
        if (first == null && second == null) {
            return;
        }
        if (first == null) {
            first = second;
        }
        if (second == null) {
            second = first;
        }
        int segments = Math.max(2, Math.min(RIVAL_BORDER_SEGMENTS, (int) Math.round(layoutSize / 3.0)));
        for (int i = 0; i < segments; i++) {
            double t0 = i / (double) segments;
            double t1 = (i + 1) / (double) segments;
            double x0 = edge.a.x + (edge.b.x - edge.a.x) * t0;
            double y0 = edge.a.y + (edge.b.y - edge.a.y) * t0;
            double x1 = edge.a.x + (edge.b.x - edge.a.x) * t1;
            double y1 = edge.a.y + (edge.b.y - edge.a.y) * t1;
            g2d.setColor(i % 2 == 0 ? first : second);
            line.setLine(x0, y0, x1, y1);
            g2d.draw(line);
        }
    }

    private Color nodeColor(
            Vertex vertex,
            boolean dynastyBorderMode,
            Function<Face, Color> plainEdgeColor,
            Color dynastyFallback) {
        if (vertex.edges.isEmpty()) {
            return null;
        }
        if (dynastyBorderMode) {
            for (Edge edge : vertex.edges) {
                if (edge.isDynastyFrontier()) {
                    return edge.dynastyBorderColor(mapCenterX, mapCenterY, dynastyFallback);
                }
            }
        }
        for (Edge edge : vertex.edges) {
            Color ocean = oceanBorderColor(edge, plainEdgeColor);
            if (ocean != null) {
                return ocean;
            }
        }
        return plainColor(vertex.edges.get(0), plainEdgeColor);
    }

    static Color oceanBorderColor(Edge edge, Function<Face, Color> plainEdgeColor) {
        if (edge == null) {
            return null;
        }
        boolean touchesOcean = isOceanFace(edge.faceA) || isOceanFace(edge.faceB)
                || (edge.faceA == null) != (edge.faceB == null);
        if (!touchesOcean) {
            return null;
        }
        Face oceanFace = isOceanFace(edge.faceA) ? edge.faceA : (isOceanFace(edge.faceB) ? edge.faceB : null);
        if (oceanFace != null && plainEdgeColor != null) {
            Color fill = plainEdgeColor.apply(oceanFace);
            if (fill != null) {
                return fill;
            }
        }
        return GameConstants.BIOME_OCEAN.getMapColor();
    }

    private static boolean isOceanFace(Face face) {
        return face != null
                && face.hex != null
                && (face.hex.getBiome() == GameConstants.BIOME_OCEAN
                    || face.hex.getBiome() == GameConstants.BIOME_DEEP_OCEAN
                    || face.hex.getBiome() == GameConstants.BIOME_SHALLOW_OCEAN);
    }

    private static Color plainColor(Edge edge, Function<Face, Color> plainEdgeColor) {
        Color plain = plainEdgeColor.apply(edge.faceA);
        if (plain == null && edge.faceB != null) {
            plain = plainEdgeColor.apply(edge.faceB);
        }
        return plain;
    }

    private static Color dynastyColorOf(Face face, Color fallback) {
        return HexMapGeometry.dynastyPaintColor(HexMapGeometry.dynastyOf(face != null ? face.hex : null), fallback);
    }

    private static double distanceSquared(Face face, double mapCenterX, double mapCenterY) {
        double dx = face.centerX - mapCenterX;
        double dy = face.centerY - mapCenterY;
        return dx * dx + dy * dy;
    }

    private static Vertex getOrCreateVertex(Map<Long, Vertex> verticesByKey, double x, double y) {
        long key = vertexKey(x, y);
        Vertex existing = verticesByKey.get(key);
        if (existing != null) {
            return existing;
        }
        Vertex created = new Vertex(x, y);
        verticesByKey.put(key, created);
        return created;
    }

    private static long vertexKey(double x, double y) {
        long qx = Math.round(x * 4.0);
        long qy = Math.round(y * 4.0);
        return (qx << 32) ^ (qy & 0xffffffffL);
    }

    private static long packFaceKey(int q, int r) {
        return (((long) q) << 32) ^ (r & 0xffffffffL);
    }
}
