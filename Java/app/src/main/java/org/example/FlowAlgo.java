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
 *    qui sont des noeuds du réseau les plus visitées (exemple : la station B). Puis, on s'intéresse au flux entre la station la 
 *    plus affluente et ses voisines (voir schéma). On identifie quelle gare voisine possède le flux le plus maximal avec la station
 *    noeudale (exemple : station C). On identifie la manchette qui contient ces deux stations comme la manchette principale à fusionner.
 *    On regarde ensuite parmi les manchettes restantes (A-B, B-D, B-E) celle qui a le plus de flux en commun avec la manchette principale
 *    (B-C). On fusionne les deux manchettes pour obtenir une manchette unique. 
 *    
 *    NOTE : la manchette principale est extraite des manchettes restantes à traiter. En revanche, les manchettes restantes ne sont pas
 *    sont issues de l'ensemble des manchettes de base. 
 *  
 *    EXEMPLE :
 *    Lors de la première itération, B-D est la manchette principale (par exemple), fusionnée avec A-B, la manchette fusionnée sera
 *    A-B-D.
 *    La liste des manchettes devient alors : [A-B-D, B-C, B-E].
 *    Puis, lors de la seconde itération, on regarde parmi les manchettes restantes (B-E et B-C) pour la séléction de la 
 *    manchette principale. Prenons B-E. Pour séléctionner la manchette avec laquelle B-E a le plus de flux en commun, on regarde
 *    toutes les manchettes (A-B, B-D, B-C) autres que B-E. Une solution peut être de fusionner B-E avec B-C pour obtenir E-B-C, mais
 *    il est tout a fait possible de fusionner B-E avec B-D pour obtenir B-D-E, si B-E et B-D partagent plus de flux en commun que B-E et 
 *    B-C. Dans ce second cas, il n'y a pas de réduction du nombre de manchettes, mais simplement amélioration de leur représentation. 
 *    La minimmisation du nombre de manchettes se fera par la suite.
 * 
 *                  D
 *                  |
 *    A ----------- B ----------- C
 *                  |
 *                  E
 * 
 * 
 *    On applique cet algorithme sur chaque noeud du réseau, de sorte a former des manchettes plus grandes, représentant les flux
 *    de trains au niveau des noeuds (qui sont les points les plus affluents du réseau).
 * 
 *    PROBLEMES RENCONTRES : 
 *    - Certains flux manquent entre les stations voisines. La manchette B-E existe sur le réseau, mais nous n'avons pas de flux la 
 *      décrivant, car la gare E n'est pas présente dans le fichier décrivant les flux du réseau.
 *    - Initialement, les manchettes générées ne reposent que sur les lignes droites du réseau, en ommettant les noeuds voisins.
 *      Il a fallu ajouter des manchettes pour décrire ces liens manquants, à l'extérieur de la phase de génération de manchettes, 
 *      lors de la récupération des manchettes pour un noeud (voir la fonction addMissingManchettesForNeighboringKnots).
 *      Exemple : la manchette B-C est ajoutée à la liste des manchettes car B et C sont voisins, mais la manchette n'est pas générée
 *      lors de la phase de génération de manchettes (aucune gare de passage entre les deux).
 * 
 *                      |             | 
 *                      D             G
 *                      |             |  
 *    --- A ----------- B ----------- C ------- F -----
 *                      |
 *                      E
 * 
 * 3. Après avoir fusionné des manchettes selon les flux, il reste à réduire leur nombre.
 *    On fusionne les manchettes qui ont une partie commune, autre que leur noeud.
 *    Exemple : A-B-C et B-C-D sont fusionnées en A-B-C-D, mais A-B-C et C-D ne sont pas fusionnées.
 */

package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.units.qual.C;
import org.checkerframework.checker.units.qual.m;

import java.util.HashMap;

import edu.uci.ics.jung.graph.Graph;

public class FlowAlgo {

    public static void manchetteBasedFlow(Graph<String, String> graph) {
        // List of stations for each flow
        Map<String, List<String>> stationsInFlow = Flow.getStationsInFlow();

        // List of flows that go through at least one station of the graph
        List<String> flowsWallet = flowsWallet(graph, stationsInFlow);

        // Basic manchettes generation based on straight lines on the rail network
        List<List<String>> initialManchettes = manchettesOneways(graph);

        // Case where two knots are neighbors, therefore there is no manchette between
        // them, need to create one
        for (String knot : getKnotsAsIC(graph)) {
            addMissingManchettesForNeighboringKnots(graph, RailNetwork.getName(knot), initialManchettes);
        }

        // Sort the knots by their affluence
        List<String> mostVisitedknotsAsIC = getMostVisitedStations(getKnotsAsIC(graph), flowsWallet, stationsInFlow,
                graph);

        // Improve the manchettes based on the flows
        List<List<String>> improvedManchettes = new ArrayList<>(initialManchettes);

        // for ech knot, we try to merge the two best manchettes for a given knot
        for (int i = 0; i < mostVisitedknotsAsIC.size(); i++) {
            String mostVisitedKnotAsIC = mostVisitedknotsAsIC.get(i);
            boolean flowsBetweenManchettes = true;
            // while there is a possibility of merging manchettes, we continue
            while (flowsBetweenManchettes) {
                flowsBetweenManchettes = mergeTheTwoBestManchettesForGivenKnot(graph, improvedManchettes, flowsWallet,
                        stationsInFlow,
                        mostVisitedKnotAsIC, initialManchettes);
            }
        }

        // Once the manchettes have been improved, we merge the manchettes that have a
        // common part to reduce their number
        mergeManchettesWithCommonPart(graph, improvedManchettes);

        System.out.println("Manchettes after improvement : " + improvedManchettes.size());
    }

    private static void mergeManchettesWithCommonPart(Graph<String, String> graph,
            List<List<String>> improvedManchettes) {
        for (int i = 0; i < improvedManchettes.size(); i++) {
            List<String> manchetteSelected = improvedManchettes.get(i);
            List<List<String>> manchettesWithCommonParts = getManchettesWithCommonParts(improvedManchettes,
                    manchetteSelected, graph);
        }
    }

    private static List<List<String>> getManchettesWithCommonParts(List<List<String>> improvedManchettes,
            List<String> givenManchette, Graph<String, String> graph) {
        List<List<String>> manchettesWithCommonParts = new ArrayList<>();
        for (List<String> manchette : improvedManchettes) {
            if (manchette.equals(givenManchette)) {
                continue;
            }
            // Check if the manchettes have a common part except for the knots
            List<String> knotsAsIC = getKnotsAsIC(graph);
            boolean hasCommonPart = hasCommonPart(manchette, givenManchette, knotsAsIC);
            // System.out.println("Manchette " + manchette + " has common part with " +
            // givenManchette + " : "
            // + hasCommonPart);
            // System.out.println();
        }
        return manchettesWithCommonParts;
    }

    private static boolean hasCommonPart(List<String> manchette, List<String> givenManchette, List<String> knotsAsIC) {
        // Get the list of stations in common between two manchettes, excluding the
        // knots
        List<String> commonStations = new ArrayList<>(manchette);
        commonStations.retainAll(givenManchette);
        commonStations.removeAll(knotsAsIC);

        if (!commonStations.isEmpty()) {
            return true;
        }
        return false;
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

    // Function to merge the two best manchettes for a given knot, returns true if
    // the manchettes have been merged, false otherwise
    // The manchettes can't be merged if there are no flows between them, or if they
    // are empty
    private static boolean mergeTheTwoBestManchettesForGivenKnot(Graph<String, String> graph,
            List<List<String>> improvedManchettes, List<String> flowsWallet, Map<String, List<String>> stationsInFlow,
            String mostVisitedKnotAsIC, List<List<String>> initialManchettes) {
        List<String> knotsAsIC = getKnotsAsIC(graph);

        // Look for the manchettes that contain the most visited knot
        List<List<String>> allManchettesForKnot = getManchettesForKnot(mostVisitedKnotAsIC, initialManchettes, graph);
        List<List<String>> manchettesForKnotNotMergedYet = getManchettesForKnot(mostVisitedKnotAsIC, improvedManchettes,
                graph);

        // For each manchette that contains the most visited knot, get the flows that go
        // through it
        Map<List<String>, List<String>> flowsForManchetteMap = new HashMap<>();
        for (List<String> manchette : allManchettesForKnot) {
            List<String> flowsForManchette = flowsForManchette(manchette, flowsWallet, stationsInFlow, knotsAsIC);
            flowsForManchetteMap.put(manchette, flowsForManchette);
        }

        // Sort the manchettes by the number of flows that go through them
        List<Map.Entry<List<String>, List<String>>> sortedManchettes = new ArrayList<>(flowsForManchetteMap.entrySet());
        sortedManchettes.sort((entry1, entry2) -> entry2.getValue().size() - entry1.getValue().size());

        // Sort the manchettes that have not been merged yet by the number of flows that
        // go through them
        List<List<String>> sortedManchettesNotMergedYet = new ArrayList<>();
        for (Map.Entry<List<String>, List<String>> entry : sortedManchettes) {
            if (manchettesForKnotNotMergedYet.contains(entry.getKey())) {
                sortedManchettesNotMergedYet.add(entry.getKey());
            }
        }

        boolean hasFlows = false;
        for (List<String> manchette : sortedManchettesNotMergedYet) {
            if (flowsForManchetteMap.get(manchette).size() > 0) {
                hasFlows = true;
                break;
            }
        }
        if (!hasFlows) {
            return false;
        }

        // Get the manchette with the most flows
        List<String> manchette1 = sortedManchettesNotMergedYet.get(0);

        // Get the manchette with the most flows in common with the first manchette
        List<String> manchette2 = getManchetteWithMostFlowsInCommon(allManchettesForKnot, flowsForManchetteMap,
                manchette1);

        // Ensure that both manchettes are not empty before merging
        if (manchette1.isEmpty() || manchette2.isEmpty()) {
            return false;
        }

        // Merge the two manchettes that have the most flows in common
        completeMergeManchettes(manchette1, manchette2, improvedManchettes, flowsWallet, stationsInFlow);

        return true;
    }

    private static List<String> mergeManchettes(List<String> manchette1, List<String> manchette2) {
        if (manchette1.isEmpty() || manchette2.isEmpty()) {
            throw new IllegalArgumentException("Manchettes cannot be empty");
        }
        if (manchette1.get(manchette1.size() - 1).equals(manchette2.get(0))) {
            List<String> mergedManchette = new ArrayList<>(manchette1);
            mergedManchette.addAll(manchette2.subList(1, manchette2.size()));
            return mergedManchette;
        } else if (manchette1.get(0).equals(manchette2.get(manchette2.size() - 1))) {
            List<String> mergedManchette = new ArrayList<>(manchette2);
            mergedManchette.addAll(manchette1.subList(1, manchette1.size()));
            return mergedManchette;
        } else if (manchette1.get(0).equals(manchette2.get(0))) {
            Collections.reverse(manchette1);
            List<String> mergedManchette = new ArrayList<>(manchette1);
            mergedManchette.addAll(manchette2.subList(1, manchette2.size()));
            return mergedManchette;
        } else if (manchette1.get(manchette1.size() - 1).equals(manchette2.get(manchette2.size() - 1))) {
            Collections.reverse(manchette2);
            List<String> mergedManchette = new ArrayList<>(manchette1);
            mergedManchette.addAll(manchette2.subList(1, manchette2.size()));
            return mergedManchette;
        }
        return new ArrayList<>();
    }

    private static void completeMergeManchettes(List<String> manchette1, List<String> manchette2,
            List<List<String>> improvedManchettes, List<String> flowsWallet, Map<String, List<String>> stationsInFlow) {
        List<String> mergedManchette = mergeManchettes(manchette1, manchette2);
        improvedManchettes.remove(manchette1);
        improvedManchettes.remove(manchette2);
        improvedManchettes.add(mergedManchette);
        System.out.println("Merged manchettes " + manchette1 + " and " + manchette2 +
                " to get " + mergedManchette);
    }

    private static List<String> getManchetteWithMostFlowsInCommon(List<List<String>> manchettesForKnot,
            Map<List<String>, List<String>> flowsForManchetteMap, List<String> manchetteToCompareWith) {
        List<String> manchetteWithMostFlowsInCommon = new ArrayList<>();
        for (List<String> manchette : manchettesForKnot) {
            if (manchette.equals(manchetteToCompareWith)) {
                continue;
            }
            List<String> flowsInCommonWithManchetteToCompare = new ArrayList<>();
            for (String flow : flowsForManchetteMap.get(manchette)) {
                if (flowsForManchetteMap.get(manchetteToCompareWith).contains(flow)) {
                    flowsInCommonWithManchetteToCompare.add(flow);
                }
            }
            // Retrieve the manchette with the maximum of flows in common
            if (manchetteWithMostFlowsInCommon.size() < flowsInCommonWithManchetteToCompare.size()) {
                manchetteWithMostFlowsInCommon = manchette;
            }
        }

        return manchetteWithMostFlowsInCommon;
    }

    private static List<List<String>> getManchettesForKnot(String knotIC, List<List<String>> manchettes,
            Graph<String, String> graph) {
        List<List<String>> manchettesForKnot = new ArrayList<>();
        String knotName = RailNetwork.getName(knotIC);

        for (List<String> manchette : manchettes) {
            if (manchette.get(0).equals(knotName) || manchette.get(manchette.size() - 1).equals(knotName)) {
                manchettesForKnot.add(manchette);
            }
        }
        return manchettesForKnot;
    }

    private static void addMissingManchettesForNeighboringKnots(Graph<String, String> graph, String knotName,
            List<List<String>> manchettesForKnot) {
        int expectedNumberOfManchettes = graph.getNeighborCount(knotName);
        if (manchettesForKnot.size() != expectedNumberOfManchettes) {
            // Create missing manchettes for neighboring knots
            Collection<String> neighbors = graph.getNeighbors(knotName);
            for (String neighbor : neighbors) {
                if (graph.getNeighborCount(neighbor) > 2) {
                    List<String> newManchette = Arrays.asList(knotName, neighbor);
                    if (!manchettesForKnot.contains(newManchette)
                            && !manchettesForKnot.contains(Arrays.asList(neighbor, knotName))) {
                        manchettesForKnot.add(newManchette);
                        System.out.println("Adding a manchette between " + knotName + " and " + neighbor);
                    }
                }
            }
        }
    }

    private static List<String> flowsForManchette(List<String> manchette, List<String> flowsWallet,
            Map<String, List<String>> stationsInFlow, List<String> knotAsIC) {
        List<String> flowsForManchette = new ArrayList<>();

        // Get the manchette without the knots, except for manchettes composed of 2
        // knots
        List<String> manchetteWithoutKnots = extractManchetteWithoutKnots(manchette, knotAsIC);

        for (String flow : flowsWallet) {
            List<String> stationsIC = stationsInFlow.get(flow);
            for (String stationInManchette : manchetteWithoutKnots) {
                // Case where the manchette is composed of knots only
                if (knotAsIC.contains(RailNetwork.getCodeImmu(stationInManchette))) {
                    // Look for common flows between the two knots
                    if (stationsIC.contains(RailNetwork.getCodeImmu(manchette.get(0)))
                            && stationsIC.contains(RailNetwork.getCodeImmu(manchette.get(1)))) {
                        flowsForManchette.add(flow);
                        break;
                    }
                } else if (stationsIC.contains(RailNetwork.getCodeImmu(stationInManchette))) {
                    flowsForManchette.add(flow);
                    break;
                }
            }
        }
        return flowsForManchette;
    }

    private static List<String> extractManchetteWithoutKnots(List<String> stationsNames, List<String> knotAsIC) {
        List<String> manchette = new ArrayList<>(stationsNames);
        // Case where the manchette has only 2 knots since they are neighbors
        if (manchette.size() <= 2) {
            return manchette;
        }
        if (knotAsIC.contains(RailNetwork.getCodeImmu(stationsNames.get(0)))) {
            manchette = manchette.subList(1, manchette.size());
        }

        if (knotAsIC.contains(RailNetwork.getCodeImmu(stationsNames.get(stationsNames.size() - 1)))) {
            manchette = manchette.subList(0, manchette.size() - 1);
        }
        return manchette;
    }

    private static int affluenceStationIC(String stationIC, List<String> flowsWallet,
            Map<String, List<String>> stationsInFlow) {
        int cpt = 0;
        for (String flowID : flowsWallet) {
            List<String> listStationsIC = stationsInFlow.get(flowID);
            if (listStationsIC.contains(stationIC)) {
                cpt++;
            }
        }
        return cpt;
    }

    private static int affluenceTotaleStationIC(String stationIC, List<String> flowsWallet,
            Map<String, List<String>> stationsInFlow, Graph<String, String> graph) {
        int affluence = affluenceStationIC(stationIC, flowsWallet, stationsInFlow);
        if (affluence > 0) {
            return affluence;
        }

        Collection<String> neighbors = getNeighborsAsList(graph, RailNetwork.getName(stationIC));
        Collection<String> visited = new ArrayList<>();
        visited.add(RailNetwork.getName(stationIC));

        while (neighbors.size() > 0) {
            String neighbor = neighbors.iterator().next();
            neighbors.remove(neighbor);
            visited.add(neighbor);

            // If the neighbor is a knot, we don't want to go through it
            if (graph.getIncidentEdges(neighbor).size() > 2) {
                continue;
            }

            int affluenceNeighbor = affluenceStationIC(RailNetwork.getCodeImmu(neighbor), flowsWallet, stationsInFlow);
            if (affluenceNeighbor > 0) {
                affluence += affluenceNeighbor;
            } else {
                Collection<String> newNeighbors = getNeighborsAsList(graph, neighbor);
                for (String newNeighbor : newNeighbors) {
                    if (!visited.contains(newNeighbor)) {
                        neighbors.add(newNeighbor);
                    }
                }
            }
        }
        if (affluence > 0) {
            return affluence;
        }
        return 1;
    }

    private static List<String> getMostVisitedStations(List<String> stationsIC, List<String> flowsWallet,
            Map<String, List<String>> stationsInFlow, Graph<String, String> graph) {
        Map<String, Integer> stationAffluence = new HashMap<>();

        for (String stationIC : stationsIC) {
            int affluence = affluenceTotaleStationIC(stationIC, flowsWallet, stationsInFlow, graph);
            stationAffluence.put(stationIC, affluence);
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

    // All the flows that go through at least one station of the graph
    private static List<String> flowsWallet(Graph<String, String> graph, Map<String, List<String>> stationsInFlow) {
        List<String> stationsWallet = new ArrayList<>();

        // Add the flowID to the stationsWallet if the flow goes through at least one
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

        // Create a manchette for linked stations in the onewayStations list
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
            for (String vertex : graph.getIncidentVertices(edge)) {
                if (!vertex.equals(station)) {
                    neighbors.add(vertex);
                }
            }
        }
        return neighbors;
    }
}