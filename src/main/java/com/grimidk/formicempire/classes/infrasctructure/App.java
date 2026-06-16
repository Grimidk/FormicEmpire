package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.MainFrame;

import javax.swing.SwingUtilities;

public class App {
    public static void start(){
        AssetStyles.applyGlobalStyles();
        Engine engine = new Engine();
        SwingUtilities.invokeLater(() -> {
           MainFrame main = new MainFrame(engine);
            main.setVisible(true);
            main.showCard(MainFrame.CARD_INTRO);
        });
    }
    
}
