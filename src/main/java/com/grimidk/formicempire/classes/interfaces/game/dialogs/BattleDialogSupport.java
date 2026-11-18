package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.GameControlKeyBindings;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiResourceLoader;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

final class BattleDialogSupport {

    private static final Dimension SPEED_ICON_SLOT_SIZE = new Dimension(32, 32);

    private final List<Window> hiddenWindows = new ArrayList<>();
    private JLabel dateTimeLabel;
    private JLabel tickLabel;
    private JButton playPauseButton;
    private Runnable minuteListener;
    private Engine boundEngine;

    void openFullScreen(JDialog dialog, Window owner, Engine engine) {
        hiddenWindows.clear();
        if (owner != null) {
            for (Window w : owner.getOwnedWindows()) {
                if (w != null && w != dialog && w.isVisible()
                        && (!(w instanceof java.awt.Dialog d) || d.getModalityType() == java.awt.Dialog.ModalityType.MODELESS)) {
                    hiddenWindows.add(w);
                    w.setVisible(false);
                }
            }
        }
        boundEngine = engine;
        if (boundEngine != null) {
            boundEngine.setBattleViewActive(true);
            if (minuteListener == null) {
                minuteListener = () -> SwingUtilities.invokeLater(() -> updateControls(boundEngine));
            }
            boundEngine.addTickListener(minuteListener);
        }
        Rectangle bounds = (owner != null && owner.isShowing())
                ? owner.getBounds()
                : GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        dialog.setBounds(bounds);
        GameControlKeyBindings.register(
                dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                dialog.getRootPane().getActionMap(),
                () -> togglePause(boundEngine, owner),
                () -> changeSpeed(boundEngine, owner, 1),
                () -> changeSpeed(boundEngine, owner, -1));
    }

    void closeAndRestore() {
        if (boundEngine != null) {
            boundEngine.setBattleViewActive(false);
            if (minuteListener != null) {
                boundEngine.removeTickListener(minuteListener);
            }
            boundEngine = null;
        }
        for (Window w : hiddenWindows) {
            if (w != null && w.isDisplayable()) {
                w.setVisible(true);
            }
        }
        hiddenWindows.clear();
    }

    void populateSpeedControlBar(JPanel targetPanel, Engine engine, Window owner, Runnable onClose) {
        targetPanel.removeAll();
        targetPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
        targetPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);

        dateTimeLabel = new JLabel("00:00 01/01/0000");
        dateTimeLabel.setFont(AssetStyles.FONT_NORMAL);
        dateTimeLabel.setForeground(AssetStyles.FONT_COLOR);
        dateTimeLabel.setBorder(new EmptyBorder(0, 0, 0, 8));

        JButton speedDownButton = new JButton();
        speedDownButton.setFocusable(false);
        configureIconControlButton(speedDownButton, GameConstants.ICON_SPEED_DOWN);
        speedDownButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_CONTROL_SPEED_DOWN_TT));
        speedDownButton.addActionListener(e -> changeSpeed(engine, owner, -1));

        JButton speedUpButton = new JButton();
        speedUpButton.setFocusable(false);
        configureIconControlButton(speedUpButton, GameConstants.ICON_SPEED_UP);
        speedUpButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_CONTROL_SPEED_UP_TT));
        speedUpButton.addActionListener(e -> changeSpeed(engine, owner, 1));

        tickLabel = new JLabel();
        tickLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tickLabel.setVerticalAlignment(SwingConstants.CENTER);
        tickLabel.setIconTextGap(0);
        tickLabel.setText("");
        tickLabel.setPreferredSize(SPEED_ICON_SLOT_SIZE);
        tickLabel.setMinimumSize(SPEED_ICON_SLOT_SIZE);
        tickLabel.setMaximumSize(SPEED_ICON_SLOT_SIZE);

        playPauseButton = new JButton();
        playPauseButton.setFocusable(false);
        configureIconControlButton(playPauseButton, GameConstants.ICON_SPEED_PAUSE);
        playPauseButton.addActionListener(e -> togglePause(engine, owner));

        JButton closeButton = new JButton();
        closeButton.setFocusable(false);
        Icon closeIcon = UiResourceLoader.loadDialogIcon(BattleDialogSupport.class, "/icons/ui/Close.png", 16);
        if (closeIcon != null) {
            closeButton.setIcon(closeIcon);
            closeButton.setText("");
        } else {
            closeButton.setText(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        }
        closeButton.setToolTipText(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        closeButton.addActionListener(e -> {
            if (onClose != null) {
                onClose.run();
            }
        });

        targetPanel.add(dateTimeLabel);
        targetPanel.add(speedDownButton);
        targetPanel.add(speedUpButton);
        targetPanel.add(tickLabel);
        targetPanel.add(playPauseButton);
        targetPanel.add(closeButton);

        updateControls(engine);
        targetPanel.revalidate();
        targetPanel.repaint();
    }

    void updateControls(Engine engine) {
        World w = engine != null ? engine.getWorld() : null;
        if (dateTimeLabel != null && w != null) {
            Hex activeHex = w.getActiveHex();
            int hour = activeHex != null ? w.getLocalHour(activeHex) : w.getHour();
            int day = activeHex != null ? w.getLocalDay(activeHex) : w.getDay();
            int month = activeHex != null ? w.getLocalMonth(activeHex) : w.getMonth();
            int year = activeHex != null ? w.getLocalYear(activeHex) : w.getYear();
            dateTimeLabel.setText(String.format("%02d:%02d %02d/%02d/%04d",
                    hour, w.getMinute(), day, month, year));
        }
        if (tickLabel != null) {
            if (engine == null) {
                tickLabel.setIcon(null);
                tickLabel.setToolTipText(null);
            } else if (engine.isPaused()) {
                tickLabel.setIcon(GameConstants.ICON_SPEED_ZERO);
                tickLabel.setToolTipText(LanguageStrings.get(LanguageStrings.UI_CONTROL_PAUSED_TT));
            } else {
                GameSpeed speed = engine.getSpeed();
                if (speed != null && speed.getIcon() != null) {
                    tickLabel.setIcon(speed.getIcon());
                    tickLabel.setToolTipText(LanguageStrings.format(
                            LanguageStrings.UI_CONTROL_SPEED_TT, speed.getName(), speed.getDelayMs()));
                } else {
                    tickLabel.setIcon(null);
                    tickLabel.setToolTipText(engine.getSpeedLabel());
                }
            }
        }
        if (playPauseButton != null && engine != null) {
            boolean paused = engine.isPaused();
            playPauseButton.setIcon(paused ? GameConstants.ICON_SPEED_PLAY : GameConstants.ICON_SPEED_PAUSE);
            playPauseButton.setText("");
            playPauseButton.setToolTipText(LanguageStrings.get(
                    paused ? LanguageStrings.UI_CONTROL_PLAY_TT : LanguageStrings.UI_CONTROL_PAUSE_TT));
        }
    }

    private static void configureIconControlButton(JButton button, ImageIcon icon) {
        AssetStyles.styleIconButton(button);
        button.setIcon(icon);
        button.setText("");
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setPreferredSize(SPEED_ICON_SLOT_SIZE);
        button.setMinimumSize(SPEED_ICON_SLOT_SIZE);
        button.setMaximumSize(SPEED_ICON_SLOT_SIZE);
    }

    private void changeSpeed(Engine engine, Window owner, int direction) {
        if (engine == null) {
            return;
        }
        engine.adjustSpeedStep(direction);
        syncOwnerGamePanel(engine, owner);
        updateControls(engine);
    }

    private void togglePause(Engine engine, Window owner) {
        if (engine == null) {
            return;
        }
        engine.togglePause();
        syncOwnerGamePanel(engine, owner);
        updateControls(engine);
    }

    private static void syncOwnerGamePanel(Engine engine, Window owner) {
        if (owner instanceof MainFrame mf && mf.getGamePanel() != null) {
            mf.getGamePanel().onGameSpeedChanged();
            mf.getGamePanel().updateSpeedLabel();
            mf.getGamePanel().updateStatusIndicator(engine.isPaused());
        }
    }
}
