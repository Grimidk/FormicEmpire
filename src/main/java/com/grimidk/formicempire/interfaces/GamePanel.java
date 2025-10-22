package com.grimidk.formicempire.interfaces;

import com.grimidk.formicempire.classes.Engine;
import com.grimidk.formicempire.classes.Savefile;
import com.grimidk.formicempire.classes.World;
import com.grimidk.formicempire.classes.Colony;
import com.grimidk.formicempire.classes.SaveManager;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private final MainFrame frame;
    private final JLabel statusLabel;
    private final JLabel totalLabel;
    private final JLabel queensLabel;
    private final JLabel soldiersLabel;
    private final JLabel workersLabel;
    private Runnable tickListener;
    private final JButton playPauseButton;
    private final JLabel minuteLabel;
    private final JLabel hourLabel;
    private final JLabel dayLabel;
    private final JLabel monthLabel;
    private final JLabel yearLabel;
    private final JLabel statusIndicator;
    private final JButton speedUpButton;
    private final JButton speedDownButton;
    private final JLabel tickLabel;
    private volatile boolean engineStarted = false;
    private int speedLevel = 1; 

    public GamePanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
    statusLabel = new JLabel("Game not started");

        // Status panel (indicator + text)
        statusIndicator = new JLabel();
        statusIndicator.setOpaque(true);
        statusIndicator.setBackground(Color.GRAY);
        statusIndicator.setPreferredSize(new Dimension(12, 12));
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(statusIndicator);
        north.add(statusLabel);
        add(north, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(4, 1));
        totalLabel = new JLabel("Total ants: 0");
        queensLabel = new JLabel("Queens: 0");
        soldiersLabel = new JLabel("Soldiers: 0");
        workersLabel = new JLabel("Workers: 0");
        stats.add(totalLabel);
        stats.add(queensLabel);
        stats.add(soldiersLabel);
        stats.add(workersLabel);
        add(stats, BorderLayout.CENTER);

        // Time panel (right)
        JPanel timePanel = new JPanel(new GridLayout(5, 1));
        minuteLabel = new JLabel("Minute: 0");
        hourLabel = new JLabel("Hour: 0");
        dayLabel = new JLabel("Day: 0");
        monthLabel = new JLabel("Month: 0");
        yearLabel = new JLabel("Year: 0");
        timePanel.add(minuteLabel);
        timePanel.add(hourLabel);
        timePanel.add(dayLabel);
        timePanel.add(monthLabel);
        timePanel.add(yearLabel);
        add(timePanel, BorderLayout.EAST);

        // South panel with speed controls, play/pause and back
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        speedDownButton = new JButton("Speed-");
        speedUpButton = new JButton("Speed+");
        tickLabel = new JLabel("Tick: 1000ms");
        speedDownButton.addActionListener(e -> {
            if (speedLevel > 0) speedLevel--;
            applySpeedLevel();
        });
        speedUpButton.addActionListener(e -> {
            if (speedLevel < 6) speedLevel++;
            applySpeedLevel();
        });
        playPauseButton = new JButton("Pause");
        playPauseButton.addActionListener(e -> {
            Engine engine = frame.getEngine();
            if (engine == null || !engineStarted) return;
            if (engine.isPaused()) {
                engine.resumeEngine();
                playPauseButton.setText("Pause");
                updatePauseIndicator(false);
            } else {
                engine.pauseEngine();
                playPauseButton.setText("Play");
                updatePauseIndicator(true);
            }
        });
        JButton back = new JButton("Back");
        back.addActionListener(e -> {
            Engine eng = frame.getEngine();
            if (eng != null) {
                eng.pauseEngine();
            }
            updatePauseIndicator(true);
            unregisterTickListener();
            // autosave current world to dedicated autosave file asynchronously before returning
            try {
                SaveManager sm = new SaveManager();
                Engine engine = frame.getEngine();
                if (engine != null && engine.getWorld() != null) {
                    sm.saveAutosaveAsync(engine.getWorld(), () -> {
                        JOptionPane.showMessageDialog(this, "Autosaved", "Autosave", JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            frame.showCard(MainFrame.CARD_SAVE);
        });
        // add speed controls on the left of the south panel
        south.add(speedDownButton);
        south.add(speedUpButton);
        south.add(tickLabel);
        south.add(playPauseButton);
        south.add(back);
        add(south, BorderLayout.SOUTH);

        this.tickListener = null;
        updateTickLabel(frame.getEngine());
        updatePauseIndicator(false);
    }

    public void enterWithSavefile(Savefile savefile) {
        statusLabel.setText("Starting game...");
        Engine engine = frame.getEngine();
        new Thread(() -> {
            engine.startUp(savefile);
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Game started");
                registerTickListener();
                updateCounts();
                if (engine != null && !engineStarted) {
                    engineStarted = true;
                    engine.start();
                }
            });
        }).start();
    }

    private void registerTickListener() {
        unregisterTickListener();
        Engine engine = frame.getEngine();
        if (engine == null) return;
        tickListener = () -> SwingUtilities.invokeLater(this::updateCounts);
        engine.addTickListener(tickListener);
        updateTickLabel(engine);
        updatePauseIndicator(engine.isPaused());
    }
    private void unregisterTickListener() {
        if (tickListener != null) {
            Engine engine = frame.getEngine();
            if (engine != null) engine.removeTickListener(tickListener);
        }
        tickListener = null;
    }

    private void updateTickLabel(Engine eng) {
        if (eng == null) {
            tickLabel.setText("Tick: -");
            return;
        }
        tickLabel.setText("Tick: " + (long) eng.getDelay() + "ms");
    }

    private void updatePauseIndicator(boolean paused) {
        if (!engineStarted) {
            statusIndicator.setBackground(Color.GRAY);
            statusLabel.setText("Game not started");
            return;
        }
        if (paused) {
            statusIndicator.setBackground(Color.RED);
            statusLabel.setText("Paused");
        } else {
            statusIndicator.setBackground(Color.GREEN);
            statusLabel.setText("Running");
        }
    }

    private void applySpeedLevel() {
        Engine eng = frame.getEngine();
        if (eng == null) return;
        switch (speedLevel) {
            case 0:
                eng.pauseEngine();
                updatePauseIndicator(true);
                tickLabel.setText("Tick: PAUSED");
                break;
            case 1:
                eng.setDelay(1000f);
                eng.resumeEngine();
                updatePauseIndicator(false);
                tickLabel.setText("Tick: 1000ms");
                break;
            case 2:
                eng.setDelay(500f);
                eng.resumeEngine();
                updatePauseIndicator(false);
                tickLabel.setText("Tick: 500ms");
                break;
            case 3:
                eng.setDelay(100f);
                eng.resumeEngine();
                updatePauseIndicator(false);
                tickLabel.setText("Tick: 100ms");
                break;
            case 4:
                eng.setDelay(50f);
                eng.resumeEngine();
                updatePauseIndicator(false);
                tickLabel.setText("Tick: 50ms");
                break;
            case 5:
                eng.setDelay(25f);
                eng.resumeEngine();
                updatePauseIndicator(false);
                tickLabel.setText("Tick: 25ms");
                break;
            case 6:
                eng.setDelay(10f);
                eng.resumeEngine();
                updatePauseIndicator(false);
                tickLabel.setText("Tick: 10ms");
                break;
            default:
                eng.setDelay(1000f);
                eng.resumeEngine();
                updatePauseIndicator(false);
                tickLabel.setText("Tick: 1000ms");
        }
    }

    private void updateCounts() {
        Engine engine = frame.getEngine();
        if (engine == null) return;
        World world = engine.getWorld();
        if (world == null) return;
        if (world.getHexes() == null || world.getHexes().isEmpty()) return;
        Colony colony = world.getSpawnHex().getColony();
        if (colony == null) return;

        int total = colony.getAntTotal();
        int queens = colony.getQueens() != null ? colony.getQueens().size() : 0;
        int soldiers = colony.getSoldiers() != null ? colony.getSoldiers().size() : 0;
        int workers = colony.getWorkers() != null ? colony.getWorkers().size() : 0;

        totalLabel.setText("Total ants: " + total);
        queensLabel.setText("Queens: " + queens);
        soldiersLabel.setText("Soldiers: " + soldiers);
        workersLabel.setText("Workers: " + workers);
        minuteLabel.setText("Minute: " + world.getMinute());
        hourLabel.setText("Hour: " + world.getHour());
        dayLabel.setText("Day: " + world.getDay());
        monthLabel.setText("Month: " + world.getMonth());
        yearLabel.setText("Year: " + world.getYear());
    }
}
