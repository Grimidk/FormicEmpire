package com.grimidk.formicempire.classes.interfaces.game.rendering;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.services.colony.ColonySpatialLayout;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
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

    private static void drawSprite(Graphics2D g, ImageIcon icon, int x, int y, Component obs) {
        if (icon == null) {
            return;
        }
        Image img = icon.getImage();
        int iw = icon.getIconWidth();
        int ih = icon.getIconHeight();
        if (iw <= 0 || ih <= 0) {
            return;
        }
        g.drawImage(img, x, y, iw, ih, obs);
    }

    private static int rowWidth(List<ImageIcon> icons) {
        int n = icons.size();
        if (n <= 0) {
            return 0;
        }
        int width = 0;
        for (ImageIcon icon : icons) {
            if (icon != null) {
                width += icon.getIconWidth();
            }
        }
        return width + (n - 1) * ICON_GAP;
    }

    private static int rowHeight(List<ImageIcon> icons) {
        int height = 0;
        for (ImageIcon icon : icons) {
            if (icon != null) {
                height = Math.max(height, icon.getIconHeight());
            }
        }
        return height;
    }

    private static void drawCenteredIconRow(
            Graphics2D g,
            Rectangle in,
            int y,
            List<ImageIcon> icons,
            Component obs) {
        if (icons.isEmpty()) {
            return;
        }
        int x = in.x + (in.width - rowWidth(icons)) / 2;
        for (ImageIcon icon : icons) {
            if (icon != null) {
                drawSprite(g, icon, x, y, obs);
                x += icon.getIconWidth() + ICON_GAP;
            }
        }
    }

    private static void withInteriorClip(Graphics2D g, Rectangle in, Runnable draw) {
        if (in.width < 1 || in.height < 1) {
            return;
        }
        Shape prev = g.getClip();
        try {
            g.clipRect(in.x, in.y, in.width, in.height);
            draw.run();
        } finally {
            g.setClip(prev);
        }
    }

    private static void drawTwoBandOverlay(
            Graphics2D g,
            Rectangle in,
            List<ImageIcon> topRow,
            List<ImageIcon> bottomRow,
            Component obs) {
        withInteriorClip(g, in, () -> {
            if (!topRow.isEmpty()) {
                drawCenteredIconRow(g, in, in.y, topRow, obs);
            }
            if (!bottomRow.isEmpty()) {
                int bottomY = in.y + in.height - rowHeight(bottomRow);
                drawCenteredIconRow(g, in, bottomY, bottomRow, obs);
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
        Rectangle in = shrinkRect(decorationInteriorRect(rx, ry, rw, rh), DECORATION_CONTENT_MARGIN_PX);
        drawTwoBandOverlay(g, in, topRow, bottomRow, obs);
    }

    public static void drawFarmRoomDecorations(
            Graphics2D g, Colony colony, int rx, int ry, int rw, int rh, Component obs) {
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
        drawTwoBandOverlay(g, in, topRow, bottomRow, obs);
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
        drawTwoBandOverlay(g, in, topRow, bottomRow, obs);
    }

    public static void drawRoyalRoomDecorations(
            Graphics2D g, Colony colony, int rx, int ry, int rw, int rh, Component obs) {
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
        drawTwoBandOverlay(g, in, topRow, bottomRow, obs);
    }
}
