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

/**
 *
 * @author Antoine
 */
public class UI implements Runnable {

    static Analyse p;
            
    void UI() {
        System.out.println("Init UI");
    }

    @Override
    public void run() {
        System.out.println("Starting UI ...");

        final UIFrameHome f = new UIFrameHome();
        f.setVisible(true);
        f.ButtonGO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (f.ButtonGO.getText().contains("Pause")){
                    //TODO implement pause and resume (wait and notify)
                }else if (f.config_is_valid()) {
                    f.disableAll();
                    p = new Analyse(f.get_config());
                    p.setPriority(Thread.MIN_PRIORITY);
                    p.start();
                    
                    f.ButtonGO.setEnabled(true);
                    f.ButtonGO.setText("Pause");
                }
            }
        });
    }
}
