package com.grimidk.formicempire.classes.interfaces.game.rendering;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.services.colony.ColonySpatialLayout;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

public final class RoomDecorationRenderer {

    private RoomDecorationRenderer() {
    }

    public static Building highestUnlocked(Colony colony, Building[] chainHighestFirst) {
        if (colony == null || chainHighestFirst == null) {
            return null;
        }
        for (Building b : chainHighestFirst) {
            if (colony.hasBuilding(b)) {
                return b;
            }
        }
        return null;
    }

    private static final int ICON_GAP = 4;

    private static final int DECORATION_CONTENT_MARGIN_PX = 14;

    private static final int SPARSE_ROOM_ICON_SCALE_NUM = 1;
    private static final int SPARSE_ROOM_ICON_SCALE_DEN = 2;
    private static final int SPARSE_ROOM_ICON_MIN_PX = 12;

    private static Rectangle shrinkRect(Rectangle r, int margin) {
        int m = Math.max(0, margin);
        return new Rectangle(
                r.x + m,
                r.y + m,
                Math.max(1, r.width - 2 * m),
                Math.max(1, r.height - 2 * m));
    }

    static Rectangle decorationInteriorRect(int rx, int ry, int rw, int rh) {
        int hallCx = ColonySpatialLayout.ANCHOR_CENTER_X;
        double centerX = rx + rw / 2.0;
        boolean isRightSide = centerX > hallCx;
        int pLeft = ColonySpatialLayout.PAD_WALL;
        int pRight = ColonySpatialLayout.PAD_WALL;
        if (Math.abs(centerX - hallCx) < rw + 200) {
            if (isRightSide) {
                pLeft = ColonySpatialLayout.PAD_DOOR;
            } else {
                pRight = ColonySpatialLayout.PAD_DOOR;
            }
        }
        int ix = rx + pLeft;
        int iy = ry + ColonySpatialLayout.PAD_TOP;
        int iw = rw - pLeft - pRight;
        int ih = rh - ColonySpatialLayout.PAD_TOP - ColonySpatialLayout.PAD_BOTTOM;
        return new Rectangle(ix, iy, Math.max(1, iw), Math.max(1, ih));
    }

    private static void drawSprite(
            Graphics2D g, ImageIcon icon, int x, int y, int maxW, int maxH, Component obs, boolean rotateIcon180) {
        if (icon == null) {
            return;
        }
        Image img = icon.getImage();
        int iw = icon.getIconWidth();
        int ih = icon.getIconHeight();
        if (iw <= 0 || ih <= 0) {
            return;
        }
        double scale = Math.min((double) maxW / iw, (double) maxH / ih);
        int tw = (int) Math.round(iw * scale);
        int th = (int) Math.round(ih * scale);
        if (!rotateIcon180) {
            g.drawImage(img, x, y, tw, th, obs);
            return;
        }
        AffineTransform prev = g.getTransform();
        try {
            g.translate(x + tw / 2.0, y + th / 2.0);
            g.rotate(Math.PI);
            g.drawImage(img, -tw / 2, -th / 2, tw, th, obs);
        } finally {
            g.setTransform(prev);
        }
    }

    private static int storageRoomHorizSlots(int topBandSlots, int bottomBandSlots) {
        return Math.max(1, Math.max(topBandSlots, bottomBandSlots == 0 ? 1 : bottomBandSlots));
    }

    private static int storageRoomVertSlots(int topBandSlots, int bottomBandSlots) {
        return (topBandSlots > 0 && bottomBandSlots > 0) ? 2 : 1;
    }

    private static int iconEdgeForRoom(int innerW, int innerH, int minEdge, int horizSlots, int vertSlots) {
        int cap = GameConstants.BUILDING_ROOM_ICON_SIZE_PX;
        int iw = Math.max(1, innerW);
        int ih = Math.max(1, innerH);
        int edge = Math.min(cap, Math.min(iw, ih));
        if (horizSlots > 0) {
            edge = Math.min(edge, (iw - (horizSlots - 1) * ICON_GAP) / horizSlots);
        }
        if (vertSlots > 0) {
            edge = Math.min(edge, (ih - (vertSlots - 1) * ICON_GAP) / vertSlots);
        }
        return Math.max(minEdge, edge);
    }

    private static int iconPxForTopBottomBandCounts(Rectangle in, int topCount, int bottomCount) {
        int horiz = storageRoomHorizSlots(topCount, bottomCount);
        int vert = storageRoomVertSlots(topCount, bottomCount);
        return iconEdgeForRoom(in.width, in.height, 16, horiz, vert);
    }

    private static int iconPxScaledForSparseRowsVsStorage(int rawFromSlotMath) {
        int scaled = (rawFromSlotMath * SPARSE_ROOM_ICON_SCALE_NUM) / SPARSE_ROOM_ICON_SCALE_DEN;
        return Math.max(SPARSE_ROOM_ICON_MIN_PX, scaled);
    }

    private static int decorationIconPx(Rectangle in, int topCount, int bottomCount, boolean scaleToMatchStorageDensity) {
        int raw = iconPxForTopBottomBandCounts(in, topCount, bottomCount);
        return scaleToMatchStorageDensity ? iconPxScaledForSparseRowsVsStorage(raw) : raw;
    }

    private static int yTopBand(int iconPx, Rectangle in, boolean swapBandYForRotatedRoom) {
        return swapBandYForRotatedRoom ? in.y + in.height - iconPx : in.y;
    }

    private static int yBottomBand(int iconPx, Rectangle in, boolean swapBandYForRotatedRoom) {
        return swapBandYForRotatedRoom ? in.y : in.y + in.height - iconPx;
    }

    private static void drawCenteredIconRow(
            Graphics2D g,
            Rectangle in,
            int iconPx,
            int y,
            List<ImageIcon> icons,
            Component obs,
            boolean rotateIcons180) {
        int n = icons.size();
        if (n <= 0) {
            return;
        }
        int rowW = n * iconPx + (n - 1) * ICON_GAP;
        int x0 = in.x + (in.width - rowW) / 2;
        for (int i = 0; i < n; i++) {
            ImageIcon ic = icons.get(i);
            if (ic != null) {
                drawSprite(g, ic, x0 + i * (iconPx + ICON_GAP), y, iconPx, iconPx, obs, rotateIcons180);
            }
        }
    }

    private static void withInteriorClip(Graphics2D g, Rectangle in, Runnable draw) {
        if (in.width < 1 || in.height < 1) {
            return;
        }
        Shape prev = g.getClip();
        Object hint = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        try {
            g.clipRect(in.x, in.y, in.width, in.height);
            draw.run();
        } finally {
            g.setClip(prev);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
        }
    }

    private static void drawTwoBandOverlay(
            Graphics2D g,
            Rectangle in,
            int iconPx,
            boolean swapBandYForRotatedRoom,
            boolean rotateIcons180,
            List<ImageIcon> topRow,
            List<ImageIcon> bottomRow,
            Component obs) {
        withInteriorClip(g, in, () -> {
            if (!topRow.isEmpty()) {
                drawCenteredIconRow(
                        g,
                        in,
                        iconPx,
                        yTopBand(iconPx, in, swapBandYForRotatedRoom),
                        topRow,
                        obs,
                        rotateIcons180);
            }
            if (!bottomRow.isEmpty()) {
                drawCenteredIconRow(
                        g,
                        in,
                        iconPx,
                        yBottomBand(iconPx, in, swapBandYForRotatedRoom),
                        bottomRow,
                        obs,
                        rotateIcons180);
            }
        });
    }

    public static void drawStorageRoomDecorations(Graphics2D g, Colony colony, int rx, int ry, int rw, int rh, Component obs) {
        if (colony == null) {
            return;
        }
        Building[][] chains = {
                GameUnlocks.BUILDING_CHAIN_ROCK,
                GameUnlocks.BUILDING_CHAIN_WATER,
                GameUnlocks.BUILDING_CHAIN_MEAT,
                GameUnlocks.BUILDING_CHAIN_SYRUP,
                GameUnlocks.BUILDING_CHAIN_RESIN
        };
        List<ImageIcon> topRow = new ArrayList<>();
        if (colony.hasBuilding(GameUnlocks.PASSIVE_WATER) && GameUnlocks.PASSIVE_WATER.getSprite() != null) {
            topRow.add(GameUnlocks.PASSIVE_WATER.getSprite());
        }
        if (colony.hasBuilding(GameUnlocks.PASSIVE_APHID) && GameUnlocks.PASSIVE_APHID.getSprite() != null) {
            topRow.add(GameUnlocks.PASSIVE_APHID.getSprite());
        }
        if (colony.hasBuilding(GameUnlocks.PASSIVE_GRAVE) && GameUnlocks.PASSIVE_GRAVE.getSprite() != null) {
            topRow.add(GameUnlocks.PASSIVE_GRAVE.getSprite());
        }
        List<ImageIcon> bottomRow = new ArrayList<>();
        for (Building[] ch : chains) {
            Building b = highestUnlocked(colony, ch);
            if (b != null && b.getSprite() != null) {
                bottomRow.add(b.getSprite());
            }
        }
        boolean hasComposter = colony.hasBuilding(GameUnlocks.BUILDING_COMPOSTER) && GameUnlocks.BUILDING_COMPOSTER.getSprite() != null;
        if (hasComposter) {
            bottomRow.add(GameUnlocks.BUILDING_COMPOSTER.getSprite());
        }
        if (bottomRow.isEmpty() && topRow.isEmpty()) {
            return;
        }
        int topPassives = topRow.size();
        int bottomSlots = bottomRow.size();
        Rectangle in = shrinkRect(decorationInteriorRect(rx, ry, rw, rh), DECORATION_CONTENT_MARGIN_PX);
        int iconPx = decorationIconPx(in, topPassives, bottomSlots, false);
        drawTwoBandOverlay(g, in, iconPx, false, false, topRow, bottomRow, obs);
    }

    public static void drawFarmRoomDecorations(
            Graphics2D g, Colony colony, int rx, int ry, int rw, int rh, Component obs, boolean rightColumnRoom) {
        if (colony == null) {
            return;
        }
        Building mush = highestUnlocked(colony, GameUnlocks.BUILDING_CHAIN_MUSHROOM);
        Building plant = highestUnlocked(colony, GameUnlocks.BUILDING_CHAIN_PLANT);
        boolean hasPassiveFarm = colony.hasBuilding(GameUnlocks.PASSIVE_FARM) && GameUnlocks.PASSIVE_FARM.getSprite() != null;
        List<ImageIcon> topRow = new ArrayList<>();
        if (hasPassiveFarm) {
            topRow.add(GameUnlocks.PASSIVE_FARM.getSprite());
        }
        List<ImageIcon> bottomRow = new ArrayList<>();
        if (mush != null && mush.getSprite() != null) {
            bottomRow.add(mush.getSprite());
        }
        if (plant != null && plant.getSprite() != null) {
            bottomRow.add(plant.getSprite());
        }
        if (topRow.isEmpty() && bottomRow.isEmpty()) {
            return;
        }
        Rectangle in = shrinkRect(decorationInteriorRect(rx, ry, rw, rh), DECORATION_CONTENT_MARGIN_PX);
        int iconPx = decorationIconPx(in, topRow.size(), bottomRow.size(), true);
        drawTwoBandOverlay(g, in, iconPx, rightColumnRoom, rightColumnRoom, topRow, bottomRow, obs);
    }

    public static void drawNurseryRoomDecorations(Graphics2D g, Colony colony, int rx, int ry, int rw, int rh, Component obs) {
        if (colony == null) {
            return;
        }
        Building egg = highestUnlocked(colony, GameUnlocks.BUILDING_CHAIN_EGG);
        boolean hasEgg = egg != null && egg.getSprite() != null;
        boolean hasNurse = colony.hasBuilding(GameUnlocks.PASSIVE_NURSE) && GameUnlocks.PASSIVE_NURSE.getSprite() != null;
        if (!hasEgg && !hasNurse) {
            return;
        }
        List<ImageIcon> topRow = new ArrayList<>();
        if (hasNurse) {
            topRow.add(GameUnlocks.PASSIVE_NURSE.getSprite());
        }
        List<ImageIcon> bottomRow = new ArrayList<>();
        if (hasEgg) {
            bottomRow.add(egg.getSprite());
        }
        Rectangle in = shrinkRect(decorationInteriorRect(rx, ry, rw, rh), DECORATION_CONTENT_MARGIN_PX);
        int iconPx = decorationIconPx(in, topRow.size(), bottomRow.size(), true);
        drawTwoBandOverlay(g, in, iconPx, false, false, topRow, bottomRow, obs);
    }

    public static void drawRoyalRoomDecorations(
            Graphics2D g, Colony colony, int rx, int ry, int rw, int rh, Component obs, boolean rightColumnRoom) {
        if (colony == null) {
            return;
        }
        Building royal = highestUnlocked(colony, GameUnlocks.BUILDING_CHAIN_ROYAL);
        boolean hasRoyal = royal != null && royal.getSprite() != null;
        boolean hasLab = colony.hasBuilding(GameUnlocks.PASSIVE_LAB) && GameUnlocks.PASSIVE_LAB.getSprite() != null;
        List<ImageIcon> topRow = new ArrayList<>();
        if (hasLab) {
            topRow.add(GameUnlocks.PASSIVE_LAB.getSprite());
        }
        List<ImageIcon> bottomRow = new ArrayList<>();
        if (hasRoyal) {
            bottomRow.add(royal.getSprite());
        }
        if (topRow.isEmpty() && bottomRow.isEmpty()) {
            return;
        }
        Rectangle in = shrinkRect(decorationInteriorRect(rx, ry, rw, rh), DECORATION_CONTENT_MARGIN_PX);
        int iconPx = decorationIconPx(in, topRow.size(), bottomRow.size(), true);
        drawTwoBandOverlay(g, in, iconPx, rightColumnRoom, rightColumnRoom, topRow, bottomRow, obs);
    }
}
