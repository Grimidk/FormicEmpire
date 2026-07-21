package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IntroPanel extends JPanel {
    private static final int STUDIO_SLIDE_MS = 5000;
    private static final int WARNING_SLIDE_MS = 10000;
    private static final long SKIP_DEBOUNCE_MS = 200;
    private static final String STUDIO_NAME = "GrimIDK";

    private static final int SLIDE_STUDIO = 0;
    private static final int SLIDE_ARACHNOPHOBIA = 1;
    private static final int SLIDE_PHOTOSENSITIVITY = 2;

    private final MainFrame frame;
    private final JLabel messageLabel = new FullAreaLabel();
    private final MouseAdapter skipListener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            skipIntro();
        }
    };
    private Timer advanceTimer;
    private int slideIndex;
    private long lastSkipMs;

    public IntroPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        setBackground(AssetStyles.COLOR_ABSOLUTE_BLACK);

        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setForeground(AssetStyles.COLOR_ABSOLUTE_WHITE);
        messageLabel.setFont(AssetStyles.FONT_NORMAL);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(48, 48, 48, 48);
        add(messageLabel, c);

        addMouseListener(skipListener);
        messageLabel.addMouseListener(skipListener);
        setFocusable(true);

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                beginIntro();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                cancelAdvance();
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }

    public void refreshTranslations() {
        if (slideIndex == SLIDE_ARACHNOPHOBIA) {
            showArachnophobiaSlide();
        } else if (slideIndex == SLIDE_PHOTOSENSITIVITY) {
            showPhotosensitivitySlide();
        }
    }

    private void beginIntro() {
        cancelAdvance();
        slideIndex = SLIDE_STUDIO;
        Cursor clickable = AssetStyles.cursorClickable();
        setCursor(clickable);
        messageLabel.setCursor(clickable);
        showStudioSlide();
        scheduleNextSlide(STUDIO_SLIDE_MS);
        requestFocusInWindow();
    }

    private void skipIntro() {
        long now = System.currentTimeMillis();
        if (now - lastSkipMs < SKIP_DEBOUNCE_MS) {
            return;
        }
        lastSkipMs = now;
        advanceSlide();
    }

    private void showStudioSlide() {
        messageLabel.setText("<html><body style='text-align: center; color: " + AssetStyles.COLOR_ABSOLUTE_WHITE_HTML
                + "; font-family: " + AssetStyles.themeFontFamilyCss() + "; font-size: 20pt;'>"
                + STUDIO_NAME + "</body></html>");
    }

    private void showArachnophobiaSlide() {
        String text = LanguageStrings.withAppDisplayName(LanguageStrings.INTRO_WARNING);
        messageLabel.setText(warningHtml(text));
    }

    private void showPhotosensitivitySlide() {
        String text = LanguageStrings.get(LanguageStrings.INTRO_PHOTOSENSITIVITY_WARNING);
        messageLabel.setText(warningHtml(text));
    }

    private static String warningHtml(String text) {
        return "<html><body style='width: 520px; text-align: center; color: " + AssetStyles.COLOR_ABSOLUTE_WHITE_HTML
                + "; font-family: " + AssetStyles.themeFontFamilyCss() + "; font-size: 13pt;'>"
                + text + "</body></html>";
    }

    private void scheduleNextSlide(int delayMs) {
        cancelAdvance();
        advanceTimer = new Timer(delayMs, e -> advanceSlide());
        advanceTimer.setRepeats(false);
        advanceTimer.start();
    }

    private void advanceSlide() {
        cancelAdvance();
        if (slideIndex == SLIDE_STUDIO) {
            slideIndex = SLIDE_ARACHNOPHOBIA;
            showArachnophobiaSlide();
            scheduleNextSlide(WARNING_SLIDE_MS);
        } else if (slideIndex == SLIDE_ARACHNOPHOBIA) {
            slideIndex = SLIDE_PHOTOSENSITIVITY;
            showPhotosensitivitySlide();
            scheduleNextSlide(WARNING_SLIDE_MS);
        } else {
            frame.showCard(MainFrame.CARD_INIT);
        }
    }

    private void cancelAdvance() {
        if (advanceTimer != null) {
            advanceTimer.stop();
            advanceTimer = null;
        }
    }

    private static final class FullAreaLabel extends JLabel {
        @Override
        public boolean contains(int x, int y) {
            return getWidth() > 0 && getHeight() > 0;
        }
    }
}
