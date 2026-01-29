package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.misc.ColonyRank;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Civilization;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class MapDialog extends ZeroDialog {

    private final World world;
    private final HexMapPanel mapPanel;
    private final JButton homeButton;
    private final JButton closeButton;
    private final Runnable onHexChange;

    public MapDialog(JFrame owner, World world, Runnable onHexChange) {
        super(owner, "World Map", new Dimension(1000, 800));
        this.world = world;
        this.onHexChange = onHexChange;

        // --- Main Map Panel ---
        this.mapPanel = new HexMapPanel();
        
        // --- Buttons ---
        homeButton = new JButton("Center on Home");
        homeButton.setFocusable(false);
        homeButton.addActionListener(e -> travelToHomeHex());

        closeButton = new JButton("Close");
        closeButton.setFocusable(false);
        closeButton.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(homeButton);
        bottomPanel.add(closeButton);

        add(mapPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        registerCloseKey(KeyEvent.VK_I);
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
        
        if (onHexChange != null) {
            onHexChange.run();
        }
        dispose();
    }

    @Override
    protected void refreshDialog() {
        if (mapPanel != null) {
            mapPanel.repaint();
        }
    }

    private class HexMapPanel extends JPanel {
        private int hexRadius = 26; 
        
        private final Map<Integer, Color> biomeColorCache = new HashMap<>();

        public HexMapPanel() {
            setBackground(Color.WHITE);
            // Register with ToolTipManager to ensure getToolTipText is called
            ToolTipManager.sharedInstance().registerComponent(this);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleMouseClick(e.getPoint());
                }
            });
        }

        private void calculateHexSize() {
            if (world == null) return;
            
            int worldR = world.getWorldRadius();
            
            int panelW = getWidth();
            int panelH = getHeight();
            
            if (panelW <= 0 || panelH <= 0) return; 

            double hexesAcross = (worldR * 2 + 1) + 1.5; 
            double hexesHigh = (worldR * 2 + 1) + 1.5; 
            double maxRadiusW = panelW / (hexesAcross * Math.sqrt(3));
            double maxRadiusH = panelH / (hexesHigh * 1.5);
            
            this.hexRadius = (int) Math.min(Math.min(maxRadiusW, maxRadiusH), 55);
            if (this.hexRadius < 10) this.hexRadius = 10; 
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            if (world == null || world.getHexes() == null) return null;

            Point p = e.getPoint();
            Point centerOffset = getCenterOffset();

            for (Hex hex : world.getHexes()) {
                Polygon poly = getHexPolygon(hex, centerOffset.x, centerOffset.y);
                if (poly.contains(p)) {
                    StringBuilder sb = new StringBuilder("<html>");
                    
                    // 1. Biome Name
                    if (hex.getBiome() != null) {
                        sb.append("<b>Biome:</b> ").append(hex.getBiome().getName());
                    } else {
                        sb.append("<b>Biome:</b> Unknown");
                    }
                    
                    // 2. Colony Info
                    Colony c = hex.getColony();
                    if (c != null) {
                        if (c.getRank() != null) {
                            sb.append("<br><b>Rank:</b> ").append(c.getRank().getName());
                        }
                        
                        if (c.getSpecies() != null) {
                            sb.append("<br><b>Species:</b> ").append(c.getSpecies().getName());
                        } else {
                            sb.append("<br><b>Species:</b> Unknown");
                        }
                        
                        if (c.getName() != null) {
                            sb.append("<br><i>").append(c.getName()).append("</i>");
                        }

                        // Added Civ Info
                        Civilization civ = c.getCivilization();
                        if (civ != null) {
                            sb.append("<br><b>Civ:</b> ").append(civ.getName());
                            if (civ.getRank() != null) {
                                sb.append("<br><b>Civ Rank:</b> ").append(civ.getRank().getName());
                            }
                        }
                    } else {
                        sb.append("<br><i>Empty</i>");
                    }
                    
                    sb.append("</html>");
                    return sb.toString();
                }
            }
            return null; // No hex under cursor
        }

        private void handleMouseClick(Point p) {
            if (world == null || world.getHexes() == null) return;

            Point centerOffset = getCenterOffset();

            for (Hex hex : world.getHexes()) {
                Polygon poly = getHexPolygon(hex, centerOffset.x, centerOffset.y);
                if (poly.contains(p)) {
                    changeHex(hex);
                    return;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (world == null || world.getHexes() == null) return;
            
            calculateHexSize();

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            Point centerOffset = getCenterOffset();
            Hex activeHex = world.getActiveHex();

            for (Hex hex : world.getHexes()) {
                drawHex(g2d, hex, centerOffset.x, centerOffset.y, hex == activeHex);
            }
        }
        
        private Point getCenterOffset() {
            return new Point(getWidth() / 2, getHeight() / 2);
        }

        private void drawHex(Graphics2D g2d, Hex hex, int centerX, int centerY, boolean isActive) {
            Polygon poly = getHexPolygon(hex, centerX, centerY);
            Rectangle bounds = poly.getBounds();
            int cx = (int)bounds.getCenterX();
            int cy = (int)bounds.getCenterY();
            
            // 1. Fill Background
            Biome biome = hex.getBiome();
            Color fillColor = Color.LIGHT_GRAY;
            
            if (biome != null) {
                fillColor = getBiomeColor(biome);
            }
            
            if (!isActive) {
                fillColor = fadeToWhite(fillColor, 0.4f); 
            }
            
            g2d.setColor(fillColor);
            g2d.fillPolygon(poly);

            // 2. Draw Biome Icon
            float scale = 0.85f;
            int iconSize = (int)(hexRadius * scale); 
            
            if (biome != null && biome.getIcon() != null) {
                Image icon = biome.getIcon().getImage();
                int iconX = cx - (iconSize / 2);
                int iconY = cy - (iconSize / 2);
                
                g2d.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
            }

            // 3. Draw Colony Rank 
            if (hex.getColony() != null) {
                Colony c = hex.getColony();
                ColonyRank rank = c.getRank();
                
                if (rank != null && rank.getIcon() != null) {
                    Image rankImg = rank.getIcon().getImage();
                    
                    int rankSize = (int)(hexRadius * scale);                    
                    int rankX = cx - (rankSize / 2);
                    int rankY = cy - (rankSize / 2) - (int)(hexRadius * scale); 
                    
                    g2d.drawImage(rankImg, rankX, rankY, rankSize, rankSize, null);
                }
            }

            // 4. Draw Border
            g2d.setStroke(new BasicStroke(isActive ? 3 : 1));
            if (isActive) {
                g2d.setColor(Color.RED); 
            } else {
                g2d.setColor(Color.BLACK); 
            }
            g2d.drawPolygon(poly);
        }

        private Polygon getHexPolygon(Hex hex, int offsetX, int offsetY) {
            double x = hexRadius * (Math.sqrt(3) * hex.getQ() + Math.sqrt(3) / 2.0 * hex.getR());
            double y = hexRadius * (3.0 / 2.0 * hex.getR());

            int centerX = (int) (x + offsetX);
            int centerY = (int) (y + offsetY);

            Polygon poly = new Polygon();
            for (int i = 0; i < 6; i++) {
                double angle_rad = Math.PI / 180 * (60 * i - 30);
                int px = (int) (centerX + hexRadius * Math.cos(angle_rad));
                int py = (int) (centerY + hexRadius * Math.sin(angle_rad));
                poly.addPoint(px, py);
            }
            return poly;
        }

        private Color getBiomeColor(Biome biome) {
            if (biomeColorCache.containsKey(biome.getId())) {
                return biomeColorCache.get(biome.getId());
            }

            if (biome.getIcon() == null) {
                return Color.LIGHT_GRAY;
            }

            Color avgColor = calculateAverageColor(biome.getIcon());
            avgColor = lighten(avgColor, 0.5f); 
            
            biomeColorCache.put(biome.getId(), avgColor);
            return avgColor;
        }

        private Color calculateAverageColor(ImageIcon icon) {
            try {
                Image img = icon.getImage();
                BufferedImage bi = new BufferedImage(
                    img.getWidth(null), 
                    img.getHeight(null), 
                    BufferedImage.TYPE_INT_ARGB
                );
                
                Graphics g = bi.createGraphics();
                g.drawImage(img, 0, 0, null);
                g.dispose();

                long sumR = 0, sumG = 0, sumB = 0;
                long count = 0;

                for (int x = 0; x < bi.getWidth(); x++) {
                    for (int y = 0; y < bi.getHeight(); y++) {
                        int pixel = bi.getRGB(x, y);
                        int alpha = (pixel >> 24) & 0xff;
                        
                        if (alpha < 20) continue; 
                        
                        if ((x % 3 == 0) && (y % 3 == 0)) { 
                            sumR += (pixel >> 16) & 0xff;
                            sumG += (pixel >> 8) & 0xff;
                            sumB += (pixel) & 0xff;
                            count++;
                        }
                    }
                }

                if (count == 0) return Color.WHITE;

                return new Color((int)(sumR/count), (int)(sumG/count), (int)(sumB/count));
                
            } catch (Exception e) {
                return Color.LIGHT_GRAY;
            }
        }
        
        private Color lighten(Color c, float amount) {
            int r = Math.min(255, (int)(c.getRed() + (255 - c.getRed()) * amount));
            int g = Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * amount));
            int b = Math.min(255, (int)(c.getBlue() + (255 - c.getBlue()) * amount));
            return new Color(r, g, b, c.getAlpha());
        }
        
        private Color fadeToWhite(Color c, float factor) {
            int r = (int) (c.getRed() * (1 - factor) + 255 * factor);
            int g = (int) (c.getGreen() * (1 - factor) + 255 * factor);
            int b = (int) (c.getBlue() * (1 - factor) + 255 * factor);
            return new Color(r, g, b);
        }
    }
}