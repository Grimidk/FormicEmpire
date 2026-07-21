package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.MainFrame;

import javax.swing.SwingUtilities;

public class App {
    public static void start(){
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        Engine engine = new Engine();
        AssetStyles.applyTheme(engine.isDarkMode());
        AssetStyles.applyGlobalStyles();
        SwingUtilities.invokeLater(() -> {
           MainFrame main = new MainFrame(engine);
            main.setVisible(true);
            main.showCard(MainFrame.CARD_INTRO);
        });
    }
    
}
