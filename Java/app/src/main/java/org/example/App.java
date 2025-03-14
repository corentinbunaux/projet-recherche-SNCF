package org.example;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import com.formdev.flatlaf.FlatLightLaf;

import edu.uci.ics.jung.graph.Graph;

public class App {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        Map<String, Point2D> positions = new HashMap<>();
        Graph<String, String> railNetwork = RailNetwork.createRailNetwork(positions);
        Graph<String, String> subgraphTest = RailNetwork.graphBorder(railNetwork, positions, "Marseille-St-Charles", 50);
        // Graph<String, String> subgraphTest = RailNetwork.graphBorder(railNetwork, positions, "Chambéry-Challes-les-Eaux", 50);
        // GUI.display(railNetwork, positions);
        // System.out.println(positions);
        GUI.display(subgraphTest, positions);
        FlowAlgo.manchetteBasedFlow(subgraphTest);
        // ManchetteOptiFlow.generateManchettes(subgraphTest, positions);
    }
}