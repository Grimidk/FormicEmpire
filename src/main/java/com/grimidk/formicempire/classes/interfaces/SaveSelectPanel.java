package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.constants.misc.DynastyTitle;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class SaveSelectPanel extends JPanel {
    private static final Dimension CREATE_SAVE_FIELD_SIZE = new Dimension(460, 32);
    private static final Dimension CREATE_SAVE_PANEL_SIZE = new Dimension(640, 220);

    private final MainFrame frame;
    private final SaveManager saveManager;

    private final JButton[] slotButtons = new JButton[3];
    private final JButton[] deleteButtons = new JButton[3];
    private final JLabel[] slotLabels = new JLabel[3];
    private final JButton backButton;
    
    private final Savefile[] cachedSaves = new Savefile[3];

    public SaveSelectPanel(MainFrame frame) {
        this.frame = frame;
        this.saveManager = frame.getEngine().getSaveManager();
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8,8,8,8);

        for (int i = 0; i < 3; i++) {
            int slotId = i + 1;
            slotLabels[i] = new JLabel(LanguageStrings.get(LanguageStrings.SAVE_EMPTY_SLOT));
            slotButtons[i] = new JButton(LanguageStrings.get(LanguageStrings.UI_CREATE));
            deleteButtons[i] = new JButton(LanguageStrings.get(LanguageStrings.UI_DELETE));
            AssetStyles.styleButton(slotButtons[i]);
            AssetStyles.styleButton(deleteButtons[i]);
            deleteButtons[i].setVisible(false);
            
            setupNavigation(slotButtons[i]);
            setupNavigation(deleteButtons[i]);

            int idx = i;
            slotButtons[i].addActionListener(e -> onCreateOrLoad(slotId, idx));
            deleteButtons[i].addActionListener(e -> onDelete(slotId, idx));
            c.gridx = 0; c.gridy = i; add(slotLabels[i], c);
            c.gridx = 1; add(slotButtons[i], c);
            c.gridx = 2; add(deleteButtons[i], c);
        }

        backButton = new JButton(LanguageStrings.get(LanguageStrings.UI_BACK));
        AssetStyles.styleButton(backButton);
        setupNavigation(backButton);
        backButton.addActionListener(e -> {
            frame.showCard(MainFrame.CARD_INIT);
        });

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; add(backButton, c);

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (slotButtons[0] != null) {
                    slotButtons[0].requestFocusInWindow();
                }
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }
    
    public void refreshTranslations() {
        refreshSlots();
        backButton.setText(LanguageStrings.get(LanguageStrings.UI_BACK));
    }

    public void refreshTheme() {
        AssetStyles.applyThemeToContainer(this);
    }
    
    private void setupNavigation(JButton button) {
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

    public void refreshSlots() {
        for (int i = 0; i < 3; i++) {
            int slotId = i + 1;
            Savefile s = saveManager.loadSlot(slotId);
            cachedSaves[i] = s;

            if (s == null) {
                slotLabels[i].setText(LanguageStrings.get(LanguageStrings.SAVE_EMPTY_SLOT));
                slotButtons[i].setText(LanguageStrings.get(LanguageStrings.UI_CREATE));
                deleteButtons[i].setText(LanguageStrings.get(LanguageStrings.UI_DELETE));
                deleteButtons[i].setVisible(false);
            } else {
                int totalDays = (s.getDay() - 1) + ((s.getMonth() - 1) * 30) + (s.getYear() * 12 * 30);
                String displayName = LanguageStrings.formatSaveSlotDisplayName(
                        s.getName(), s.resolvePlayerDynastyTitleId(), slotId);
                slotLabels[i].setText(LanguageStrings.format(LanguageStrings.SAVE_DAYS_FORMAT, displayName, totalDays));
                slotButtons[i].setText(LanguageStrings.get(LanguageStrings.UI_LOAD));
                deleteButtons[i].setText(LanguageStrings.get(LanguageStrings.UI_DELETE));
                deleteButtons[i].setVisible(true);
            }
        }
    }

    private void onCreateOrLoad(int slotId, int idx) {
        Savefile existing = cachedSaves[idx];
        
        if (existing == null) {
            NewSaveRequest request = promptNewSave();
            if (request == null) return;

            Savefile save = new Savefile(slotId, request.baseName());
            save.setPlayerDynastyTitleId(request.titleId());
            saveManager.saveUserSlotAsync(save, () -> {
                refreshSlots(); 

                Savefile newSave = cachedSaves[idx];
                if (newSave != null) {
                    HelpPanel.showTutorialDialog(frame);
                    frame.openGameWithSave(newSave); 
                } else {
                    UiOptionPane.showMessageDialog(frame, LanguageStrings.get(LanguageStrings.SAVE_ERROR_CREATE), LanguageStrings.get(LanguageStrings.UI_ERROR), JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            frame.openGameWithSave(existing);
        }
    }

    private NewSaveRequest promptNewSave() {
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

        JTextField nameField = new JTextField();
        nameField.setPreferredSize(CREATE_SAVE_FIELD_SIZE);
        nameField.setMinimumSize(CREATE_SAVE_FIELD_SIZE);
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
        titleCombo.setSelectedItem(GameConstants.DYNASTY_TITLE_DYNASTY);
        titleCombo.setPreferredSize(CREATE_SAVE_FIELD_SIZE);
        titleCombo.setMinimumSize(CREATE_SAVE_FIELD_SIZE);
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

        Runnable refreshPreview = () -> updateDynastyPreview(previewLabel, nameField, titleCombo);
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshPreview.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshPreview.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshPreview.run();
            }
        });
        titleCombo.addActionListener(e -> refreshPreview.run());
        refreshPreview.run();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AssetStyles.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(16, 24, 8, 24));
        panel.setPreferredSize(CREATE_SAVE_PANEL_SIZE);
        panel.setMinimumSize(CREATE_SAVE_PANEL_SIZE);
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

        String rawName = nameField.getText();
        if (rawName == null || rawName.trim().isEmpty()) {
            return null;
        }

        DynastyTitle selectedTitle = (DynastyTitle) titleCombo.getSelectedItem();
        int titleId = selectedTitle != null
                ? selectedTitle.getId()
                : GameConstants.DYNASTY_TITLE_DYNASTY.getId();
        String baseName = formatBaseName(rawName);
        String fullName = LanguageStrings.formatDynastyName(baseName, titleId);

        int confirm = UiOptionPane.showConfirmDialog(
                this,
                LanguageStrings.format(LanguageStrings.SAVE_DYNASTY_CONFIRM, fullName),
                LanguageStrings.get(LanguageStrings.SAVE_CREATE_TITLE),
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return null;
        }

        return new NewSaveRequest(baseName, titleId);
    }

    private static void updateDynastyPreview(JLabel previewLabel, JTextField nameField, JComboBox<DynastyTitle> titleCombo) {
        String baseName = formatBaseName(nameField.getText());
        DynastyTitle title = (DynastyTitle) titleCombo.getSelectedItem();
        if (title == null) {
            title = GameConstants.DYNASTY_TITLE_DYNASTY;
        }
        if (baseName.isEmpty()) {
            previewLabel.setText(LanguageStrings.format(LanguageStrings.SAVE_DYNASTY_PREVIEW, title.getName()));
        } else {
            previewLabel.setText(LanguageStrings.format(LanguageStrings.SAVE_DYNASTY_PREVIEW,
                    LanguageStrings.formatDynastyName(baseName, title)));
        }
    }

    private static String formatBaseName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        String trimmed = name.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }

    private void onDelete(int slotId, int idx) {
        int res = UiOptionPane.showConfirmDialog(this, LanguageStrings.format(LanguageStrings.SAVE_DELETE_CONFIRM, slotId), LanguageStrings.get(LanguageStrings.SAVE_DELETE_TITLE), JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;
        boolean ok = saveManager.deleteSlot(slotId);
        if (!ok) UiOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.SAVE_DELETE_ERROR));
        refreshSlots();
    }

    private record NewSaveRequest(String baseName, int titleId) {}
}
