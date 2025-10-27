package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.Engine;
import com.grimidk.formicempire.classes.Savefile;
import com.grimidk.formicempire.classes.World;
import com.grimidk.formicempire.classes.Colony;
import com.grimidk.formicempire.classes.SaveManager;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private final MainFrame frame;
    private JLabel statusLabel;
    private JLabel totalLabel;
    private JLabel queensLabel;
    private JLabel princessLabel;
    private JLabel droneLabel;  
    private JLabel majorLabel;
    private JLabel soldiersLabel;
    private JLabel workersLabel;
    private JLabel larvaLabel;
    private JLabel pupaLabel;
    private JLabel eggsLabel;
    private JLabel deadAntsLabel;
    private JLabel mushroomsLabel;
    private JLabel planLabel;
    private JLabel proteinLabel;
    private JLabel waterLabel;  
    private JLabel syrupLabel;
    private JLabel resinLabel;
    private JLabel mineralLabel;

    private Runnable tickListener;
    private JButton playPauseButton;
    private JLabel minuteLabel;
    private JLabel hourLabel;
    private JLabel dayLabel;
    private JLabel monthLabel;
    private JLabel yearLabel;
    private JLabel timeOfDayLabel;
    private JLabel moonPhaseLabel;
    private JLabel seasonLabel;
    private JLabel weatherLabel;
    private JLabel statusIndicator;
    private JButton speedUpButton;
    private JButton speedDownButton;
    private JLabel tickLabel;
    private volatile boolean engineStarted = false;
    private int speedLevel = 1;

    private static final float[] SPEED_DELAYS = {
        -1f,    // Level 0 (Paused)
        250f,   // Level 1
        125f,   // Level 2
        50f,    // Level 3
        25f,    // Level 4
        10f,    // Level 5
        5f      // Level 6
    };

    public GamePanel(MainFrame frame) {
        this.frame = frame;
        this.tickListener = null;

        initComponents();
        initLayout();
        initListeners();

        updateTickLabel(frame.getEngine());
        updateStatusIndicator(false);
    }

    private void initComponents() {
        statusLabel = new JLabel("Game not started");
        statusIndicator = new JLabel();
        totalLabel = new JLabel("Total ants: 0");
        queensLabel = new JLabel("Queens: 0");
        princessLabel = new JLabel("Princesses: 0");
        droneLabel = new JLabel("Drones: 0");
        majorLabel = new JLabel("Majors: 0");
        soldiersLabel = new JLabel("Soldiers: 0");
        workersLabel = new JLabel("Workers: 0");
        larvaLabel = new JLabel("Larva: 0");
        pupaLabel = new JLabel("Pupa: 0");  
        eggsLabel = new JLabel("Eggs: 0");
        deadAntsLabel = new JLabel("Dead ants: 0");
        mushroomsLabel = new JLabel("Mushrooms: 0");
        planLabel = new JLabel("Plant matter: 0");
        proteinLabel = new JLabel("Protein: 0");
        waterLabel = new JLabel("Water: 0");
        syrupLabel = new JLabel("Syrup: 0");
        resinLabel = new JLabel("Resin: 0");
        mineralLabel = new JLabel("Minerals: 0");
        minuteLabel = new JLabel("Minute: 0");
        hourLabel = new JLabel("Hour: 0");
        dayLabel = new JLabel("Day: 0");
        monthLabel = new JLabel("Month: 0");
        yearLabel = new JLabel("Year: 0");
        timeOfDayLabel = new JLabel("Time of Day: Dawn");
        moonPhaseLabel = new JLabel("Moon Phase: New Moon");
        seasonLabel = new JLabel("Season: Spring");
        weatherLabel = new JLabel("Weather: Clear");
        speedDownButton = new JButton("Speed-");
        speedUpButton = new JButton("Speed+");
        tickLabel = new JLabel("Tick: 500ms");
        playPauseButton = new JButton("Pause");
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        add(createNorthPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createEastPanel(), BorderLayout.EAST);
        add(createSouthPanel(), BorderLayout.SOUTH);
    }

    private JPanel createNorthPanel() {
        statusIndicator.setOpaque(true);
        statusIndicator.setBackground(Color.GRAY);
        statusIndicator.setPreferredSize(new Dimension(12, 12));
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(statusIndicator);
        north.add(statusLabel);
        return north;
    }

    private JPanel createCenterPanel() {
        JPanel stats = new JPanel(new GridLayout(4, 1));
        stats.add(totalLabel);
        stats.add(queensLabel);
        stats.add(princessLabel);
        stats.add(droneLabel);
        stats.add(majorLabel);
        stats.add(soldiersLabel);
        stats.add(workersLabel);
        stats.add(larvaLabel);
        stats.add(pupaLabel);
        stats.add(eggsLabel);
        stats.add(deadAntsLabel);
        stats.add(mushroomsLabel);
        stats.add(planLabel);
        stats.add(proteinLabel);
        stats.add(waterLabel);
        stats.add(syrupLabel);
        stats.add(resinLabel);
        stats.add(mineralLabel);
        return stats;
    }

    private JPanel createEastPanel() {
        JPanel timePanel = new JPanel(new GridLayout(5, 1));
        timePanel.add(minuteLabel);
        timePanel.add(hourLabel);
        timePanel.add(dayLabel);
        timePanel.add(monthLabel);
        timePanel.add(yearLabel);
        timePanel.add(timeOfDayLabel);
        timePanel.add(moonPhaseLabel);
        timePanel.add(seasonLabel);
        timePanel.add(weatherLabel); 
        return timePanel;
    }

    private JPanel createSouthPanel() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(speedDownButton);
        south.add(speedUpButton);
        south.add(tickLabel);
        south.add(playPauseButton);
        south.add(new JButton("Back")); 
        return south;
    }

    private void initListeners() {
        speedDownButton.addActionListener(e -> {
            if (speedLevel > 0) speedLevel--;
            applySpeedLevel();
        });

        speedUpButton.addActionListener(e -> {
            if (speedLevel < SPEED_DELAYS.length - 1) speedLevel++;
            applySpeedLevel();
        });

        playPauseButton.addActionListener(e -> {
            Engine engine = frame.getEngine();
            if (engine == null || !engineStarted) return;
            if (engine.isPaused()) {
                engine.resumeEngine();
                playPauseButton.setText("Pause");
                updateStatusIndicator(false);
            } else {
                engine.pauseEngine();
                playPauseButton.setText("Play");
                updateStatusIndicator(true);
            }
        });

        JPanel southPanel = (JPanel) getComponent(3); // 0=N, 1=C, 2=E, 3=S
        southPanel.removeAll();
        
        JButton back = new JButton("Back");
        back.addActionListener(e -> handleBackButton());

        southPanel.add(speedDownButton);
        southPanel.add(speedUpButton);
        southPanel.add(tickLabel);
        southPanel.add(playPauseButton);
        southPanel.add(back);
    }

    private void handleBackButton() {
        Engine eng = frame.getEngine();
        if (eng != null) {
            eng.pauseEngine();
        }
        updateStatusIndicator(true);
        unregisterTickListener();

        try {
            SaveManager sm = new SaveManager();
            Engine engine = frame.getEngine();
            if (engine != null && engine.getWorld() != null) {
                World w = engine.getWorld();
                int slotIdLocal = 0;
                try {
                    slotIdLocal = w.getSaveSlotId();
                } catch (Exception ignore) {
                    slotIdLocal = 0;
                }
                
                final int capturedSlot = slotIdLocal;
                if (capturedSlot > 0) {
                    Savefile existing = sm.loadSlot(capturedSlot);
                    String nameToUse = (existing != null && existing.getName() != null && !existing.getName().trim().isEmpty())
                                       ? existing.getName() : ("Save " + capturedSlot);
                    
                    sm.saveWorldToSlotUserAsync(engine.getWorld(), capturedSlot, nameToUse, () -> {
                        frame.showCard(MainFrame.CARD_SAVE);
                    });
                    return; 
                } else {
                    sm.saveWorldToSlot(engine.getWorld(), 0);
                    frame.showCard(MainFrame.CARD_SAVE);
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        frame.showCard(MainFrame.CARD_SAVE);
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
        updateStatusIndicator(engine.isPaused());
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

    private void updateStatusIndicator(boolean paused) {
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

        if (speedLevel < 0) speedLevel = 0;
        if (speedLevel >= SPEED_DELAYS.length) speedLevel = SPEED_DELAYS.length - 1;

        float delay = SPEED_DELAYS[speedLevel];

        if (delay == -1f) { 
            eng.pauseEngine();
            updateStatusIndicator(true);
            tickLabel.setText("Tick: PAUSED");
        } else {
            eng.setDelay(delay);
            eng.resumeEngine();
            updateStatusIndicator(false);
            updateTickLabel(eng); 
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
        int princesses = colony.getPrincesses() != null ? colony.getPrincesses().size() : 0;
        int drones = colony.getDrones() != null ? colony.getDrones().size() : 0;
        int majors = colony.getMajors() != null ? colony.getMajors().size() : 0;
        int soldiers = colony.getSoldiers() != null ? colony.getSoldiers().size() : 0;
        int workers = colony.getWorkers() != null ? colony.getWorkers().size() : 0;
        int larva = colony.getLarvae() != null ? colony.getLarvae().size() : 0;
        int pupa = colony.getPupae() != null ? colony.getPupae().size() : 0;
        int eggs = colony.getEggs() != null ? colony.getEggs().size() : 0;
        int deadAnts = colony.getDeadAnts() != null ? colony.getDeadAnts().size() : 0;
        int mushrooms = colony.getMushrooms();
        int plants = colony.getPlants();
        int protein = colony.getProtein();
        int water = colony.getWater();
        int syrups = colony.getSyrups();
        int resins = colony.getResins();    
        int minerals = colony.getMinerals();

        totalLabel.setText("Total ants: " + total);
        queensLabel.setText("Queens: " + queens);
        princessLabel.setText("Princesses: " + princesses);
        droneLabel.setText("Drones: " + drones);
        majorLabel.setText("Majors: " + majors);
        soldiersLabel.setText("Soldiers: " + soldiers);
        workersLabel.setText("Workers: " + workers);
        larvaLabel.setText("Larva: " + larva);
        pupaLabel.setText("Pupa: " + pupa);
        eggsLabel.setText("Eggs: " + eggs);
        deadAntsLabel.setText("Dead ants: " + deadAnts);
        mushroomsLabel.setText("Mushrooms: " + mushrooms);
        planLabel.setText("Plant matter: " + plants);
        proteinLabel.setText("Protein: " + protein);
        waterLabel.setText("Water: " + water);
        syrupLabel.setText("Syrup: " + syrups);
        resinLabel.setText("Resin: " + resins);
        mineralLabel.setText("Minerals: " + minerals);
        minuteLabel.setText("Minute: " + world.getMinute());
        hourLabel.setText("Hour: " + world.getHour());
        dayLabel.setText("Day: " + world.getDay());
        monthLabel.setText("Month: " + world.getMonth());
        yearLabel.setText("Year: " + world.getYear());
        timeOfDayLabel.setText("Time of Day: " + world.getTimeOfDay().getName());
        moonPhaseLabel.setText("Moon Phase: " + world.getMoonPhase().getName());
        seasonLabel.setText("Season: " + world.getSeason().getName());
        weatherLabel.setText("Weather: " + world.getWeather().getName());
    }
}