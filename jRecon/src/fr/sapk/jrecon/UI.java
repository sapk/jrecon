/*
 This file is part of jRecon.

 jRecon is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 jRecon is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with jRecon.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.sapk.jrecon;

import java.awt.FlowLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Antoine
 */
public class UI implements Runnable {
    
    void UI(){    
        System.out.println("Init UI");
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        System.out.println("Starting UI ...");
        
        UIFrameHome f = new UIFrameHome();
        f.setVisible(true);
        /*
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        JFrame f = new JFrame("Hello, World!");
        // Sets the behavior for when the window is closed
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Add a layout manager so that the button is not placed on top of the label
        f.setLayout(new FlowLayout());
        // Add a label and a button
        f.add(new JLabel("Hello, world!"));
        f.add(new JButton("Press me!"));
        // Arrange the components inside the window
        f.pack();
        // By default, the window is not visible. Make it visible.
        f.setVisible(true);
        */
    }
}
