package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class MapDialog extends JDialog {

    private final World world;
    private final JTextArea mapArea;

    public MapDialog(JFrame owner, World world) {
        super(owner, "World Map", true);
        this.world = world;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));

        mapArea = new JTextArea();
        mapArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        mapArea.setEditable(false);
        mapArea.setBackground(new Color(30, 30, 30));
        mapArea.setForeground(Color.GREEN);
        
        add(new JScrollPane(mapArea), BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshMap());

        JPanel south = new JPanel();
        south.add(refreshButton);
        south.add(closeButton);
        add(south, BorderLayout.SOUTH);

        // Close on Escape
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(owner);
    }

    public void showDialog() {
        refreshMap();
        setVisible(true);
    }

    private void refreshMap() {
        if (world == null) {
            mapArea.setText("No world loaded.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        int size = world.getWorldRadius();
        
        Map<String, Hex> hexMap = new HashMap<>();
        for (Hex h : world.getHexes()) {
            hexMap.put(h.getQ() + "," + h.getR(), h);
        }

        // Ascii Hex Grid Logic
        for (int r = -size; r <= size; r++) {
            // Indentation for hex layout
            for (int k = 0; k < Math.abs(r); k++) sb.append("  "); 

            int q1 = Math.max(-size, -r - size);
            int q2 = Math.min(size, -r + size);

            for (int q = q1; q <= q2; q++) {
                Hex hex = hexMap.get(q + "," + r);
                if (hex != null && hex.getBiome() != null) {
                    char c = hex.getBiome().getName().charAt(0);
                    if (hex.getColony() != null) {
                        sb.append("[").append(c).append("] ");
                    } else {
                        sb.append(" ").append(c).append("  ");
                    }
                } else {
                    sb.append(" .  ");
                }
            }
            sb.append("\n");
        }
        
        sb.append("\nLegend:\n");
        sb.append("[X] : Colony Location (Center)\n");
        sb.append(" P  : Plains    T  : Taiga/Tundra\n");
        sb.append(" F  : Forest    D  : Desert\n");
        sb.append(" J  : Jungle    M  : Mountain\n");
        sb.append(" S  : Swamp     V  : Volcanic\n");
        
        mapArea.setText(sb.toString());
    }
}