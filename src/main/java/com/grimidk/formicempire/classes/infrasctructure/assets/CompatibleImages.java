package com.grimidk.formicempire.classes.infrasctructure.assets;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

final class CompatibleImages {

    private CompatibleImages() {
    }

    static BufferedImage copyToCompatible(BufferedImage source) {
        if (source == null) {
            return null;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= 0 || height <= 0) {
            return source;
        }
        try {
            if (GraphicsEnvironment.isHeadless()) {
                return copyArgb(source, width, height);
            }
            GraphicsConfiguration config = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();
            if (config == null) {
                return copyArgb(source, width, height);
            }
            BufferedImage compatible = config.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            Graphics2D g = compatible.createGraphics();
            try {
                g.drawImage(source, 0, 0, null);
            } finally {
                g.dispose();
            }
            return compatible;
        } catch (Exception ignore) {
            return copyArgb(source, width, height);
        }
    }

    private static BufferedImage copyArgb(BufferedImage source, int width, int height) {
        if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
            return source;
        }
        BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        try {
            g.drawImage(source, 0, 0, null);
        } finally {
            g.dispose();
        }
        return copy;
    }
}
