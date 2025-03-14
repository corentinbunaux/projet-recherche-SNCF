/*IDEE DE L'ALGO */
/*
 * On récupère les flux de trains dans la zone d'étude par la fonction flowsWallet. La fonction getStationsInFlow donne 
 * l'ensemble des stations pour un flux donné.
 *  
 * 1. On commence par générer des manchettes basiques basées sur les lignes droites du réseau ferroviaire (manchettesOneWays).
 *   On considère les stations qui ont au plus 2 arêtes incidentes comme des stations de passage. On ajoute chacune de ces
 *   stations à une manchette. On continue à ajouter les voisins de chaque station de la manchette jusqu'à ce qu'il n'y ait plus. 
 *   On ajoute en tout dernier les noeuds de chaque manchette, afin de savoir quelle manchettes peuvent être reliées.
 *   
 * 2. On améliore ces manchettes en se basant sur les flux de trains. Dans un premier temps, il faut récupérer les stations 
 *    qui sont des noeuds du réseau les plus visitées. Puis, on s'intéresse au flux entre ces stations et leurs voisines. On
 *    relie les manchettes qui ont le flux le plus maximal entre la gare noeudale et ses voisines. On réitère jusqu'à ce que les 
 *    manchettes initiales ne possèdent qu'à leurs bords les stations isolées (une seule station voisine). 
 * 
 * 
 * PROBLEMES
 * Station Gardanne pas référencée dans le flow.json (pour l'exemple), donc pas de données pour le nombre de visites de cette station...
 * Récupérer l'affluence des gares autour qui ne sont pas un noeud ?? 
 */

package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import edu.uci.ics.jung.graph.Graph;

public class FlowAlgo {

    public static void manchetteBasedFlow(Graph<String, String> graph) {
        Map<String, List<String>> stationsInFlow = Flow.getStationsInFlow(); // List of stations for each flow
        List<String> flowsWallet = flowsWallet(graph, stationsInFlow); // List of flows that go through at least one
                                                                       // station of the graph
        // System.out.println("Flows in this zone : " + stationsWallet);

        List<List<String>> manchettes = manchettesOneways(graph); // Basic manchettes generation based on straight lines
                                                                  // on the rail network
        // System.out.println("Manchettes: " + manchettes);

        manchettes = improveManchettesWithFlows(graph, manchettes, flowsWallet, stationsInFlow); // Imporve manchettes
                                                                                                 // based on flows
        System.out.println("Number of manchettes after improvement : " + manchettes.size());
    }

    public static List<String> getKnotsAsIC(Graph<String, String> graph) {
        List<String> knots = new ArrayList<>();
        for (String vertex : graph.getVertices()) {
            if (graph.getIncidentEdges(vertex).size() > 2) {
                knots.add(RailNetwork.getCodeImmu(vertex));
            }
        }
        return knots;
    }

    private static List<List<String>> improveManchettesWithFlows(Graph<String, String> graph,
            List<List<String>> manchettes, List<String> flowsWallet, Map<String, List<String>> stationsInFlow) {

        List<String> knotsAsIC = getKnotsAsIC(graph);
        for(String knot : knotsAsIC){
            System.out.println(RailNetwork.getName(knot));
        }
        System.out.println();

        List<String> mostVisitedknotsAsIC = getMostVisitedStations(getKnotsAsIC(graph), flowsWallet, stationsInFlow);
        for(String knot : mostVisitedknotsAsIC){
            System.out.println(RailNetwork.getName(knot));
        }
        // Get the most visited stations
        // List<Map.Entry<String, Integer>> sortedStations =
        // mostVisitedStations(flowsWallet, stationsInFlow, graph);
        // for (Map.Entry<String, Integer> entry : sortedEntries) {
        // System.out.println(entry.getKey() + " : " + entry.getValue());
        // }
        // int i = 0;
        // int j = 1;
        // while (isThereAFlowBetweenTwoStations(sortedStations.get(i).getKey(),
        // sortedStations.get(j).getKey(),
        // flowsWallet, stationsInFlow) == null) {
        // i++;
        // j++;
        // }
        // String flowId =
        // isThereAFlowBetweenTwoStations(sortedStations.get(i).getKey(),
        // sortedStations.get(j).getKey(),
        // flowsWallet, stationsInFlow);
        // System.out.println("Possibility of a flow between two of the most visited
        // stations : ");
        // System.out.println(RailNetwork.getName(sortedStations.get(i).getKey()));
        // System.out.println(RailNetwork.getName(sortedStations.get(j).getKey()));
        // System.out.println("Flow ID : " + flowId);
        return manchettes;
    }

    private static String isThereAFlowBetweenTwoStations(String station1, String station2, List<String> flowsWallet,
            Map<String, List<String>> stationsInFlow) {
        for (String flowID : flowsWallet) {
            List<String> stationsIC = stationsInFlow.get(flowID);
            if (stationsIC.contains(station1) && stationsIC.contains(station2)) {
                return flowID;
            }
        }
        return null;
    }

    private static List<String> getMostVisitedStations(List<String> stations, List<String> flowsWallet,
            Map<String, List<String>> stationsInFlow) {
        Map<String, Integer> stationAffluence = new HashMap<>();
        for (String flowID : flowsWallet) {
            List<String> stationsIC = stationsInFlow.get(flowID);
            for (String stationIC : stationsIC) {
                if (stations.contains(stationIC)) {
                    if (stationAffluence.containsKey(stationIC)) {
                        stationAffluence.put(stationIC, stationAffluence.get(stationIC) + 1);
                    } else {
                        stationAffluence.put(stationIC, 1);
                    }
                }
            }
        }

        // Sort the map by values (affluence) in descending order
        List<Map.Entry<String, Integer>> sortedStations = new ArrayList<>(stationAffluence.entrySet());
        sortedStations.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        List<String> sortedStationNames = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedStations) {
            sortedStationNames.add(entry.getKey());
        }
        return sortedStationNames;
    }

    // all the flows that go through at least one station of the graph
    private static List<String> flowsWallet(Graph<String, String> graph, Map<String, List<String>> stationsInFlow) {
        List<String> stationsWallet = new ArrayList<>();

        // add the flowID to the stationsWallet if the flow goes through at least one
        // station of the graph
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
            List<String> borderStations = new ArrayList<>(
                    Arrays.asList(manchette.get(0), manchette.get(manchette.size() - 1)));
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