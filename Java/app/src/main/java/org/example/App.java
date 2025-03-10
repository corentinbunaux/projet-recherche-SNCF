package org.example;

<<<<<<< Updated upstream

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

=======
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
>>>>>>> Stashed changes
import com.formdev.flatlaf.FlatLightLaf;

import edu.uci.ics.jung.graph.Graph;

public class App {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        Map<String, Point2D> positions = new HashMap<>();
        Graph<String, String> railNetwork = RailNetwork.createRailNetwork(positions);
        GUI.display(railNetwork, positions);
<<<<<<< Updated upstream
        // Graph<String, String> railNetworkXML = RailNetworkXML.createRailNetwork();
=======
        //Graph<String, String> railNetwork = RailNetworkXML.createRailNetwork();
>>>>>>> Stashed changes
    }
}