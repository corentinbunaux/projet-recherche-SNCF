package org.example;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.graph.Graph;

public class App {
    public static void main(String[] args) {
        Map<String, Point2D> positions = new HashMap<>();
        
        Graph<String, String> railNetwork = RailNetwork.createRailNetwork(positions);
        Graph<String, String> subGrapgNeighborhood = RailNetwork.graphNeighborhood(railNetwork, "Le Havre", 5);
        Graph<String, String> subGrapgBorderLH = RailNetwork.graphBorder(railNetwork, positions, "Le Havre", 50);
        Graph<String, String> subGrapgBorderMarseille = RailNetwork.graphBorder(railNetwork, positions, "Marseille-St-Charles", 20);

        //Graph visualisation
        GraphVisualizer.Graph(railNetwork, positions);
        GraphVisualizer.Graph(subGrapgNeighborhood, positions);
        GraphVisualizer.Graph(subGrapgBorderMarseille, positions);

        List<List<String>> manchettes = ManchetteGenerator.generateManchettes(subGrapgBorderMarseille);
        ManchetteGenerator.printManchettes(manchettes);
    }
}