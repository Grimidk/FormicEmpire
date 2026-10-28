package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.maps.SavedMapData;
import com.grimidk.formicempire.classes.infrasctructure.maps.SavedMapService;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.MiniMapPanel;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.EdgeTriggeredKeyBindings;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiDialogUtils;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedMapSelectionDialog extends JDialog {

    private static final int PAGE_SIZE = 12;

    private final List<JPanel> cards = new ArrayList<>();
    private final List<MiniMapPanel> miniMaps = new ArrayList<>();
    private final List<JLabel> nameLabels = new ArrayList<>();
    private final List<JLabel> statLabels = new ArrayList<>();
    private final List<JButton> selectButtons = new ArrayList<>();

    private final JButton prevButton;
    private final JButton nextButton;
    private final JLabel pageLabel;
    private final JButton closeButton;

    private int currentPage = 0;
    private File[] allFiles = new File[0];
    private final Map<String, SavedMapData> cachedData = new HashMap<>();
    private final Map<String, World> cachedWorlds = new HashMap<>();
    private final SavedMapData[] pageData = new SavedMapData[PAGE_SIZE];

    private SavedMapData selectedMapData = null;
    private SwingWorker<Void, PageLoadItem> currentWorker = null;

    public SavedMapSelectionDialog(JFrame owner) {
        super(owner, LanguageStrings.get(LanguageStrings.SAVED_MAP_GALLERY_TITLE), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(860, 640));
        setPreferredSize(new Dimension(1080, 780));

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(AssetStyles.UI_BG_PRIMARY);

        JPanel gridPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        gridPanel.setBackground(AssetStyles.UI_BG_PRIMARY);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < PAGE_SIZE; i++) {
            final int slotIndex = i;
            JPanel card = new JPanel(new BorderLayout(0, 4));
            card.setBackground(AssetStyles.UI_BG_SECONDARY);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AssetStyles.BORDER_COLOR, AssetStyles.BORDER_THICKNESS_INTERNAL),
                    BorderFactory.createEmptyBorder(6, 6, 6, 6)));

            JLabel nameLabel = new JLabel("", SwingConstants.CENTER);
            nameLabel.setFont(AssetStyles.FONT_BOLD);
            nameLabel.setForeground(AssetStyles.FONT_COLOR_BRIGHT);
            nameLabels.add(nameLabel);
            card.add(nameLabel, BorderLayout.NORTH);

            MiniMapPanel miniMap = new MiniMapPanel((World) null);
            miniMap.setPreferredSize(new Dimension(220, 140));
            miniMap.setMinimumSize(new Dimension(120, 80));
            miniMaps.add(miniMap);
            card.add(miniMap, BorderLayout.CENTER);

            MouseAdapter clickHandler = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        selectMapAt(slotIndex);
                    }
                }
            };
            card.addMouseListener(clickHandler);
            AssetStyles.markClickable(card);

            JPanel cardSouth = new JPanel(new BorderLayout(0, 4));
            cardSouth.setOpaque(false);

            JLabel statLabel = new JLabel("", SwingConstants.CENTER);
            statLabel.setFont(AssetStyles.FONT_NORMAL);
            statLabel.setForeground(AssetStyles.FONT_COLOR);
            statLabels.add(statLabel);
            cardSouth.add(statLabel, BorderLayout.NORTH);

            JButton selectBtn = new JButton(LanguageStrings.get(LanguageStrings.SAVED_MAP_SELECT));
            AssetStyles.styleCompactButton(selectBtn);
            selectBtn.setEnabled(false);
            selectBtn.addActionListener(e -> selectMapAt(slotIndex));
            selectButtons.add(selectBtn);
            cardSouth.add(selectBtn, BorderLayout.SOUTH);

            card.add(cardSouth, BorderLayout.SOUTH);
            cards.add(card);
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

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        navPanel.setOpaque(false);

        prevButton = new JButton("< " + LanguageStrings.get(LanguageStrings.SAVED_MAP_PAGE_FMT).split(" ")[0]);
        prevButton.setText("Previous");
        AssetStyles.styleButton(prevButton);
        prevButton.addActionListener(e -> changePage(-1));
        navPanel.add(prevButton);

        pageLabel = new JLabel("Page 1 / 1");
        pageLabel.setFont(AssetStyles.FONT_BOLD);
        pageLabel.setForeground(AssetStyles.FONT_COLOR_BRIGHT);
        navPanel.add(pageLabel);

        nextButton = new JButton("Next >");
        AssetStyles.styleButton(nextButton);
        nextButton.addActionListener(e -> changePage(1));
        navPanel.add(nextButton);

        closeButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        AssetStyles.styleButton(closeButton);
        closeButton.setPreferredSize(new Dimension(Math.max(100, AssetStyles.MIN_CONTROL_HIT_SIZE * 4), AssetStyles.MIN_CONTROL_HIT_SIZE + 8));
        closeButton.addActionListener(e -> dispose());

        southPanel.add(navPanel, BorderLayout.WEST);
        southPanel.add(closeButton, BorderLayout.EAST);
        contentPane.add(southPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelCurrentWorker();
                dispose();
            }
        });

        EdgeTriggeredKeyBindings.bind(
                getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
                getRootPane().getActionMap(),
                KeyEvent.VK_ESCAPE,
                "saved_map_dialog_close",
                this::dispose);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadFilesAndRenderPage();
            }
        });

        UiDialogUtils.prepareDialog(this, owner);
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(maxBounds);
    }

    private void cancelCurrentWorker() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
            currentWorker = null;
        }
    }

    private void changePage(int delta) {
        cancelCurrentWorker();
        int totalPages = Math.max(1, (int) Math.ceil((double) allFiles.length / PAGE_SIZE));
        int next = currentPage + delta;
        if (next >= 0 && next < totalPages) {
            currentPage = next;
            renderCurrentPage();
        }
    }

    private void loadFilesAndRenderPage() {
        allFiles = SavedMapService.getSavedMapFiles();
        currentPage = 0;
        renderCurrentPage();
    }

    private record PageLoadItem(int slotIndex, SavedMapData data, World world) {}

    private void renderCurrentPage() {
        cancelCurrentWorker();
        int totalFiles = allFiles.length;
        int totalPages = Math.max(1, (int) Math.ceil((double) totalFiles / PAGE_SIZE));
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        pageLabel.setText(LanguageStrings.format(LanguageStrings.SAVED_MAP_PAGE_FMT, currentPage + 1, totalPages));
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);

        int startIndex = currentPage * PAGE_SIZE;

        for (int i = 0; i < PAGE_SIZE; i++) {
            pageData[i] = null;
            int fileIndex = startIndex + i;
            if (fileIndex < totalFiles) {
                cards.get(i).setVisible(true);
                nameLabels.get(i).setText("Loading...");
                statLabels.get(i).setText("");
                selectButtons.get(i).setEnabled(false);
                miniMaps.get(i).setExplicitWorld(null);
            } else {
                cards.get(i).setVisible(false);
            }
        }

        currentWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (int i = 0; i < PAGE_SIZE; i++) {
                    if (isCancelled()) return null;
                    int fileIndex = startIndex + i;
                    if (fileIndex >= totalFiles) break;

                    File f = allFiles[fileIndex];
                    String path = f.getAbsolutePath();

                    SavedMapData data = cachedData.get(path);
                    World world = cachedWorlds.get(path);
                    if (data == null) {
                        try {
                            data = SavedMapService.loadMapData(f);
                            cachedData.put(path, data);
                            world = SavedMapService.buildWorldFromMapData(data);
                            cachedWorlds.put(path, world);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (world == null) {
                        world = SavedMapService.buildWorldFromMapData(data);
                        cachedWorlds.put(path, world);
                    }

                    if (data != null && world != null) {
                        publish(new PageLoadItem(i, data, world));
                    }
                }
                return null;
            }

            @Override
            protected void process(List<PageLoadItem> chunks) {
                for (PageLoadItem item : chunks) {
                    if (isCancelled()) return;
                    int idx = item.slotIndex();
                    SavedMapData d = item.data();
                    World w = item.world();
                    pageData[idx] = d;

                    String displayName = (d.mapName != null && !d.mapName.isEmpty())
                            ? d.mapName
                            : ("Map " + (startIndex + idx + 1));
                    nameLabels.get(idx).setText(displayName);
                    miniMaps.get(idx).setExplicitWorld(w);

                    int totalHexes = (w.getHexes() != null) ? w.getHexes().size() : 0;
                    int landHexes = 0;
                    if (w.getHexes() != null) {
                        for (Hex h : w.getHexes()) {
                            if (h != null && h.getBiome() != GameConstants.BIOME_OCEAN
                                    && h.getBiome() != GameConstants.BIOME_DEEP_OCEAN
                                    && h.getBiome() != GameConstants.BIOME_SHALLOW_OCEAN) {
                                landHexes++;
                            }
                        }
                    }
                    double landPct = (totalHexes > 0) ? (100.0 * landHexes / totalHexes) : 0.0;
                    String info = String.format("%dx%d | Land: %.1f%% | Cont: %d | Isl: %d",
                            w.getWorldWidth(), w.getWorldHeight(), landPct, w.getContinentCount(), w.getIslandCount());
                    statLabels.get(idx).setText(info);
                    selectButtons.get(idx).setEnabled(true);
                }
            }
        };
        currentWorker.execute();
    }

    private void selectMapAt(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= PAGE_SIZE) {
            return;
        }
        SavedMapData data = pageData[slotIndex];
        if (data == null) {
            return;
        }
        this.selectedMapData = data;
        cancelCurrentWorker();
        dispose();
    }

    public SavedMapData getSelectedMapData() {
        return selectedMapData;
    }
}
