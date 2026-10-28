package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.dynasty.DynastyTitle;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.MiniMapPanel;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.EdgeTriggeredKeyBindings;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiDialogUtils;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class MultiMapGalleryDialog extends JDialog {
    private static final int MAP_COUNT = 12;
    private final Engine engine;
    private final List<MiniMapPanel> mapPanels = new ArrayList<>();
    private final List<JLabel> statLabels = new ArrayList<>();
    private final List<JButton> copyButtons = new ArrayList<>();
    private final World[] generatedWorlds = new World[MAP_COUNT];
    private final JSpinner continentSpinner;
    private final JSpinner islandSpinner;
    private final JSpinner landShareSpinner;
    private final JSpinner icecapSpinner;
    private final JCheckBox randomizeCheck;
    private final JButton resetParamsButton;
    private final JButton regenerateButton;
    private final JButton closeButton;
    private final JLabel statusLabel;
    private SwingWorker<Void, World> currentWorker;
    private int generatedCount = 0;

    public MultiMapGalleryDialog(JFrame owner) {
        this((Window) owner, null);
    }

    public MultiMapGalleryDialog(Window owner) {
        this(owner, null);
    }

    public MultiMapGalleryDialog(Window owner, Engine engine) {
        super(owner, LanguageStrings.get(LanguageStrings.MULTI_MAP_TITLE), ModalityType.DOCUMENT_MODAL);
        this.engine = engine;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        if (owner instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        } else if (owner != null) {
            Window root = SwingUtilities.getWindowAncestor(owner);
            if (root instanceof MainFrame mf) {
                mf.applyGameCursors(this);
            }
        }

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(AssetStyles.UI_BG_PRIMARY);
        contentPane.setBorder(BorderFactory.createLineBorder(AssetStyles.BORDER_COLOR, AssetStyles.BORDER_THICKNESS_EXTERNAL));

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        northPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        northPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, AssetStyles.BORDER_THICKNESS_INTERNAL, 0, AssetStyles.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        JLabel contLabel = new JLabel(LanguageStrings.get(LanguageStrings.MULTI_MAP_PARAM_CONTINENTS));
        contLabel.setFont(AssetStyles.FONT_NORMAL);
        contLabel.setForeground(AssetStyles.FONT_COLOR);
        continentSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 8, 1));
        continentSpinner.setPreferredSize(AssetStyles.preferredSpinnerSize(70));
        AssetStyles.styleSpinner(continentSpinner);
        continentSpinner.setToolTipText("0 = Auto, 1 - 8");

        JLabel islandLabel = new JLabel(LanguageStrings.get(LanguageStrings.MULTI_MAP_PARAM_ISLANDS));
        islandLabel.setFont(AssetStyles.FONT_NORMAL);
        islandLabel.setForeground(AssetStyles.FONT_COLOR);
        islandSpinner = new JSpinner(new SpinnerNumberModel(-1, -1, 50, 1));
        islandSpinner.setPreferredSize(AssetStyles.preferredSpinnerSize(70));
        AssetStyles.styleSpinner(islandSpinner);
        islandSpinner.setToolTipText("-1 = Auto, 0 - 50");

        JLabel landLabel = new JLabel(LanguageStrings.get(LanguageStrings.MULTI_MAP_PARAM_LAND_SHARE));
        landLabel.setFont(AssetStyles.FONT_NORMAL);
        landLabel.setForeground(AssetStyles.FONT_COLOR);
        landShareSpinner = new JSpinner(new SpinnerNumberModel(28, 10, 60, 2));
        landShareSpinner.setPreferredSize(AssetStyles.preferredSpinnerSize(70));
        AssetStyles.styleSpinner(landShareSpinner);
        landShareSpinner.setToolTipText("10% - 60%");

        JLabel icecapLabel = new JLabel(LanguageStrings.get(LanguageStrings.MULTI_MAP_PARAM_ICECAP));
        icecapLabel.setFont(AssetStyles.FONT_NORMAL);
        icecapLabel.setForeground(AssetStyles.FONT_COLOR);
        icecapSpinner = new JSpinner(new SpinnerNumberModel(18, 0, 45, 1));
        icecapSpinner.setPreferredSize(AssetStyles.preferredSpinnerSize(70));
        AssetStyles.styleSpinner(icecapSpinner);
        icecapSpinner.setToolTipText("0% = None, 18% = Default");

        randomizeCheck = new JCheckBox(LanguageStrings.get(LanguageStrings.MULTI_MAP_PARAM_RANDOMIZE));
        randomizeCheck.setFont(AssetStyles.FONT_NORMAL);
        randomizeCheck.setForeground(AssetStyles.FONT_COLOR);
        AssetStyles.styleCheckBox(randomizeCheck);
        randomizeCheck.setToolTipText("Randomize parameters uniquely for each generated map");
        randomizeCheck.addActionListener(e -> updateParamControlsState());

        resetParamsButton = new JButton(LanguageStrings.get(LanguageStrings.MULTI_MAP_PARAM_RESET));
        AssetStyles.styleCompactButton(resetParamsButton);
        resetParamsButton.addActionListener(e -> resetParameters());

        northPanel.add(contLabel);
        northPanel.add(continentSpinner);
        northPanel.add(islandLabel);
        northPanel.add(islandSpinner);
        northPanel.add(landLabel);
        northPanel.add(landShareSpinner);
        northPanel.add(icecapLabel);
        northPanel.add(icecapSpinner);
        northPanel.add(randomizeCheck);
        northPanel.add(resetParamsButton);
        contentPane.add(northPanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        gridPanel.setBackground(AssetStyles.UI_BG_PRIMARY);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < MAP_COUNT; i++) {
            final int cardIndex = i;
            JPanel card = new JPanel(new BorderLayout(0, 4));
            card.setBackground(AssetStyles.UI_BG_SECONDARY);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AssetStyles.BORDER_COLOR, AssetStyles.BORDER_THICKNESS_INTERNAL),
                    BorderFactory.createEmptyBorder(6, 6, 6, 6)));

            MiniMapPanel miniMap = new MiniMapPanel(null, (World) null, () -> openFullMap(cardIndex));
            miniMap.setPreferredSize(new Dimension(240, 150));
            miniMap.setToolTipText(LanguageStrings.get(LanguageStrings.MULTI_MAP_INSPECT_MAP));
            mapPanels.add(miniMap);
            card.add(miniMap, BorderLayout.CENTER);

            MouseAdapter openMapClick = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        openFullMap(cardIndex);
                    }
                }
            };
            card.addMouseListener(openMapClick);
            card.setToolTipText(LanguageStrings.get(LanguageStrings.MULTI_MAP_INSPECT_MAP));
            AssetStyles.markClickable(card);

            JPanel cardSouth = new JPanel(new BorderLayout(0, 4));
            cardSouth.setOpaque(false);

            JLabel statLabel = new JLabel("Generating...", SwingConstants.CENTER);
            statLabel.setFont(AssetStyles.FONT_NORMAL);
            statLabel.setForeground(AssetStyles.FONT_COLOR);
            statLabel.setToolTipText(LanguageStrings.get(LanguageStrings.MULTI_MAP_INSPECT_MAP));
            statLabel.addMouseListener(openMapClick);
            AssetStyles.markClickable(statLabel);
            statLabels.add(statLabel);
            cardSouth.add(statLabel, BorderLayout.NORTH);

            JButton copyBtn = new JButton(LanguageStrings.get(LanguageStrings.MULTI_MAP_COPY_BUTTON));
            AssetStyles.styleCompactButton(copyBtn);
            copyBtn.setEnabled(false);
            copyBtn.addActionListener(e -> copyMapToNewSave(cardIndex));
            copyButtons.add(copyBtn);
            cardSouth.add(copyBtn, BorderLayout.SOUTH);

            card.add(cardSouth, BorderLayout.SOUTH);
            gridPanel.add(card);
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(AssetStyles.UI_BG_PRIMARY);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        southPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(AssetStyles.BORDER_THICKNESS_INTERNAL, 0, 0, 0, AssetStyles.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionPanel.setOpaque(false);

        regenerateButton = new JButton(LanguageStrings.get(LanguageStrings.MULTI_MAP_REGENERATE));
        AssetStyles.styleButton(regenerateButton);
        regenerateButton.setPreferredSize(new Dimension(Math.max(130, AssetStyles.MIN_CONTROL_HIT_SIZE * 5), AssetStyles.MIN_CONTROL_HIT_SIZE + 8));
        regenerateButton.addActionListener(e -> generateBatch());
        actionPanel.add(regenerateButton);

        statusLabel = new JLabel("");
        statusLabel.setFont(AssetStyles.FONT_NORMAL);
        statusLabel.setForeground(AssetStyles.FONT_COLOR);
        actionPanel.add(statusLabel);

        closeButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        AssetStyles.styleButton(closeButton);
        closeButton.setPreferredSize(new Dimension(Math.max(100, AssetStyles.MIN_CONTROL_HIT_SIZE * 4), AssetStyles.MIN_CONTROL_HIT_SIZE + 8));
        closeButton.addActionListener(e -> closeDialog());

        southPanel.add(actionPanel, BorderLayout.WEST);
        southPanel.add(closeButton, BorderLayout.EAST);
        contentPane.add(southPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDialog();
            }
        });

        EdgeTriggeredKeyBindings.bind(
                getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
                getRootPane().getActionMap(),
                KeyEvent.VK_ESCAPE,
                "escapeClose",
                this::closeDialog);

        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(maxBounds);
        setPreferredSize(maxBounds.getSize());
        UiDialogUtils.prepareDialog(this, owner);
        setBounds(maxBounds);

        generateBatch();
    }

    private void resetParameters() {
        randomizeCheck.setSelected(false);
        continentSpinner.setValue(0);
        islandSpinner.setValue(-1);
        landShareSpinner.setValue(28);
        icecapSpinner.setValue(18);
        updateParamControlsState();
    }

    private void updateParamControlsState() {
        boolean random = randomizeCheck.isSelected();
        continentSpinner.setEnabled(!random);
        islandSpinner.setEnabled(!random);
        landShareSpinner.setEnabled(!random);
        icecapSpinner.setEnabled(!random);
        resetParamsButton.setEnabled(!random);
    }

    private void closeDialog() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
        dispose();
    }

    public void refreshDialog() {
        AssetStyles.styleButton(regenerateButton);
        AssetStyles.styleButton(closeButton);
        AssetStyles.styleCompactButton(resetParamsButton);
        AssetStyles.styleCheckBox(randomizeCheck);
        AssetStyles.styleSpinner(continentSpinner);
        AssetStyles.styleSpinner(islandSpinner);
        AssetStyles.styleSpinner(landShareSpinner);
        AssetStyles.styleSpinner(icecapSpinner);
        for (JButton b : copyButtons) {
            AssetStyles.styleCompactButton(b);
        }
        AssetStyles.applyThemeToContainer(this);
        for (MiniMapPanel panel : mapPanels) {
            panel.refreshTheme();
        }
    }

    private void openFullMap(int index) {
        if (index < 0 || index >= generatedWorlds.length) {
            return;
        }
        World targetWorld = generatedWorlds[index];
        if (targetWorld == null) {
            return;
        }
        MapDialog dialog = new MapDialog(this, null, targetWorld, null, null);
        dialog.showDialog();
    }

    private SaveManager resolveSaveManager() {
        if (engine != null && engine.getSaveManager() != null) {
            return engine.getSaveManager();
        }
        if (getOwner() instanceof MainFrame mf && mf.getEngine() != null) {
            return mf.getEngine().getSaveManager();
        }
        return null;
    }

    private record DynastyCustomization(String baseName, DynastyTitle title) {}

    private DynastyCustomization promptDynastyCreation(String defaultName, DynastyTitle defaultTitle) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AssetStyles.BACKGROUND_COLOR);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 12);
        c.anchor = GridBagConstraints.WEST;

        JLabel nameLabel = new JLabel(LanguageStrings.get(LanguageStrings.SAVE_ENTER_NAME));
        nameLabel.setFont(AssetStyles.FONT_NORMAL);
        nameLabel.setForeground(AssetStyles.FONT_COLOR);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        form.add(nameLabel, c);

        JTextField nameField = new JTextField(defaultName);
        Dimension fieldSize = new Dimension(220, AssetStyles.MIN_CONTROL_HIT_SIZE + 6);
        nameField.setPreferredSize(fieldSize);
        nameField.setMinimumSize(fieldSize);
        c.gridx = 1;
        c.weightx = 1.0;
        form.add(nameField, c);

        JLabel titleLabel = new JLabel(LanguageStrings.get(LanguageStrings.SAVE_ENTER_DYNASTY_TITLE));
        titleLabel.setFont(AssetStyles.FONT_NORMAL);
        titleLabel.setForeground(AssetStyles.FONT_COLOR);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        form.add(titleLabel, c);

        JComboBox<DynastyTitle> titleCombo = new JComboBox<>(
                GameConstants.getDynastyTitles().toArray(new DynastyTitle[0]));
        titleCombo.setSelectedItem(defaultTitle != null ? defaultTitle : GameConstants.DYNASTY_TITLE_DYNASTY);
        titleCombo.setPreferredSize(fieldSize);
        titleCombo.setMinimumSize(fieldSize);
        titleCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value != null ? value.getName() : "");
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });
        AssetStyles.styleComboBox(titleCombo);
        c.gridx = 1;
        c.weightx = 1.0;
        form.add(titleCombo, c);

        JLabel previewLabel = new JLabel();
        previewLabel.setFont(AssetStyles.FONT_BOLD);
        previewLabel.setForeground(AssetStyles.FONT_COLOR_BRIGHT);
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.insets = new Insets(14, 0, 0, 0);
        form.add(previewLabel, c);

        Runnable refreshPreview = () -> {
            String base = formatBaseName(nameField.getText());
            DynastyTitle title = (DynastyTitle) titleCombo.getSelectedItem();
            if (title == null) {
                title = GameConstants.DYNASTY_TITLE_DYNASTY;
            }
            if (base.isEmpty()) {
                previewLabel.setText(LanguageStrings.format(LanguageStrings.SAVE_DYNASTY_PREVIEW, title.getName()));
            } else {
                previewLabel.setText(LanguageStrings.format(LanguageStrings.SAVE_DYNASTY_PREVIEW,
                        LanguageStrings.formatDynastyName(base, title)));
            }
        };

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { refreshPreview.run(); }
            @Override
            public void removeUpdate(DocumentEvent e) { refreshPreview.run(); }
            @Override
            public void changedUpdate(DocumentEvent e) { refreshPreview.run(); }
        });
        titleCombo.addActionListener(e -> refreshPreview.run());
        refreshPreview.run();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        panel.add(form, BorderLayout.CENTER);

        int result = UiOptionPane.showConfirmDialog(
                this,
                panel,
                LanguageStrings.get(LanguageStrings.SAVE_CREATE_TITLE),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String raw = nameField.getText();
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String base = formatBaseName(raw);
        DynastyTitle selected = (DynastyTitle) titleCombo.getSelectedItem();
        return new DynastyCustomization(base, selected != null ? selected : GameConstants.DYNASTY_TITLE_DYNASTY);
    }

    private static String formatBaseName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        String trimmed = name.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }

    private void copyMapToNewSave(int index) {
        if (index < 0 || index >= generatedWorlds.length) {
            return;
        }
        World targetWorld = generatedWorlds[index];
        if (targetWorld == null) {
            return;
        }
        SaveManager sm = resolveSaveManager();
        if (sm == null) {
            return;
        }
        int emptySlot = -1;
        for (int s = 1; s <= 3; s++) {
            if (sm.loadSlot(s) == null) {
                emptySlot = s;
                break;
            }
        }
        if (emptySlot <= 0) {
            UiOptionPane.showMessageDialog(this,
                    LanguageStrings.get(LanguageStrings.MULTI_MAP_NO_SLOTS),
                    LanguageStrings.get(LanguageStrings.UI_ERROR),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Dynasty existingPlayerDynasty = targetWorld.findPlayerDynasty();
        String initialName = existingPlayerDynasty != null && existingPlayerDynasty.getThemeBase() != null
                ? existingPlayerDynasty.getThemeBase()
                : "Formica";
        DynastyTitle initialTitle = existingPlayerDynasty != null && existingPlayerDynasty.getTitle() != null
                ? existingPlayerDynasty.getTitle()
                : GameConstants.DYNASTY_TITLE_DYNASTY;

        DynastyCustomization custom = promptDynastyCreation(initialName, initialTitle);
        if (custom == null) {
            return;
        }

        final int slotId = emptySlot;
        JButton btn = copyButtons.get(index);
        btn.setEnabled(false);
        targetWorld.setSaveSlotId(slotId);

        Dynasty playerDynasty = targetWorld.findPlayerDynasty();
        if (playerDynasty != null) {
            playerDynasty.setThemeBase(custom.baseName());
            playerDynasty.setTitleKey(custom.title().getNameKey());
            playerDynasty.applyLocalizedName();
            if (targetWorld.getNamingService() != null && playerDynasty.getCapital() != null) {
                String capName = targetWorld.getNamingService().generateCapitalName(custom.baseName());
                playerDynasty.getCapital().setName(capName);
            }
        }

        String slotName = custom.baseName();
        sm.saveWorldToSlotUserAsync(targetWorld, engine, slotId, slotName, success -> {
            if (success) {
                btn.setText(LanguageStrings.get(LanguageStrings.MULTI_MAP_COPIED));
                UiOptionPane.showMessageDialog(this,
                        String.format(LanguageStrings.get(LanguageStrings.MULTI_MAP_SAVED_FMT), slotId),
                        LanguageStrings.get(LanguageStrings.MULTI_MAP_COPY_BUTTON),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                btn.setEnabled(true);
                UiOptionPane.showMessageDialog(this,
                        LanguageStrings.get(LanguageStrings.SAVE_ERROR_CREATE),
                        LanguageStrings.get(LanguageStrings.UI_ERROR),
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void generateBatch() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
        regenerateButton.setEnabled(false);
        generatedCount = 0;
        statusLabel.setText("Generating " + MAP_COUNT + " standalone maps...");
        for (int i = 0; i < MAP_COUNT; i++) {
            statLabels.get(i).setText("Generating...");
            copyButtons.get(i).setEnabled(false);
            copyButtons.get(i).setText(LanguageStrings.get(LanguageStrings.MULTI_MAP_COPY_BUTTON));
            generatedWorlds[i] = null;
        }

        final boolean isFullRandom = randomizeCheck.isSelected();
        final int targetContinents = ((Number) continentSpinner.getValue()).intValue();
        final int targetIslands = ((Number) islandSpinner.getValue()).intValue();
        final int landSharePct = ((Number) landShareSpinner.getValue()).intValue();
        final int icecapPct = ((Number) icecapSpinner.getValue()).intValue();

        currentWorker = new SwingWorker<Void, World>() {
            @Override
            protected Void doInBackground() {
                for (int i = 0; i < MAP_COUNT; i++) {
                    if (isCancelled()) {
                        return null;
                    }
                    try {
                        World world = new World();
                        if (isFullRandom) {
                            int rContinents = 1 + GameRandom.nextInt(7);
                            int rIslands = GameRandom.nextInt(41);
                            double rLandShare = (15 + GameRandom.nextInt(31)) / 100.0;
                            boolean rHasIce = GameRandom.nextInt(100) >= 15;
                            double rIcePolar = 1.0 - ((10 + GameRandom.nextInt(26)) / 100.0);

                            world.setTargetContinentCount(rContinents);
                            world.setMinIslandCount(rIslands);
                            world.setContinentalLandPercentage(rLandShare);
                            world.setHasIceCaps(rHasIce);
                            if (rHasIce) {
                                world.setIceCapPolarLimit(rIcePolar);
                            }
                        } else {
                            if (targetContinents > 0) {
                                world.setTargetContinentCount(targetContinents);
                            } else {
                                world.setTargetContinentCount(0);
                            }
                            world.setMinIslandCount(targetIslands);
                            world.setContinentalLandPercentage(landSharePct / 100.0);
                            if (icecapPct <= 0) {
                                world.setHasIceCaps(false);
                            } else {
                                world.setHasIceCaps(true);
                                world.setIceCapPolarLimit(1.0 - (icecapPct / 100.0));
                            }
                        }
                        world.generateRectangularWorld(null, null, "World " + (i + 1), null);
                        if (!isCancelled()) {
                            publish(world);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void process(List<World> chunks) {
                for (World w : chunks) {
                    if (isCancelled()) {
                        return;
                    }
                    if (generatedCount < mapPanels.size()) {
                        int index = generatedCount;
                        generatedWorlds[index] = w;
                        MiniMapPanel panel = mapPanels.get(index);
                        panel.setExplicitWorld(w);

                        int totalHexes = (w.getHexes() != null) ? w.getHexes().size() : 0;
                        int landHexes = 0;
                        if (w.getHexes() != null) {
                            for (Hex h : w.getHexes()) {
                                if (h != null && h.getBiome() != GameConstants.BIOME_OCEAN) {
                                    landHexes++;
                                }
                            }
                        }
                        double landPct = (totalHexes > 0) ? (100.0 * landHexes / totalHexes) : 0.0;
                        String info = String.format("%dx%d | Land: %.1f%% | Cont: %d | Isl: %d",
                                w.getWorldWidth(), w.getWorldHeight(), landPct, w.getContinentCount(), w.getIslandCount());
                        statLabels.get(index).setText(info);
                        copyButtons.get(index).setEnabled(true);
                        generatedCount++;
                        statusLabel.setText("Generated " + generatedCount + "/" + MAP_COUNT + " maps...");
                    }
                }
            }

            @Override
            protected void done() {
                regenerateButton.setEnabled(true);
                if (isCancelled()) {
                    return;
                }
                statusLabel.setText("Generated " + generatedCount + " standalone maps.");
            }
        };
        currentWorker.execute();
    }
}
