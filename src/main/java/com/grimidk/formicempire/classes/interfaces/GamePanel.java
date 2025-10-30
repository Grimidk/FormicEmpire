package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.Engine;
import com.grimidk.formicempire.classes.Savefile;
import com.grimidk.formicempire.classes.World;
import com.grimidk.formicempire.classes.Colony;
import com.grimidk.formicempire.classes.SaveManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {
    private final MainFrame frame;
    private JLabel statusLabel;
    private JLabel totalAntLabel;
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
    private JLabel totalResourcesLabel;

    private JPanel timePanel;
    private JPanel antsDetailPanel;
    private JPanel resourcesDetailPanel;

    private Runnable tickListener;
    private JButton playPauseButton;

    private JLabel dateTimeLabel;
    private JLabel timeOfDayLabel;
    private JLabel moonPhaseLabel;
    private JLabel seasonLabel;
    private JLabel weatherLabel;

    private JLabel statusIndicator;
    private JButton speedUpButton;
    private JButton speedDownButton;
    private JLabel tickLabel;

    private JButton menuButton;
    private JPopupMenu gameMenu;

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

    private ImageIcon dawnIcon;
    private ImageIcon dayIcon;
    private ImageIcon duskIcon;
    private ImageIcon nightIcon;

    private ImageIcon clearIcon;
    private ImageIcon rainIcon;
    private ImageIcon snowIcon;

    public GamePanel(MainFrame frame) {
        this.frame = frame;
        this.tickListener = null;

        initComponents();
        initLayout();
        initListeners();
        initKeyBindings();

        updateTickLabel(frame.getEngine());
        updateStatusIndicator(false);
    }

    private void initComponents() {
        statusLabel = new JLabel("Game not started");
        statusIndicator = new JLabel();

        totalAntLabel = new JLabel("Total ants: 0");
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

        totalResourcesLabel = new JLabel("Total resources: 0");
        mushroomsLabel = new JLabel("Mushrooms: 0");
        planLabel = new JLabel("Plant matter: 0");
        proteinLabel = new JLabel("Protein: 0");
        waterLabel = new JLabel("Water: 0");
        syrupLabel = new JLabel("Syrup: 0");
        resinLabel = new JLabel("Resin: 0");
        mineralLabel = new JLabel("Minerals: 0");

        dateTimeLabel = new JLabel("00:00 00/00/0000");
        timeOfDayLabel = new JLabel("Time of Day: Dawn");
        moonPhaseLabel = new JLabel("Moon Phase: New Moon");
        seasonLabel = new JLabel("Season: Spring");
        weatherLabel = new JLabel("Weather: Clear");

        speedDownButton = new JButton("Speed- (-)");
        speedUpButton = new JButton("Speed+ (+)");
        tickLabel = new JLabel("Tick: 250ms");
        playPauseButton = new JButton("Pause (Space)");
        menuButton = new JButton("Menu (ESC)");

        antsDetailPanel = new JPanel();
        antsDetailPanel.setBorder(new TitledBorder("Ants"));
        antsDetailPanel.setLayout(new BoxLayout(antsDetailPanel, BoxLayout.Y_AXIS));
        antsDetailPanel.add(queensLabel);
        antsDetailPanel.add(princessLabel);
        antsDetailPanel.add(droneLabel);
        antsDetailPanel.add(majorLabel);
        antsDetailPanel.add(soldiersLabel);
        antsDetailPanel.add(workersLabel);
        antsDetailPanel.add(larvaLabel);
        antsDetailPanel.add(pupaLabel);
        antsDetailPanel.add(eggsLabel);
        antsDetailPanel.add(deadAntsLabel);

        resourcesDetailPanel = new JPanel();
        resourcesDetailPanel.setBorder(new TitledBorder("Resources"));
        resourcesDetailPanel.setLayout(new BoxLayout(resourcesDetailPanel, BoxLayout.Y_AXIS));
        resourcesDetailPanel.add(mushroomsLabel);
        resourcesDetailPanel.add(planLabel);
        resourcesDetailPanel.add(proteinLabel);
        resourcesDetailPanel.add(waterLabel);
        resourcesDetailPanel.add(syrupLabel);
        resourcesDetailPanel.add(resinLabel);
        resourcesDetailPanel.add(mineralLabel);

        timePanel = new JPanel();
        timePanel.setBorder(new TitledBorder("Time"));
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
        timePanel.add(dateTimeLabel);
        timePanel.add(timeOfDayLabel);
        timePanel.add(moonPhaseLabel);
        timePanel.add(weatherLabel);
        timePanel.add(seasonLabel);

        try {
            dawnIcon = new ImageIcon(getClass().getResource("/icons/times/dawn.png"));
            dayIcon = new ImageIcon(getClass().getResource("/icons/times/day.png"));
            duskIcon = new ImageIcon(getClass().getResource("/icons/times/dusk.png"));
            nightIcon = new ImageIcon(getClass().getResource("/icons/times/night.png"));

            clearIcon = new ImageIcon(getClass().getResource("/icons/weather/clear.png"));
            rainIcon = new ImageIcon(getClass().getResource("/icons/weather/rain.png"));
            snowIcon = new ImageIcon(getClass().getResource("/icons/weather/snow.png"));
        } catch (Exception e) {
            System.err.println("Error loading icons: " + e.getMessage());
        }
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
        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        stats.add(totalAntLabel);
        stats.add(totalResourcesLabel);
        stats.add(antsDetailPanel);
        stats.add(resourcesDetailPanel);
        return stats;
    }

    private JPanel createEastPanel() {
        JPanel east = new JPanel();
        east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
        east.add(timePanel);
        return east;
    }

    private JPanel createSouthPanel() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(speedDownButton);
        south.add(speedUpButton);
        south.add(tickLabel);
        south.add(playPauseButton);
        south.add(menuButton);
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
                playPauseButton.setText("Pause (Space)");
                updateStatusIndicator(false);
            } else {
                engine.pauseEngine();
                playPauseButton.setText("Play (Space)");
                updateStatusIndicator(true);
            }
        });

        gameMenu = new JPopupMenu();
        JMenuItem backToGame = new JMenuItem("Back to Game");
        JMenuItem quitToMenu = new JMenuItem("Quit to Main Menu");

        quitToMenu.addActionListener(e -> handleBackButton());
        gameMenu.add(backToGame);
        gameMenu.add(quitToMenu);

        menuButton.addActionListener(e -> {
            gameMenu.show(menuButton, 0, -gameMenu.getPreferredSize().height);
        });
    }

    private void initKeyBindings() {
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "togglePause");
        actionMap.put("togglePause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playPauseButton.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "speedUp");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.SHIFT_DOWN_MASK), "speedUp");
        actionMap.put("speedUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speedUpButton.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "speedDown");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "speedDown");
        actionMap.put("speedDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speedDownButton.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "openMenu");
        actionMap.put("openMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameMenu.isVisible()) {
                    gameMenu.setVisible(false);
                } else {
                    menuButton.doClick();
                }
            }
        });
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
                if(engine.isPaused()) {
                    playPauseButton.setText("Play (Space)");
                } else {
                    playPauseButton.setText("Pause (Space)");
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
            playPauseButton.setText("Play (Space)");
            tickLabel.setText("Tick: PAUSED");
        } else {
            eng.setDelay(delay);
            eng.resumeEngine();
            updateStatusIndicator(false);
            playPauseButton.setText("Pause (Space)");
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

        int totalAnts = colony.getAntTotal();
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
        int totalResources = mushrooms + plants + protein + water + syrups + resins + minerals;

        totalAntLabel.setText("Total ants: " + totalAnts);
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

        totalResourcesLabel.setText("Total resources: " + totalResources);
        mushroomsLabel.setText("Mushrooms: " + mushrooms);
        planLabel.setText("Plant matter: " + plants);
        proteinLabel.setText("Protein: " + protein);
        waterLabel.setText("Water: " + water);
        syrupLabel.setText("Syrup: " + syrups);
        resinLabel.setText("Resin: " + resins);
        mineralLabel.setText("Minerals: " + minerals);

        String dateTime = String.format("%02d:%02d %02d/%02d/%04d",
        world.getHour(), world.getMinute(), world.getDay(), world.getMonth(), world.getYear());
        dateTimeLabel.setText(dateTime);

        String timeOfDayName = world.getTimeOfDay().getName();
        timeOfDayLabel.setText(null);
        timeOfDayLabel.setToolTipText(timeOfDayName);
        switch (timeOfDayName) {
            case "Dawn":
                timeOfDayLabel.setIcon(dawnIcon);
                break;
            case "Daytime":
                timeOfDayLabel.setIcon(dayIcon);
                break;
            case "Dusk":
                timeOfDayLabel.setIcon(duskIcon);
                break;
            case "Nightime":
                timeOfDayLabel.setIcon(nightIcon);
                break;
            default:
                timeOfDayLabel.setIcon(null);
                timeOfDayLabel.setText(timeOfDayName);
                break;
        }

        moonPhaseLabel.setText("Moon Phase: " + world.getMoonPhase().getName());
        seasonLabel.setText("Season: " + world.getSeason().getName());

        String weatherName = world.getWeather().getName();
        weatherLabel.setText(null);
        weatherLabel.setToolTipText(weatherName);
        switch (weatherName) {
            case "Clear":
                weatherLabel.setIcon(clearIcon);
                break;
            case "Rain":
                weatherLabel.setIcon(rainIcon);
                break;
            case "Snow":
                weatherLabel.setIcon(snowIcon);
                break;
            default:
                weatherLabel.setIcon(null);
                weatherLabel.setText(weatherName);
                break;
        }
    }
}