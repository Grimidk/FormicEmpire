package com.grimidk.formicempire.interfaces;

import com.grimidk.formicempire.classes.Savefile;

import javax.swing.*;
import java.awt.*;

public class SaveSelectPanel extends JPanel {
    private final MainFrame frame;

    public SaveSelectPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8,8,8,8);

        JButton back = new JButton("Back");
        JButton play1 = new JButton("Play First File");

    back.addActionListener(e -> this.frame.showCard(MainFrame.CARD_INIT));
    play1.addActionListener(e -> this.frame.openGameWithSave(new Savefile(1, "Grimidk")));

        c.gridy = 0; add(play1,c);
        c.gridy = 1; add(back,c);
    }
}
