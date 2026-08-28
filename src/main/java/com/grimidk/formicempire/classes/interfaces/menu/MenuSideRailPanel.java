package com.grimidk.formicempire.classes.interfaces.menu;

import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.net.URI;

public class MenuSideRailPanel extends JPanel {
    private static final int ICON_DISPLAY_SIZE = 64;
    private static final Dimension ICON_SLOT = new Dimension(ICON_DISPLAY_SIZE, ICON_DISPLAY_SIZE);
    private static final String DISCORD_URL = "https://discord.gg/2jF6s7jjTh";
    private static final String INSTAGRAM_URL = "https://www.instagram.com/grim.idk/";
    private static final String FEEDBACK_FORM_URL = "https://forms.gle/d8Y43MxvkUcUbGFj7";

    private final MainFrame frame;
    private final JButton discordButton;
    private final JButton instagramButton;
    private final JButton helpButton;
    private final JButton feedbackButton;
    private final JButton settingsButton;

    public MenuSideRailPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 8, 8, 12));

        JPanel topRail = new JPanel();
        topRail.setOpaque(false);
        topRail.setLayout(new BoxLayout(topRail, BoxLayout.Y_AXIS));

        discordButton = createRailButton(GameConstants.ICON_SOCIAL_DISCORD, e -> openUrl(
                DISCORD_URL, LanguageStrings.UI_DISCORD));
        instagramButton = createRailButton(GameConstants.ICON_SOCIAL_INSTAGRAM, e -> openUrl(
                INSTAGRAM_URL, LanguageStrings.UI_INSTAGRAM));

        topRail.add(discordButton);
        topRail.add(Box.createVerticalStrut(8));
        topRail.add(instagramButton);

        JPanel bottomRail = new JPanel();
        bottomRail.setOpaque(false);
        bottomRail.setLayout(new BoxLayout(bottomRail, BoxLayout.Y_AXIS));

        helpButton = createRailButton(GameConstants.ICON_SOCIAL_HELP,
                e -> frame.showCard(MainFrame.CARD_HELP));
        feedbackButton = createRailButton(GameConstants.ICON_SOCIAL_FEEDBACK, e -> openUrl(
                FEEDBACK_FORM_URL, LanguageStrings.UI_FEEDBACK));
        settingsButton = createRailButton(
                GameConstants.ICON_SOCIAL_SETTINGS,
                e -> frame.showSettingsMenu(MainFrame.CARD_INIT));

        bottomRail.add(helpButton);
        bottomRail.add(Box.createVerticalStrut(8));
        bottomRail.add(feedbackButton);
        bottomRail.add(Box.createVerticalStrut(8));
        bottomRail.add(settingsButton);

        add(topRail, BorderLayout.NORTH);
        add(bottomRail, BorderLayout.SOUTH);
        refreshTranslations();
    }

    public void refreshTranslations() {
        discordButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_DISCORD));
        instagramButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_INSTAGRAM));
        helpButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_HELP_TT));
        feedbackButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_FEEDBACK_TT));
        settingsButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_SETTINGS));
    }

    public void refreshTheme() {
        styleRailButton(discordButton, GameConstants.ICON_SOCIAL_DISCORD);
        styleRailButton(instagramButton, GameConstants.ICON_SOCIAL_INSTAGRAM);
        styleRailButton(helpButton, GameConstants.ICON_SOCIAL_HELP);
        styleRailButton(feedbackButton, GameConstants.ICON_SOCIAL_FEEDBACK);
        styleRailButton(settingsButton, GameConstants.ICON_SOCIAL_SETTINGS);
    }

    private static JButton createRailButton(ImageIcon icon, ActionListener action) {
        JButton button = new JButton();
        button.setFocusable(false);
        button.addActionListener(action);
        styleRailButton(button, icon);
        return button;
    }

    private static void styleRailButton(JButton button, ImageIcon icon) {
        AssetStyles.styleIconButton(button);
        button.setIcon(displayIcon(icon));
        button.setText("");
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setPreferredSize(ICON_SLOT);
        button.setMinimumSize(ICON_SLOT);
        button.setMaximumSize(ICON_SLOT);
    }

    private static ImageIcon displayIcon(ImageIcon source) {
        if (source == null || source.getIconWidth() <= 0) {
            return source;
        }
        if (source.getIconWidth() == ICON_DISPLAY_SIZE && source.getIconHeight() == ICON_DISPLAY_SIZE) {
            return source;
        }
        Image scaled = source.getImage().getScaledInstance(
                ICON_DISPLAY_SIZE, ICON_DISPLAY_SIZE, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void openUrl(String url, String titleKey) {
        try {
            if (!Desktop.isDesktopSupported()) {
                throw new UnsupportedOperationException();
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                throw new UnsupportedOperationException();
            }
            desktop.browse(URI.create(url));
        } catch (Exception ex) {
            UiOptionPane.showForegroundMessageDialog(
                    this,
                    url,
                    LanguageStrings.get(titleKey),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
