package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public abstract class ZeroDialog extends JDialog {

    protected final JPanel southPanel;
    
    private final String titleKey;
    private final JButton closeButton;

    public ZeroDialog(JFrame owner, String titleKey, Dimension preferredSize) {
        super(owner, LanguageStrings.get(titleKey), true);
        this.titleKey = titleKey;

        if (owner instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        }
        
        getContentPane().setBackground(AssetStyles.UI_BG_PRIMARY);
        setLayout(new BorderLayout());
        
        Dimension size = (preferredSize != null) ? preferredSize : AssetStyles.DEFAULT_DIALOG_SIZE;
        setPreferredSize(size);

        southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        
        closeButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        closeButton.setFocusable(false);
        AssetStyles.styleButton(closeButton);
        closeButton.addActionListener(e -> dispose());
        southPanel.add(closeButton);
        add(southPanel, BorderLayout.SOUTH);

        initGlobalKeyBindings();

        getRootPane().registerKeyboardAction(e -> handleEscapeKey(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        setFocusable(true);
    }
    
    public void refreshTranslations() {
        setTitle(LanguageStrings.get(titleKey));
        closeButton.setText(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        refreshDialog();
    }

    public void refreshTheme() {
        getContentPane().setBackground(AssetStyles.UI_BG_PRIMARY);
        southPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        AssetStyles.styleButton(closeButton);
        AssetStyles.applyThemeToContainer(getContentPane());
        refreshDialog();
    }

    protected void disableFocusTraversal(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof AbstractButton || comp instanceof JComboBox || comp instanceof JSpinner || comp instanceof JList || comp instanceof JTable || comp instanceof JTabbedPane) {
                comp.setFocusable(false);
            }
            if (comp instanceof Container) {
                disableFocusTraversal((Container) comp);
            }
        }
    }

    private void initGlobalKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "togglePause");
        actionMap.put("togglePause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Engine engine = getEngine();
                if (engine == null) return;
                if (engine.isPaused()) {
                    engine.resumeEngine();
                } else {
                    engine.pauseEngine();
                }
                syncWithMainControlPanel();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "speedUp");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.SHIFT_DOWN_MASK), "speedUp");
        inputMap.put(KeyStroke.getKeyStroke('+'), "speedUp");
        actionMap.put("speedUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustSpeed(1);
                syncWithMainControlPanel();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "speedDown");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "speedDown");
        inputMap.put(KeyStroke.getKeyStroke('-'), "speedDown");
        actionMap.put("speedDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustSpeed(-1);
                syncWithMainControlPanel();
            }
        });
    }

    private void handleEscapeKey() {
        if (getOwner() instanceof MainFrame frame && frame.getGamePanel() != null
                && frame.getGamePanel().handleEscapeKey()) {
            return;
        }
        dispose();
    }

    private void adjustSpeed(int delta) {
        Engine engine = getEngine();
        if (engine == null) return;

        GameSpeed current = engine.getSpeed();
        GameSpeed next = delta > 0
                ? GameSpeed.getNext(current, engine.isAllowTurboMode())
                : GameSpeed.getPrevious(current);
        if (current != next) {
            engine.setSpeed(next);
            if (engine.isPaused()) {
                engine.resumeEngine();
            }
        }
    }

    private void syncWithMainControlPanel() {
        if (getOwner() instanceof MainFrame frame) {
            if (frame.getGamePanel() != null) {
                frame.getGamePanel().refreshAllGUIData();
                
                Engine engine = frame.getEngine();
                if (engine != null) {
                    frame.getGamePanel().updateStatusIndicator(engine.isPaused());
                }
            }
        }
    }

    private Engine getEngine() {
        if (getOwner() instanceof MainFrame frame) {
            return frame.getEngine();
        }
        return null;
    }

    protected void registerCloseKey(int keyEvent) {
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(keyEvent, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    protected void addToSouthPanel(JComponent component) {
        southPanel.add(component, 0); 
    }

    public void showDialog() {
        refreshDialog();

        disableFocusTraversal(this);

        Dimension size = getPreferredSize();
        if (size == null || size.width < AssetStyles.DEFAULT_DIALOG_SIZE.width) {
            size = AssetStyles.DEFAULT_DIALOG_SIZE;
        }
        setSize(size);

        if (getOwner() != null && getOwner().isVisible()) {
            Point ownerLoc = getOwner().getLocationOnScreen();
            int x = ownerLoc.x + (getOwner().getWidth() - getWidth()) / 2;
            int y = ownerLoc.y + 100;
            setLocation(x, y);
        } else {
            setLocationRelativeTo(null);
            Point loc = getLocation();
            setLocation(loc.x, loc.y + 60);
        }

        if (getOwner() instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        }

        setVisible(true);
    }

    protected abstract void refreshDialog();

    public void liveUpdate() {}
}
