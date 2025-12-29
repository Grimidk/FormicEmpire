package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class MapDialog extends ZeroDialog {

    private final World world;
    private final JTextArea mapArea;

    public MapDialog(JFrame owner, World world) {
        super(owner, "World Map", new Dimension(800, 600));
        this.world = world;

        mapArea = new JTextArea();
        mapArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        mapArea.setEditable(false);
        mapArea.setFocusable(false); 
        mapArea.setBackground(new Color(30, 30, 30));
        mapArea.setForeground(Color.GREEN);
        
        add(new JScrollPane(mapArea), BorderLayout.CENTER);

        registerCloseKey(KeyEvent.VK_I);
    }

    @Override
    protected void refreshDialog() {
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

        for (int r = -size; r <= size; r++) {
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
        mapArea.setCaretPosition(0);
    }
}