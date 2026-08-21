package com.grimidk.formicempire.classes.interfaces.game.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
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

import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;

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

        private Edge(Vertex a, Vertex b, Face faceA, Face faceB) {
            this.a = a;
            this.b = b;
            this.faceA = faceA;
            this.faceB = faceB;
            a.edges.add(this);
            b.edges.add(this);
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

        private Face(Hex hex, double centerX, double centerY) {
            this.hex = hex;
            this.q = hex.getQ();
            this.r = hex.getR();
            this.centerX = centerX;
            this.centerY = centerY;
        }

        public Polygon toPolygon() {
            Polygon poly = new Polygon();
            for (Vertex corner : corners) {
                poly.addPoint((int) Math.round(corner.x), (int) Math.round(corner.y));
            }
            return poly;
        }
    }

    private final List<Face> faces;
    private final List<Edge> edges;
    private final List<Vertex> vertices;
    private final double layoutSize;
    private final double mapCenterX;
    private final double mapCenterY;

    private HexGridMesh(
            List<Face> faces,
            List<Edge> edges,
            List<Vertex> vertices,
            double layoutSize,
            double mapCenterX,
            double mapCenterY) {
        this.faces = List.copyOf(faces);
        this.edges = List.copyOf(edges);
        this.vertices = List.copyOf(vertices);
        this.layoutSize = layoutSize;
        this.mapCenterX = mapCenterX;
        this.mapCenterY = mapCenterY;
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

    public Face faceAt(int q, int r) {
        for (Face face : faces) {
            if (face.q == q && face.r == r) {
                return face;
            }
        }
        return null;
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

        for (Hex hex : hexes) {
            if (hex == null) {
                continue;
            }
            double cx = HexMapGeometry.flatTopCenterXAbs(hex.getQ(), hex.getR(), originX, layoutSize);
            double cy = HexMapGeometry.flatTopCenterYAbs(hex.getQ(), hex.getR(), originY, layoutSize);
            Face face = new Face(hex, cx, cy);
            for (int i = 0; i < 6; i++) {
                double angleRad = Math.PI / 180.0 * (60 * i - 30);
                double vx = cx + layoutSize * Math.cos(angleRad);
                double vy = cy + layoutSize * Math.sin(angleRad);
                face.corners[i] = getOrCreateVertex(verticesByKey, vx, vy);
            }
            faces.add(face);
            facesByKey.put(packFaceKey(hex.getQ(), hex.getR()), face);
        }

        for (Face face : faces) {
            for (int i = 0; i < 6; i++) {
                int[] offset = HexMapGeometry.FLAT_TOP_NEIGHBOR_OFFSETS[i];
                int nq = face.q + offset[0];
                int nr = face.r + offset[1];
                Hex neighborHex = neighborAt.apply(nq, nr);
                if (!HexMapGeometry.ownsSharedEdge(face.q, face.r, neighborHex != null ? nq : null, neighborHex != null ? nr : null)) {
                    continue;
                }
                Face neighborFace = neighborHex != null ? facesByKey.get(packFaceKey(nq, nr)) : null;
                Vertex a = face.corners[i];
                Vertex b = face.corners[(i + 1) % 6];
                edges.add(new Edge(a, b, face, neighborFace));
            }
        }

        return new HexGridMesh(faces, edges, new ArrayList<>(verticesByKey.values()), layoutSize, originX, originY);
    }

    public void paintFills(Graphics2D g2d, Function<Face, Color> fillColor) {
        Objects.requireNonNull(g2d, "g2d");
        Objects.requireNonNull(fillColor, "fillColor");
        Object previousAa = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (Face face : faces) {
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
        BasicStroke stroke = new BasicStroke(DIVIDER_STROKE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        g2d.setStroke(stroke);
        for (Edge edge : edges) {
            Color plain = plainColor(edge, plainEdgeColor);
            if (dynastyBorderMode && edge.isRivalDynastyFrontier()) {
                paintAlternatingRivalEdge(g2d, edge, dynastyFallback != null ? dynastyFallback : plain);
                continue;
            }
            Color color;
            if (dynastyBorderMode && edge.isDynastyFrontier()) {
                color = edge.dynastyBorderColor(mapCenterX, mapCenterY, dynastyFallback != null ? dynastyFallback : plain);
            } else {
                color = plain;
            }
            if (color == null) {
                continue;
            }
            g2d.setColor(color);
            g2d.draw(new Line2D.Double(edge.a.x, edge.a.y, edge.b.x, edge.b.y));
        }

        double nodeRadius = DIVIDER_STROKE * 0.5;
        for (Vertex vertex : vertices) {
            Color nodeColor = nodeColor(vertex, dynastyBorderMode, plainEdgeColor, dynastyFallback);
            if (nodeColor == null) {
                continue;
            }
            g2d.setColor(nodeColor);
            g2d.fill(new Ellipse2D.Double(
                    vertex.x - nodeRadius,
                    vertex.y - nodeRadius,
                    nodeRadius * 2.0,
                    nodeRadius * 2.0));
        }
    }

    private void paintAlternatingRivalEdge(Graphics2D g2d, Edge edge, Color fallback) {
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
        for (int i = 0; i < RIVAL_BORDER_SEGMENTS; i++) {
            double t0 = i / (double) RIVAL_BORDER_SEGMENTS;
            double t1 = (i + 1) / (double) RIVAL_BORDER_SEGMENTS;
            double x0 = edge.a.x + (edge.b.x - edge.a.x) * t0;
            double y0 = edge.a.y + (edge.b.y - edge.a.y) * t0;
            double x1 = edge.a.x + (edge.b.x - edge.a.x) * t1;
            double y1 = edge.a.y + (edge.b.y - edge.a.y) * t1;
            g2d.setColor(i % 2 == 0 ? first : second);
            g2d.draw(new Line2D.Double(x0, y0, x1, y1));
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
        return plainColor(vertex.edges.get(0), plainEdgeColor);
    }

    private static Color plainColor(Edge edge, Function<Face, Color> plainEdgeColor) {
        Color plain = plainEdgeColor.apply(edge.faceA);
        if (plain == null && edge.faceB != null) {
            plain = plainEdgeColor.apply(edge.faceB);
        }
        return plain;
    }

    private static Color dynastyColorOf(Face face, Color fallback) {
        Dynasty dynasty = HexMapGeometry.dynastyOf(face != null ? face.hex : null);
        if (dynasty != null && dynasty.getColor() != null) {
            return dynasty.getColor();
        }
        return fallback;
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
