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

import static java.lang.Thread.sleep;

/**
 *
 * @author Antoine
 */
public class UI extends Thread {

    static Analyse p;
    static UIFrameHome f;

    void UI() {
        System.out.println("Init UI");
    }

    @Override
    public void run() {
        System.out.println("Starting UI ...");

        f = new UIFrameHome();
        f.setVisible(true);
        /* TODO presentation */
        f.ButtonGO.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (f.ButtonGO.getText().contains("Pause")) {
                    //TODO implement pause and resume (wait and notify)
                } else if (f.config_is_valid()) {
                    f.disableAll();
                    p = new Analyse(f.get_config());
                    //TODO test difference
                    //p.setPriority(Thread.MIN_PRIORITY);
                    p.start();

                    f.ButtonGO.setEnabled(true);
                    f.ButtonGO.setText("Pause");

                    f.bProgressBar.setEnabled(true);
                    f.bProgressBar.setStringPainted(true);
                    f.bProgressBar.setMaximum((int) p.request_total);
                    f.bProgressBar.setMinimum(0);
                    f.bProgressBar.setValue(0);

                    new Thread(watcher).start();
                }
            }
        });
    }

    static Runnable watcher = new Runnable() {

        @Override
        public void run() {

            while (true) {
                if (p.state == "Running ...") {
                   // System.out.println("Avencement : " + p.request_done + " / " + p.request_total + "");
                    f.bProgressBar.setString(p.request_done + " / " + p.request_total);
                } else {
                    f.bProgressBar.setString(p.state);
                }
                
                f.bProgressBar.setValue((int) p.request_done);
                
                if ((p.request_done >= p.request_total ) || p.state == "Finished !" || !p.isAlive()) {
                    f.bProgressBar.setString("Finished !");
                    p = null;
                    f.enableAll();

                    f.ButtonGO.setText("GO !");
                    break;
                }
                

                try {
                    sleep(300);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

    };
}
