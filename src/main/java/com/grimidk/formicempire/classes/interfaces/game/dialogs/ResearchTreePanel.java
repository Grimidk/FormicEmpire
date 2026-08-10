package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Synergy;
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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.OverlayLayout;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResearchTreePanel extends JPanel implements UpgradeDialog.LiveUpdatePanel {

    private static final int NODE_SIZE = AssetStyles.MIN_CONTROL_HIT_SIZE;
    private static final int NODE_PAD = 4;
    private static final int NODE_HIT = NODE_SIZE + NODE_PAD * 2;
    private static final int UNIT_SIZE = NODE_HIT;
    private static final int PADDING = 32;
    private static final float EDGE_STROKE = 2.5f;
    private static final int BORDER_STROKE = 2;
    private static final int ARROW_SIZE = 9;
    private static final int DRAG_THRESHOLD_PX = 8;
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
    private boolean didInitialScroll;

    public ResearchTreePanel(Colony colony, Engine engine, Runnable onTreeChanged) {
        this(colony, engine, onTreeChanged, false);
    }

    public ResearchTreePanel(Colony colony, Engine engine, Runnable onTreeChanged, boolean encyclopediaMode) {
        super(new BorderLayout());
        this.colony = colony;
        this.engine = engine;
        this.onTreeChanged = onTreeChanged;
        this.encyclopediaMode = encyclopediaMode;
        this.didInitialScroll = false;
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
        graph = ResearchTreeGraph.build(colony, engine, encyclopediaMode);
        if (detailCard.isVisible()) {
            detailCard.refreshFromSelection();
        }
        canvas.rebuildLayout();
        canvas.revalidate();
        canvas.repaint();
        if (!didInitialScroll) {
            didInitialScroll = true;
            SwingUtilities.invokeLater(canvas::scrollCenterIntoView);
        }
    }

    @Override
    public void liveUpdate() {
        updateResearchPointsLabel();
        if (detailCard.isVisible()) {
            detailCard.refreshFromSelection();
        }
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

    public boolean closeDetailIfOpen() {
        if (!detailCard.isVisible()) {
            return false;
        }
        closeDetail();
        return true;
    }

    private final class DetailCard extends JPanel {
        private Upgrade selectedUpgrade;
        private ResearchTreeGraph.NodeState selectedState;
        private final JLabel iconLabel;
        private final JLabel nameLabel;
        private final JTextPane flavorPane;
        private final JTextPane descriptionPane;
        private final JLabel tierLabel;
        private final JLabel requirementLabel;
        private final JTextPane progressHintPane;
        private final JLabel progressMetricLabel;
        private final JPanel progressCopyPanel;
        private final JProgressBar costBar;
        private final JButton buyButton;
        private final JButton cancelButton;

        DetailCard() {
            setLayout(new BorderLayout(8, 8));
            setBackground(AssetStyles.BACKGROUND_SECONDARY);
            setBorder(AssetStyles.PANEL_BORDER);
            setOpaque(true);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setBorder(new EmptyBorder(8, 10, 4, 10));

            JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            nameRow.setOpaque(false);
            nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            iconLabel = new JLabel();
            nameLabel = new JLabel(" ");
            nameLabel.setFont(AssetStyles.FONT_BOLD);
            nameLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            nameRow.add(iconLabel);
            nameRow.add(nameLabel);
            center.add(nameRow);
            center.add(Box.createRigidArea(new Dimension(0, 4)));

            flavorPane = centeredPane(AssetStyles.FONT_NORMAL, AssetStyles.FONT_COLOR_VALUE);
            center.add(flavorPane);
            center.add(Box.createRigidArea(new Dimension(0, 6)));

            descriptionPane = centeredPane(AssetStyles.FONT_NORMAL, AssetStyles.FONT_COLOR);
            center.add(descriptionPane);
            center.add(Box.createRigidArea(new Dimension(0, 10)));

            JPanel meta = new JPanel();
            meta.setOpaque(false);
            meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
            meta.setAlignmentX(Component.LEFT_ALIGNMENT);
            meta.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            tierLabel = metaLabel();
            requirementLabel = metaLabel();
            progressHintPane = leftPane(AssetStyles.FONT_NORMAL, AssetStyles.FONT_COLOR);
            progressMetricLabel = metaLabel();
            progressCopyPanel = new JPanel();
            progressCopyPanel.setOpaque(false);
            progressCopyPanel.setLayout(new BoxLayout(progressCopyPanel, BoxLayout.Y_AXIS));
            progressCopyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            progressCopyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            progressCopyPanel.add(progressHintPane);
            progressCopyPanel.add(Box.createRigidArea(new Dimension(0, 2)));
            progressCopyPanel.add(progressMetricLabel);
            progressCopyPanel.setVisible(false);
            costBar = new JProgressBar(0, 100);
            AssetStyles.styleProgressBar(costBar);
            costBar.setStringPainted(true);
            costBar.setAlignmentX(Component.LEFT_ALIGNMENT);
            costBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, AssetStyles.MIN_CONTROL_HIT_SIZE * 2));
            costBar.setPreferredSize(new Dimension(10, AssetStyles.MIN_CONTROL_HIT_SIZE));
            meta.add(tierLabel);
            meta.add(Box.createRigidArea(new Dimension(0, 4)));
            meta.add(requirementLabel);
            meta.add(Box.createRigidArea(new Dimension(0, 4)));
            meta.add(progressCopyPanel);
            meta.add(Box.createRigidArea(new Dimension(0, 4)));
            meta.add(costBar);
            center.add(meta);
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
            label.setHorizontalAlignment(SwingConstants.LEFT);
            return label;
        }

        private JTextPane centeredPane(java.awt.Font font, Color color) {
            JTextPane pane = new JTextPane();
            pane.setEditable(false);
            pane.setFocusable(false);
            pane.setOpaque(false);
            pane.setBorder(null);
            pane.setFont(font);
            pane.setForeground(color);
            pane.setAlignmentX(Component.LEFT_ALIGNMENT);
            pane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            return pane;
        }

        private JTextPane leftPane(java.awt.Font font, Color color) {
            return centeredPane(font, color);
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

            String name = upgrade.getTitleName();
            if (!name.equals(nameLabel.getText())) {
                nameLabel.setText(name);
            }
            ImageIcon icon = upgrade.getIcon() != null ? upgrade.getIcon() : GameConstants.ICON_UNKNOWN;
            if (iconLabel.getIcon() != icon) {
                iconLabel.setIcon(icon);
            }

            String flavor = upgrade.getFlavorName();
            setCenteredText(flavorPane, flavor);
            setVisibleIfChanged(flavorPane, !flavor.isBlank() && !flavor.equals(name));
            setCenteredText(descriptionPane, upgrade.getDescription());

            if (tierLabel.getIcon() != upgrade.getTierIcon()) {
                tierLabel.setIcon(upgrade.getTierIcon());
            }
            String tierName = upgrade.getTier().getName();
            if (!tierName.equals(tierLabel.getText())) {
                tierLabel.setText(tierName);
            }
            String tierTip = ResearchTreeGraph.formatTierRequirement(upgrade.getTier());
            if (tierTip == null ? tierLabel.getToolTipText() != null : !tierTip.equals(tierLabel.getToolTipText())) {
                tierLabel.setToolTipText(tierTip);
            }

            Upgrade requirement = upgrade.getRequirement();
            Assimilation assimilation = ResearchTreeGraph.assimilationForReward(upgrade);
            Synergy synergy = ResearchTreeGraph.synergyForReward(upgrade);
            String reqDetail = null;
            if (assimilation != null) {
                reqDetail = LanguageStrings.format(
                        LanguageStrings.UPGRADE_REQUIRES_ASSIMILATION_FMT, assimilation.getName());
            } else if (synergy != null) {
                reqDetail = LanguageStrings.format(
                        LanguageStrings.UPGRADE_REQUIRES_SYNERGY_FMT, synergy.getName());
            } else if (requirement != null) {
                reqDetail = requirement.getDisplayName();
            }
            if (reqDetail != null) {
                String reqText = LanguageStrings.format(LanguageStrings.UPGRADE_REQUIRES_FMT, reqDetail);
                if (!reqText.equals(requirementLabel.getText())) {
                    requirementLabel.setText(reqText);
                }
                setVisibleIfChanged(requirementLabel, true);
            } else {
                if (!requirementLabel.getText().isEmpty()) {
                    requirementLabel.setText("");
                }
                setVisibleIfChanged(requirementLabel, false);
            }

            updateCostBar(upgrade);

            String cancelText = LanguageStrings.get(LanguageStrings.UI_CANCEL);
            if (!cancelText.equals(cancelButton.getText())) {
                cancelButton.setText(cancelText);
            }
            String buyText = LanguageStrings.get(LanguageStrings.UI_BUY);
            if (!buyText.equals(buyButton.getText())) {
                buyButton.setText(buyText);
            }
            if (encyclopediaMode) {
                setVisibleIfChanged(buyButton, false);
                buyButton.setToolTipText(null);
                return;
            }
            boolean canBuy = selectedState == ResearchTreeGraph.NodeState.AFFORDABLE;
            boolean showBuy = selectedState != ResearchTreeGraph.NodeState.OWNED;
            setVisibleIfChanged(buyButton, showBuy);
            if (buyButton.isEnabled() != canBuy) {
                buyButton.setEnabled(canBuy);
            }
            String tip = showBuy && !canBuy
                    ? ResearchTreeGraph.buyButtonTooltip(colony, engine, upgrade)
                    : null;
            if (tip == null ? buyButton.getToolTipText() != null : !tip.equals(buyButton.getToolTipText())) {
                buyButton.setToolTipText(tip);
            }
        }

        private void updateCostBar(Upgrade upgrade) {
            if (selectedState == ResearchTreeGraph.NodeState.OWNED) {
                clearTriggerProgressCopy();
                costBar.setValue(0);
                costBar.setString("");
                setVisibleIfChanged(costBar, false);
                return;
            }

            TriggerProgress progress = colony == null
                    ? null
                    : TriggerProgressService.find(colony, engine, upgrade);
            boolean showTrigger = progress != null
                    && progress.getRequired() > 0
                    && (selectedState == ResearchTreeGraph.NodeState.TRIGGER_PROGRESS
                    || selectedState == ResearchTreeGraph.NodeState.SPECIAL_PROGRESS
                    || upgrade.getCost() <= 0);
            if (showTrigger) {
                int current = Math.max(0, progress.getCurrent());
                int required = Math.max(1, progress.getRequired());
                costBar.setMaximum(required);
                costBar.setValue(Math.min(current, required));
                String barText = AssetStyles.formatNumber(current) + " / " + AssetStyles.formatNumber(required);
                if (!barText.equals(costBar.getString())) {
                    costBar.setString(barText);
                }
                setAlignedText(progressHintPane, progress.getHint(), false);
                String metric = progress.getMetricLabel();
                if (!metric.equals(progressMetricLabel.getText())) {
                    progressMetricLabel.setText(metric);
                }
                setVisibleIfChanged(progressCopyPanel, true);
                setVisibleIfChanged(costBar, true);
                return;
            }

            clearTriggerProgressCopy();

            if (upgrade.getCost() > 0) {
                long owned = colony != null ? colony.getResearchPoints() : 0L;
                int cost = upgrade.getCost();
                costBar.setMaximum(cost);
                costBar.setValue((int) Math.min(Math.max(0L, owned), cost));
                String barText = LanguageStrings.format(
                        LanguageStrings.UPGRADE_COST_RP,
                        AssetStyles.formatNumber(owned) + " / " + AssetStyles.formatNumber(cost));
                if (!barText.equals(costBar.getString())) {
                    costBar.setString(barText);
                }
                setVisibleIfChanged(costBar, true);
                return;
            }

            if (progress != null && progress.getRequired() > 0) {
                int current = Math.max(0, progress.getCurrent());
                int required = Math.max(1, progress.getRequired());
                costBar.setMaximum(required);
                costBar.setValue(Math.min(current, required));
                String barText = AssetStyles.formatNumber(current) + " / " + AssetStyles.formatNumber(required);
                if (!barText.equals(costBar.getString())) {
                    costBar.setString(barText);
                }
                setAlignedText(progressHintPane, progress.getHint(), false);
                String metric = progress.getMetricLabel();
                if (!metric.equals(progressMetricLabel.getText())) {
                    progressMetricLabel.setText(metric);
                }
                setVisibleIfChanged(progressCopyPanel, true);
                setVisibleIfChanged(costBar, true);
                return;
            }

            costBar.setValue(0);
            costBar.setString("");
            setVisibleIfChanged(costBar, false);
        }

        private void clearTriggerProgressCopy() {
            setAlignedText(progressHintPane, "", false);
            if (!progressMetricLabel.getText().isEmpty()) {
                progressMetricLabel.setText("");
            }
            setVisibleIfChanged(progressCopyPanel, false);
        }

        private void setCenteredText(JTextPane pane, String text) {
            setAlignedText(pane, text, true);
        }

        private void setAlignedText(JTextPane pane, String text, boolean center) {
            String next = text != null ? text : "";
            if (next.equals(pane.getText())) {
                return;
            }
            pane.setText(next);
            StyledDocument doc = pane.getStyledDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setAlignment(
                    attrs, center ? StyleConstants.ALIGN_CENTER : StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
            pane.setCaretPosition(0);
        }

        private void setVisibleIfChanged(Component component, boolean visible) {
            if (component.isVisible() != visible) {
                component.setVisible(visible);
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
        private ResearchTreeGraph.Node pressedNode;

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
                    pressedNode = nodeAt(e.getPoint());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (pressedNode != null && !dragged) {
                        openDetail(pressedNode);
                    } else if (!dragged && nodeAt(e.getPoint()) == null) {
                        closeDetail();
                    }
                    pressScreen = null;
                    viewOrigin = null;
                    pressedNode = null;
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (pressScreen == null || viewOrigin == null || pressedNode != null) {
                        return;
                    }
                    Point now = e.getLocationOnScreen();
                    int dx = now.x - pressScreen.x;
                    int dy = now.y - pressScreen.y;
                    if (Math.abs(dx) < DRAG_THRESHOLD_PX && Math.abs(dy) < DRAG_THRESHOLD_PX) {
                        return;
                    }
                    dragged = true;
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

            if (graph.getNodes().isEmpty()) {
                centerX = PADDING;
                centerY = PADDING;
                setPreferredSize(new Dimension(200, 80));
                colorIcons.clear();
                return;
            }

            double spanX = graph.getMaxX() - graph.getMinX();
            double spanY = graph.getMaxY() - graph.getMinY();
            int width = (int) Math.ceil(spanX * UNIT_SIZE) + NODE_HIT + PADDING * 2;
            int height = (int) Math.ceil(spanY * UNIT_SIZE) + NODE_HIT + PADDING * 2;
            centerX = PADDING + (int) Math.round((-graph.getMinX()) * UNIT_SIZE) + NODE_HIT / 2;
            centerY = PADDING + (int) Math.round((-graph.getMinY()) * UNIT_SIZE) + NODE_HIT / 2;
            setPreferredSize(new Dimension(Math.max(width, 200), Math.max(height, 200)));

            Set<Upgrade> keep = new HashSet<>();
            for (ResearchTreeGraph.Node node : graph.getNodes()) {
                Upgrade upgrade = node.getUpgrade();
                keep.add(upgrade);
                int x = centerX + (int) Math.round(node.getPosX() * UNIT_SIZE) - NODE_HIT / 2;
                int y = centerY + (int) Math.round(node.getPosY() * UNIT_SIZE) - NODE_HIT / 2;
                nodeBounds.put(upgrade, new Rectangle(x, y, NODE_HIT, NODE_HIT));
                colorIcons.computeIfAbsent(upgrade, this::iconImage);
            }
            colorIcons.keySet().retainAll(keep);
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
            return icon.getImage();
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
