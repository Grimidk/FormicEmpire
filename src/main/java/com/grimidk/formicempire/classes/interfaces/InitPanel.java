package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.interfaces.menu.MenuHeaderPanel;
import com.grimidk.formicempire.classes.interfaces.menu.VersionLicenseFooter;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class InitPanel extends JPanel {
    private static final String DISCORD_URL = "https://discord.gg/2jF6s7jjTh";

    private final MainFrame frame;
    
    private final MenuHeaderPanel menuHeader = new MenuHeaderPanel();
    private final JPanel menuColumn = new JPanel(new GridBagLayout());
    private final VersionLicenseFooter footer =
            new VersionLicenseFooter(AssetStyles.COLOR_ABSOLUTE_BLACK);
    
    private JButton play;
    private JButton help;
    private JButton audit;
    private JButton roadmap;
    private JButton credits;
    private JButton discord;
    private JButton settings;
    private JButton quit;

    public InitPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout());

        menuColumn.setOpaque(false);
        add(menuColumn, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        
        initComponents();
        
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (!isShowing()) {
                    return;
                }
                refreshMenuOptions();
                play.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }
    
    private void initComponents() {
        play = createMenuButton(LanguageStrings.UI_PLAY, e -> frame.showCard(MainFrame.CARD_SAVE));
        help = createMenuButton(LanguageStrings.UI_HELP, e -> frame.showCard(MainFrame.CARD_HELP));
        audit = createMenuButton(LanguageStrings.UI_AUDIT, e -> HelpPanel.showAuditDialog(this));
        roadmap = createMenuButton(LanguageStrings.UI_ROADMAP, e -> HelpPanel.showRoadmapDialog(this));
        credits = createMenuButton(LanguageStrings.UI_CREDITS, e -> HelpPanel.showCreditsDialog(this));
        discord = createMenuButton(LanguageStrings.UI_DISCORD, e -> openDiscord());
        settings = createMenuButton(LanguageStrings.UI_SETTINGS, e -> frame.showSettingsMenu(MainFrame.CARD_INIT));
        quit = createMenuButton(LanguageStrings.UI_QUIT, e -> frame.requestExit());

        refreshMenuOptions();
    }

    private JButton createMenuButton(String labelKey, java.awt.event.ActionListener action) {
        JButton button = new JButton(LanguageStrings.get(labelKey));
        button.addActionListener(action);
        setupNavigation(button);
        AssetStyles.styleMenuButton(button);
        return button;
    }

    private void openDiscord() {
        try {
            if (!Desktop.isDesktopSupported()) {
                throw new UnsupportedOperationException();
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                throw new UnsupportedOperationException();
            }
            desktop.browse(URI.create(DISCORD_URL));
        } catch (Exception ex) {
            UiOptionPane.showForegroundMessageDialog(
                    this,
                    DISCORD_URL,
                    LanguageStrings.get(LanguageStrings.UI_DISCORD),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void refreshMenuOptions() {
        boolean showAudit = frame.getEngine().isShowAuditMenu();
        audit.setVisible(showAudit);
        layoutMenuButtons();
    }

    private void layoutMenuButtons() {
        menuColumn.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;

        c.gridy = 0;
        menuColumn.add(menuHeader, c);

        JButton[] buttons = audit.isVisible()
                ? new JButton[] { play, help, audit, roadmap, credits, discord, settings, quit }
                : new JButton[] { play, help, roadmap, credits, discord, settings, quit };

        for (int i = 0; i < buttons.length; i++) {
            c.gridy = i + 1;
            menuColumn.add(buttons[i], c);
        }

        menuColumn.revalidate();
        menuColumn.repaint();
    }
    
    public void refreshTranslations() {
        play.setText(LanguageStrings.get(LanguageStrings.UI_PLAY));
        help.setText(LanguageStrings.get(LanguageStrings.UI_HELP));
        audit.setText(LanguageStrings.get(LanguageStrings.UI_AUDIT));
        roadmap.setText(LanguageStrings.get(LanguageStrings.UI_ROADMAP));
        credits.setText(LanguageStrings.get(LanguageStrings.UI_CREDITS));
        discord.setText(LanguageStrings.get(LanguageStrings.UI_DISCORD));
        settings.setText(LanguageStrings.get(LanguageStrings.UI_SETTINGS));
        quit.setText(LanguageStrings.get(LanguageStrings.UI_QUIT));
        footer.refreshTranslations();
    }

    public void refreshTheme() {
        AssetStyles.applyThemeToContainer(this);
        footer.applyTextColor();
    }

    private void setupNavigation(JButton button) {
        button.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "pressed");
        button.getActionMap().put("pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button.doClick();
            }
        });

        Set<AWTKeyStroke> forwardKeys = new HashSet<>(button.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.add(KeyStroke.getKeyStroke("DOWN"));
        forwardKeys.add(KeyStroke.getKeyStroke("RIGHT"));
        button.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

        Set<AWTKeyStroke> backwardKeys = new HashSet<>(button.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.add(KeyStroke.getKeyStroke("UP"));
        backwardKeys.add(KeyStroke.getKeyStroke("LEFT"));
        button.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
    }
}
