package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.graph.Graph;

public class FlowAlgo {

    public static void manchetteBasedFlow(Graph<String, String> graph) {
        // Make manchettes based on the oneway rails of the graph
        List<List<String>> manchettes = manchettesOneways(graph);
        System.out.println("Manchettes: " + manchettes);
    }

    // all the flows that go through at least one station of the graph
    private static List<String> stationsWallet(Graph<String, String> graph, Map<String, List<String>> stationsInFlow) {
        List<String> stationsWallet = new ArrayList<>();

        // add the flowID to the stationsWallet if the flow goes through at least one station of the graph
        for (Map.Entry<String, List<String>> entry : stationsInFlow.entrySet()) {
            String flowID = entry.getKey();
            List<String> stationsIC = entry.getValue();
            for (String stationIC : stationsIC) {
                if (graph.containsVertex(RailNetwork.getName(stationIC))) {
                    stationsWallet.add(flowID);
                    break;
                }
            }
        }
        return stationsWallet;
    }

    public static List<List<String>> manchettesOneways(Graph<String, String> graph) {
        List<List<String>> manchettes = new ArrayList<>();
        List<String> onewayStations = getOnewayStations(graph);

        // create a manchette for linked stations in the onewayStations list
        while (!onewayStations.isEmpty()) {
            List<String> manchette = createManchette(graph, onewayStations);
            manchettes.add(manchette);
        }

        // Add the border stations to the manchettes
        addBorderStationsToManchettes(graph, manchettes);

        return manchettes;
    }

    // Get the list of stations with at most 2 incident edges
    private static List<String> getOnewayStations(Graph<String, String> graph) {
        List<String> onewayStations = new ArrayList<>();
        for (String vertex : graph.getVertices()) {
            if (graph.getIncidentEdges(vertex).size() <= 2) {
                onewayStations.add(vertex);
            }
        }
        return onewayStations;
    }

    // Create a manchette for linked stations in the onewayStations list
    private static List<String> createManchette(Graph<String, String> graph, List<String> onewayStations) {
        List<String> manchette = new ArrayList<>();
        String station = onewayStations.remove(0);
        manchette.add(station);

        List<String> neighbors = getNeighborsAsList(graph, station);

        while (!neighbors.isEmpty()) {
            String neighbor = neighbors.remove(0);
            if (onewayStations.contains(neighbor)) {
                manchette.add(neighbor);
                onewayStations.remove(neighbor);
                neighbors.addAll(getNeighborsAsList(graph, neighbor));
            }
        }
        return manchette;
    }

    // Add the border stations to the manchettes
    private static void addBorderStationsToManchettes(Graph<String, String> graph, List<List<String>> manchettes) {
        for (List<String> manchette : manchettes) {
            List<String> borderStations = new ArrayList<>(Arrays.asList(manchette.get(0), manchette.get(manchette.size() - 1)));
            for (String borderStation : borderStations) {
                List<String> neighbors = getNeighborsAsList(graph, borderStation);
                for (String neighbor : neighbors) {
                    if (!manchette.contains(neighbor)) {
                        if (borderStation.equals(manchette.get(0))) {
                            manchette.add(0, neighbor); // Add neighbor at the start of the manchette
                        } else {
                            manchette.add(neighbor); // Add neighbor at the end of the manchette
                        }
                    }
                }
            }
        }
    }

    // Get the list of neighbors for a given station
    private static List<String> getNeighborsAsList(Graph<String, String> graph, String station) {
        List<String> neighbors = new ArrayList<>();
        for (String edge : graph.getIncidentEdges(station)) {
            neighbors.addAll(graph.getIncidentVertices(edge));
        }
        neighbors.remove(station); // Remove the original station from the neighbors list
        return neighbors;
    }
}