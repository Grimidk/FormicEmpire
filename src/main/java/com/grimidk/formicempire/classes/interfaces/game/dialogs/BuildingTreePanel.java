package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingTreePanel extends JPanel implements UpgradeDialog.LiveUpdatePanel {

    private static final int NODE_SIZE = AssetStyles.MIN_CONTROL_HIT_SIZE;
    private static final int NODE_PAD = 4;
    private static final int NODE_HIT = NODE_SIZE + NODE_PAD * 2;
    private static final int UNIT_SIZE = NODE_HIT;
    private static final int PADDING = 32;
    private static final float EDGE_STROKE = 2.5f;
    private static final float DIVIDER_STROKE = 1.5f;
    private static final int BORDER_STROKE = 2;
    private static final int ARROW_SIZE = 9;
    private static final int DRAG_THRESHOLD_PX = 6;
    private static final Dimension DETAIL_CARD_SIZE = new Dimension(360, 400);

    private final Colony colony;
    private final Runnable onTreeChanged;
    private final boolean encyclopediaMode;
    private final JLabel mineralsLabel;
    private final JLabel resinLabel;
    private final JLabel buildersLabel;
    private final JLabel cranesLabel;
    private final TreeCanvas canvas;
    private final JScrollPane scrollPane;
    private final DetailCard detailCard;

    private BuildingTreeGraph.Result graph = BuildingTreeGraph.build(null);

    public BuildingTreePanel(Colony colony, Runnable onTreeChanged) {
        this(colony, onTreeChanged, false);
    }

    public BuildingTreePanel(Colony colony, Runnable onTreeChanged, boolean encyclopediaMode) {
        super(new BorderLayout());
        this.colony = colony;
        this.onTreeChanged = onTreeChanged;
        this.encyclopediaMode = encyclopediaMode;
        setBackground(AssetStyles.BACKGROUND_COLOR);

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mineralsLabel = statusLabel(GameConstants.RESOURCE_ROCK.getIcon());
        resinLabel = statusLabel(GameConstants.RESOURCE_RESIN.getIcon());
        buildersLabel = statusLabel(GameConstants.ROLE_BUILDER.getIcon());
        cranesLabel = statusLabel(GameConstants.ROLE_CRANE.getIcon());
        northPanel.add(mineralsLabel);
        northPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        northPanel.add(resinLabel);
        northPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        northPanel.add(buildersLabel);
        northPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        northPanel.add(cranesLabel);
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

    private JLabel statusLabel(Icon icon) {
        JLabel label = new JLabel(icon);
        label.setFont(AssetStyles.FONT_NORMAL);
        label.setForeground(AssetStyles.FONT_COLOR);
        return label;
    }

    @Override
    public void updateData() {
        updateResourceLabels();
        graph = BuildingTreeGraph.build(colony);
        if (detailCard.isVisible()) {
            detailCard.refreshFromSelection();
        }
        canvas.rebuildLayout();
        canvas.revalidate();
        canvas.repaint();
        SwingUtilities.invokeLater(canvas::scrollBasicsIntoView);
    }

    @Override
    public void liveUpdate() {
    }

    private void updateResourceLabels() {
        if (encyclopediaMode || colony == null) {
            return;
        }
        mineralsLabel.setText(AssetStyles.formatRatio(colony.getMinerals(), colony.getMineralsCapacity()));
        resinLabel.setText(AssetStyles.formatRatio(colony.getResins(), colony.getResinsCapacity()));
        buildersLabel.setText(LanguageStrings.format(
                LanguageStrings.BUILD_STATUS_BUILDERS,
                colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER)));
        if (colony.hasUpgrade(GameUnlocks.ROLE_CRANE)) {
            cranesLabel.setVisible(true);
            cranesLabel.setText(LanguageStrings.format(
                    LanguageStrings.BUILD_STATUS_CRANES,
                    colony.getAssignedRoleCount(GameConstants.ROLE_CRANE)));
        } else {
            cranesLabel.setVisible(false);
        }
    }

    private void openDetail(BuildingTreeGraph.Node node) {
        detailCard.showBuilding(node);
        detailCard.setVisible(true);
        detailCard.revalidate();
        detailCard.repaint();
    }

    private void closeDetail() {
        detailCard.hideCard();
    }

    private final class DetailCard extends JPanel {
        private Building selectedBuilding;
        private BuildingTreeGraph.NodeState selectedState;
        private final JTextArea titleArea;
        private final JLabel iconLabel;
        private final JLabel tierLabel;
        private final JTextArea costArea;
        private final JTextArea requirementArea;
        private final JTextArea progressArea;
        private final JTextArea descriptionArea;
        private final JButton actionButton;
        private final JButton closeButton;

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
            center.add(descriptionArea);
            add(center, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            actions.setOpaque(false);
            closeButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CANCEL));
            AssetStyles.styleButton(closeButton);
            closeButton.addActionListener(e -> closeDetail());
            actionButton = new JButton(LanguageStrings.get(LanguageStrings.UI_BUILD));
            AssetStyles.styleButton(actionButton);
            actionButton.addActionListener(e -> onAction());
            actions.add(closeButton);
            actions.add(actionButton);
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

        void showBuilding(BuildingTreeGraph.Node node) {
            selectedBuilding = node.getBuilding();
            selectedState = node.getState();
            refreshFromSelection();
        }

        void hideCard() {
            selectedBuilding = null;
            selectedState = null;
            setVisible(false);
        }

        void refreshFromSelection() {
            if (selectedBuilding == null) {
                return;
            }
            selectedState = BuildingTreeGraph.stateFor(colony, selectedBuilding);
            Building building = selectedBuilding;

            setTextIfChanged(titleArea, building.getDisplayName());
            ImageIcon icon = building.getIcon() != null ? building.getIcon() : GameConstants.ICON_UNKNOWN;
            if (iconLabel.getIcon() != icon) {
                iconLabel.setIcon(icon);
            }

            if (tierLabel.getIcon() != building.getTierIcon()) {
                tierLabel.setIcon(building.getTierIcon());
            }
            tierLabel.setText(null);
            tierLabel.setToolTipText(building.getTier().getName());

            setTextIfChanged(costArea,
                    GameConstants.RESOURCE_ROCK.getName() + ": "
                            + AssetStyles.formatNumber(building.getMineralCost()) + "\n"
                            + GameConstants.RESOURCE_RESIN.getName() + ": "
                            + AssetStyles.formatNumber(building.getResinCost()) + "\n"
                            + LanguageStrings.get(LanguageStrings.HELP_BUILD_BASE_COST) + ": "
                            + AssetStyles.formatNumber(building.getBuildTime()));
            setVisibleIfChanged(costArea, true);

            Building requirement = building.getRequirement();
            if (requirement != null) {
                setTextIfChanged(requirementArea, LanguageStrings.format(
                        LanguageStrings.UPGRADE_REQUIRES_FMT, requirement.getDisplayName()));
                setVisibleIfChanged(requirementArea, true);
            } else {
                setTextIfChanged(requirementArea, "");
                setVisibleIfChanged(requirementArea, false);
            }

            if (colony != null
                    && (selectedState == BuildingTreeGraph.NodeState.IN_PROGRESS
                    || colony.getCurrentBuildingProject() == building)) {
                double efficiency = colony.getConstructionEfficiency();
                double requiredHours = efficiency > 0
                        ? building.getBuildTime() / efficiency
                        : Double.POSITIVE_INFINITY;
                double progressHours = colony.getBuildingProgressHours();
                setTextIfChanged(progressArea, LanguageStrings.format(
                        LanguageStrings.BUILD_PROGRESS_HOURS, progressHours, requiredHours));
                setVisibleIfChanged(progressArea, true);
            } else {
                setTextIfChanged(progressArea, "");
                setVisibleIfChanged(progressArea, false);
            }

            setTextIfChanged(descriptionArea, building.getDescription());

            String closeText = LanguageStrings.get(LanguageStrings.UI_CANCEL);
            if (!closeText.equals(closeButton.getText())) {
                closeButton.setText(closeText);
            }
            if (encyclopediaMode) {
                setVisibleIfChanged(actionButton, false);
                actionButton.setToolTipText(null);
                return;
            }
            if (selectedState == BuildingTreeGraph.NodeState.OWNED) {
                setVisibleIfChanged(actionButton, false);
            } else if (colony != null
                    && (selectedState == BuildingTreeGraph.NodeState.IN_PROGRESS
                    || colony.getCurrentBuildingProject() == building)) {
                setVisibleIfChanged(actionButton, true);
                if (!actionButton.isEnabled()) {
                    actionButton.setEnabled(true);
                }
                String cancelText = LanguageStrings.get(LanguageStrings.UI_CANCEL);
                if (!cancelText.equals(actionButton.getText())) {
                    actionButton.setText(cancelText);
                }
                actionButton.setToolTipText(null);
            } else {
                setVisibleIfChanged(actionButton, true);
                String buildText = LanguageStrings.get(LanguageStrings.UI_BUILD);
                if (!buildText.equals(actionButton.getText())) {
                    actionButton.setText(buildText);
                }
                boolean canBuild = selectedState == BuildingTreeGraph.NodeState.AFFORDABLE;
                if (actionButton.isEnabled() != canBuild) {
                    actionButton.setEnabled(canBuild);
                }
                String tip = null;
                if (!canBuild && colony != null) {
                    int builders = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
                    int cranes = colony.getAssignedRoleCount(GameConstants.ROLE_CRANE);
                    if (builders <= 0 && cranes <= 0) {
                        tip = LanguageStrings.get(LanguageStrings.BUILD_REQUIREMENT_ERROR);
                    } else if (colony.getMinerals() < building.getMineralCost()
                            || colony.getResins() < building.getResinCost()) {
                        tip = LanguageStrings.get(LanguageStrings.BUILD_RESOURCES_ERROR);
                    }
                }
                if (tip == null ? actionButton.getToolTipText() != null
                        : !tip.equals(actionButton.getToolTipText())) {
                    actionButton.setToolTipText(tip);
                }
            }
        }

        private void setTextIfChanged(JTextArea area, String text) {
            String next = text != null ? text : "";
            if (!next.equals(area.getText())) {
                area.setText(next);
                area.setCaretPosition(0);
            }
        }

        private void setVisibleIfChanged(Component component, boolean visible) {
            if (component.isVisible() != visible) {
                component.setVisible(visible);
            }
        }

        private void onAction() {
            if (encyclopediaMode || colony == null || selectedBuilding == null) {
                return;
            }
            Building building = selectedBuilding;
            if (colony.getCurrentBuildingProject() == building) {
                colony.setMinerals(colony.getMinerals() + building.getMineralCost());
                colony.setResins(colony.getResins() + building.getResinCost());
                colony.setCurrentBuildingProject(null);
                colony.setBuildingProgressHours(0.0);
                closeDetail();
                if (onTreeChanged != null) {
                    onTreeChanged.run();
                }
                return;
            }
            if (BuildingTreeGraph.stateFor(colony, building) != BuildingTreeGraph.NodeState.AFFORDABLE) {
                return;
            }
            if (colony.startBuildingProject(building)) {
                closeDetail();
                if (onTreeChanged != null) {
                    onTreeChanged.run();
                }
            }
        }
    }

    private final class TreeCanvas extends JPanel {
        private final Map<Building, Rectangle> nodeBounds = new HashMap<>();
        private final Map<Building, Image> colorIcons = new HashMap<>();
        private final List<DividerLabel> dividerLabels = new ArrayList<>();
        private int originX;
        private int originY;
        private Point pressScreen;
        private Point viewOrigin;
        private boolean dragged;

        private final class DividerLabel {
            private final Rectangle bounds;
            private final String tip;

            DividerLabel(Rectangle bounds, String tip) {
                this.bounds = bounds;
                this.tip = tip;
            }
        }

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
                    BuildingTreeGraph.Node node = nodeAt(e.getPoint());
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
            dividerLabels.clear();

            if (graph.getNodes().isEmpty()) {
                originX = PADDING;
                originY = PADDING;
                setPreferredSize(new Dimension(200, 80));
                return;
            }

            double spanX = graph.getMaxX() - graph.getMinX();
            double spanY = graph.getMaxY() - graph.getMinY();
            int width = (int) Math.ceil(spanX * UNIT_SIZE) + NODE_HIT + PADDING * 2;
            int height = (int) Math.ceil(spanY * UNIT_SIZE) + NODE_HIT + PADDING * 2;
            originX = PADDING + (int) Math.round((-graph.getMinX()) * UNIT_SIZE) + NODE_HIT / 2;
            originY = PADDING + (int) Math.round((-graph.getMinY()) * UNIT_SIZE) + NODE_HIT / 2;
            setPreferredSize(new Dimension(Math.max(width, 200), Math.max(height, 200)));

            for (BuildingTreeGraph.Node node : graph.getNodes()) {
                int x = originX + (int) Math.round(node.getPosX() * UNIT_SIZE) - NODE_HIT / 2;
                int y = originY + (int) Math.round(node.getPosY() * UNIT_SIZE) - NODE_HIT / 2;
                nodeBounds.put(node.getBuilding(), new Rectangle(x, y, NODE_HIT, NODE_HIT));
                colorIcons.put(node.getBuilding(), iconImage(node.getBuilding()));
            }

            int lineLeft = PADDING / 2;
            for (BuildingTreeGraph.TierDivider divider : graph.getTierDividers()) {
                if (divider.getUpperTier() < 0 || divider.getUpperTier() >= GameConstants.getTiers().size()) {
                    continue;
                }
                int y = originY + (int) Math.round(divider.getPosY() * UNIT_SIZE);
                dividerLabels.add(new DividerLabel(
                        new Rectangle(lineLeft + 4, y - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE),
                        GameConstants.getTiers().get(divider.getUpperTier()).getName()));
            }
        }

        void scrollBasicsIntoView() {
            JViewport viewport = scrollPane.getViewport();
            Dimension extent = viewport.getExtentSize();
            int basicsY = originY + (int) Math.round(0 * UNIT_SIZE);
            int x = Math.max(0, originX - extent.width / 2);
            int y = Math.max(0, basicsY - extent.height * 3 / 4);
            viewport.setViewPosition(new Point(x, y));
        }

        private Image iconImage(Building building) {
            ImageIcon icon = building.getIcon();
            if (icon == null || icon.getImage() == null) {
                icon = GameConstants.ICON_UNKNOWN;
            }
            Image image = icon.getImage();
            if (image.getWidth(null) != NODE_SIZE || image.getHeight(null) != NODE_SIZE) {
                return image.getScaledInstance(NODE_SIZE, NODE_SIZE, Image.SCALE_SMOOTH);
            }
            return image;
        }

        private BuildingTreeGraph.Node nodeAt(Point point) {
            for (BuildingTreeGraph.Node node : graph.getNodes()) {
                Rectangle bounds = nodeBounds.get(node.getBuilding());
                if (bounds != null && bounds.contains(point)) {
                    return node;
                }
            }
            return null;
        }

        private Color borderColor(BuildingTreeGraph.NodeState state) {
            return switch (state) {
                case OWNED -> AssetStyles.COLOR_MEDIUM_GREEN;
                case AFFORDABLE -> AssetStyles.COLOR_MEDIUM_RED;
                case IN_PROGRESS -> AssetStyles.COLOR_MEDIUM_BLUE;
                case UNAVAILABLE -> AssetStyles.COLOR_MEDIUM_GRAY;
            };
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            BuildingTreeGraph.Node node = nodeAt(event.getPoint());
            if (node != null) {
                return node.getBuilding().getDisplayName()
                        + " (" + LanguageStrings.get(LanguageStrings.UPGRADE_CLICK_FOR_DETAILS) + ")";
            }
            for (DividerLabel label : dividerLabels) {
                if (label.bounds.contains(event.getPoint())) {
                    return label.tip;
                }
            }
            return null;
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
                g2d.drawString(LanguageStrings.get(LanguageStrings.BUILD_NO_CONSTRUCTIONS), PADDING, PADDING + 16);
                g2d.dispose();
                return;
            }

            g2d.setColor(AssetStyles.COLOR_MEDIUM_GRAY);
            g2d.setStroke(new BasicStroke(
                    DIVIDER_STROKE,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10f,
                    new float[] {6f, 6f},
                    0f));
            int lineLeft = PADDING / 2;
            int lineRight = getPreferredSize().width - PADDING / 2;
            for (BuildingTreeGraph.TierDivider divider : graph.getTierDividers()) {
                int y = originY + (int) Math.round(divider.getPosY() * UNIT_SIZE);
                g2d.drawLine(lineLeft, y, lineRight, y);
                if (divider.getUpperTier() >= 0 && divider.getUpperTier() < GameConstants.getTiers().size()) {
                    ImageIcon icon = GameConstants.getTiers().get(divider.getUpperTier()).getIcon();
                    if (icon != null && icon.getImage() != null) {
                        g2d.drawImage(
                                icon.getImage(),
                                lineLeft + 4,
                                y - NODE_SIZE / 2,
                                NODE_SIZE,
                                NODE_SIZE,
                                this);
                    }
                }
            }

            g2d.setColor(AssetStyles.BORDER_COLOR);
            g2d.setStroke(new BasicStroke(EDGE_STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (BuildingTreeGraph.Edge edge : graph.getEdges()) {
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

            for (BuildingTreeGraph.Node node : graph.getNodes()) {
                Rectangle rect = nodeBounds.get(node.getBuilding());
                if (rect == null) {
                    continue;
                }
                int iconX = rect.x + NODE_PAD;
                int iconY = rect.y + NODE_PAD;
                g2d.setColor(AssetStyles.BACKGROUND_COLOR);
                g2d.fillRect(rect.x, rect.y, rect.width, rect.height);

                Image image = colorIcons.get(node.getBuilding());
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
