package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.misc.ColonyRank;
import com.grimidk.formicempire.classes.constants.misc.DiplomaticReputation;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatChevronButton;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiScrollBarStyles;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
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
        super(owner, LanguageStrings.DIALOG_MAP_TITLE, AssetStyles.MAP_DIALOG_SIZE);
        this.world = world;
        this.onHexChange = onHexChange;

        this.mapPanel = new HexMapPanel();
        this.legendPanel = new LegendPanel();

        homeButton = new JButton(LanguageStrings.get(LanguageStrings.MAP_HOME_BUTTON));
        homeButton.setFocusable(false);
        AssetStyles.styleButton(homeButton);
        homeButton.addActionListener(e -> travelToHomeHex());

        closeButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        closeButton.setFocusable(false);
        AssetStyles.styleButton(closeButton);
        closeButton.addActionListener(e -> dispose());

        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
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
            changeHex(homeHex, true);
        }
    }

    private void changeHex(Hex newHex, boolean closeDialog) {
        if (newHex == null) return;
        world.changeActiveHex(newHex);

        if (onHexChange != null) {
            onHexChange.run();
        }
        if (mapPanel != null) {
            mapPanel.repaint();
        }
        if (closeDialog) {
            dispose();
        }
    }

    private Dynasty findPlayerDynasty() {
        if (world == null || world.getDynastys() == null) {
            return null;
        }
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isPlayer()) {
                return dynasty;
            }
        }
        return null;
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

    @Override
    public void refreshTheme() {
        super.refreshTheme();
        AssetStyles.styleButton(homeButton);
        AssetStyles.styleButton(closeButton);
        bottomPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        if (mapPanel != null) {
            mapPanel.onThemeChanged();
        }
        if (legendPanel != null) {
            legendPanel.onThemeChanged();
        }
    }

    private final JPanel bottomPanel;

    private class LegendPanel extends JPanel {
        private static final int LEGEND_EXPANDED_WIDTH = 420;
        private static final int LEGEND_COLLAPSED_WIDTH = 32;
        private static final int METRICS_COLUMN_WIDTH = 52;
        private static final int SORT_POPULATION = 0;
        private static final int SORT_DIPLOMACY = 1;
        private static final int SORT_MILITARY = 2;

        private final JPanel legendBody;
        private final JPanel content;
        private final JScrollPane legendScroll;
        private final JButton sortButton;
        private final FlatChevronButton collapseControl;
        private int metricsColumnWidth = METRICS_COLUMN_WIDTH;
        private int sortMode = SORT_POPULATION;
        private boolean expanded = true;

        public LegendPanel() {
            setLayout(new BorderLayout());
            setBackground(AssetStyles.BACKGROUND_COLOR);
            setPreferredSize(new Dimension(LEGEND_EXPANDED_WIDTH, 0));
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AssetStyles.BORDER_COLOR));

            collapseControl = new FlatChevronButton();
            collapseControl.setPointsLeft(true);
            collapseControl.setOpaque(true);
            collapseControl.setToolTipText(LanguageStrings.get(LanguageStrings.MAP_LEGEND_HIDE));
            collapseControl.addActionListener(e -> setExpanded(!expanded));
            collapseControl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    collapseControl.setBackground(AssetStyles.BACKGROUND_SECONDARY);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    collapseControl.setBackground(AssetStyles.BACKGROUND_COLOR);
                }
            });

            JPanel collapseWrap = new JPanel(new BorderLayout());
            collapseWrap.setBackground(AssetStyles.BACKGROUND_COLOR);
            collapseWrap.setPreferredSize(new Dimension(LEGEND_COLLAPSED_WIDTH, 0));
            collapseWrap.add(collapseControl, BorderLayout.NORTH);
            add(collapseWrap, BorderLayout.EAST);

            legendBody = new JPanel(new BorderLayout());
            legendBody.setBackground(AssetStyles.BACKGROUND_COLOR);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(10, 5, 5, 5));

            JLabel title = new JLabel(LanguageStrings.get(LanguageStrings.MAP_LEGEND_TITLE), SwingConstants.CENTER);
            title.setFont(AssetStyles.FONT_BOLD);
            title.setForeground(AssetStyles.FONT_COLOR_HEADER);
            header.add(title, BorderLayout.NORTH);

            sortButton = new JButton(LanguageStrings.get(LanguageStrings.MAP_SORT_BY_POPULATION));
            sortButton.setFocusable(false);
            AssetStyles.styleCompactButton(sortButton);
            sortButton.addActionListener(e -> {
                sortMode = (sortMode + 1) % 3;
                updateSortButtonLabel();
                updateLegend();
            });
            JPanel sortWrap = new JPanel(new BorderLayout());
            sortWrap.setOpaque(false);
            sortWrap.setBorder(new EmptyBorder(0, 4, 0, 4));
            sortWrap.add(sortButton, BorderLayout.CENTER);
            header.add(sortWrap, BorderLayout.SOUTH);
            legendBody.add(header, BorderLayout.NORTH);

            JPanel columnHeader = new JPanel(new BorderLayout(4, 0));
            columnHeader.setOpaque(false);
            columnHeader.setBorder(new EmptyBorder(0, 10, 4, 8));
            JLabel dynastyHeader = new JLabel(LanguageStrings.get(LanguageStrings.STAT_DYNASTY));
            dynastyHeader.setFont(AssetStyles.FONT_BOLD.deriveFont(10f));
            dynastyHeader.setForeground(AssetStyles.FONT_COLOR);

            JPanel metricsHeader = new JPanel(new GridLayout(1, 2, 6, 0));
            metricsHeader.setOpaque(false);
            JLabel popHeader = new JLabel(LanguageStrings.get(LanguageStrings.PANEL_POPULATION), SwingConstants.RIGHT);
            popHeader.setFont(AssetStyles.FONT_BOLD.deriveFont(10f));
            popHeader.setForeground(AssetStyles.FONT_COLOR);
            popHeader.setIcon(GameConstants.ICON_STAT_POPULATION);
            popHeader.setHorizontalTextPosition(SwingConstants.LEFT);
            popHeader.setIconTextGap(2);
            JLabel militaryHeader = new JLabel(LanguageStrings.get(LanguageStrings.MAP_LEGEND_MILITARY), SwingConstants.RIGHT);
            militaryHeader.setFont(AssetStyles.FONT_BOLD.deriveFont(10f));
            militaryHeader.setForeground(AssetStyles.FONT_COLOR);
            militaryHeader.setIcon(GameConstants.ICON_STAT_MILITARY_POWER);
            militaryHeader.setHorizontalTextPosition(SwingConstants.LEFT);
            militaryHeader.setIconTextGap(2);
            metricsColumnWidth = Math.max(METRICS_COLUMN_WIDTH,
                    Math.max(popHeader.getPreferredSize().width, militaryHeader.getPreferredSize().width) + 4);
            popHeader.setPreferredSize(new Dimension(metricsColumnWidth, popHeader.getPreferredSize().height));
            militaryHeader.setPreferredSize(new Dimension(metricsColumnWidth, militaryHeader.getPreferredSize().height));
            metricsHeader.add(popHeader);
            metricsHeader.add(militaryHeader);
            metricsHeader.setPreferredSize(new Dimension(metricsColumnWidth * 2 + 6, metricsHeader.getPreferredSize().height));

            columnHeader.add(dynastyHeader, BorderLayout.CENTER);
            columnHeader.add(metricsHeader, BorderLayout.EAST);

            content = new LegendScrollContent();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(AssetStyles.BACKGROUND_COLOR);
            content.setBorder(new EmptyBorder(0, 5, 5, 8));

            JPanel listPanel = new JPanel(new BorderLayout());
            listPanel.setOpaque(false);
            listPanel.add(columnHeader, BorderLayout.NORTH);

            JScrollPane scroll = new JScrollPane(content);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            UiScrollBarStyles.hide(scroll);
            legendScroll = scroll;
            attachLegendWheelScroll(scroll, this, legendBody, listPanel, content, header, columnHeader);
            listPanel.add(scroll, BorderLayout.CENTER);
            legendBody.add(listPanel, BorderLayout.CENTER);

            add(legendBody, BorderLayout.CENTER);
            updateLegend();
        }

        private void attachLegendWheelScroll(JScrollPane scroll, JComponent... targets) {
            MouseWheelListener wheelListener = this::handleLegendWheel;
            scroll.setWheelScrollingEnabled(true);
            scroll.addMouseWheelListener(wheelListener);
            scroll.getViewport().addMouseWheelListener(wheelListener);
            for (JComponent target : targets) {
                target.addMouseWheelListener(wheelListener);
            }
        }

        private void handleLegendWheel(MouseWheelEvent e) {
            if (!expanded || legendScroll == null) {
                return;
            }
            JScrollBar bar = legendScroll.getVerticalScrollBar();
            int next = bar.getValue() + e.getUnitsToScroll() * bar.getUnitIncrement();
            int max = Math.max(bar.getMinimum(), bar.getMaximum() - bar.getVisibleAmount() + 1);
            bar.setValue(Math.max(bar.getMinimum(), Math.min(max, next)));
            e.consume();
        }

        private void setExpanded(boolean expanded) {
            this.expanded = expanded;
            legendBody.setVisible(expanded);
            collapseControl.setPointsLeft(expanded);
            collapseControl.setToolTipText(LanguageStrings.get(
                    expanded ? LanguageStrings.MAP_LEGEND_HIDE : LanguageStrings.MAP_LEGEND_SHOW));
            int width = expanded ? LEGEND_EXPANDED_WIDTH : LEGEND_COLLAPSED_WIDTH;
            setPreferredSize(new Dimension(width, 0));
            setMinimumSize(new Dimension(width, 0));
            setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
            revalidate();
            Container parent = getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
            if (mapPanel != null) {
                mapPanel.revalidate();
                mapPanel.repaint();
            }
        }

        private void updateSortButtonLabel() {
            String labelKey = switch (sortMode) {
                case SORT_DIPLOMACY -> LanguageStrings.MAP_SORT_BY_DIPLOMACY;
                case SORT_MILITARY -> LanguageStrings.MAP_SORT_BY_MILITARY;
                default -> LanguageStrings.MAP_SORT_BY_POPULATION;
            };
            String label = LanguageStrings.get(labelKey);
            sortButton.setText(label);
            sortButton.setToolTipText(label);
            AssetStyles.styleCompactButton(sortButton);
            var fm = sortButton.getFontMetrics(sortButton.getFont());
            var insets = AssetStyles.BUTTON_COMPACT_MARGIN_INSETS;
            int width = insets.left + insets.right + fm.stringWidth(label) + 4;
            int height = insets.top + insets.bottom + fm.getHeight();
            sortButton.setPreferredSize(new Dimension(width, height));
            sortButton.revalidate();
        }

        private void onThemeChanged() {
            setBackground(AssetStyles.BACKGROUND_COLOR);
            legendBody.setBackground(AssetStyles.BACKGROUND_COLOR);
            content.setBackground(AssetStyles.BACKGROUND_COLOR);
            collapseControl.setBackground(AssetStyles.BACKGROUND_COLOR);
            collapseControl.setForeground(AssetStyles.FONT_COLOR);
            collapseControl.repaint();
            AssetStyles.styleCompactButton(sortButton);
            updateSortButtonLabel();
            updateLegend();
            revalidate();
            repaint();
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
            if (world.getDynastys() != null) {
                for (Dynasty d : world.getDynastys()) {
                    if (d.isDefeated()) {
                        activeDynastiesMap.putIfAbsent(d.getId(), d);
                    }
                }
            }

            Dynasty playerDynasty = findPlayerDynasty();
            List<Dynasty> sortedDynasties = new ArrayList<>(activeDynastiesMap.values());
            if (sortMode == SORT_DIPLOMACY && playerDynasty != null && playerDynasty.getDiplomacyService() != null) {
                sortedDynasties.sort(Comparator
                        .comparingInt((Dynasty d) -> d.isPlayer() ? Integer.MIN_VALUE
                                : playerDynasty.getDiplomacyService().getEffectiveDiplomaticReputation(d, world))
                        .thenComparing(Dynasty::getName, String.CASE_INSENSITIVE_ORDER));
            } else if (sortMode == SORT_MILITARY) {
                sortedDynasties.sort(Comparator
                        .comparingInt((Dynasty d) -> d.isPlayer() ? Integer.MAX_VALUE : d.getMilitaryPower())
                        .reversed()
                        .thenComparing(Dynasty::getName, String.CASE_INSENSITIVE_ORDER));
            } else {
                sortedDynasties.sort((d1, d2) -> {
                    int p1 = d1.getStatService().getTotalPopulation(d1);
                    int p2 = d2.getStatService().getTotalPopulation(d2);
                    return Integer.compare(p2, p1);
                });
            }

            for (Dynasty d : sortedDynasties) {
                JPanel item = new JPanel(new BorderLayout(4, 0));
                item.setOpaque(false);
                item.setAlignmentX(Component.LEFT_ALIGNMENT);
                item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
                item.setToolTipText(LanguageStrings.format(LanguageStrings.MAP_CLICK_VIEW_CAPITAL, d.getName()));

                item.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Colony capital = d.getCapital();
                        if (capital != null) {
                            Hex capitalHex = world.getHexOfColony(capital);
                            if (capitalHex != null) {
                                changeHex(capitalHex, true);
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

                JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
                badges.setOpaque(false);

                JPanel colorBox = new JPanel();
                colorBox.setPreferredSize(new Dimension(12, 12));
                colorBox.setBackground(d.getColor());
                colorBox.setBorder(BorderFactory.createLineBorder(AssetStyles.COLOR_ABSOLUTE_BLACK, 1));
                badges.add(colorBox);

                if (d.getSpecies() != null && d.getSpecies().getIcon() != null) {
                    badges.add(new JLabel(d.getSpecies().getIcon()));
                }

                boolean defeated = d.isDefeated();
                if (defeated && GameConstants.TYPE_DEAD.getIcon() != null) {
                    badges.add(new JLabel(GameConstants.TYPE_DEAD.getIcon()));
                }

                DiplomaticReputation stance = null;
                if (!defeated && !d.isPlayer() && playerDynasty != null && playerDynasty.getDiplomacyService() != null) {
                    int rep = playerDynasty.getDiplomacyService().getEffectiveDiplomaticReputation(d, world);
                    stance = GameConstants.getDiplomaticReputationLevel(rep);
                    if (stance.getIcon() != null) {
                        badges.add(new JLabel(stance.getIcon()));
                    }
                    item.setToolTipText(playerDynasty.getDiplomacyService().buildReputationModifierTooltip(d, world));
                }

                String nameStr = d.getName();
                if (defeated) {
                    nameStr += " (" + GameConstants.TYPE_DEAD.getName() + ")";
                    item.setToolTipText(LanguageStrings.format(LanguageStrings.MAP_CLICK_VIEW_CAPITAL, d.getName())
                            + " — " + GameConstants.TYPE_DEAD.getName());
                } else if (d.isPlayer()) {
                    nameStr += LanguageStrings.get(LanguageStrings.MAP_YOU_PLAYER);
                } else if (stance != null) {
                    nameStr = String.format(
                            LanguageStrings.get(LanguageStrings.MAP_DYNASTY_DIPLO_FORMAT),
                            nameStr,
                            stance.getName());
                }

                JLabel name = new JLabel(nameStr);
                name.setFont(d.isPlayer() ? AssetStyles.FONT_BOLD.deriveFont(10f) : AssetStyles.FONT_SMALL);
                name.setForeground(defeated ? AssetStyles.FONT_COLOR_ERROR : AssetStyles.FONT_COLOR);

                int pop = d.getStatService().getTotalPopulation(d);
                int military = d.getMilitaryPower();

                JPanel metricsPanel = new JPanel(new GridLayout(1, 2, 6, 0));
                metricsPanel.setOpaque(false);
                JLabel popLabel = new JLabel(AssetStyles.formatNumber(pop), SwingConstants.RIGHT);
                popLabel.setFont(AssetStyles.FONT_SMALL);
                popLabel.setForeground(AssetStyles.FONT_COLOR);
                popLabel.setPreferredSize(new Dimension(metricsColumnWidth, popLabel.getPreferredSize().height));
                JLabel militaryLabel = new JLabel(AssetStyles.formatNumber(military), SwingConstants.RIGHT);
                militaryLabel.setFont(AssetStyles.FONT_SMALL);
                militaryLabel.setForeground(AssetStyles.FONT_COLOR);
                militaryLabel.setPreferredSize(new Dimension(metricsColumnWidth, militaryLabel.getPreferredSize().height));
                metricsPanel.add(popLabel);
                metricsPanel.add(militaryLabel);
                metricsPanel.setPreferredSize(new Dimension(metricsColumnWidth * 2 + 6, metricsPanel.getPreferredSize().height));

                item.add(badges, BorderLayout.WEST);
                item.add(name, BorderLayout.CENTER);
                item.add(metricsPanel, BorderLayout.EAST);

                content.add(item);
                content.add(Box.createRigidArea(new Dimension(0, 2)));
            }

            content.revalidate();
            content.repaint();
        }

        /** Scrollable legend list — tracks viewport width so rows are not clipped on the right. */
        private final class LegendScrollContent extends JPanel implements Scrollable {
            LegendScrollContent() {
                setOpaque(true);
            }

            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }

            @Override
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 16;
            }

            @Override
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
                return Math.max(visibleRect.height - 16, 16);
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }

            @Override
            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
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

        void onThemeChanged() {
            biomeColorCache.clear();
            setBackground(AssetStyles.BACKGROUND_COLOR);
            repaint();
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
                        sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_MILITARY_POWER))
                                .append(AssetStyles.formatNumber(c.getMilitaryPower()));

                        Dynasty dynasty = c.getDynasty();
                        if (dynasty != null) {
                            sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_DYNASTY)).append(dynasty.getName());
                            if (dynasty.isDefeated()) {
                                sb.append(" (").append(GameConstants.TYPE_DEAD.getName()).append(")");
                            }
                            if (dynasty.getRank() != null) {
                                sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_DYNASTY_RANK)).append(dynasty.getRank().getName());
                            }
                            sb.append(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_DYNASTY_MILITARY_POWER))
                                    .append(AssetStyles.formatNumber(dynasty.getMilitaryPower()));
                            Dynasty playerDynasty = findPlayerDynasty();
                            if (!dynasty.isDefeated() && playerDynasty != null && !dynasty.isPlayer()
                                    && playerDynasty.getDiplomacyService() != null) {
                                int rep = playerDynasty.getDiplomacyService().getEffectiveDiplomaticReputation(dynasty, world);
                                DiplomaticReputation stance = GameConstants.getDiplomaticReputationLevel(rep);
                                sb.append("<br><b>")
                                        .append(LanguageStrings.get(LanguageStrings.DYNASTY_REPUTATION))
                                        .append(":</b> ")
                                        .append(stance.getName())
                                        .append(" (")
                                        .append(rep)
                                        .append(")");
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
                    changeHex(hex, true);
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
                Rectangle bounds = poly.getBounds();
                int cx = (int)bounds.getCenterX();
                int cy = (int)bounds.getCenterY();

                Biome biome = hex.getBiome();
                Color fillColor = AssetStyles.BACKGROUND_COLOR;

                if (biome != null) {
                    fillColor = getBiomeColor(biome);
                }

                if (hex != world.getActiveHex()) {
                    float fade = AssetStyles.isDarkMode() ? 0.25f : 0.4f;
                    fillColor = AssetStyles.fadeTowardBackground(fillColor, fade);
                }

                g2d.setColor(fillColor);
                g2d.fillPolygon(poly);

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
            avgColor = AssetStyles.lightenTowardBackground(avgColor, 0.5f);

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

                return AssetStyles.colorFromAveragedRgb(sumR, sumG, sumB, count);

            } catch (Exception e) {
                return AssetStyles.BACKGROUND_COLOR;
            }
        }
    }
}
