package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.shared.TriggerProgressService;
import com.grimidk.formicempire.classes.entities.services.shared.TriggerProgressService.TriggerProgress;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.OverlayLayout;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.Map;

public class ResearchTreePanel extends JPanel implements UpgradeDialog.LiveUpdatePanel {

    private static final int NODE_SIZE = AssetStyles.MIN_CONTROL_HIT_SIZE;
    private static final int NODE_PAD = 4;
    private static final int NODE_HIT = NODE_SIZE + NODE_PAD * 2;
    private static final int UNIT_SIZE = NODE_HIT;
    private static final int PADDING = 32;
    private static final float EDGE_STROKE = 2.5f;
    private static final int BORDER_STROKE = 2;
    private static final int ARROW_SIZE = 9;
    private static final int DRAG_THRESHOLD_PX = 6;
    private static final Dimension DETAIL_CARD_SIZE = new Dimension(360, 400);

    private final Colony colony;
    private final Engine engine;
    private final Runnable onTreeChanged;
    private final boolean encyclopediaMode;
    private final JLabel researchPointsLabel;
    private final TreeCanvas canvas;
    private final JScrollPane scrollPane;
    private final DetailCard detailCard;

    private ResearchTreeGraph.Result graph = ResearchTreeGraph.build(null, null);

    public ResearchTreePanel(Colony colony, Engine engine, Runnable onTreeChanged) {
        this(colony, engine, onTreeChanged, false);
    }

    public ResearchTreePanel(Colony colony, Engine engine, Runnable onTreeChanged, boolean encyclopediaMode) {
        super(new BorderLayout());
        this.colony = colony;
        this.engine = engine;
        this.onTreeChanged = onTreeChanged;
        this.encyclopediaMode = encyclopediaMode;
        setBackground(AssetStyles.BACKGROUND_COLOR);

        researchPointsLabel = new JLabel();
        researchPointsLabel.setFont(AssetStyles.FONT_BOLD);
        researchPointsLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        northPanel.add(researchPointsLabel);
        northPanel.setVisible(!encyclopediaMode);
        add(northPanel, BorderLayout.NORTH);

        canvas = new TreeCanvas();
        scrollPane = new JScrollPane(canvas);
        scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(true);

        detailCard = new DetailCard();
        detailCard.setVisible(false);
        detailCard.setAlignmentX(0.5f);
        detailCard.setAlignmentY(0.5f);
        detailCard.setMaximumSize(DETAIL_CARD_SIZE);
        detailCard.setPreferredSize(DETAIL_CARD_SIZE);

        scrollPane.setAlignmentX(0.5f);
        scrollPane.setAlignmentY(0.5f);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel stack = new JPanel();
        stack.setLayout(new OverlayLayout(stack));
        stack.setBackground(AssetStyles.BACKGROUND_COLOR);
        stack.add(detailCard);
        stack.add(scrollPane);
        add(stack, BorderLayout.CENTER);
    }

    @Override
    public void updateData() {
        updateResearchPointsLabel();
        graph = ResearchTreeGraph.build(colony, engine);
        if (detailCard.isVisible()) {
            detailCard.refreshFromSelection();
        }
        canvas.rebuildLayout();
        canvas.revalidate();
        canvas.repaint();
        SwingUtilities.invokeLater(canvas::scrollCenterIntoView);
    }

    @Override
    public void liveUpdate() {
        updateResearchPointsLabel();
        graph = ResearchTreeGraph.build(colony, engine);
        if (detailCard.isVisible()) {
            detailCard.refreshFromSelection();
        }
        canvas.repaint();
    }

    private void updateResearchPointsLabel() {
        if (encyclopediaMode || colony == null) {
            return;
        }
        researchPointsLabel.setText(LanguageStrings.format(
                LanguageStrings.UPGRADE_RESEARCH_AVAILABLE, colony.getResearchPoints()));
    }

    private void openDetail(ResearchTreeGraph.Node node) {
        detailCard.showUpgrade(node);
        detailCard.setVisible(true);
        detailCard.revalidate();
        detailCard.repaint();
    }

    private void closeDetail() {
        detailCard.hideCard();
    }

    private final class DetailCard extends JPanel {
        private Upgrade selectedUpgrade;
        private ResearchTreeGraph.NodeState selectedState;
        private final JTextArea titleArea;
        private final JLabel iconLabel;
        private final JLabel tierLabel;
        private final JTextArea costArea;
        private final JTextArea requirementArea;
        private final JTextArea progressArea;
        private final JTextArea descriptionArea;
        private final JButton buyButton;
        private final JButton cancelButton;

        DetailCard() {
            setLayout(new BorderLayout(8, 8));
            setBackground(AssetStyles.BACKGROUND_SECONDARY);
            setBorder(AssetStyles.PANEL_BORDER);
            setOpaque(true);

            titleArea = wrappingArea(AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER);
            titleArea.setBorder(new EmptyBorder(8, 10, 0, 10));
            add(titleArea, BorderLayout.NORTH);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setBorder(new EmptyBorder(4, 10, 4, 10));

            iconLabel = new JLabel();
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(iconLabel);
            center.add(Box.createRigidArea(new Dimension(0, 6)));

            tierLabel = metaLabel();
            costArea = wrappingArea(AssetStyles.FONT_NORMAL, AssetStyles.FONT_COLOR_VALUE);
            requirementArea = wrappingArea(AssetStyles.FONT_NORMAL, AssetStyles.FONT_COLOR_VALUE);
            progressArea = wrappingArea(AssetStyles.FONT_NORMAL, AssetStyles.FONT_COLOR_VALUE);
            center.add(tierLabel);
            center.add(costArea);
            center.add(requirementArea);
            center.add(progressArea);
            center.add(Box.createRigidArea(new Dimension(0, 6)));

            descriptionArea = wrappingArea(AssetStyles.FONT_NORMAL, AssetStyles.FONT_COLOR);
            descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            center.add(descriptionArea);
            add(center, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            actions.setOpaque(false);
            cancelButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CANCEL));
            AssetStyles.styleButton(cancelButton);
            cancelButton.addActionListener(e -> closeDetail());
            buyButton = new JButton(LanguageStrings.get(LanguageStrings.UI_BUY));
            AssetStyles.styleButton(buyButton);
            buyButton.addActionListener(e -> purchaseSelected());
            actions.add(cancelButton);
            actions.add(buyButton);
            add(actions, BorderLayout.SOUTH);
        }

        private JLabel metaLabel() {
            JLabel label = new JLabel(" ");
            label.setFont(AssetStyles.FONT_NORMAL);
            label.setForeground(AssetStyles.FONT_COLOR_VALUE);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            return label;
        }

        private JTextArea wrappingArea(Font font, Color color) {
            JTextArea area = new JTextArea();
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setEditable(false);
            area.setFocusable(false);
            area.setOpaque(false);
            area.setBorder(null);
            area.setFont(font);
            area.setForeground(color);
            area.setAlignmentX(Component.LEFT_ALIGNMENT);
            return area;
        }

        void showUpgrade(ResearchTreeGraph.Node node) {
            selectedUpgrade = node.getUpgrade();
            selectedState = node.getState();
            refreshFromSelection();
        }

        void hideCard() {
            selectedUpgrade = null;
            selectedState = null;
            setVisible(false);
        }

        void refreshFromSelection() {
            if (selectedUpgrade == null) {
                return;
            }
            selectedState = ResearchTreeGraph.stateFor(colony, engine, selectedUpgrade);
            Upgrade upgrade = selectedUpgrade;

            titleArea.setText(upgrade.getDisplayName());
            ImageIcon icon = upgrade.getIcon() != null ? upgrade.getIcon() : GameConstants.ICON_UNKNOWN;
            iconLabel.setIcon(icon);

            tierLabel.setIcon(upgrade.getTierIcon());
            tierLabel.setText(null);
            tierLabel.setToolTipText(upgrade.getTier().getName());

            if (upgrade.getCost() > 0) {
                costArea.setText(LanguageStrings.format(LanguageStrings.UPGRADE_COST_RP, upgrade.getCost()));
                costArea.setVisible(true);
            } else {
                costArea.setText("");
                costArea.setVisible(false);
            }

            Upgrade requirement = upgrade.getRequirement();
            if (requirement != null) {
                requirementArea.setText(LanguageStrings.format(
                        LanguageStrings.UPGRADE_REQUIRES_FMT, requirement.getDisplayName()));
                requirementArea.setVisible(true);
            } else {
                requirementArea.setText("");
                requirementArea.setVisible(false);
            }

            TriggerProgress progress = colony == null
                    ? null
                    : TriggerProgressService.find(colony, engine, upgrade);
            if (progress != null) {
                progressArea.setText(progress.getHint() + " — " + LanguageStrings.format(
                        LanguageStrings.TRIGGER_PROGRESS_METRIC_FMT,
                        progress.getMetricLabel(),
                        progress.getCurrent(),
                        progress.getRequired()));
                progressArea.setVisible(true);
            } else {
                progressArea.setText("");
                progressArea.setVisible(false);
            }

            descriptionArea.setText(upgrade.getDescription());
            descriptionArea.setCaretPosition(0);
            titleArea.setCaretPosition(0);

            cancelButton.setText(LanguageStrings.get(LanguageStrings.UI_CANCEL));
            buyButton.setText(LanguageStrings.get(LanguageStrings.UI_BUY));
            if (encyclopediaMode) {
                buyButton.setVisible(false);
                buyButton.setToolTipText(null);
                return;
            }
            boolean canBuy = selectedState == ResearchTreeGraph.NodeState.AFFORDABLE;
            buyButton.setVisible(upgrade.getCost() > 0 && selectedState != ResearchTreeGraph.NodeState.OWNED);
            buyButton.setEnabled(canBuy);
            if (colony != null
                    && upgrade.getCost() > 0
                    && selectedState != ResearchTreeGraph.NodeState.OWNED
                    && selectedState != ResearchTreeGraph.NodeState.AFFORDABLE
                    && (upgrade.getRequirement() == null || colony.hasUpgrade(upgrade.getRequirement()))) {
                buyButton.setToolTipText(LanguageStrings.get(LanguageStrings.UPGRADE_NOT_ENOUGH_RP));
            } else {
                buyButton.setToolTipText(null);
            }
        }

        private void purchaseSelected() {
            if (encyclopediaMode || colony == null || selectedUpgrade == null) {
                return;
            }
            Upgrade upgrade = selectedUpgrade;
            if (upgrade.getCost() <= 0 || colony.getResearchPoints() < upgrade.getCost()) {
                return;
            }
            if (ResearchTreeGraph.stateFor(colony, engine, upgrade) != ResearchTreeGraph.NodeState.AFFORDABLE) {
                return;
            }
            colony.setResearchPoints(colony.getResearchPoints() - upgrade.getCost());
            colony.unlockUpgrade(upgrade);
            closeDetail();
            if (onTreeChanged != null) {
                onTreeChanged.run();
            }
        }
    }

    private final class TreeCanvas extends JPanel {
        private final Map<Upgrade, Rectangle> nodeBounds = new HashMap<>();
        private final Map<Upgrade, Image> colorIcons = new HashMap<>();
        private int centerX;
        private int centerY;
        private Point pressScreen;
        private Point viewOrigin;
        private boolean dragged;

        TreeCanvas() {
            setBackground(AssetStyles.BACKGROUND_COLOR);
            setOpaque(true);
            ToolTipManager.sharedInstance().registerComponent(this);
            AssetStyles.markClickable(this);
            addMouseWheelListener(this::onMouseWheel);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    pressScreen = e.getLocationOnScreen();
                    viewOrigin = scrollPane.getViewport().getViewPosition();
                    dragged = false;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressScreen = null;
                    viewOrigin = null;
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (dragged) {
                        return;
                    }
                    ResearchTreeGraph.Node node = nodeAt(e.getPoint());
                    if (node == null) {
                        closeDetail();
                        return;
                    }
                    openDetail(node);
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (pressScreen == null || viewOrigin == null) {
                        return;
                    }
                    Point now = e.getLocationOnScreen();
                    int dx = now.x - pressScreen.x;
                    int dy = now.y - pressScreen.y;
                    if (Math.abs(dx) >= DRAG_THRESHOLD_PX || Math.abs(dy) >= DRAG_THRESHOLD_PX) {
                        dragged = true;
                    }
                    JViewport viewport = scrollPane.getViewport();
                    Dimension viewSize = getPreferredSize();
                    Dimension extent = viewport.getExtentSize();
                    int x = Math.max(0, Math.min(viewOrigin.x - dx, Math.max(0, viewSize.width - extent.width)));
                    int y = Math.max(0, Math.min(viewOrigin.y - dy, Math.max(0, viewSize.height - extent.height)));
                    viewport.setViewPosition(new Point(x, y));
                }
            });
        }

        private void onMouseWheel(MouseWheelEvent e) {
            JViewport viewport = scrollPane.getViewport();
            Point pos = viewport.getViewPosition();
            int delta = e.getUnitsToScroll() * 12;
            if (e.isShiftDown()) {
                pos.x = Math.max(0, pos.x + delta);
            } else {
                pos.y = Math.max(0, pos.y + delta);
            }
            Dimension viewSize = getPreferredSize();
            Dimension extent = viewport.getExtentSize();
            pos.x = Math.min(pos.x, Math.max(0, viewSize.width - extent.width));
            pos.y = Math.min(pos.y, Math.max(0, viewSize.height - extent.height));
            viewport.setViewPosition(pos);
            e.consume();
        }

        void rebuildLayout() {
            nodeBounds.clear();
            colorIcons.clear();

            if (graph.getNodes().isEmpty()) {
                centerX = PADDING;
                centerY = PADDING;
                setPreferredSize(new Dimension(200, 80));
                return;
            }

            double spanX = graph.getMaxX() - graph.getMinX();
            double spanY = graph.getMaxY() - graph.getMinY();
            int width = (int) Math.ceil(spanX * UNIT_SIZE) + NODE_HIT + PADDING * 2;
            int height = (int) Math.ceil(spanY * UNIT_SIZE) + NODE_HIT + PADDING * 2;
            centerX = PADDING + (int) Math.round((-graph.getMinX()) * UNIT_SIZE) + NODE_HIT / 2;
            centerY = PADDING + (int) Math.round((-graph.getMinY()) * UNIT_SIZE) + NODE_HIT / 2;
            setPreferredSize(new Dimension(Math.max(width, 200), Math.max(height, 200)));

            for (ResearchTreeGraph.Node node : graph.getNodes()) {
                int x = centerX + (int) Math.round(node.getPosX() * UNIT_SIZE) - NODE_HIT / 2;
                int y = centerY + (int) Math.round(node.getPosY() * UNIT_SIZE) - NODE_HIT / 2;
                nodeBounds.put(node.getUpgrade(), new Rectangle(x, y, NODE_HIT, NODE_HIT));
                colorIcons.put(node.getUpgrade(), iconImage(node.getUpgrade()));
            }
        }

        void scrollCenterIntoView() {
            JViewport viewport = scrollPane.getViewport();
            Dimension viewSize = viewport.getExtentSize();
            int x = Math.max(0, centerX - viewSize.width / 2);
            int y = Math.max(0, centerY - viewSize.height / 2);
            viewport.setViewPosition(new Point(x, y));
        }

        private Image iconImage(Upgrade upgrade) {
            ImageIcon icon = upgrade.getIcon();
            if (icon == null || icon.getImage() == null) {
                icon = GameConstants.ICON_UNKNOWN;
            }
            Image image = icon.getImage();
            if (image.getWidth(null) != NODE_SIZE || image.getHeight(null) != NODE_SIZE) {
                return image.getScaledInstance(NODE_SIZE, NODE_SIZE, Image.SCALE_SMOOTH);
            }
            return image;
        }

        private ResearchTreeGraph.Node nodeAt(Point point) {
            for (ResearchTreeGraph.Node node : graph.getNodes()) {
                Rectangle bounds = nodeBounds.get(node.getUpgrade());
                if (bounds != null && bounds.contains(point)) {
                    return node;
                }
            }
            return null;
        }

        private Color borderColor(ResearchTreeGraph.NodeState state) {
            return switch (state) {
                case OWNED -> AssetStyles.COLOR_MEDIUM_GREEN;
                case AFFORDABLE -> AssetStyles.COLOR_MEDIUM_RED;
                case TRIGGER_PROGRESS -> AssetStyles.COLOR_MEDIUM_BLUE;
                case SPECIAL_PROGRESS -> AssetStyles.COLOR_LIGHT_YELLOW;
                case UNAVAILABLE -> AssetStyles.COLOR_MEDIUM_GRAY;
            };
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            ResearchTreeGraph.Node node = nodeAt(event.getPoint());
            if (node == null) {
                return null;
            }
            return node.getUpgrade().getDisplayName()
                    + " (" + LanguageStrings.get(LanguageStrings.UPGRADE_CLICK_FOR_DETAILS) + ")";
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            if (graph.getNodes().isEmpty()) {
                g2d.setColor(AssetStyles.FONT_COLOR);
                g2d.setFont(AssetStyles.FONT_NORMAL);
                g2d.drawString(LanguageStrings.get(LanguageStrings.UPGRADE_NO_RESEARCH), PADDING, PADDING + 16);
                g2d.dispose();
                return;
            }

            g2d.setColor(AssetStyles.BORDER_COLOR);
            g2d.setStroke(new BasicStroke(EDGE_STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (ResearchTreeGraph.Edge edge : graph.getEdges()) {
                Rectangle from = nodeBounds.get(edge.getFrom());
                Rectangle to = nodeBounds.get(edge.getTo());
                if (from == null || to == null) {
                    continue;
                }
                drawArrow(
                        g2d,
                        from.x + from.width / 2,
                        from.y + from.height / 2,
                        to.x + to.width / 2,
                        to.y + to.height / 2);
            }

            for (ResearchTreeGraph.Node node : graph.getNodes()) {
                Rectangle rect = nodeBounds.get(node.getUpgrade());
                if (rect == null) {
                    continue;
                }
                int iconX = rect.x + NODE_PAD;
                int iconY = rect.y + NODE_PAD;
                g2d.setColor(AssetStyles.BACKGROUND_COLOR);
                g2d.fillRect(rect.x, rect.y, rect.width, rect.height);

                Image image = colorIcons.get(node.getUpgrade());
                if (image != null) {
                    g2d.drawImage(image, iconX, iconY, NODE_SIZE, NODE_SIZE, this);
                }
                g2d.setStroke(new BasicStroke(BORDER_STROKE));
                g2d.setColor(borderColor(node.getState()));
                g2d.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
            }
            g2d.dispose();
        }

        private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
            double dx = x2 - x1;
            double dy = y2 - y1;
            double len = Math.hypot(dx, dy);
            if (len < 1) {
                return;
            }
            double ux = dx / len;
            double uy = dy / len;
            double inset = NODE_HIT / 2.0;
            if (len <= inset * 2 + ARROW_SIZE) {
                return;
            }
            int sx = (int) Math.round(x1 + ux * inset);
            int sy = (int) Math.round(y1 + uy * inset);
            int ex = (int) Math.round(x2 - ux * inset);
            int ey = (int) Math.round(y2 - uy * inset);
            g2d.drawLine(sx, sy, ex, ey);

            double angle = Math.atan2(uy, ux);
            int ax1 = (int) Math.round(ex - ARROW_SIZE * Math.cos(angle - Math.PI / 6));
            int ay1 = (int) Math.round(ey - ARROW_SIZE * Math.sin(angle - Math.PI / 6));
            int ax2 = (int) Math.round(ex - ARROW_SIZE * Math.cos(angle + Math.PI / 6));
            int ay2 = (int) Math.round(ey - ARROW_SIZE * Math.sin(angle + Math.PI / 6));
            Polygon head = new Polygon();
            head.addPoint(ex, ey);
            head.addPoint(ax1, ay1);
            head.addPoint(ax2, ay2);
            g2d.fillPolygon(head);
        }
    }
}
