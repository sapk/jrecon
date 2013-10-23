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

import javax.swing.JFrame;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.DragForce;
import prefuse.visual.VisualItem;

/**
 *
 * @author sapk
 */
public class UIFrameMap implements Runnable {

    private Graph graph = null;
    private Visualization vis = null;

    public UIFrameMap(String file) {

        System.out.println("Map visualtion starting ...");
        try {
            graph = new GraphMLReader().readGraph(file);
        } catch (DataIOException e) {
            e.printStackTrace();
            System.err.println("Error loading graph. Exiting...");
            //System.exit(1);

        }

    }

    private void config_vis() {
        LabelRenderer r = new LabelRenderer("ip");
        r.setRoundedCorner(8, 8);

        vis.setRendererFactory(new DefaultRendererFactory(r));

        ColorAction fill = new ColorAction("graph.nodes", VisualItem.FILLCOLOR, ColorLib.rgb(160, 240, 160));
        ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));
        ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));

        ActionList color = new ActionList();
        color.add(fill);
        color.add(text);
        color.add(edges);

        // create an action list with an animated layout
        // the INFINITY parameter tells the action list to run indefinitely
        ActionList layout = new ActionList(Activity.INFINITY,Activity.DEFAULT_STEP_TIME);
        //ActionList layout = new ActionList(1000,Activity.DEFAULT_STEP_TIME);;
        //layout.add(new ForceDirectedLayout("graph"));
        //layout.add(new ForceDirectedLayout("graph",true));
        layout.add(new ForceDirectedLayout("graph",false));
        layout.add(new RepaintAction());
        // add the actions to the visualization
        vis.putAction("color", color);
        vis.putAction("layout", layout);

    }

    @Override
    public void run() {
        vis = new Visualization();
        vis.add("graph", graph);
        config_vis();
        // create a new Display that pull from our Visualization
        Display display = new Display(vis);
        display.setSize(720, 500); // set display size
        display.addControlListener(new DragControl()); // drag items around
        display.addControlListener(new PanControl());  // pan with background left-drag
        display.addControlListener(new ZoomControl()); // zoom with vertical right-drag

        // create a new window to hold the visualization
        JFrame frame = new JFrame("Map");
        frame.setIconImage(Tool.loadImageIcon("/img/icon.png").getImage());
        // ensure application exits when window is closed
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(display);
        frame.pack();           // layout components in window
        frame.setVisible(true); // show the window

        vis.run("color");  // assign the colors
        vis.run("layout"); // start up the animated layout
        System.out.println("Map visualitation ready !");
    }

}
