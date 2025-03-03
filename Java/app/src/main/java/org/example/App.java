package org.example;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.jung.graph.Graph;

public class App {
    public static void main(String[] args) {
        Map<String, Point2D> positions = new HashMap<>();
        
        Graph<String, String> railNetwork = RailNetwork.createRailNetwork(positions);
        Graph<String, String> subGrapgNeighborhood = RailNetwork.graphNeighborhood(railNetwork, "Le Havre", 5);
        Graph<String, String> subGrapgBorder = RailNetwork.graphBorder(railNetwork, positions, "Le Havre", 50);

        //Graph visualisation
        GraphVisualizer.displayGraph(railNetwork, positions);
        GraphVisualizer.displayGraph(subGrapgNeighborhood, positions);
        GraphVisualizer.displayGraph(subGrapgBorder, positions);
    }
}