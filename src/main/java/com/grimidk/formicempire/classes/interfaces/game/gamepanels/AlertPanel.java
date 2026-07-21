package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.ArrayList;

public class AlertPanel extends ZeroGamePanel {

    public static final int ALERT_ICON_PX = 14;

    private static final Map<Image, Icon> SCALED_ICONS = new WeakHashMap<>();

    private JPanel listPanel;
    private JScrollPane scrollPane;
    private List<Alert> lastAlerts = new ArrayList<>();
    private String hoveredAlertKey;
    private boolean rebuilding;

    public AlertPanel() {
        super(new BorderLayout());
        initComponents();
        initLayout();
    }

    @Override
    protected void initComponents() {
        setPreferredSize(new Dimension(250, 150));
        setTitledBorder(LanguageStrings.PANEL_ALERTS);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(AssetStyles.BACKGROUND_COLOR);

        scrollPane = new JScrollPane(listPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
    }

    @Override
    protected void initLayout() {
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void refreshTranslations() {
        super.refreshTranslations();
        updateAlerts(lastAlerts);
    }

    public String getHoveredAlertKey() {
        return hoveredAlertKey;
    }

    private static String truncateForRow(String text) {
        if (text == null || text.length() <= 36) {
            return text;
        }
        return text.substring(0, 35) + "…";
    }

    public static Icon scaleAlertIcon(ImageIcon source) {
        if (source == null) {
            return null;
        }
        Image image = source.getImage();
        if (image == null) {
            return null;
        }
        synchronized (SCALED_ICONS) {
            Icon cached = SCALED_ICONS.get(image);
            if (cached != null) {
                return cached;
            }
            Image scaled = image.getScaledInstance(ALERT_ICON_PX, ALERT_ICON_PX, Image.SCALE_SMOOTH);
            Icon icon = new ImageIcon(scaled);
            SCALED_ICONS.put(image, icon);
            return icon;
        }
    }

    private static boolean sameDisplay(List<Alert> a, List<Alert> b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            Alert left = a.get(i);
            Alert right = b.get(i);
            if (!Objects.equals(left.key, right.key)
                    || !Objects.equals(left.message, right.message)
                    || !Objects.equals(left.tooltip, right.tooltip)
                    || left.icon != right.icon) {
                return false;
            }
        }
        return true;
    }

    private void setAlertHovered(Alert alert, boolean hovered) {
        if (alert == null) {
            return;
        }
        alert.setHovered(hovered);
        if (hovered) {
            hoveredAlertKey = alert.key;
        } else if (Objects.equals(hoveredAlertKey, alert.key)) {
            hoveredAlertKey = null;
        }
    }

    public void updateAlerts(List<Alert> alerts) {
        if (sameDisplay(lastAlerts, alerts)) {
            this.lastAlerts = alerts;
            if (hoveredAlertKey != null) {
                for (Alert alert : alerts) {
                    if (Objects.equals(alert.key, hoveredAlertKey)) {
                        alert.setHovered(true);
                        break;
                    }
                }
            }
            return;
        }

        rebuilding = true;
        String keepHover = hoveredAlertKey;
        this.lastAlerts = alerts;
        listPanel.removeAll();

        if (alerts.isEmpty()) {
            hoveredAlertKey = null;
            JLabel emptyLabel = new JLabel(LanguageStrings.get(LanguageStrings.PANEL_NO_ALERTS));
            emptyLabel.setForeground(AssetStyles.BACKGROUND_SECONDARY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(emptyLabel);
            listPanel.add(Box.createVerticalGlue());
        } else {
            for (Alert alert : alerts) {
                JPanel itemPanel = new JPanel(new BorderLayout(4, 0));
                itemPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
                itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, AssetStyles.BORDER_THICKNESS_INTERNAL, 0, AssetStyles.BORDER_COLOR),
                    new EmptyBorder(2, 4, 2, 4)
                ));

                if (alert.icon != null) {
                    JLabel iconLabel = new JLabel(alert.icon);
                    iconLabel.setOpaque(false);
                    itemPanel.add(iconLabel, BorderLayout.WEST);
                }

                String display = truncateForRow(alert.message);
                JLabel msgLabel = new JLabel(display);
                msgLabel.setFont(AssetStyles.FONT_NORMAL.deriveFont(11f));
                msgLabel.setForeground(AssetStyles.FONT_COLOR);
                itemPanel.add(msgLabel, BorderLayout.CENTER);

                String tip = alert.tooltip != null && !alert.tooltip.isEmpty() ? alert.tooltip : alert.message;
                if (tip != null && !tip.isEmpty()) {
                    itemPanel.setToolTipText(tip);
                    msgLabel.setToolTipText(tip);
                }

                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        setAlertHovered(alert, true);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (rebuilding) {
                            return;
                        }
                        Point p = SwingUtilities.convertPoint(itemPanel, e.getPoint(), listPanel);
                        if (listPanel.getComponentAt(p) == itemPanel) {
                            return;
                        }
                        setAlertHovered(alert, false);
                    }
                });

                if (Objects.equals(keepHover, alert.key)) {
                    setAlertHovered(alert, true);
                }
                listPanel.add(itemPanel);
            }
            if (keepHover != null && (hoveredAlertKey == null || !hoveredAlertKey.equals(keepHover))) {
                boolean stillPresent = false;
                for (Alert alert : alerts) {
                    if (Objects.equals(alert.key, keepHover)) {
                        stillPresent = true;
                        break;
                    }
                }
                if (!stillPresent) {
                    hoveredAlertKey = null;
                }
            }
        }

        rebuilding = false;
        listPanel.revalidate();
        listPanel.repaint();
    }

    public static class Alert {
        public String key;
        public String message;
        public Icon icon;
        public String tooltip;
        public long expiresAt;
        private long remainingWhenPaused = -1;

        public Alert(String key, String message, Icon icon, long durationMs) {
            this(key, message, icon, message, durationMs);
        }

        public Alert(String key, String message, Icon icon, String tooltip, long durationMs) {
            this.key = key;
            this.message = message;
            this.icon = icon;
            this.tooltip = tooltip;
            this.expiresAt = System.currentTimeMillis() + durationMs;
        }

        public void setHovered(boolean hovered) {
            if (hovered) {
                if (remainingWhenPaused < 0) {
                    remainingWhenPaused = Math.max(0L, expiresAt - System.currentTimeMillis());
                }
                return;
            }
            if (remainingWhenPaused >= 0) {
                expiresAt = System.currentTimeMillis() + remainingWhenPaused;
                remainingWhenPaused = -1;
            }
        }

        public boolean isHovered() {
            return remainingWhenPaused >= 0;
        }

        public void bumpExpiry(long durationMs) {
            if (isHovered()) {
                remainingWhenPaused = durationMs;
                return;
            }
            expiresAt = System.currentTimeMillis() + durationMs;
        }

        public boolean isExpired() {
            if (isHovered()) {
                return false;
            }
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
