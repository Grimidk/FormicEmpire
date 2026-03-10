package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Humidity;
import com.grimidk.formicempire.classes.constants.world.MoonPhase;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.constants.Constant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HelpPanel extends JPanel {
    private final MainFrame frame;

    public HelpPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(AssetStyles.BACKGROUND_COLOR);

        JTabbedPane mainTabs = new JTabbedPane();
        mainTabs.setFont(AssetStyles.FONT_BOLD);
        mainTabs.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        mainTabs.setForeground(AssetStyles.FONT_COLOR);

        // Filter Role Upgrades vs Generic Upgrades
        List<Constant> roleConstants = GameUnlocks.getUpgrades().stream()
                .filter(u -> u.getName().toLowerCase().contains("role"))
                .collect(Collectors.toList());
        
        List<Constant> genericUpgrades = GameUnlocks.getUpgrades().stream()
                .filter(u -> !u.getName().toLowerCase().contains("role"))
                .collect(Collectors.toList());

        // Add tabs
        mainTabs.addTab("Welcome", createWelcomePanel());
        mainTabs.addTab("Getting Started", createGettingStartedPanel());
        mainTabs.addTab("Dynasty", createEmpireManagementPanel());
        mainTabs.addTab("Hotkeys", createHotkeysPanel());
        mainTabs.addTab("Species", createSpeciesPanel());
        mainTabs.addTab("Ant Types", createAntTypesPanel());
        mainTabs.addTab("Roles", createDictionaryPanel(roleConstants));
        mainTabs.addTab("Upgrades", createDictionaryPanel(genericUpgrades));
        mainTabs.addTab("Buildings", createDictionaryPanel(new ArrayList<>(GameUnlocks.getBuildings())));
        mainTabs.addTab("Assimilations", createDictionaryPanel(new ArrayList<>(GameUnlocks.getAssimilations())));
        mainTabs.addTab("World", createWorldPanel());

        setupTabPaneNavigation(mainTabs);

        add(mainTabs, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JButton back = new JButton("Back");
        back.setFont(AssetStyles.FONT_BOLD);
        back.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        back.setForeground(AssetStyles.FONT_COLOR);
        back.addActionListener(e -> this.frame.showCard(MainFrame.CARD_INIT));
        
        setupButtonNavigation(back);
        
        southPanel.add(back);
        add(southPanel, BorderLayout.SOUTH);

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                back.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }
    
    private void setupButtonNavigation(JButton button) {
        button.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "pressed");
        button.getActionMap().put("pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button.doClick();
            }
        });

        Set<AWTKeyStroke> forwardKeys = new HashSet<>(button.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.add(KeyStroke.getKeyStroke("DOWN"));
        forwardKeys.add(KeyStroke.getKeyStroke("RIGHT"));
        button.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

        Set<AWTKeyStroke> backwardKeys = new HashSet<>(button.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.add(KeyStroke.getKeyStroke("UP"));
        backwardKeys.add(KeyStroke.getKeyStroke("LEFT"));
        button.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
    }

    private void setupTabPaneNavigation(JTabbedPane tabs) {
        Set<AWTKeyStroke> forwardKeys = new HashSet<>(tabs.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.add(KeyStroke.getKeyStroke("DOWN"));
        tabs.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

        Set<AWTKeyStroke> backwardKeys = new HashSet<>(tabs.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.add(KeyStroke.getKeyStroke("UP"));
        tabs.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
    }

    private JComponent createWelcomePanel() {
        String story = "<html><div style='width: 450px; font-family: sans-serif;'>" +
                "<p style='font-size: 14pt;'>" +
                "Nearly all other ants are extinct. You are an ant queen and have within you " +
                "all the genetic knowledge of every ant species. You must unlock it and " +
                "take over the world as the dominant species. " +
                "<br><br>" +
                "Build up your colony and begin to spread while fighting other ant colonies, " +
                "you will need to adapt to new environments by absorbing and researching their abilities." +
                "</p></div></html>";
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel(story);
        label.setFont(AssetStyles.FONT_NORMAL);
        label.setForeground(AssetStyles.FONT_COLOR);
        panel.add(label);
        return panel;
    }

    private JComponent createGettingStartedPanel() {
        String gameInfo = "<html><div style='width: 450px; font-family: sans-serif; font-size: 11pt;'>" +
                "<b>Basic Survival:</b><br>" +
                "Your ants need <b>Fungi</b> (Mushrooms) to eat and <b>Water</b> to drink every day. Without them, your colony will starve or die of dehydration.<br>" +
                "- <b>Foragers</b> gather Plants and Water.<br>" +
                "- <b>Hunters</b> gather Protein (Meat).<br>" +
                "- <b>Farmers</b> convert Plants and Protein into Fungi.<br>" +
                "- <b>Nurses</b> care for your brood. Neglected brood will perish.<br><br>" +
                "<b>Watching Your Colony:</b><br>" +
                "Press <b>A</b> to toggle your view between the <b>Underworld</b> (inside the nest) and the <b>Overworld</b> (outside gathering).<br><br>" +
                "<b>Colony Management:</b><br>" +
                "Use <b>Hatch Rates (P)</b> to control ant births. " +
                "<b>Researchers</b> generate Research Points (RP) for upgrades in the <b>Research Menu (Y)</b>. " +
                "<b>Builders</b> use Minerals and Resin to construct facilities via the <b>Build Menu (U)</b>.<br><br>" +
                "<b>Advanced Threats:</b><br>" +
                "- <b>Contamination:</b> Dead ants must be cleared by <b>Gravers</b>.<br>" +
                "- <b>Parasites:</b> Secretly drain food. Assign <b>Police</b> to eliminate them.<br>" +
                "- <b>Depletion:</b> Resource nodes dry up; assign <b>Scouts</b> to find new ones." +
                "</div></html>";
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel(gameInfo);
        label.setFont(AssetStyles.FONT_NORMAL);
        label.setForeground(AssetStyles.FONT_COLOR);
        panel.add(label);
        return panel;
    }

    private JComponent createEmpireManagementPanel() {
        String empireInfo = "<html><div style='width: 450px; font-family: sans-serif; font-size: 11pt;'>" +
                "<b>Expanding Your Dynasty:</b><br>" +
                "Once you unlock <b>Breeders</b>, perform Nuptial Flights to spread via the <b>World Map (I)</b>.<br>" +
                "<i>Tip: Eclipses trigger spontaneous, free Nuptial Flights!</i><br><br>" +
                "<b>Logistics & Trade:</b><br>" +
                "Founding multiple colonies unlocks <b>Trade Routes</b>. Assign <b>Couriers</b> to transport resources.<br>" +
                "- <b>Land:</b> Standard trade method.<br>" +
                "- <b>Tunnel:</b> Secure and fast, requires <b>Tunnels</b> and <b>Borers</b>.<br>" +
                "- <b>Air:</b> Fast but low capacity, requires <b>Sky Transports</b> (Princesses).<br>" +
                "- <b>Bilateral:</b> Allows two-way resource transport.<br><br>" +
                "<b>Dynasty Milestones:</b><br>" +
                "- <b>Dynasty Menu (S):</b> View and manage all colonies.<br>" +
                "- <b>Mass Colonization:</b> Removes satellite colony limits.<br>" +
                "- <b>Automation:</b> Allows NPC colonies to manage themselves.<br><br>" +
                "<b>Evolution:</b><br>" +
                "Unlock <b>Synergies</b> to combine upgrades and <b>Assimilations</b> to absorb other ant species' traits." +
                "</div></html>";
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel(empireInfo);
        label.setFont(AssetStyles.FONT_NORMAL);
        label.setForeground(AssetStyles.FONT_COLOR);
        panel.add(label);
        return panel;
    }

    private JComponent createSpeciesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (Species s : GameConstants.getSpecies()) {
            JPanel entry = new JPanel(new BorderLayout(10, 0));
            entry.setBackground(AssetStyles.BACKGROUND_COLOR);
            entry.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, s.getName(), 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));

            JLabel icon = new JLabel(s.getIcon());
            icon.setBorder(new EmptyBorder(5, 5, 5, 5));
            entry.add(icon, BorderLayout.WEST);

            String baseUpgrades = s.getBaseUpgrades().stream().map(Upgrade::getName).collect(Collectors.joining(", "));
            String info = "<html><div style='width: 350px; font-family: sans-serif; font-size: 11pt;'><b>Scientific Name:</b> <i>" + s.getScientific() + "</i><br>" +
                          "<b>Base Traits:</b> " + baseUpgrades + "</div></html>";
            
            JLabel infoLabel = new JLabel(info);
            infoLabel.setFont(AssetStyles.FONT_NORMAL);
            infoLabel.setForeground(AssetStyles.FONT_COLOR);
            infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            
            entry.add(infoLabel, BorderLayout.CENTER);
            panel.add(entry);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JComponent createHotkeysPanel() {
        JPanel hotkeyPanel = new JPanel(new GridBagLayout());
        hotkeyPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        hotkeyPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 10, 5, 10);
        c.anchor = GridBagConstraints.WEST;

        class HotkeyRow {
            private int gridY = 0;
            void add(String key, String desc) {
                c.gridx = 0;
                c.gridy = gridY;
                JLabel keyLabel = new JLabel(key);
                keyLabel.setFont(AssetStyles.FONT_BOLD);
                keyLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
                hotkeyPanel.add(keyLabel, c);
                
                c.gridx = 1;
                JLabel descLabel = new JLabel(desc);
                descLabel.setFont(AssetStyles.FONT_NORMAL);
                descLabel.setForeground(AssetStyles.FONT_COLOR);
                hotkeyPanel.add(descLabel, c);
                gridY++;
            }
            void addSeparator() {
                c.gridx = 0;
                c.gridy = gridY;
                c.gridwidth = 2;
                c.fill = GridBagConstraints.HORIZONTAL;
                JSeparator sep = AssetStyles.createInternalSeparator();
                hotkeyPanel.add(sep, c);
                c.gridwidth = 1;
                c.fill = GridBagConstraints.NONE;
                gridY++;
            }
        }
        
        HotkeyRow row = new HotkeyRow();
        row.add("Spacebar", "Pause / Resume Game");
        row.add("+ / -", "Increase / Decrease Game Speed");
        row.add("A", "Toggle Overworld/Underworld View");
        row.add("ESC", "Open Game Menu / Close Dialogs");
        row.addSeparator();
        row.add("Q / W / E / R / T", "Manage Roles (Press again to close)");
        row.addSeparator();
        row.add("P", "Hatch Rates Menu (Toggles)");
        row.add("Y / U / I / O", "Upgrade Tabs (Research, Build, etc. Toggles)");
        row.add("Z", "Colony Operations Menu (Toggles)");
        row.add("S / A", "Dynasty Tabs (Overview, Trade. Toggles)");
        row.add("M", "World Map (Toggles)");
        row.add("X", "Statistics (Toggles)");

        return hotkeyPanel;
    }

    private JComponent createAntTypesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (AntType type : GameConstants.getAntTypes()) {
            if (type == GameConstants.TYPE_DEAD || type == GameConstants.TYPE_ZOMBIE) continue;

            JPanel entry = new JPanel(new BorderLayout(10, 0));
            entry.setBackground(AssetStyles.BACKGROUND_COLOR);
            entry.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, type.getName(), 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));

            JLabel icon = new JLabel(type.getIcon());
            icon.setBorder(new EmptyBorder(5, 5, 5, 5));
            entry.add(icon, BorderLayout.WEST);

            String desc = "";
            if (type == GameConstants.TYPE_EGG) {
                desc = "The first stage of ant life. Requires a Nurse to survive. Will hatch into a Larva.";
            } else if (type == GameConstants.TYPE_LARVA) {
                desc = "The second stage. Larvae must be fed by Nurses to grow. Will pupate into a Pupa.";
            } else if (type == GameConstants.TYPE_PUPA) {
                desc = "The final juvenile stage. Does not eat. Will hatch into an adult ant based on your Hatch Rates.";
            } else if (type == GameConstants.TYPE_WORKER) {
                desc = "The backbone of the colony. Can be assigned to roles like Forager, Farmer, Nurse, and Builder.";
            } else if (type == GameConstants.TYPE_SOLDIER) {
                desc = "A combat ant. Stronger than a Worker. Unlocks the Hunter role for gathering Protein.";
            } else if (type == GameConstants.TYPE_MAJOR) {
                desc = "A heavy combat ant, significantly stronger and tougher than a Soldier. Unlocks the Brute role.";
            } else if (type == GameConstants.TYPE_PRINCESS) {
                desc = "A winged reproductive. Can be assigned to the Breeder role to mate with a Drone and become a new Queen.";
            } else if (type == GameConstants.TYPE_DRONE) {
                desc = "A winged male reproductive. Its only purpose is to mate with a Princess, after which it dies.";
            } else if (type == GameConstants.TYPE_QUEEN) {
                desc = "The heart of the colony. Can be assigned to Lay Eggs or Research new technologies.";
            }
            
            JTextArea descArea = new JTextArea(desc);
            descArea.setFont(AssetStyles.FONT_NORMAL);
            descArea.setForeground(AssetStyles.FONT_COLOR);
            descArea.setWrapStyleWord(true);
            descArea.setLineWrap(true);
            descArea.setEditable(false);
            descArea.setFocusable(false);
            descArea.setBackground(panel.getBackground());
            descArea.setBorder(new EmptyBorder(5, 5, 5, 5));
            
            entry.add(descArea, BorderLayout.CENTER);
            panel.add(entry);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }
    
    private JPanel createSeasonListPanel(String title, List<Season> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (Season constant : constants) {
            JLabel item = new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private JPanel createWeatherListPanel(String title, List<Weather> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (Weather constant : constants) {
            JLabel item = new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private JPanel createTimeOfDayListPanel(String title, List<TimeOfDay> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (TimeOfDay constant : constants) {
            JLabel item = new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private JPanel createMoonPhaseListPanel(String title, List<MoonPhase> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (MoonPhase constant : constants) {
            JLabel item = new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private JPanel createTemperatureListPanel(String title, List<Temperature> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (Temperature constant : constants) {
            JLabel item = new JLabel(constant.getName() + " (Max " + constant.getMaxTemp() + "°C)", constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private JPanel createHumidityListPanel(String title, List<Humidity> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(AssetStyles.FONT_BOLD);
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        panel.add(titleLabel);
        
        for (Humidity constant : constants) {
            JLabel item = new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT);
            item.setFont(AssetStyles.FONT_NORMAL);
            item.setForeground(AssetStyles.FONT_COLOR);
            panel.add(item);
        }
        return panel;
    }

    private JComponent createWorldPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Resources
        JPanel resourcesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resourcesPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        resourcesPanel.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, "Resources", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));
        
        for (ResourceType res : GameConstants.getResources()) {
            JLabel resLabel = new JLabel(res.getName(), res.getIcon(), SwingConstants.LEFT);
            resLabel.setFont(AssetStyles.FONT_NORMAL);
            resLabel.setForeground(AssetStyles.FONT_COLOR);
            resourcesPanel.add(resLabel);
        }
        panel.add(resourcesPanel);

        // Biomes
        JPanel biomesPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        biomesPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        biomesPanel.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, "Biomes", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));
        
        for (Biome b : GameConstants.getBiomes()) {
            String bInfo = "<html><div style='width: 120px; font-family: sans-serif;'><b>" + b.getName() + "</b><br>" +
                           "Temp: " + b.getTemperature() + "°C<br>" +
                           "Humid: " + b.isIsHumid() + "/5<br>" +
                           "Plants: " + b.getPlantAbundance() + "x<br>" +
                           "Meat: " + b.getAnimalAbundance() + "x<br>" +
                           "Minerals: " + b.getMineralAbundance() + "x</div></html>";
            JLabel bLabel = new JLabel(bInfo, b.getIcon(), SwingConstants.LEFT);
            bLabel.setFont(AssetStyles.FONT_SMALL);
            bLabel.setForeground(AssetStyles.FONT_COLOR);
            biomesPanel.add(bLabel);
        }
        panel.add(biomesPanel);

        // World Info (Seasons, Weather, etc.)
        JPanel worldInfoPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        worldInfoPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        worldInfoPanel.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, "World Info", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));
        
        worldInfoPanel.add(createSeasonListPanel("Seasons", GameConstants.getSeasons()));
        worldInfoPanel.add(createWeatherListPanel("Weather", GameConstants.getWeathers()));
        worldInfoPanel.add(createTimeOfDayListPanel("Time of Day", GameConstants.getTimesOfDay()));
        worldInfoPanel.add(createMoonPhaseListPanel("Moon Phases", GameConstants.getMoonPhases()));
        worldInfoPanel.add(createTemperatureListPanel("Temperatures", GameConstants.getTemperature()));
        worldInfoPanel.add(createHumidityListPanel("Humidity", GameConstants.getHumidity()));

        panel.add(worldInfoPanel);
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JComponent createDictionaryPanel(List<Constant> items) {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JList<Constant> list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(AssetStyles.BACKGROUND_COLOR);
        list.setForeground(AssetStyles.FONT_COLOR);
        list.setSelectionBackground(AssetStyles.BACKGROUND_SECONDARY);
        list.setSelectionForeground(AssetStyles.FONT_COLOR_HEADER);
        list.setFont(AssetStyles.FONT_NORMAL);
        
        DefaultListModel<Constant> model = new DefaultListModel<>();
        
        List<Constant> sortedItems = new ArrayList<>(items);
        Collections.sort(sortedItems, (a, b) -> {
            String nameA = (a instanceof Upgrade) ? ((Upgrade) a).getFlavorName() : a.getName();
            String nameB = (b instanceof Upgrade) ? ((Upgrade) b).getFlavorName() : b.getName();
            return nameA.compareTo(nameB);
        });
        
        for (Constant item : sortedItems) {
            model.addElement(item);
        }
        
        list.setModel(model);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Constant) {
                    Constant c = (Constant) value;
                    setText((c instanceof Upgrade) ? ((Upgrade) c).getFlavorName() : c.getName());
                    setIcon(c.getIcon());
                }
                return this;
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(list);
        listScrollPane.setMinimumSize(new Dimension(200, 100));
        splitPane.setLeftComponent(listScrollPane);

        JEditorPane descriptionArea = new JEditorPane();
        descriptionArea.setEditorKit(new HTMLEditorKit());
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        descriptionArea.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JScrollPane textScrollPane = new JScrollPane(descriptionArea);
        textScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        splitPane.setRightComponent(textScrollPane);
        
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Constant selected = list.getSelectedValue();
                    if (selected == null) {
                        descriptionArea.setText("<html><div style='font-family: sans-serif; font-size: 11pt; color: black;'>Select an item from the list to see its description.</div></html>");
                        return;
                    }
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html><div style='font-family: sans-serif; font-size: 11pt; color: black; width: 250px;'>");
                    
                    if (selected instanceof Upgrade) {
                        Upgrade u = (Upgrade) selected;
                        sb.append("<b>").append(u.getFlavorName()).append("</b><br><br>");
                        sb.append(u.getDescription()).append("<br><br>");
                        sb.append("<b>Cost:</b> ").append(u.getCost()).append(" RP");
                        if (u.getRequirement() != null) {
                            sb.append("<br><b>Requires:</b> ").append(u.getRequirement().getFlavorName());
                        }
                    } else if (selected instanceof Building) {
                        Building b = (Building) selected;
                        sb.append("<b>").append(b.getName()).append("</b><br><br>");
                        sb.append(b.getDescription());
                        if (b.getBuildTime() > 0) {
                             sb.append("<br><br><b>Base Cost:</b><br>");
                             sb.append(b.getMineralCost()).append(" Minerals, ");
                             sb.append(b.getResinCost()).append(" Resin, ");
                             sb.append(b.getBuildTime()).append(" Hours");
                        }
                    } else if (selected instanceof Assimilation) {
                        Assimilation a = (Assimilation) selected;
                        sb.append("<b>").append(a.getName()).append("</b><br><br>");
                        sb.append(a.getDescription()).append("<br><br>");
                        sb.append("<b>Cost:</b> ").append(a.getCost()).append(" RP");
                    }
                    
                    sb.append("</div></html>");
                    descriptionArea.setText(sb.toString());
                    descriptionArea.setCaretPosition(0);
                }
            }
        });
        
        splitPane.setDividerLocation(250);
        return splitPane;
    }

    public static void showTutorialDialog(Component parent) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(window, "Welcome to Formic Dynasty!", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());

        JPanel cardPanel = new JPanel(new CardLayout());
        cardPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        // --- Page 1: Story ---
        String story = "<html><div style='width: 350px; font-family: sans-serif;'><p style='font-size: 12pt;'>" +
                "Nearly all other ants are extinct. You are an ant queen and have within you " +
                "all the genetic knowledge of every ant species. You must unlock it and " +
                "take over the world as the dominant species." +
                "</p></div></html>";
        JPanel page1 = new JPanel(new BorderLayout());
        page1.setBackground(AssetStyles.BACKGROUND_COLOR);
        page1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel storyLabel = new JLabel(story);
        storyLabel.setForeground(AssetStyles.FONT_COLOR);
        page1.add(storyLabel, BorderLayout.CENTER);

        // --- Page 2: Game Info ---
        String gameInfo = "<html><div style='width: 350px; font-family: sans-serif;'><p style='font-size: 11pt;'>" +
                "<b>Basic Tips:</b><br><br>" +
                "Your main food is <b>Fungi</b> (Mushrooms), which ants will eat daily.<br><br>" +
                "Assign <b>Workers</b> (Q) to <b>Forager</b> roles to gather Plants and Water. Without them your ants will die of thirst!<br><br>" +
                "Assign <b>Farmers</b> (Q) to convert gathered Plants into Fungi. One Farmer can generally handle 6 Foragers.<br><br>" +
                "Assign <b>Nurses</b> (Q) to care for your <b>Eggs, Larvae, and Pupae</b>. Without enough nurses, your young will perish from neglect!<br><br>" +
                "Press <b>A</b> to toggle your view between the <b>Underworld</b>  and the <b>Overworld</b> .<br><br>" +
                "<i>QoL Tip: If you assign a new role but have no unassigned ants, the game automatically pulls from your default workforce (like Foragers).</i>" +
                "</p></div></html>";
        JPanel page2 = new JPanel(new BorderLayout());
        page2.setBackground(AssetStyles.BACKGROUND_COLOR);
        page2.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel infoLabel = new JLabel(gameInfo);
        infoLabel.setForeground(AssetStyles.FONT_COLOR);
        page2.add(infoLabel, BorderLayout.CENTER);
        
        // --- Page 3: Threats & Mechanics ---
        String threatInfo = "<html><div style='width: 350px; font-family: sans-serif;'><p style='font-size: 11pt;'>" +
                "<b>Colony Threats:</b><br><br>" +
                "<b>Contamination:</b> Ants naturally die of old age. If bodies pile up, disease will spread and kill your colony. Assign <b>Gravers</b> to clear the dead.<br><br>" +
                "<b>Parasites:</b> Unseen pests will leech your Fungi reserves. Assign <b>Police</b> to detect and eliminate them.<br><br>" +
                "<b>Depletion:</b> Resource nodes don't last forever. If your foragers run out of plants or water, assign <b>Scouts</b> to find new resources nearby." +
                "</p></div></html>";
        JPanel page3 = new JPanel(new BorderLayout());
        page3.setBackground(AssetStyles.BACKGROUND_COLOR);
        page3.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel threatLabel = new JLabel(threatInfo);
        threatLabel.setForeground(AssetStyles.FONT_COLOR);
        page3.add(threatLabel, BorderLayout.CENTER);

        // --- Page 4: Dynasty Management ---
        String empireInfo = "<html><div style='width: 350px; font-family: sans-serif;'><p style='font-size: 11pt;'>" +
                "<b>Dynasty Management:</b><br><br>" +
                "As your colony thrives, you will unlock <b>Breeder</b> Princesses. These allow you to establish satellite colonies via the <b>World Map (I)</b> .<br>" +
                "<i>Tip: Keep an eye on the sky! Rare Solar or Lunar Eclipses will trigger spontaneous, free Nuptial Flights!</i><br><br>" +
                "Founding multiple colonies unlocks the <b>Dynasty Menu (S)</b>. Reaching certain milestones will allow you to construct <b>Trade Routes</b>, remove spreading limits, and even <b>Automate</b> your expanding dynasty!" +
                "</p></div></html>";
        JPanel page4 = new JPanel(new BorderLayout());
        page4.setBackground(AssetStyles.BACKGROUND_COLOR);
        page4.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel empireLabel = new JLabel(empireInfo);
        empireLabel.setForeground(AssetStyles.FONT_COLOR);
        page4.add(empireLabel, BorderLayout.CENTER);

        // --- Page 5: Hotkeys ---
        JPanel hotkeyPanel = new JPanel(new GridLayout(0, 2, 10, 5)); 
        hotkeyPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        hotkeyPanel.setBorder(BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, "Hotkeys",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                AssetStyles.FONT_BOLD, AssetStyles.FONT_COLOR_HEADER));

        String[] keys = {
            "Pause/Play:", "Spacebar",
            "Game Menu:", "ESC",
            "Toggle View:", "A",
            "Roles (Q-T):", "Toggle Menus",
            "Upgrades (Y-O):", "Toggle Menus",
            "Hatch Rates:", "P (Toggle)",
            "Dynasty (S/A):", "Toggle Menus",
            "World Map:", "M (Toggle)",
            "Stats:", "X (Toggle)"
        };
        
        for (int i = 0; i < keys.length; i+=2) {
            JLabel k = new JLabel(keys[i]);
            k.setFont(AssetStyles.FONT_BOLD);
            k.setForeground(AssetStyles.FONT_COLOR_HEADER);
            hotkeyPanel.add(k);
            
            JLabel v = new JLabel(keys[i+1]);
            v.setFont(AssetStyles.FONT_NORMAL);
            v.setForeground(AssetStyles.FONT_COLOR);
            hotkeyPanel.add(v);
        }
        
        hotkeyPanel.add(AssetStyles.createInternalSeparator()); hotkeyPanel.add(AssetStyles.createInternalSeparator());
        
        JPanel page5 = new JPanel(new BorderLayout());
        page5.setBackground(AssetStyles.BACKGROUND_COLOR);
        page5.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        page5.add(hotkeyPanel, BorderLayout.CENTER);

        cardPanel.add(page1, "0");
        cardPanel.add(page2, "1");
        cardPanel.add(page3, "2");
        cardPanel.add(page4, "3");
        cardPanel.add(page5, "4");

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        JButton skipBtn = new JButton("Skip Tutorial");
        JButton backBtn = new JButton("< Back");
        JButton nextBtn = new JButton("Next >");
        JButton finishBtn = new JButton("Finish");
        
        for(JButton btn : new JButton[]{skipBtn, backBtn, nextBtn, finishBtn}) {
            btn.setFont(AssetStyles.FONT_BOLD);
            btn.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            btn.setForeground(AssetStyles.FONT_COLOR);
        }

        skipBtn.addActionListener(e -> dialog.dispose());
        finishBtn.addActionListener(e -> dialog.dispose());

        final int[] currentPage = {0};
        final int MAX_PAGES = 5;

        Runnable updateButtons = () -> {
            backBtn.setEnabled(currentPage[0] > 0);
            if (currentPage[0] == MAX_PAGES - 1) {
                nextBtn.setVisible(false);
                finishBtn.setVisible(true);
            } else {
                nextBtn.setVisible(true);
                finishBtn.setVisible(false);
            }
            CardLayout cl = (CardLayout) cardPanel.getLayout();
            cl.show(cardPanel, String.valueOf(currentPage[0]));
        };

        backBtn.addActionListener(e -> {
            if (currentPage[0] > 0) {
                currentPage[0]--;
                updateButtons.run();
            }
        });

        nextBtn.addActionListener(e -> {
            if (currentPage[0] < MAX_PAGES - 1) {
                currentPage[0]++;
                updateButtons.run();
            }
        });

        buttonPanel.add(skipBtn);
        buttonPanel.add(Box.createHorizontalStrut(20)); 
        buttonPanel.add(backBtn);
        buttonPanel.add(nextBtn);
        buttonPanel.add(finishBtn);

        updateButtons.run();

        dialog.add(cardPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
