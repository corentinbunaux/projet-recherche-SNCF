package org.example;


import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.formdev.flatlaf.FlatLightLaf;

import edu.uci.ics.jung.graph.Graph;

public class App {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        Map<String, Point2D> positions = new HashMap<>();
        Graph<String, String> railNetwork = RailNetwork.createRailNetwork(positions);
        //GUI.display(railNetwork, positions);
        // Graph<String, String> railNetworkXML = RailNetworkXML.createRailNetwork();

        Graph<String, String> subGrapgBorderMarseille = RailNetwork.graphBorder(railNetwork, positions, "Marseille-St-Charles", 450);
        RailNetwork.printSubgraph(subGrapgBorderMarseille);
        GUI.display(subGrapgBorderMarseille, positions);
        List<List<String>> manchettes = ManchettesOptimized.generateManchettes(subGrapgBorderMarseille);
        ManchettesOptimized.printManchettes(manchettes);
    }
}