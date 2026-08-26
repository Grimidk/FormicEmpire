package com.grimidk.formicempire.classes.interfaces.game.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

public final class HexMapOverlays {
    private static final float TRADE_LINE_STROKE = 2f;
    private static final float[] TUNNEL_DASH = {4f, 3f};
    private static final double TUNNEL_HALF_SPAN_FACTOR = 0.22;
    private static final double TUNNEL_RAIL_GAP_FACTOR = 0.16;

    private HexMapOverlays() {
    }

    public static void paintTrades(Graphics2D g2d, World world, HexGridMesh mesh) {
        if (g2d == null || world == null || mesh == null) {
            return;
        }
        Engine engine = world.getEngine();
        TradeManager tradeManager = engine != null ? engine.getTradeManager() : null;
        if (tradeManager == null) {
            return;
        }
        double layoutSize = mesh.layoutSize();
        int markerSize = Math.max(10, (int) Math.round(layoutSize * 0.45));
        BasicStroke stroke = new BasicStroke(TRADE_LINE_STROKE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        g2d.setStroke(stroke);
        int minuteOfHour = Math.max(0, Math.min(59, world.getMinute()));

        for (Trade trade : tradeManager.getActiveTrades()) {
            if (trade == null || !trade.isActive()) {
                continue;
            }
            Hex origin = trade.getOrigin();
            Hex destination = trade.getDestination();
            if (origin == null || destination == null || !areNeighbors(origin, destination)) {
                continue;
            }
            HexGridMesh.Face originFace = mesh.faceAt(origin.getQ(), origin.getR());
            HexGridMesh.Face destFace = mesh.faceAt(destination.getQ(), destination.getR());
            if (originFace == null || destFace == null) {
                continue;
            }

            Dynasty originDynasty = HexMapGeometry.dynastyOf(origin);
            Dynasty destDynasty = HexMapGeometry.dynastyOf(destination);
            boolean crossDynasty = originDynasty != null
                    && destDynasty != null
                    && originDynasty.getId() != destDynasty.getId();

            if (crossDynasty) {
                paintAlternatingTradeLine(
                        g2d,
                        originFace,
                        destFace,
                        dynastyColor(originDynasty, AssetStyles.FONT_COLOR),
                        dynastyColor(destDynasty, AssetStyles.FONT_COLOR),
                        mesh.mapCenterX(),
                        mesh.mapCenterY());
            } else {
                Color lineColor = dynastyColor(originDynasty != null ? originDynasty : destDynasty, AssetStyles.FONT_COLOR);
                g2d.setColor(lineColor);
                g2d.drawLine(
                        (int) Math.round(originFace.centerX),
                        (int) Math.round(originFace.centerY),
                        (int) Math.round(destFace.centerX),
                        (int) Math.round(destFace.centerY));
            }

            boolean returning = trade.isReturning();
            double fromX = returning ? destFace.centerX : originFace.centerX;
            double fromY = returning ? destFace.centerY : originFace.centerY;
            double toX = returning ? originFace.centerX : destFace.centerX;
            double toY = returning ? originFace.centerY : destFace.centerY;
            double progress = tradeLegProgress(trade, minuteOfHour);
            double markerX = fromX + (toX - fromX) * progress;
            double markerY = fromY + (toY - fromY) * progress;
            ImageIcon markerIcon = originSpeciesAssimilationIcon(origin);
            if (markerIcon != null) {
                paintTradeProgressMarker(
                        g2d,
                        markerIcon,
                        markerX,
                        markerY,
                        markerSize,
                        tradeMarkerFacingRadians(fromX, fromY, toX, toY));
            } else {
                int dotRadius = Math.max(3, markerSize / 2);
                Color dotColor = crossDynasty
                        ? closerDynastyColor(
                                originFace,
                                destFace,
                                originDynasty,
                                destDynasty,
                                mesh.mapCenterX(),
                                mesh.mapCenterY(),
                                AssetStyles.FONT_COLOR)
                        : dynastyColor(originDynasty != null ? originDynasty : destDynasty, AssetStyles.FONT_COLOR);
                paintTradeProgressDot(g2d, markerX, markerY, dotRadius, dotColor);
            }
        }
    }

    public static void paintTunnels(Graphics2D g2d, World world, HexGridMesh mesh) {
        if (g2d == null || world == null || mesh == null || world.getDynastys() == null) {
            return;
        }
        Object previousAa = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        double layoutSize = mesh.layoutSize();
        Set<Long> drawn = new HashSet<>();

        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty == null || dynasty.getTunnels() == null) {
                continue;
            }
            Color lineColor = HexMapGeometry.dynastyPaintColor(dynasty, AssetStyles.BORDER_COLOR);
            for (Tunnel tunnel : dynasty.getTunnels()) {
                if (tunnel == null) {
                    continue;
                }
                Hex hexA = tunnel.getHexA();
                Hex hexB = tunnel.getHexB();
                if (hexA == null || hexB == null || !areNeighbors(hexA, hexB)) {
                    continue;
                }
                long key = edgeKey(hexA.getQ(), hexA.getR(), hexB.getQ(), hexB.getR());
                if (!drawn.add(key)) {
                    continue;
                }
                HexGridMesh.Face faceA = mesh.faceAt(hexA.getQ(), hexA.getR());
                HexGridMesh.Face faceB = mesh.faceAt(hexB.getQ(), hexB.getR());
                if (faceA == null || faceB == null) {
                    continue;
                }
                paintTunnelBridge(g2d, faceA, faceB, layoutSize, lineColor, tunnel.isComplete());
            }
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, previousAa);
    }

    public static void paintBattles(Graphics2D g2d, World world, HexGridMesh mesh) {
        if (g2d == null || world == null || mesh == null || world.getWarService() == null) {
            return;
        }
        double layoutSize = mesh.layoutSize();
        int iconSize = Math.max(10, (int) Math.round(layoutSize * 0.55));
        ImageIcon warIcon = GameConstants.ICON_STAT_MILITARY_POWER;
        List<War> wars = world.getWarService().getActiveWars();
        if (wars == null || warIcon == null) {
            return;
        }

        Set<Long> drawnEdges = new HashSet<>();
        for (War war : wars) {
            if (war == null || !war.isActive()) {
                continue;
            }
            int idA = war.getDynastyIdA();
            int idB = war.getDynastyIdB();
            for (HexGridMesh.Face face : mesh.faces()) {
                Dynasty dynasty = HexMapGeometry.dynastyOf(face.hex);
                if (dynasty == null) {
                    continue;
                }
                int faceId = dynasty.getId();
                if (faceId != idA && faceId != idB) {
                    continue;
                }
                int rivalId = faceId == idA ? idB : idA;
                for (int[] offset : HexMapGeometry.FLAT_TOP_NEIGHBOR_OFFSETS) {
                    HexGridMesh.Face neighbor = mesh.faceAt(face.q + offset[0], face.r + offset[1]);
                    if (neighbor == null) {
                        continue;
                    }
                    Dynasty neighborDynasty = HexMapGeometry.dynastyOf(neighbor.hex);
                    if (neighborDynasty == null || neighborDynasty.getId() != rivalId) {
                        continue;
                    }
                    long key = edgeKey(face.q, face.r, neighbor.q, neighbor.r);
                    if (!drawnEdges.add(key)) {
                        continue;
                    }
                    double midX = (face.centerX + neighbor.centerX) * 0.5;
                    double midY = (face.centerY + neighbor.centerY) * 0.5;
                    drawCenteredIcon(g2d, warIcon, midX, midY, iconSize);
                }
            }
        }
    }

    static double tradeLegProgress(Trade trade) {
        return tradeLegProgress(trade, 0);
    }

    static double tradeLegProgress(Trade trade, int minuteOfHour) {
        if (trade == null) {
            return 0.0;
        }
        int total = Math.max(1, trade.getTotalHours());
        int remaining = Math.max(0, trade.getRemainingHours());
        double fractionalHour = HexMapGeometry.clamp(minuteOfHour / 60.0, 0.0, 59.0 / 60.0);
        double hoursLeft = HexMapGeometry.clamp(remaining - fractionalHour, 0.0, total);
        return 1.0 - (hoursLeft / (double) total);
    }

    static double tradeMarkerFacingRadians(double fromX, double fromY, double toX, double toY) {
        return Math.atan2(toY - fromY, toX - fromX) + Math.PI / 2.0;
    }

    private static ImageIcon originSpeciesAssimilationIcon(Hex origin) {
        if (origin == null) {
            return null;
        }
        Colony colony = origin.getColony();
        if (colony == null) {
            return null;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null) {
            return null;
        }
        AntSpecies species = dynasty.getSpecies();
        if (species == null) {
            return null;
        }
        Assimilation assimilation = species.getAssimilation();
        return assimilation != null ? assimilation.getIcon() : null;
    }

    private static void paintTradeProgressMarker(
            Graphics2D g2d,
            ImageIcon icon,
            double centerX,
            double centerY,
            int size,
            double facingRadians) {
        if (icon == null || size <= 0) {
            return;
        }
        Image image = icon.getImage();
        if (image == null) {
            return;
        }
        AffineTransform previous = g2d.getTransform();
        g2d.translate(centerX, centerY);
        g2d.rotate(facingRadians);
        g2d.drawImage(image, -size / 2, -size / 2, size, size, null);
        g2d.setTransform(previous);
    }

    private static void paintTradeProgressDot(
            Graphics2D g2d,
            double centerX,
            double centerY,
            int radius,
            Color fill) {
        int outer = radius + Math.max(1, radius / 3);
        int ox = (int) Math.round(centerX - outer);
        int oy = (int) Math.round(centerY - outer);
        int ow = outer * 2;
        g2d.setColor(AssetStyles.BACKGROUND_COLOR);
        g2d.fillOval(ox, oy, ow, ow);
        int ix = (int) Math.round(centerX - radius);
        int iy = (int) Math.round(centerY - radius);
        int iw = radius * 2;
        g2d.setColor(fill != null ? fill : AssetStyles.FONT_COLOR);
        g2d.fillOval(ix, iy, iw, iw);
        g2d.setColor(AssetStyles.BORDER_COLOR);
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2d.drawOval(ix, iy, iw, iw);
    }

    private static void paintTunnelBridge(
            Graphics2D g2d,
            HexGridMesh.Face faceA,
            HexGridMesh.Face faceB,
            double layoutSize,
            Color lineColor,
            boolean complete) {
        double dx = faceB.centerX - faceA.centerX;
        double dy = faceB.centerY - faceA.centerY;
        double len = Math.hypot(dx, dy);
        if (len < 1e-6) {
            return;
        }
        double ux = dx / len;
        double uy = dy / len;
        double bx = -uy;
        double by = ux;

        double midX = (faceA.centerX + faceB.centerX) * 0.5;
        double midY = (faceA.centerY + faceB.centerY) * 0.5;
        double halfSpan = layoutSize * TUNNEL_HALF_SPAN_FACTOR;
        double railGap = layoutSize * TUNNEL_RAIL_GAP_FACTOR;

        BasicStroke stroke = complete
                ? new BasicStroke(HexGridMesh.DIVIDER_STROKE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)
                : new BasicStroke(
                        HexGridMesh.DIVIDER_STROKE,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10f,
                        TUNNEL_DASH,
                        0f);
        g2d.setStroke(stroke);
        g2d.setColor(lineColor);

        for (int side = -1; side <= 1; side += 2) {
            double ox = bx * railGap * side;
            double oy = by * railGap * side;
            g2d.draw(new Line2D.Double(
                    midX + ox - ux * halfSpan,
                    midY + oy - uy * halfSpan,
                    midX + ox + ux * halfSpan,
                    midY + oy + uy * halfSpan));
        }
    }

    private static void paintAlternatingTradeLine(
            Graphics2D g2d,
            HexGridMesh.Face faceA,
            HexGridMesh.Face faceB,
            Color colorA,
            Color colorB,
            double mapCenterX,
            double mapCenterY) {
        Color first = closerFace(faceA, faceB, mapCenterX, mapCenterY) == faceA ? colorA : colorB;
        Color second = first == colorA ? colorB : colorA;
        for (int i = 0; i < HexGridMesh.RIVAL_BORDER_SEGMENTS; i++) {
            double t0 = i / (double) HexGridMesh.RIVAL_BORDER_SEGMENTS;
            double t1 = (i + 1) / (double) HexGridMesh.RIVAL_BORDER_SEGMENTS;
            double x0 = faceA.centerX + (faceB.centerX - faceA.centerX) * t0;
            double y0 = faceA.centerY + (faceB.centerY - faceA.centerY) * t0;
            double x1 = faceA.centerX + (faceB.centerX - faceA.centerX) * t1;
            double y1 = faceA.centerY + (faceB.centerY - faceA.centerY) * t1;
            g2d.setColor(i % 2 == 0 ? first : second);
            g2d.drawLine(
                    (int) Math.round(x0),
                    (int) Math.round(y0),
                    (int) Math.round(x1),
                    (int) Math.round(y1));
        }
    }

    private static HexGridMesh.Face closerFace(
            HexGridMesh.Face faceA,
            HexGridMesh.Face faceB,
            double mapCenterX,
            double mapCenterY) {
        double distA = distanceSquared(faceA.centerX, faceA.centerY, mapCenterX, mapCenterY);
        double distB = distanceSquared(faceB.centerX, faceB.centerY, mapCenterX, mapCenterY);
        if (distA <= distB) {
            return faceA;
        }
        return faceB;
    }

    private static Color closerDynastyColor(
            HexGridMesh.Face faceA,
            HexGridMesh.Face faceB,
            Dynasty dynastyA,
            Dynasty dynastyB,
            double mapCenterX,
            double mapCenterY,
            Color fallback) {
        HexGridMesh.Face closer = closerFace(faceA, faceB, mapCenterX, mapCenterY);
        Dynasty dynasty = closer == faceA ? dynastyA : dynastyB;
        return dynastyColor(dynasty, fallback);
    }

    private static Color dynastyColor(Dynasty dynasty, Color fallback) {
        return HexMapGeometry.dynastyPaintColor(dynasty, fallback);
    }

    private static double distanceSquared(double x, double y, double cx, double cy) {
        double dx = x - cx;
        double dy = y - cy;
        return dx * dx + dy * dy;
    }

    static boolean areNeighbors(Hex a, Hex b) {
        if (a == null || b == null) {
            return false;
        }
        for (int[] offset : HexMapGeometry.FLAT_TOP_NEIGHBOR_OFFSETS) {
            if (a.getQ() + offset[0] == b.getQ() && a.getR() + offset[1] == b.getR()) {
                return true;
            }
        }
        return false;
    }

    private static long edgeKey(int q1, int r1, int q2, int r2) {
        if (q1 > q2 || (q1 == q2 && r1 > r2)) {
            int tq = q1;
            int tr = r1;
            q1 = q2;
            r1 = r2;
            q2 = tq;
            r2 = tr;
        }
        return (((long) q1) << 48) ^ (((long) r1) << 32) ^ (((long) q2) << 16) ^ (r2 & 0xffffL);
    }

    private static void drawCenteredIcon(Graphics2D g2d, ImageIcon icon, double centerX, double centerY, int size) {
        if (icon == null || size <= 0) {
            return;
        }
        Image image = icon.getImage();
        if (image == null) {
            return;
        }
        int x = HexMapGeometry.centeredIconOrigin(centerX, size);
        int y = HexMapGeometry.centeredIconOrigin(centerY, size);
        g2d.drawImage(image, x, y, size, size, null);
    }
}
