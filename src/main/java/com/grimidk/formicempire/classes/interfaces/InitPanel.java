package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class InitPanel extends JPanel {
    private final MainFrame frame;
    
    private JButton play;
    private JButton help;
    private JButton roadmap;
    private JButton credits;
    private JButton settings;
    private JButton quit;

    public InitPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        
        initComponents();
        
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                play.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }
    
    private void initComponents() {
        removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 8, 8);

        play = new JButton(LanguageStrings.get(LanguageStrings.UI_PLAY));
        help = new JButton(LanguageStrings.get(LanguageStrings.UI_HELP));
        roadmap = new JButton(LanguageStrings.get(LanguageStrings.UI_ROADMAP));
        credits = new JButton(LanguageStrings.get(LanguageStrings.UI_CREDITS));
        settings = new JButton(LanguageStrings.get(LanguageStrings.UI_SETTINGS));
        quit = new JButton(LanguageStrings.get(LanguageStrings.UI_QUIT));

        play.addActionListener(e -> this.frame.showCard(MainFrame.CARD_SAVE));
        help.addActionListener(e -> this.frame.showCard(MainFrame.CARD_HELP));
        roadmap.addActionListener(e -> HelpPanel.showRoadmapDialog(this));
        credits.addActionListener(e -> HelpPanel.showCreditsDialog(this));
        settings.addActionListener(e -> this.frame.showSettingsMenu(MainFrame.CARD_INIT));
        quit.addActionListener(e -> frame.requestExit());

        setupNavigation(play);
        setupNavigation(help);
        setupNavigation(roadmap);
        setupNavigation(credits);
        setupNavigation(settings);
        setupNavigation(quit);
        AssetStyles.styleButton(play);
        AssetStyles.styleButton(help);
        AssetStyles.styleButton(roadmap);
        AssetStyles.styleButton(credits);
        AssetStyles.styleButton(settings);
        AssetStyles.styleButton(quit);

        c.gridy = 0; add(play, c);
        c.gridy = 1; add(help, c);
        c.gridy = 2; add(roadmap, c);
        c.gridy = 3; add(credits, c);
        c.gridy = 4; add(settings, c);
        c.gridy = 5; add(quit, c);
        
        revalidate();
        repaint();
    }
    
    public void refreshTranslations() {
        play.setText(LanguageStrings.get(LanguageStrings.UI_PLAY));
        help.setText(LanguageStrings.get(LanguageStrings.UI_HELP));
        roadmap.setText(LanguageStrings.get(LanguageStrings.UI_ROADMAP));
        credits.setText(LanguageStrings.get(LanguageStrings.UI_CREDITS));
        settings.setText(LanguageStrings.get(LanguageStrings.UI_SETTINGS));
        quit.setText(LanguageStrings.get(LanguageStrings.UI_QUIT));
    }

    public void refreshTheme() {
        AssetStyles.applyThemeToContainer(this);
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