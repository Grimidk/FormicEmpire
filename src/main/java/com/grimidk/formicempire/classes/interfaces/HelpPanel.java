package com.grimidk.formicempire.classes.interfaces;

import javax.swing.*;
import java.awt.*;

public class HelpPanel extends JPanel {
    private final MainFrame frame;

    public HelpPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE; 
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.CENTER;

        JButton showTutorial = new JButton("Show Tutorial");
        showTutorial.addActionListener(e -> showTutorialDialog(frame));

        JButton back = new JButton("Back");
        back.addActionListener(e -> this.frame.showCard(MainFrame.CARD_INIT));

        c.gridy = 0;
        add(showTutorial, c);

        c.gridy = 1;
        add(back, c);
    }

    public static void showTutorialDialog(Component parent) {
        // Story Text
        String story = "<html><p style='width: 350px;'>" +
                "Nearly all other ants are extinct. You are an ant queen and have within you " +
                "all the genetic knowledge of every ant species. You must unlock it and " +
                "take over the world as the dominant species." +
                "</p></html>";

        // Hotkey Panel
        JPanel hotkeyPanel = new JPanel(new GridLayout(0, 2, 10, 5)); 
        hotkeyPanel.setBorder(BorderFactory.createTitledBorder("Hotkeys"));

        hotkeyPanel.add(new JLabel("Pause/Play:"));
        hotkeyPanel.add(new JLabel("Spacebar"));

        hotkeyPanel.add(new JLabel("Speed Up:"));
        hotkeyPanel.add(new JLabel("+ (Add)"));

        hotkeyPanel.add(new JLabel("Speed Down:"));
        hotkeyPanel.add(new JLabel("- (Subtract)"));

        hotkeyPanel.add(new JLabel("Game Menu:"));
        hotkeyPanel.add(new JLabel("ESC"));

        // Add a visual separator
        hotkeyPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        hotkeyPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

        hotkeyPanel.add(new JLabel("Worker Roles:"));
        hotkeyPanel.add(new JLabel("Q"));

        hotkeyPanel.add(new JLabel("Soldier Roles:"));
        hotkeyPanel.add(new JLabel("W"));

        hotkeyPanel.add(new JLabel("Major Roles:"));
        hotkeyPanel.add(new JLabel("E"));

        hotkeyPanel.add(new JLabel("Princess Roles:"));
        hotkeyPanel.add(new JLabel("R"));

        hotkeyPanel.add(new JLabel("Queen Roles:"));
        hotkeyPanel.add(new JLabel("T"));

        // Main Panel for JOptionPane
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.add(new JLabel(story), BorderLayout.NORTH);
        mainPanel.add(hotkeyPanel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
                parent,
                mainPanel,
                "Welcome to Formic Empire!",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}