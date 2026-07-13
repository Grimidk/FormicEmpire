package com.grimidk.formicempire.classes.interfaces.ui.util;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

public final class GameControlKeyBindings {

    private static final long ACTION_COALESCE_MS = 100;

    private GameControlKeyBindings() {
    }

    public static void register(
            InputMap inputMap,
            ActionMap actionMap,
            Runnable togglePause,
            Runnable speedUp,
            Runnable speedDown) {
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "togglePause");
        actionMap.put("togglePause", action(togglePause));

        bindSpeedUp(inputMap, actionMap, speedUp);
        bindSpeedDown(inputMap, actionMap, speedDown);
    }

    private static void bindSpeedUp(InputMap inputMap, ActionMap actionMap, Runnable speedUp) {
        AbstractAction action = coalescedAction(speedUp);
        actionMap.put("speedUp", action);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "speedUp");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.SHIFT_DOWN_MASK), "speedUp");
        inputMap.put(KeyStroke.getKeyStroke('+'), "speedUp");
    }

    private static void bindSpeedDown(InputMap inputMap, ActionMap actionMap, Runnable speedDown) {
        AbstractAction action = coalescedAction(speedDown);
        actionMap.put("speedDown", action);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "speedDown");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "speedDown");
    }

    private static AbstractAction coalescedAction(Runnable runnable) {
        final long[] lastRunMs = {0L};
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long now = System.currentTimeMillis();
                if (now - lastRunMs[0] < ACTION_COALESCE_MS) {
                    return;
                }
                lastRunMs[0] = now;
                runnable.run();
            }
        };
    }

    private static AbstractAction action(Runnable runnable) {
        return coalescedAction(runnable);
    }
}
