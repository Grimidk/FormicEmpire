package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.world.MoonPhase;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HelpPanel extends JPanel {
    private final MainFrame frame;

    public HelpPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());

        JTabbedPane mainTabs = new JTabbedPane();

        // Add tabs
        mainTabs.addTab("Welcome", createWelcomePanel());
        mainTabs.addTab("Getting Started", createGettingStartedPanel());
        mainTabs.addTab("Dynasty Management", createEmpireManagementPanel());
        mainTabs.addTab("Hotkeys", createHotkeysPanel());
        mainTabs.addTab("Ant Types", createAntTypesPanel());
        mainTabs.addTab("Upgrades", createDictionaryPanel(GameUnlocks.getUpgrades(), null));
        mainTabs.addTab("Buildings", createDictionaryPanel(null, GameUnlocks.getBuildings()));
        mainTabs.addTab("World", createWorldPanel());

        setupTabPaneNavigation(mainTabs);

        add(mainTabs, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back = new JButton("Back");
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
        String story = "<html><p style='width: 450px; font-size: 14pt;'>" +
                "Nearly all other ants are extinct. You are an ant queen and have within you " +
                "all the genetic knowledge of every ant species. You must unlock it and " +
                "take over the world as the dominant species. " +
                "<br><br>" +
                "Build up your colony and begin to spread while fighting other ant colonies, " +
                "you will need to adapt to new environments by absorbing and researching their abilities." +
                "</p></html>";
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel(story));
        return panel;
    }

    private JComponent createGettingStartedPanel() {
        String gameInfo = "<html><div style='width: 450px; font-size: 11pt;'>" +
                "<b>Basic Survival:</b><br>" +
                "Your ants need <b>Fungi</b> (Mushrooms) to eat and <b>Water</b> to drink every day. Without them, your colony will starve or die of dehydration.<br>" +
                "- <b>Foragers</b> (Q) gather Plants and Water.<br>" +
                "- <b>Hunters</b> (W) gather Protein (Meat).<br>" +
                "- <b>Farmers</b> (Q) convert Plants and Protein into Fungi. At base level, one Farmer can process the resources gathered by 6 Foragers.<br>" +
                "- <b>Nurses</b> (Q) care for your brood (Eggs, Larvae, Pupae). Neglected brood will perish.<br><br>" +
                "<b>Watching Your Colony:</b><br>" +
                "Press <b>A</b> to toggle your view between the <b>Underworld</b>  (inside the nest rooms) and the <b>Overworld</b>  (outside the nest where gathering happens).<br><br>" +
                "<b>Colony Management:</b><br>" +
                "Use the <b>Hatch Rates (P)</b> menu to control the ratio of ants born. " +
                "<b>Researchers</b> generate Research Points (RP) to buy powerful upgrades in the <b>Research Menu (Y)</b>. " +
                "<b>Builders</b> use Minerals and Resin to construct facilities via the <b>Build Menu (U)</b>.<br>" +
                "<i>Tip: When assigning roles, if you increase a role's count beyond your available unassigned ants, the system automatically pulls ants from your default workforce (like Foragers or Hunters) to cover the difference!</i><br><br>" +
                "<b>Advanced Threats:</b><br>" +
                "- <b>Contamination:</b> Dead ants must be cleared by <b>Gravers</b>, otherwise disease will spread.<br>" +
                "- <b>Parasites:</b> These pests secretly drain your food. Assign <b>Police</b> to eliminate them.<br>" +
                "- <b>Depletion:</b> If local resources run dry, assign <b>Scouts</b> to discover new resource nodes." +
                "</div></html>";
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel(gameInfo));
        return panel;
    }

    private JComponent createEmpireManagementPanel() {
        String empireInfo = "<html><div style='width: 450px; font-size: 11pt;'>" +
                "<b>Expanding Your Dynasty:</b><br>" +
                "Once you unlock the <b>Breeder</b> role for Princesses, you can perform Nuptial Flights to spread to adjacent lands via the <b>World Map (I)</b> .<br>" +
                "<i>Tip: Keep an eye on the sky! Rare Solar or Lunar Eclipses will trigger spontaneous, free Nuptial Flights for your colony.</i><br><br>" +
                "<b>Dynasty Milestones:</b><br>" +
                "- <b>2 Colonies:</b> Unlocks the <b>Dynasty Menu (S)</b> to view and manage your entire dynasty.<br>" +
                "- <b>3 Colonies:</b> Unlocks <b>Trade Routes</b>, allowing you to transport resources between your nests.<br>" +
                "- <b>5 Colonies:</b> Unlocks <b>Mass Colonization</b>, removing the limit on establishing satellite colonies.<br>" +
                "- <b>7 Colonies:</b> Unlocks <b>Automation</b>. You can set NPC colonies to manage their own roles and building queues automatically.<br><br>" +
                "<b>Evolution:</b><br>" +
                "Through the Research panel, you can unlock <b>Synergies</b> to combine upgrades, and <b>Assimilations</b> to alter the genetic code of your species entirely." +
                "</div></html>";
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel(empireInfo));
        return panel;
    }

    private JComponent createHotkeysPanel() {
        JPanel hotkeyPanel = new JPanel(new GridBagLayout());
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
                keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD));
                hotkeyPanel.add(keyLabel, c);
                
                c.gridx = 1;
                hotkeyPanel.add(new JLabel(desc), c);
                gridY++;
            }
            void addSeparator() {
                c.gridx = 0;
                c.gridy = gridY;
                c.gridwidth = 2;
                c.fill = GridBagConstraints.HORIZONTAL;
                hotkeyPanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);
                c.gridwidth = 1;
                c.fill = GridBagConstraints.NONE;
                gridY++;
            }
        }
        
        HotkeyRow row = new HotkeyRow();
        row.add("Spacebar", "Pause / Resume Game");
        row.add("+ (Add)", "Increase Game Speed");
        row.add("- (Subtract)", "Decrease Game Speed");
        row.add("A", "Toggle Overworld/Underworld View");
        row.add("ESC", "Open Game Menu / Close Dialogs");
        row.addSeparator();
        row.add("Q", "Manage Worker Roles");
        row.add("W", "Manage Soldier Roles");
        row.add("E", "Manage Major Roles");
        row.add("R", "Manage Princess Roles");
        row.add("T", "Manage Queen Roles");
        row.addSeparator();
        row.add("P", "Hatch Rates Menu");
        row.add("Y", "Research Menu");
        row.add("U", "Build Menu");
        row.add("Z", "Colony Operations Menu");
        row.add("S", "Dynasty Menu");
        row.add("I", "World Map");

        return hotkeyPanel;
    }

    private JComponent createAntTypesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (AntType type : GameConstants.getAntTypes()) {
            if (type == GameConstants.TYPE_DEAD || type == GameConstants.TYPE_ZOMBIE) continue;

            JPanel entry = new JPanel(new BorderLayout(10, 0));
            entry.setBorder(BorderFactory.createTitledBorder(type.getName()));

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

        return new JScrollPane(panel);
    }
    
    private JPanel createSeasonListPanel(String title, List<Season> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("<html><b>" + title + "</b></html>"));
        for (Season constant : constants) {
            panel.add(new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT));
        }
        return panel;
    }

    private JPanel createWeatherListPanel(String title, List<Weather> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("<html><b>" + title + "</b></html>"));
        for (Weather constant : constants) {
            panel.add(new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT));
        }
        return panel;
    }

    private JPanel createTimeOfDayListPanel(String title, List<TimeOfDay> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("<html><b>" + title + "</b></html>"));
        for (TimeOfDay constant : constants) {
            panel.add(new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT));
        }
        return panel;
    }

    private JPanel createMoonPhaseListPanel(String title, List<MoonPhase> constants) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("<html><b>" + title + "</b></html>"));
        for (MoonPhase constant : constants) {
            panel.add(new JLabel(constant.getName(), constant.getIcon(), SwingConstants.LEFT));
        }
        return panel;
    }

    private JComponent createWorldPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Resources
        JPanel resourcesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resourcesPanel.setBorder(BorderFactory.createTitledBorder("Resources"));
        for (ResourceType res : GameConstants.getResources()) {
            resourcesPanel.add(new JLabel(res.getName(), res.getIcon(), SwingConstants.LEFT));
        }
        panel.add(resourcesPanel);

        // World Info (Seasons, Weather, etc.)
        JPanel worldPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        worldPanel.setBorder(BorderFactory.createTitledBorder("World Info"));
        
        worldPanel.add(createSeasonListPanel("Seasons", GameConstants.getSeasons()));
        worldPanel.add(createWeatherListPanel("Weather", GameConstants.getWeathers()));
        worldPanel.add(createTimeOfDayListPanel("Time of Day", GameConstants.getTimesOfDay()));
        worldPanel.add(createMoonPhaseListPanel("Moon Phases", GameConstants.getMoonPhases()));

        panel.add(worldPanel);
        return new JScrollPane(panel);
    }

    private JComponent createDictionaryPanel(List<Upgrade> upgrades, List<Building> buildings) {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        JList<String> list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        DefaultListModel<String> model = new DefaultListModel<>();
        
        List<String> names = new ArrayList<>();
        if (upgrades != null) {
            for (Upgrade item : upgrades) {
                names.add(item.getFlavorName());
            }
        } else if (buildings != null) {
            for (Building item : buildings) {
                names.add(item.getName());
            }
        }
        Collections.sort(names);
        for (String name : names) {
            model.addElement(name);
        }
        
        list.setModel(model);
        
        JScrollPane listScrollPane = new JScrollPane(list);
        listScrollPane.setMinimumSize(new Dimension(200, 100));
        splitPane.setLeftComponent(listScrollPane);

        JTextArea descriptionArea = new JTextArea("Select an item from the list to see its description.");
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane textScrollPane = new JScrollPane(descriptionArea);
        splitPane.setRightComponent(textScrollPane);
        
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    
                    String selectedName = list.getSelectedValue();
                    if (selectedName == null) {
                        descriptionArea.setText("Select an item from the list to see its description.");
                        return;
                    }
                    
                    if (upgrades != null) {
                        for (Upgrade up : upgrades) {
                            if (up.getFlavorName().equals(selectedName)) {
                                descriptionArea.setText(up.getDescription());
                                descriptionArea.setCaretPosition(0);
                                return;
                            }
                        }
                    } else if (buildings != null) {
                        for (Building b : buildings) {
                            if (b.getName().equals(selectedName)) {
                                String cost = "";
                                if (b.getBuildTime() > 0) {
                                     cost = String.format("\n\nBase Cost:\n%d Minerals, %d Resin, %d Hours",
                                        b.getMineralCost(), b.getResinCost(), b.getBuildTime());
                                }
                                descriptionArea.setText(b.getDescription() + cost);
                                descriptionArea.setCaretPosition(0);
                                return;
                            }
                        }
                    }
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
        
        // --- Page 1: Story ---
        String story = "<html><p style='width: 350px; font-size: 12pt;'>" +
                "Nearly all other ants are extinct. You are an ant queen and have within you " +
                "all the genetic knowledge of every ant species. You must unlock it and " +
                "take over the world as the dominant species." +
                "</p></html>";
        JPanel page1 = new JPanel(new BorderLayout());
        page1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        page1.add(new JLabel(story), BorderLayout.CENTER);

        // --- Page 2: Game Info ---
        String gameInfo = "<html><p style='width: 350px; font-size: 11pt;'>" +
                "<b>Basic Tips:</b><br><br>" +
                "Your main food is <b>Fungi</b> (Mushrooms), which ants will eat daily.<br><br>" +
                "Assign <b>Workers</b> (Q) to <b>Forager</b> roles to gather Plants and Water. Without them your ants will die of thirst!<br><br>" +
                "Assign <b>Farmers</b> (Q) to convert gathered Plants into Fungi. One Farmer can generally handle 6 Foragers.<br><br>" +
                "Assign <b>Nurses</b> (Q) to care for your <b>Eggs, Larvae, and Pupae</b>. Without enough nurses, your young will perish from neglect!<br><br>" +
                "Press <b>A</b> to toggle your view between the <b>Underworld</b>  and the <b>Overworld</b> .<br><br>" +
                "<i>QoL Tip: If you assign a new role but have no unassigned ants, the game automatically pulls from your default workforce (like Foragers).</i>" +
                "</p></html>";
        JPanel page2 = new JPanel(new BorderLayout());
        page2.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        page2.add(new JLabel(gameInfo), BorderLayout.CENTER);
        
        // --- Page 3: Threats & Mechanics ---
        String threatInfo = "<html><p style='width: 350px; font-size: 11pt;'>" +
                "<b>Colony Threats:</b><br><br>" +
                "<b>Contamination:</b> Ants naturally die of old age. If bodies pile up, disease will spread and kill your colony. Assign <b>Gravers</b> to clear the dead.<br><br>" +
                "<b>Parasites:</b> Unseen pests will leech your Fungi reserves. Assign <b>Police</b> to detect and eliminate them.<br><br>" +
                "<b>Depletion:</b> Resource nodes don't last forever. If your foragers run out of plants or water, assign <b>Scouts</b> to find new resources nearby." +
                "</p></html>";
        JPanel page3 = new JPanel(new BorderLayout());
        page3.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        page3.add(new JLabel(threatInfo), BorderLayout.CENTER);

        // --- Page 4: Dynasty Management ---
        String empireInfo = "<html><p style='width: 350px; font-size: 11pt;'>" +
                "<b>Dynasty Management:</b><br><br>" +
                "As your colony thrives, you will unlock <b>Breeder</b> Princesses. These allow you to establish satellite colonies via the <b>World Map (I)</b> .<br>" +
                "<i>Tip: Keep an eye on the sky! Rare Solar or Lunar Eclipses will trigger spontaneous, free Nuptial Flights!</i><br><br>" +
                "Founding multiple colonies unlocks the <b>Dynasty Menu (S)</b>. Reaching certain milestones will allow you to construct <b>Trade Routes</b>, remove spreading limits, and even <b>Automate</b> your expanding dynasty!" +
                "</p></html>";
        JPanel page4 = new JPanel(new BorderLayout());
        page4.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        page4.add(new JLabel(empireInfo), BorderLayout.CENTER);

        // --- Page 5: Hotkeys ---
        JPanel hotkeyPanel = new JPanel(new GridLayout(0, 2, 10, 5)); 
        hotkeyPanel.setBorder(BorderFactory.createTitledBorder("Hotkeys"));
        hotkeyPanel.add(new JLabel("Pause/Play:")); hotkeyPanel.add(new JLabel("Spacebar"));
        hotkeyPanel.add(new JLabel("Game Menu:")); hotkeyPanel.add(new JLabel("ESC"));
        hotkeyPanel.add(new JLabel("Toggle View:")); hotkeyPanel.add(new JLabel("A"));
        hotkeyPanel.add(new JSeparator(SwingConstants.HORIZONTAL)); hotkeyPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        hotkeyPanel.add(new JLabel("Worker Roles:")); hotkeyPanel.add(new JLabel("Q"));
        hotkeyPanel.add(new JLabel("Soldier Roles:")); hotkeyPanel.add(new JLabel("W"));
        hotkeyPanel.add(new JLabel("Major Roles:")); hotkeyPanel.add(new JLabel("E"));
        hotkeyPanel.add(new JLabel("Princess Roles:")); hotkeyPanel.add(new JLabel("R"));
        hotkeyPanel.add(new JLabel("Queen Roles:")); hotkeyPanel.add(new JLabel("T"));
        hotkeyPanel.add(new JSeparator(SwingConstants.HORIZONTAL)); hotkeyPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        hotkeyPanel.add(new JLabel("Build Menu:")); hotkeyPanel.add(new JLabel("U"));
        hotkeyPanel.add(new JLabel("Dynasty Menu:")); hotkeyPanel.add(new JLabel("S"));
        hotkeyPanel.add(new JLabel("World Map:")); hotkeyPanel.add(new JLabel("I"));
        
        JPanel page5 = new JPanel(new BorderLayout());
        page5.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        page5.add(hotkeyPanel, BorderLayout.CENTER);

        cardPanel.add(page1, "0");
        cardPanel.add(page2, "1");
        cardPanel.add(page3, "2");
        cardPanel.add(page4, "3");
        cardPanel.add(page5, "4");

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton skipBtn = new JButton("Skip Tutorial");
        JButton backBtn = new JButton("< Back");
        JButton nextBtn = new JButton("Next >");
        JButton finishBtn = new JButton("Finish");

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