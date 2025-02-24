package org.example;

import edu.uci.ics.jung.graph.*;
import java.util.*;

public class App {
    public static void main(String[] args) {
        //Basic example
        Graph<String, String> railNetwork = ManchetteGenerator.createRailNetwork();
        List<List<String>> manchettes = ManchetteGenerator.generateManchettes(railNetwork);
        ManchetteGenerator.printManchettes(manchettes);

        //Graph visualisation
        GraphVisualizer.GraphVisualisation();
    }
}