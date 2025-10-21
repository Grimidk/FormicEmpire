package com.grimidk.formicempire.interfaces;

import com.grimidk.formicempire.classes.Engine;
import com.grimidk.formicempire.classes.Savefile;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private final MainFrame frame;
    private final JLabel statusLabel;

    public GamePanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        statusLabel = new JLabel("Game not started");
        add(statusLabel, BorderLayout.CENTER);

        JButton back = new JButton("Back");
        back.addActionListener(e -> frame.showCard(MainFrame.CARD_SAVE));
        add(back, BorderLayout.SOUTH);
    }

    public void enterWithSavefile(Savefile savefile) {
        statusLabel.setText("Starting game...");
        // run engine.startUp on a background thread
        Engine engine = frame.getEngine();
        new Thread(() -> {
            engine.startUp(savefile);
            SwingUtilities.invokeLater(() -> statusLabel.setText("Game started"));
        }).start();
    }
}
