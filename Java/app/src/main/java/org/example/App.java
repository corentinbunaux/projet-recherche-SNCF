package org.example;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import com.formdev.flatlaf.FlatIntelliJLaf;

import edu.uci.ics.jung.graph.Graph;

public class App {
    public static void main(String[] args) {
        FlatIntelliJLaf.setup();
        Map<String, Point2D> positions = new HashMap<>();
        Graph<String, String> railNetwork = RailNetwork.createRailNetwork(positions);
        GUI.display(railNetwork, positions);
    }
}