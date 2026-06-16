package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;

public class IntroPanel extends JPanel {
    private static final int SLIDE_MS = 5000;
    private static final String STUDIO_NAME = "Grimidk";

    private final MainFrame frame;
    private final JLabel messageLabel = new JLabel();
    private Timer advanceTimer;
    private int slideIndex;

    public IntroPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        setBackground(AssetStyles.COLOR_ABSOLUTE_BLACK);

        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(AssetStyles.FONT_NORMAL);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(48, 48, 48, 48);
        add(messageLabel, c);

        LanguageStrings.addListener(this::refreshTranslations);

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
        if (slideIndex == 1) {
            showWarningSlide();
        }
    }

    private void beginIntro() {
        cancelAdvance();
        slideIndex = 0;
        showStudioSlide();
        scheduleNextSlide();
    }

    private void showStudioSlide() {
        messageLabel.setText("<html><body style='text-align: center; color: #FFFFFF; font-family: sans-serif; font-size: 20pt;'>"
                + STUDIO_NAME + "</body></html>");
    }

    private void showWarningSlide() {
        String text = LanguageStrings.get(LanguageStrings.INTRO_WARNING);
        messageLabel.setText("<html><body style='width: 520px; text-align: center; color: #FFFFFF; font-family: sans-serif; font-size: 13pt;'>"
                + text + "</body></html>");
    }

    private void scheduleNextSlide() {
        cancelAdvance();
        advanceTimer = new Timer(SLIDE_MS, e -> advanceSlide());
        advanceTimer.setRepeats(false);
        advanceTimer.start();
    }

    private void advanceSlide() {
        cancelAdvance();
        if (slideIndex == 0) {
            slideIndex = 1;
            showWarningSlide();
            scheduleNextSlide();
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
}
