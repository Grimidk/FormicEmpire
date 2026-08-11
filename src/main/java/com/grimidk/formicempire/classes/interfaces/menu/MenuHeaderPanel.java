package com.grimidk.formicempire.classes.interfaces.menu;

import com.grimidk.formicempire.classes.interfaces.ui.util.UiResourceLoader;

import javax.swing.*;
import java.awt.*;

public class MenuHeaderPanel extends JPanel {

    private static final String HEADER_IMAGE_PATH = "/meta/menu/GameHeader.png";

    private Image headerImage;

    public MenuHeaderPanel() {
        setOpaque(false);
        Dimension size = new Dimension(500, 200);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        loadHeaderImage();
    }

    private void loadHeaderImage() {
        headerImage = UiResourceLoader.loadImage(MenuHeaderPanel.class, HEADER_IMAGE_PATH);
        if (headerImage == null) {
            headerImage = UiResourceLoader.loadImage(MenuHeaderPanel.class, HEADER_IMAGE_PATH.substring(1));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (headerImage == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w = getWidth();
        int h = getHeight();
        if (w > 0 && h > 0) {
            g2d.drawImage(headerImage, 0, 0, w, h, this);
        }
        g2d.dispose();
    }
}
