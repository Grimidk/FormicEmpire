package com.grimidk.formicempire.classes.infrasctructure.util;

import java.awt.Window;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.swing.JFrame;

public final class MacOsNativeFullscreen {
    private static final boolean MAC =
            System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("mac");

    private static Boolean eawtUsable;

    private MacOsNativeFullscreen() {
    }

    public static boolean isMac() {
        return MAC;
    }

    public static boolean isEawtAvailable() {
        if (!MAC) {
            return false;
        }
        if (eawtUsable != null) {
            return eawtUsable;
        }
        eawtUsable = probeEawt();
        return eawtUsable;
    }

    private static boolean probeEawt() {
        try {
            Class<?> util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Class.forName("com.apple.eawt.Application");
            Module desktop = util.getModule();
            Module self = MacOsNativeFullscreen.class.getModule();
            return desktop.isExported("com.apple.eawt", self) || desktop.isOpen("com.apple.eawt", self);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void markFullscreenable(JFrame frame) {
        Objects.requireNonNull(frame, "frame");
        if (!MAC) {
            return;
        }
        if (frame.getRootPane() != null) {
            frame.getRootPane().putClientProperty("apple.awt.fullscreenable", Boolean.TRUE);
        }
        if (!isEawtAvailable()) {
            return;
        }
        try {
            Class<?> util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Method setCan = util.getMethod("setWindowCanFullScreen", Window.class, boolean.class);
            setCan.invoke(null, frame, true);
        } catch (Throwable ignored) {
            eawtUsable = false;
        }
    }

    public static boolean addFullscreenListener(JFrame frame, Runnable entered, Runnable exited) {
        Objects.requireNonNull(frame, "frame");
        if (!isEawtAvailable()) {
            return false;
        }
        try {
            Class<?> util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Class<?> listenerType = Class.forName("com.apple.eawt.FullScreenListener");
            InvocationHandler handler = (proxy, method, args) -> {
                String name = method.getName();
                if ("windowEnteredFullScreen".equals(name)) {
                    if (entered != null) {
                        entered.run();
                    }
                } else if ("windowExitedFullScreen".equals(name)) {
                    if (exited != null) {
                        exited.run();
                    }
                } else if ("windowEnteringFullScreen".equals(name) || "windowExitingFullScreen".equals(name)) {
                    return null;
                } else if ("equals".equals(name)) {
                    return proxy == args[0];
                } else if ("hashCode".equals(name)) {
                    return System.identityHashCode(proxy);
                } else if ("toString".equals(name)) {
                    return "MacOsNativeFullscreen.Listener";
                }
                return null;
            };
            Object listener = Proxy.newProxyInstance(
                    listenerType.getClassLoader(),
                    new Class<?>[] { listenerType },
                    handler);
            Method add = util.getMethod("addFullScreenListenerTo", Window.class, listenerType);
            add.invoke(null, frame, listener);
            return true;
        } catch (Throwable ignored) {
            eawtUsable = false;
            return false;
        }
    }

    public static boolean requestToggle(Window window) {
        Objects.requireNonNull(window, "window");
        if (!isEawtAvailable()) {
            return false;
        }
        try {
            Class<?> appClass = Class.forName("com.apple.eawt.Application");
            Object app = appClass.getMethod("getApplication").invoke(null);
            appClass.getMethod("requestToggleFullScreen", Window.class).invoke(app, window);
            return true;
        } catch (Throwable ignored) {
            eawtUsable = false;
            return false;
        }
    }

    public static boolean requestForeground() {
        if (!isEawtAvailable()) {
            return false;
        }
        try {
            Class<?> appClass = Class.forName("com.apple.eawt.Application");
            Object app = appClass.getMethod("getApplication").invoke(null);
            appClass.getMethod("requestForeground", boolean.class).invoke(app, Boolean.TRUE);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
