package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class InitPanel extends JPanel {
    private final MainFrame frame;

    public InitPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 8, 8);

        JButton play = new JButton(LanguageStrings.UI_PLAY);
        JButton help = new JButton(LanguageStrings.UI_HELP);
        JButton settings = new JButton(LanguageStrings.UI_SETTINGS);
        JButton quit = new JButton(LanguageStrings.UI_QUIT);

        play.addActionListener(e -> this.frame.showCard(MainFrame.CARD_SAVE));
        help.addActionListener(e -> this.frame.showCard(MainFrame.CARD_HELP));
        settings.addActionListener(e -> this.frame.showCard(MainFrame.CARD_SETTINGS));
        quit.addActionListener(e -> System.exit(0));

        setupNavigation(play);
        setupNavigation(help);
        setupNavigation(settings);
        setupNavigation(quit);

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

        c.gridy = 0; add(play, c);
        c.gridy = 1; add(help, c);
        c.gridy = 2; add(settings, c);
        c.gridy = 3; add(quit, c);
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