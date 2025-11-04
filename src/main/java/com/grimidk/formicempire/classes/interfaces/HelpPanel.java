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
        
        // Game Info Text
        String gameInfo = "<html><p style='width: 350px;'>" +
                "<b>Basic Tips:</b><br>" +
                "Your main food is <b>Fungi</b> (Mushrooms), which ants will eat daily. " +
                "Assign <b>Workers</b> (Q) to <b>Forager</b> roles to gather Plants and <b>Soldiers</b> (W) to <b>Hunter</b> roles to gather Protein. " +
                "Assign <b>Farmers</b> (Q) to convert Plants and Protein into Fungi." +
                "<br><br>" +
                "Assign <b>Nurses</b> (Q) to care for your <b>Eggs, Larvae, and Pupae</b>. Without enough nurses, your young may die!" +
                "<br><br>" +
                "Use the <b>Hatch Rates</b> (P) menu to control what type of ants your Pupae become." +
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
        
        hotkeyPanel.add(new JLabel("Hatch Rates:"));
        hotkeyPanel.add(new JLabel("P"));

        // Main Panel for JOptionPane
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.add(new JLabel(story), BorderLayout.NORTH);
        mainPanel.add(new JLabel(gameInfo), BorderLayout.CENTER);
        mainPanel.add(hotkeyPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(
                parent,
                mainPanel,
                "Welcome to Formic Empire!",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}