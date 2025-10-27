package com.grimidk.formicempire;

import com.grimidk.formicempire.classes.Engine;
import com.grimidk.formicempire.classes.interfaces.MainFrame;

import javax.swing.SwingUtilities;

public class App {
    public static void start(){
        Engine engine = new Engine();
        SwingUtilities.invokeLater(() -> {
           MainFrame main = new MainFrame(engine);
            main.setVisible(true);
        });
    }
    
}
