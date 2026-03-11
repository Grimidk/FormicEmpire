package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.misc.ColonyRank;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDialog extends ZeroDialog {

    private final World world;
    private final HexMapPanel mapPanel;
    private final LegendPanel legendPanel;
    private final JButton homeButton;
    private final JButton closeButton;
    private final Runnable onHexChange;

    public MapDialog(JFrame owner, World world, Runnable onHexChange) {
        super(owner, LanguageStrings.DIALOG_MAP_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.world = world;
        this.onHexChange = onHexChange;

        // --- Main Map Panel ---
        this.mapPanel = new HexMapPanel();
        
        // --- Legend Panel ---
        this.legendPanel = new LegendPanel();
        
        // --- Buttons ---
        homeButton = new JButton(LanguageStrings.get(LanguageStrings.MAP_HOME_BUTTON));
        homeButton.setFocusable(false);
        homeButton.addActionListener(e -> travelToHomeHex());

        closeButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        closeButton.setFocusable(false);
        closeButton.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        bottomPanel.add(homeButton);
        bottomPanel.add(closeButton);

        add(legendPanel, BorderLayout.WEST);
        add(mapPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        registerCloseKey(KeyEvent.VK_M);
    }

    private void travelToHomeHex() {
        if (world == null) return;
        Hex homeHex = world.getSpawnHex();
        if (homeHex != null) {
            changeHex(homeHex);
        }
    }

    private void selectHex(Hex newHex) {
        if (newHex == null) return;
        world.changeActiveHex(newHex);
        
        if (onHexChange != null) {
            onHexChange.run();
        }
        
        if (mapPanel != null) {
            mapPanel.repaint();
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
        if (legendPanel != null) {
            legendPanel.updateLegend();
        }
    }

    private class LegendPanel extends JPanel {
        private final JPanel content;

        public LegendPanel() {
            setLayout(new BorderLayout());
            setBackground(AssetStyles.BACKGROUND_COLOR);
            setPreferredSize(new Dimension(220, 0));
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AssetStyles.BORDER_COLOR));

            JLabel title = new JLabel(LanguageStrings.get(LanguageStrings.MAP_LEGEND_TITLE), SwingConstants.CENTER);
            title.setFont(AssetStyles.FONT_BOLD);
            title.setForeground(AssetStyles.FONT_COLOR_HEADER);
            title.setBorder(new EmptyBorder(10, 5, 10, 5));
            add(title, BorderLayout.NORTH);

            content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(AssetStyles.BACKGROUND_COLOR);
            content.setBorder(new EmptyBorder(5, 10, 5, 5));
            
            JScrollPane scroll = new JScrollPane(content);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            add(scroll, BorderLayout.CENTER);

            updateLegend();
        }

        public void updateLegend() {
            content.removeAll();
            if (world == null || world.getHexes() == null) return;

            Map<Integer, Dynasty> activeDynastiesMap = new HashMap<>();
            for (Hex h : world.getHexes()) {
                if (h.getColony() != null && h.getColony().getDynasty() != null) {
                    Dynasty d = h.getColony().getDynasty();
                    activeDynastiesMap.put(d.getId(), d);
                }
            }

            List<Dynasty> sortedDynasties = new ArrayList<>(activeDynastiesMap.values());
            sortedDynasties.sort((d1, d2) -> {
                int p1 = d1.getStatService().getTotalPopulation(d1);
                int p2 = d2.getStatService().getTotalPopulation(d2);
                return Integer.compare(p2, p1);
            });

            for (Dynasty d : sortedDynasties) {
                JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
                item.setOpaque(false);
                item.setAlignmentX(Component.LEFT_ALIGNMENT);
                item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                item.setToolTipText(String.format(LanguageStrings.get(LanguageStrings.MAP_CLICK_VIEW_CAPITAL), d.getName()));

                item.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Colony capital = d.getCapital();
                        if (capital != null) {
                            Hex capitalHex = world.getHexOfColony(capital);
                            if (capitalHex != null) {
                                selectHex(capitalHex);
                            }
                        }
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        item.setOpaque(true);
                        item.setBackground(AssetStyles.BACKGROUND_SECONDARY);
                        item.repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        item.setOpaque(false);
                        item.repaint();
                    }
                });

                // Color box
                JPanel colorBox = new JPanel();
                colorBox.setPreferredSize(new Dimension(12, 12));
                colorBox.setBackground(d.getColor());
                colorBox.setBorder(BorderFactory.createLineBorder(AssetStyles.COLOR_ABSOLUTE_BLACK, 1));
                item.add(colorBox);

                // Species icon
                if (d.getSpecies() != null && d.getSpecies().getIcon() != null) {
                    JLabel icon = new JLabel(d.getSpecies().getIcon());
                    item.add(icon);
                }

                // Name
                String nameStr = d.getName();
                if (d.isPlayer()) {
                    nameStr += LanguageStrings.get(LanguageStrings.MAP_YOU_PLAYER);
                }
                JLabel name = new JLabel(nameStr);
                name.setFont(AssetStyles.FONT_SMALL);
                name.setForeground(AssetStyles.FONT_COLOR);
                if (d.isPlayer()) {
                    name.setFont(AssetStyles.FONT_BOLD.deriveFont(10f));
                }
                
                int pop = d.getStatService().getTotalPopulation(d);
                name.setToolTipText(String.format(LanguageStrings.get(LanguageStrings.MAP_POPULATION_FORMAT), pop));
                
                item.add(name);

                content.add(item);
                content.add(Box.createRigidArea(new Dimension(0, 2)));
            }

            content.revalidate();
            content.repaint();
        }
    }

    private class HexMapPanel extends JPanel {
        private int hexRadius = 26;  
        private final Map<Integer, Color> biomeColorCache = new HashMap<>();
        private final Map<Point, Hex> hexLookup = new HashMap<>();

        private final int[][] NEIGHBOR_OFFSETS = {
            {1, 0}, {0, 1}, {-1, 1}, {-1, 0}, {0, -1}, {1, -1}
        };

        public HexMapPanel() {
            setBackground(AssetStyles.BACKGROUND_COLOR);
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
                    
                    if (hex.getBiome() != null) {
                        sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_BIOME)).append(hex.getBiome().getName());
                    } else {
                        sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_BIOME)).append(LanguageStrings.get(LanguageStrings.STAT_UNKNOWN));
                    }
                    
                    Colony c = hex.getColony();
                    if (c != null) {
                        if (c.getRank() != null) {
                            sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_RANK)).append(c.getRank().getName());
                        }
                        
                        if (c.getSpecies() != null) {
                            sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_SPECIES)).append(c.getSpecies().getName());
                        } else {
                            sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_SPECIES)).append(LanguageStrings.get(LanguageStrings.STAT_UNKNOWN));
                        }
                        
                        if (c.getName() != null) {
                            sb.append("<br><i>").append(c.getName()).append("</i>");
                        }

                        Dynasty dynasty = c.getDynasty();
                        if (dynasty != null) {
                            sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_DYNASTY)).append(dynasty.getName());
                            if (dynasty.getRank() != null) {
                                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_DYNASTY_RANK)).append(dynasty.getRank().getName());
                            }
                        }
                    } else {
                        sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_EMPTY));
                    }
                    
                    sb.append("</html>");
                    return sb.toString();
                }
            }
            return null;
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

            hexLookup.clear();
            for (Hex h : world.getHexes()) {
                hexLookup.put(new Point(h.getQ(), h.getR()), h);
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Point centerOffset = getCenterOffset();
            Hex activeHex = world.getActiveHex();

            for (Hex hex : world.getHexes()) {
                drawHex(g2d, hex, centerOffset.x, centerOffset.y, false);
            }

            if (activeHex != null) {
                drawHex(g2d, activeHex, centerOffset.x, centerOffset.y, true);
            }
        }
        
        private Point getCenterOffset() {
            return new Point(getWidth() / 2, getHeight() / 2);
        }

        private void drawHex(Graphics2D g2d, Hex hex, int centerX, int centerY, boolean isSelectionPass) {
            Polygon poly = getHexPolygon(hex, centerX, centerY);
            
            if (!isSelectionPass) {
                // --- 1. Fill Background ---
                Rectangle bounds = poly.getBounds();
                int cx = (int)bounds.getCenterX();
                int cy = (int)bounds.getCenterY();
                
                Biome biome = hex.getBiome();
                Color fillColor = AssetStyles.BACKGROUND_COLOR;
                
                if (biome != null) {
                    fillColor = getBiomeColor(biome);
                }
                
                if (hex != world.getActiveHex()) {
                    fillColor = fadeToBackground(fillColor, 0.4f); 
                }
                
                g2d.setColor(fillColor);
                g2d.fillPolygon(poly);

                // --- 2. Draw Icons ---
                float scale = 0.85f;
                int iconSize = (int)(hexRadius * scale); 
                
                if (biome != null && biome.getIcon() != null) {
                    Image icon = biome.getIcon().getImage();
                    int iconX = cx - (iconSize / 2);
                    int iconY = cy - (iconSize / 2);
                    g2d.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
                }

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

                // --- 3. Draw Smart Borders ---
                drawMergedBorders(g2d, hex, poly);

            } else {
                g2d.setColor(AssetStyles.FONT_COLOR_ERROR);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawPolygon(poly);
            }
        }

        private void drawMergedBorders(Graphics2D g2d, Hex currentHex, Polygon poly) {
            int currentDynastyId = -1;
            Color dynastyColor = AssetStyles.BORDER_COLOR;
            boolean hasDynasty = false;

            if (currentHex.getColony() != null && currentHex.getColony().getDynasty() != null) {
                currentDynastyId = currentHex.getColony().getDynasty().getId();
                dynastyColor = currentHex.getColony().getDynasty().getColor();
                hasDynasty = true;
            }

            Point[] drawPoints = new Point[6];
            if (hasDynasty) {
                Rectangle bounds = poly.getBounds();
                double cx = bounds.getCenterX();
                double cy = bounds.getCenterY();
                double inset = 1.2; 
                double scale = (hexRadius - inset) / (double)hexRadius;
                
                for(int i=0; i<6; i++) {
                    double dx = poly.xpoints[i] - cx;
                    double dy = poly.ypoints[i] - cy;
                    drawPoints[i] = new Point((int)(cx + dx * scale), (int)(cy + dy * scale));
                }
            } else {
                for(int i=0; i<6; i++) {
                    drawPoints[i] = new Point(poly.xpoints[i], poly.ypoints[i]);
                }
            }

            for (int i = 0; i < 6; i++) {
                Point p1 = drawPoints[i];
                Point p2 = drawPoints[(i + 1) % 6];

                boolean shouldDrawEdge = true;

                if (hasDynasty) {
                    int[] offset = NEIGHBOR_OFFSETS[i];
                    int nQ = currentHex.getQ() + offset[0];
                    int nR = currentHex.getR() + offset[1];
                    
                    Hex neighbor = hexLookup.get(new Point(nQ, nR));

                    if (neighbor != null && neighbor.getColony() != null && neighbor.getColony().getDynasty() != null) {
                        int neighborDynastyId = neighbor.getColony().getDynasty().getId();
                        if (neighborDynastyId == currentDynastyId) {
                            shouldDrawEdge = false;
                        }
                    }
                    
                    g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.setColor(dynastyColor);
                } else {
                    g2d.setStroke(new BasicStroke(1f));
                    g2d.setColor(AssetStyles.BORDER_COLOR);
                }

                if (shouldDrawEdge) {
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
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
                return AssetStyles.BACKGROUND_COLOR;
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

                if (count == 0) return AssetStyles.BACKGROUND_COLOR;

                return new Color((int)(sumR/count), (int)(sumG/count), (int)(sumB/count));
                
            } catch (Exception e) {
                return AssetStyles.BACKGROUND_COLOR;
            }
        }
        
        private Color lighten(Color c, float amount) {
            Color bg = AssetStyles.BACKGROUND_COLOR;
            int r = Math.min(255, (int)(c.getRed() + (bg.getRed() - c.getRed()) * amount));
            int g = Math.min(255, (int)(c.getGreen() + (bg.getGreen() - c.getGreen()) * amount));
            int b = Math.min(255, (int)(c.getBlue() + (bg.getBlue() - c.getBlue()) * amount));
            return new Color(r, g, b, c.getAlpha());
        }
        
        private Color fadeToBackground(Color c, float factor) {
            Color bg = AssetStyles.BACKGROUND_COLOR;
            int r = (int) (c.getRed() * (1 - factor) + bg.getRed() * factor);
            int g = (int) (c.getGreen() * (1 - factor) + bg.getGreen() * factor);
            int b = (int) (c.getBlue() * (1 - factor) + bg.getBlue() * factor);
            return new Color(r, g, b);
        }
    }
}
