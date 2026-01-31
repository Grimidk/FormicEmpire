package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.Civilization;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class CivilizationManagementDialog extends ZeroDialog {

    private final Civilization civilization;
    private final Engine engine;
    private final java.util.function.Consumer<Colony> onGoToColony;

    private JPanel listPanel;

    public CivilizationManagementDialog(JFrame owner, Civilization civilization, Engine engine, java.util.function.Consumer<Colony> onGoToColony) {
        super(owner, "Civilization Management", new Dimension(900, 600));
        this.civilization = civilization;
        this.engine = engine;
        this.onGoToColony = onGoToColony;

        initUI();
        refreshDialog();
        registerCloseKey(KeyEvent.VK_S);
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new GridLayout(1, 6));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(Color.LIGHT_GRAY);
        
        headerPanel.add(new JLabel("Name"));
        headerPanel.add(new JLabel("Rank"));
        headerPanel.add(new JLabel("Population"));
        headerPanel.add(new JLabel("Biome"));
        headerPanel.add(new JLabel("Automation"));
        headerPanel.add(new JLabel("Actions"));

        add(headerPanel, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void refreshDialog() {
        listPanel.removeAll();

        List<Colony> colonies = civilization.getColonies();
        World world = engine.getWorld();

        for (Colony colony : colonies) {
            JPanel row = new JPanel(new GridLayout(1, 6));
            row.setPreferredSize(new Dimension(800, 40));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

            // Name
            row.add(new JLabel(" " + colony.getName()));

            // Rank
            row.add(new JLabel(colony.getRank().getName()));

            // Population
            row.add(new JLabel(String.valueOf(colony.getAntTotal())));

            // Biome logic
            String biomeName = "Unknown";
            if (world != null && world.getHexes() != null) {
                for (Hex hex : world.getHexes()) {
                    if (hex.getColony() == colony && hex.getBiome() != null) {
                        biomeName = hex.getBiome().getName();
                        break;
                    }
                }
            }
            row.add(new JLabel(biomeName));

            // Automation Checkbox
            JCheckBox autoCheck = new JCheckBox("Enabled", colony.isAutomationEnabled());
            autoCheck.addActionListener(e -> {
                colony.setAutomationEnabled(autoCheck.isSelected());
            });
            row.add(autoCheck);

            // Actions Panel
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            
            JButton editBtn = new JButton("Edit");
            editBtn.setMargin(new Insets(2, 5, 2, 5));
            editBtn.addActionListener(e -> {
                String newName = JOptionPane.showInputDialog(this, "Enter new name for " + colony.getName(), colony.getName());
                if (newName != null && !newName.trim().isEmpty()) {
                    colony.setName(newName.trim());
                    refreshDialog();
                }
            });

            JButton viewBtn = new JButton("View");
            viewBtn.setMargin(new Insets(2, 5, 2, 5));
            viewBtn.addActionListener(e -> {
                if (onGoToColony != null) {
                    onGoToColony.accept(colony);
                    dispose();
                }
            });

            actions.add(editBtn);
            actions.add(viewBtn);
            row.add(actions);

            listPanel.add(row);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
}