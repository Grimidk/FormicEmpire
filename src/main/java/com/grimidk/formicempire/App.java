/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grimidk.formicempire;

import com.grimidk.formicempire.classes.Engine;
import com.grimidk.formicempire.interfaces.MainFrame;

import javax.swing.SwingUtilities;

/**
 *
 * @author juanmendezl
 */
public class App {
    public static void start(){
        Engine engine = new Engine();
        SwingUtilities.invokeLater(() -> {
           MainFrame main = new MainFrame(engine);
            main.setVisible(true);
        });
    }
    
}
