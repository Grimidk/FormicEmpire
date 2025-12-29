package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MapDialog extends ZeroDialog {

    private final World world;
    private final JTextArea mapArea;
    private final JButton randomTravelButton;
    private final JButton homeButton; 
    private final JButton closeButton;
    private final Runnable onHexChange;

    public MapDialog(JFrame owner, World world, Runnable onHexChange) {
        super(owner, "World Map", new Dimension(800, 600));
        this.world = world;
        this.onHexChange = onHexChange;

        mapArea = new JTextArea();
        mapArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        mapArea.setEditable(false);
        mapArea.setFocusable(false); 
        mapArea.setBackground(new Color(30, 30, 30));
        mapArea.setForeground(Color.GREEN);
        
        // Random Travel 
        randomTravelButton = new JButton("Random Hex (Test)");
        randomTravelButton.setFocusable(false);
        randomTravelButton.addActionListener(e -> travelToRandomHex());
        
        // Home Button
        homeButton = new JButton("Go to Home");
        homeButton.setFocusable(false);
        homeButton.addActionListener(e -> travelToHomeHex());

        // Close Button
        closeButton = new JButton("Close");
        closeButton.setFocusable(false);
        closeButton.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(homeButton);
        bottomPanel.add(randomTravelButton);
        bottomPanel.add(closeButton);
        
        add(new JScrollPane(mapArea), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        registerCloseKey(KeyEvent.VK_I);        
        refreshDialog();
    }
    
    private void travelToRandomHex() {
        if (world == null || world.getHexes().isEmpty()) return;
        
        java.util.List<Hex> hexes = world.getHexes();
        Hex randomHex = hexes.get(new Random().nextInt(hexes.size()));
        
        changeHex(randomHex);
    }

    private void travelToHomeHex() {
        if (world == null) return;
        Hex homeHex = world.getSpawnHex();
        if (homeHex != null) {
            changeHex(homeHex);
        }
    }

    private void changeHex(Hex newHex) {
        if (newHex == null) return;
        world.changeActiveHex(newHex);
        refreshDialog();
        
        if (onHexChange != null) {
            onHexChange.run();
        }
    }

    @Override
    protected void refreshDialog() {
        if (world == null) {
            mapArea.setText("No world loaded.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        int size = world.getWorldRadius();
        Hex activeHex = world.getActiveHex();
        
        Map<String, Hex> hexMap = new HashMap<>();
        for (Hex h : world.getHexes()) {
            hexMap.put(h.getQ() + "," + h.getR(), h);
        }

        for (int r = -size; r <= size; r++) {
            for (int k = 0; k < Math.abs(r); k++) sb.append("  "); 

            int q1 = Math.max(-size, -r - size);
            int q2 = Math.min(size, -r + size);

            for (int q = q1; q <= q2; q++) {
                Hex hex = hexMap.get(q + "," + r);
                if (hex != null && hex.getBiome() != null) {
                    char c = hex.getBiome().getName().charAt(0);
                    
                    if (hex == activeHex) {
                        sb.append("<").append(c).append("> ");
                    } else if (hex.getColony() != null) {
                        if (hex.getColony().isPlayer()) {
                            sb.append("[").append(c).append("] ");
                        } else {
                            sb.append("{").append(c).append("} ");
                        }
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
        sb.append("<X> : Active Hex (You are here)\n");
        sb.append("[X] : Player Colony\n");
        sb.append("{X} : Wild Colony\n");
        sb.append(" P  : Plains    T  : Taiga/Tundra\n");
        sb.append(" F  : Forest    D  : Desert\n");
        sb.append(" J  : Jungle    M  : Mountain\n");
        sb.append(" S  : Swamp     V  : Volcanic\n");
        
        mapArea.setText(sb.toString());
        mapArea.setCaretPosition(0);
    }
}