package org.example;

import java.awt.geom.Point2D;
import java.util.Map;

import edu.uci.ics.jung.graph.Graph;

public class GUI {
    public static void display(Graph<String, String> railNetwork, Map<String, Point2D> positions){
        GraphVisualizer.displayGraph(railNetwork, positions);
    }
}
