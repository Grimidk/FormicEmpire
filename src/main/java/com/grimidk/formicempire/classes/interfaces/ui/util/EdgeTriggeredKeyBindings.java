package com.grimidk.formicempire.classes.interfaces.ui.util;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

public final class EdgeTriggeredKeyBindings {

    private static final long STUCK_KEY_RECOVERY_MS = 500L;

    private static final Set<Integer> BOUND_KEYS = ConcurrentHashMap.newKeySet();
    private static final Set<Integer> PHYSICALLY_DOWN = ConcurrentHashMap.newKeySet();
    private static final Map<Integer, Long> LAST_PRESSED_WHEN_MS = new ConcurrentHashMap<>();
    private static final AtomicBoolean DISPATCHER_INSTALLED = new AtomicBoolean();

    private EdgeTriggeredKeyBindings() {
    }

    public static void bind(InputMap inputMap, ActionMap actionMap, int keyCode, String actionId, Runnable onPress) {
        bind(inputMap, null, actionMap, keyCode, actionId, onPress);
    }

    public static void bind(
            InputMap inputMap,
            InputMap alsoMap,
            ActionMap actionMap,
            int keyCode,
            String actionId,
            Runnable onPress) {
        ensureAutoRepeatBlocker();
        BOUND_KEYS.add(keyCode);

        KeyStroke press = KeyStroke.getKeyStroke(keyCode, 0, false);
        if (inputMap != null) {
            inputMap.put(press, actionId);
        }
        if (alsoMap != null) {
            alsoMap.put(press, actionId);
        }
        actionMap.put(actionId, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onPress.run();
            }
        });
    }

    private static void ensureAutoRepeatBlocker() {
        if (!DISPATCHER_INSTALLED.compareAndSet(false, true)) {
            return;
        }
        KeyEventDispatcher dispatcher = e -> {
            int code = e.getKeyCode();
            if (e.getID() == KeyEvent.KEY_RELEASED) {
                PHYSICALLY_DOWN.remove(code);
                LAST_PRESSED_WHEN_MS.remove(code);
                return false;
            }
            if (e.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }

            long when = e.getWhen();
            Long previousWhen = LAST_PRESSED_WHEN_MS.put(code, when);
            boolean alreadyDown = !PHYSICALLY_DOWN.add(code);
            if (alreadyDown
                    && previousWhen != null
                    && when - previousWhen > STUCK_KEY_RECOVERY_MS) {
                alreadyDown = false;
            }

            if (!alreadyDown || !BOUND_KEYS.contains(code) || isEditableTextFocus()) {
                return false;
            }

            e.consume();
            return true;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
    }

    private static boolean isEditableTextFocus() {
        Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return focus instanceof JTextComponent text && text.isEditable() && text.isEnabled();
    }
}
