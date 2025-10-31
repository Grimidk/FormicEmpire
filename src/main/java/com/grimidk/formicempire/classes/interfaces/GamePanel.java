package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.World;
import com.grimidk.formicempire.classes.Colony;
import com.grimidk.formicempire.classes.constants.MoonPhase;
import com.grimidk.formicempire.classes.constants.Season;
import com.grimidk.formicempire.classes.constants.TimeOfDay;
import com.grimidk.formicempire.classes.constants.Weather;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;

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
        queensLabel = new JLabel("0");
        princessLabel = new JLabel("0");
        droneLabel = new JLabel("0");
        majorLabel = new JLabel("0");
        soldiersLabel = new JLabel("0");
        workersLabel = new JLabel("0");
        pupaLabel = new JLabel("0");
        larvaLabel = new JLabel("0");
        eggsLabel = new JLabel("0");
        deadAntsLabel = new JLabel("Dead: 0"); 

        totalResourcesLabel = new JLabel("Total resources: 0");
        mushroomsLabel = new JLabel("0");
        planLabel = new JLabel("0");
        proteinLabel = new JLabel("0");
        waterLabel = new JLabel("0");
        syrupLabel = new JLabel("0");
        resinLabel = new JLabel("0");
        mineralLabel = new JLabel("0");

        dateTimeLabel = new JLabel("00:00 00/00/0000");
        timeOfDayLabel = new JLabel(); 
        moonPhaseLabel = new JLabel(); 
        seasonLabel = new JLabel();    
        weatherLabel = new JLabel();  

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
        antsDetailPanel.add(pupaLabel);
        antsDetailPanel.add(larvaLabel);
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

        // --- Set Static Icons ---
        queensLabel.setIcon(GameConstants.TYPE_QUEEN.getIcon());
        princessLabel.setIcon(GameConstants.TYPE_PRINCESS.getIcon());
        droneLabel.setIcon(GameConstants.TYPE_DRONE.getIcon());
        majorLabel.setIcon(GameConstants.TYPE_MAJOR.getIcon());
        soldiersLabel.setIcon(GameConstants.TYPE_SOLDIER.getIcon());
        workersLabel.setIcon(GameConstants.TYPE_WORKER.getIcon());
        pupaLabel.setIcon(GameConstants.TYPE_PUPA.getIcon());
        larvaLabel.setIcon(GameConstants.TYPE_LARVA.getIcon());
        eggsLabel.setIcon(GameConstants.TYPE_EGG.getIcon());

        queensLabel.setToolTipText(GameConstants.TYPE_QUEEN.getName());
        princessLabel.setToolTipText(GameConstants.TYPE_PRINCESS.getName());
        droneLabel.setToolTipText(GameConstants.TYPE_DRONE.getName());
        majorLabel.setToolTipText(GameConstants.TYPE_MAJOR.getName());
        soldiersLabel.setToolTipText(GameConstants.TYPE_SOLDIER.getName());
        workersLabel.setToolTipText(GameConstants.TYPE_WORKER.getName());
        pupaLabel.setToolTipText(GameConstants.TYPE_PUPA.getName());
        larvaLabel.setToolTipText(GameConstants.TYPE_LARVA.getName());
        eggsLabel.setToolTipText(GameConstants.TYPE_EGG.getName());

        mushroomsLabel.setIcon(GameConstants.FUNGI_RESOURCE.getIcon()); 
        planLabel.setIcon(GameConstants.PLANT_RESOURCE.getIcon());
        proteinLabel.setIcon(GameConstants.MEAT_RESOURCE.getIcon());  
        waterLabel.setIcon(GameConstants.WATER_RESOURCE.getIcon());  
        syrupLabel.setIcon(GameConstants.SYRUP_RESOURCE.getIcon());    
        resinLabel.setIcon(GameConstants.RESIN_RESOURCE.getIcon());
        mineralLabel.setIcon(GameConstants.ROCK_RESOURCE.getIcon());

        mushroomsLabel.setToolTipText(GameConstants.FUNGI_RESOURCE.getName());
        planLabel.setToolTipText(GameConstants.PLANT_RESOURCE.getName());
        proteinLabel.setToolTipText(GameConstants.MEAT_RESOURCE.getName());
        waterLabel.setToolTipText(GameConstants.WATER_RESOURCE.getName());
        syrupLabel.setToolTipText(GameConstants.SYRUP_RESOURCE.getName());
        resinLabel.setToolTipText(GameConstants.RESIN_RESOURCE.getName());
        mineralLabel.setToolTipText(GameConstants.ROCK_RESOURCE.getName());
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        add(createNorthPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.WEST); 
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

        JPanel antsWrapper = new JPanel(new BorderLayout());
        antsWrapper.add(antsDetailPanel, BorderLayout.NORTH);
        stats.add(antsWrapper);

        JPanel resourcesWrapper = new JPanel(new BorderLayout());
        resourcesWrapper.add(resourcesDetailPanel, BorderLayout.NORTH);
        stats.add(resourcesWrapper);

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
        int pupa = colony.getPupae() != null ? colony.getPupae().size() : 0;
        int larva = colony.getLarvae() != null ? colony.getLarvae().size() : 0;
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
        queensLabel.setText(String.valueOf(queens));
        princessLabel.setText(String.valueOf(princesses));
        droneLabel.setText(String.valueOf(drones));
        majorLabel.setText(String.valueOf(majors));
        soldiersLabel.setText(String.valueOf(soldiers));
        workersLabel.setText(String.valueOf(workers));
        pupaLabel.setText(String.valueOf(pupa));
        larvaLabel.setText(String.valueOf(larva));
        eggsLabel.setText(String.valueOf(eggs));
        deadAntsLabel.setText("Dead ants: " + deadAnts);

        totalResourcesLabel.setText("Total resources: " + totalResources);
        mushroomsLabel.setText(String.valueOf(mushrooms));
        planLabel.setText(String.valueOf(plants));
        proteinLabel.setText(String.valueOf(protein));
        waterLabel.setText(String.valueOf(water));
        syrupLabel.setText(String.valueOf(syrups));
        resinLabel.setText(String.valueOf(resins));
        mineralLabel.setText(String.valueOf(minerals));

        String dateTime = String.format("%02d:%02d %02d/%02d/%04d",
        world.getHour(), world.getMinute(), world.getDay(), world.getMonth(), world.getYear());
        dateTimeLabel.setText(dateTime);

        TimeOfDay currentTimeOfDay = world.getTimeOfDay();
        timeOfDayLabel.setIcon(currentTimeOfDay.getIcon());
        timeOfDayLabel.setToolTipText(currentTimeOfDay.getName());

        MoonPhase currentMoonPhase = world.getMoonPhase();
        moonPhaseLabel.setIcon(currentMoonPhase.getIcon());
        moonPhaseLabel.setToolTipText(currentMoonPhase.getName());

        Season currentSeason = world.getSeason();
        seasonLabel.setIcon(currentSeason.getIcon());
        seasonLabel.setToolTipText(currentSeason.getName());

        Weather currentWeather = world.getWeather();
        weatherLabel.setIcon(currentWeather.getIcon());
        weatherLabel.setToolTipText(currentWeather.getName());
    }
}