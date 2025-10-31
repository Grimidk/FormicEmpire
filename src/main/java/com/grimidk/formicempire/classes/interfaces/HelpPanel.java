package com.grimidk.formicempire.classes.interfaces;

import javax.swing.*;
import java.awt.*;

public class HelpPanel extends JPanel {
    private final MainFrame frame;

    public HelpPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8,8,8,8);

        JButton back = new JButton("Back");
        back.addActionListener(e -> this.frame.showCard(MainFrame.CARD_INIT));

        c.gridy = 0; add(new JLabel("Help content"), c);
        c.gridy = 1; add(back, c);
    }
}
