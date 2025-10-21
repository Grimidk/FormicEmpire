package com.grimidk.formicempire.interfaces;

import javax.swing.*;
import java.awt.*;

public class InitPanel extends JPanel {
    private final MainFrame frame;

    public InitPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 8, 8);

        JButton play = new JButton("Play");
        JButton help = new JButton("Help");
        JButton settings = new JButton("Settings");
        JButton quit = new JButton("Quit");

    play.addActionListener(e -> this.frame.showCard(MainFrame.CARD_SAVE));
    help.addActionListener(e -> this.frame.showCard(MainFrame.CARD_HELP));
    settings.addActionListener(e -> this.frame.showCard(MainFrame.CARD_SETTINGS));
        quit.addActionListener(e -> System.exit(0));

        c.gridy = 0; add(play, c);
        c.gridy = 1; add(help, c);
        c.gridy = 2; add(settings, c);
        c.gridy = 3; add(quit, c);
    }
}
