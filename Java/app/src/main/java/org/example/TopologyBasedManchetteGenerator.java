package org.example;

import edu.uci.ics.jung.graph.Graph;
import java.util.*;

public class TopologyBasedManchetteGenerator {
    public static List<List<String>> generateManchettes(Graph<String, String> railNetwork) {
        List<List<String>> manchettes = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (String station : railNetwork.getVertices()) {
            if (!visited.contains(station)) {
                List<String> manchette = new ArrayList<>();
                exploreStation(railNetwork, station, manchette, visited);
                manchettes.add(manchette);
            }
        }
        return manchettes;
    }

    private static void exploreStation(Graph<String, String> graph, String station,
                                       List<String> manchette, Set<String> visited) {
        Stack<String> stack = new Stack<>();
        stack.push(station);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (!visited.contains(current)) {
                visited.add(current);
                manchette.add(current);
                for (String neighbor : graph.getNeighbors(current)) {
                    if (!visited.contains(neighbor)) {
                        stack.push(neighbor);
                    }
                }
            }
        }
    }
}