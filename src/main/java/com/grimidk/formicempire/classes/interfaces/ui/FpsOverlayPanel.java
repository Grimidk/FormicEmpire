package com.grimidk.formicempire.classes.interfaces.ui;

import com.grimidk.formicempire.classes.infrasctructure.diagnostics.FrameRateTracker;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.ui.theme.UiColorUtils;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class FpsOverlayPanel extends JPanel {

    private static final int PADDING_X = 10;
    private static final int PADDING_Y = 6;
    private static final int MARGIN = 12;

    private final FrameRateTracker tracker;
    private final Timer refreshTimer;
    private String displayText = "";

    public FpsOverlayPanel(FrameRateTracker tracker) {
        this.tracker = tracker;
        setOpaque(false);
        refreshTimer = new Timer(250, e -> refreshDisplayText());
        refreshTimer.setRepeats(true);
    }

    public void applyEnabled(boolean enabled) {
        if (enabled) {
            setVisible(true);
            refreshTimer.start();
            refreshDisplayText();
        } else {
            refreshTimer.stop();
            tracker.reset();
            displayText = "";
            setVisible(false);
        }
        repaint();
    }

    public void onFramePainted() {
        if (!isVisible()) {
            return;
        }
        tracker.recordFrame();
    }

    private void refreshDisplayText() {
        if (!isVisible()) {
            return;
        }
        String next;
        if (!tracker.hasSamples()) {
            next = LanguageStrings.get(LanguageStrings.SETTINGS_FPS_OVERLAY_WAITING);
        } else {
            next = LanguageStrings.format(
                    LanguageStrings.SETTINGS_FPS_OVERLAY,
                    String.format("%.0f", tracker.getSmoothedFps()),
                    String.format("%.1f", tracker.getSmoothedFrameMs()));
        }
        if (!next.equals(displayText)) {
            displayText = next;
            repaint();
        }
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible() || displayText.isEmpty()) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(AssetStyles.FONT_BOLD);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(displayText);
            int textHeight = fm.getHeight();
            int boxWidth = textWidth + PADDING_X * 2;
            int boxHeight = textHeight + PADDING_Y;
            int boxX = Math.max(MARGIN, getWidth() - MARGIN - boxWidth);
            int boxY = MARGIN;
            g2d.setColor(UiColorUtils.colorFromRgba(0, 0, 0, 170));
            g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 6, 6);
            g2d.setColor(AssetStyles.FONT_COLOR);
            g2d.drawString(displayText, boxX + PADDING_X, boxY + fm.getAscent() + (PADDING_Y / 2));
        } finally {
            g2d.dispose();
        }
    }
}
