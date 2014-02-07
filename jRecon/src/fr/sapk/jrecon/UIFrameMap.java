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

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.HoverActionControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.ForceSimulator;
import prefuse.visual.VisualItem;

/**
 *
 * @author sapk
 */
public class UIFrameMap implements Runnable {

    private Graph graph = null;
    private Visualization vis = null;

    public UIFrameMap(String file) throws Throwable {

        System.out.println("Map visualtion starting ...");
        System.out.println(file);
        try {
            graph = new GraphMLReader().readGraph(file);
        } catch (DataIOException e) {
            e.printStackTrace();
            System.err.println("Error loading graph. Exiting...");
            //System.exit(1);
            this.finalize();
        }

    }

    private void config_vis() {
        LabelRenderer r = new LabelRenderer("ip");
        r.setRoundedCorner(8, 8);

        vis.setRendererFactory(new DefaultRendererFactory(r));

        //final GraphDistanceFilter filter = new GraphDistanceFilter("graph", 4);
        ColorAction fill = new ColorAction("graph.nodes", VisualItem.FILLCOLOR, ColorLib.rgb(160, 240, 160));
        ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));
        //ColorAction shape = new ColorAction("graph.nodes", VisualItem.SHAPE, ColorLib.gray(0));

        ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));

//        fill.add(VisualItem.FIXED, ColorLib.rgb(20, 16, 16));
        fill.add("_hover", ColorLib.rgb(240, 160, 160));
        fill.add("_highlight", ColorLib.rgb(255, 200, 125));
        edges.add("_highlight", ColorLib.rgb(255, 230, 155));
//DataColorAction edges = new DataColorAction("graph.edges",;
        DataSizeAction sizenodeAction = new DataSizeAction("graph.nodes", "size");

        DataSizeAction sizeedgeAction = new DataSizeAction("graph.edges", "size");
        //TODO tweak this avlue for beautiful vis
        sizeedgeAction.setMinimumSize(5);

        //sizeAction.setBinCount(500);
        // sizeAction.getScale();
        ActionList color = new ActionList();
//        color.add(fill);
        //color.add(filter);
        color.add(text);
//        color.add(edges);
        color.add(sizeedgeAction);
        color.add(sizenodeAction);

        ActionList hover = new ActionList();
        hover.add(fill);
        hover.add(edges);
        //TODO 37.187.4.165
        // create an action list with an animated layout
        // the INFINITY parameter tells the action list to run indefinitely
        //
        //RadialTreeLayout treeLayout = new RadialTreeLayout("graph");
        //vis.putAction("treeLayout", treeLayout);
        ActionList layout = new ActionList(Activity.INFINITY, Activity.DEFAULT_STEP_TIME);
        //ActionList layout = new ActionList(1000,Activity.DEFAULT_STEP_TIME);;

        ForceDirectedLayout fdl = new ForceDirectedLayout("graph");
        ForceSimulator fsim = fdl.getForceSimulator();
        fsim.getForces()[0].setParameter(0, -4f);
//        fdl.setPacingFunction(new SlowInSlowOutPacer());
        fsim.setSpeedLimit((float) 0.05);
//        fsim.getSprings()[0].set
        layout.add(fdl);
//        layout.setPacingFunction(new SlowInSlowOutPacer());

        //layout.add(new ForceDirectedLayout("graph",true));
        //layout.add(new ForceDirectedLayout("graph", false));
        //RadialTreeLayout treeL = new RadialTreeLayout("graph");
        //treeL.setAngularBounds(-Math.PI/2, Math.PI);
        //layout.add(treeL);
        //layout.add( new CollapsedSubtreeLayout("graph"));
        /*
         layout.setPacingFunction(new SlowInSlowOutPacer());
         layout.add(new QualityControlAnimator());
         layout.add(new VisibilityAnimator("graph"));
         layout.add(new PolarLocationAnimator("graph.nodes", "linear"));
         //layout.add(new ForceDirectedLayout("graph", false));
         //layout.add(new ColorAnimator("graph.nodes"));
         */
        layout.add(new RepaintAction());

        /*
         // recolor
         ActionList recolor = new ActionList();
         recolor.add(nodeColor);
         recolor.add(textColor);
         vis.putAction("recolor", recolor);
         // repaint
         ActionList repaint = new ActionList();
         repaint.add(recolor);
         repaint.add(new RepaintAction());
         vis.putAction("repaint", repaint);
         */
        // add the actions to the visualization
        vis.putAction("color", color);
        vis.putAction("hover", hover);
        vis.putAction("layout", layout);
        //vis.alwaysRunAfter("hover", "layout");
        //

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
        display.addControlListener(new ZoomToFitControl()); //auto-zoom on right click with no dragging
        display.addControlListener(new HoverActionControl("hover"));
        display.addControlListener(new WheelZoomControl());
        display.addControlListener(new FocusControl(1)); //TODO
        display.addControlListener(new NeighborHighlightControl());
        display.addControlListener(new ToolTipControl("hostname"));

        vis.run("color");  // assign the colors
        vis.run("hover"); // start up the animated layout
        vis.run("layout"); // start up the animated layout

        JPanel panel = new JPanel();
        panel.setBackground(ColorLib.getColor(100, 100, 100, (float) 0.5));
        JFormattedTextField textfield = new JFormattedTextField("no data");
        textfield.setBackground(ColorLib.getColor(100, 100, 100, (float) 0.5));
        textfield.setEditable(false);
        textfield.setLocation(0, 0);
        panel.add(textfield);
        // create a new JSplitPane to present the interface
        JSplitPane split = new JSplitPane();
        split.setLeftComponent(display);
        split.setRightComponent(panel);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(false);
        //split.setDividerLocation(580);
        split.setDividerLocation(0.8);
        textfield.setSize(panel.getSize());

        // create a new window to hold the visualization
        JFrame frame = new JFrame("Map");
        frame.setIconImage(Tool.loadImageIcon("/img/icon.png").getImage());
        // ensure application exits when window is closed
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(split);
        frame.pack();           // layout components in window
        frame.setVisible(true); // show the window
        System.out.println("Map visualitation ready !");
    }

}
