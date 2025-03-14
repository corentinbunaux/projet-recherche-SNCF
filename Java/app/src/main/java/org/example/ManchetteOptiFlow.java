package org.example;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ManchetteOptiFlow {
    
    private static List<List<String>> manchettes = new ArrayList<>();
    private static List<String> outliers = new ArrayList<>();
    private static Set<String> allVisited = new HashSet<>();
    private static Set<String> visitedOutliers = new HashSet<>();
    private static Map<String, List<String>> lines;

    

    public static List<List<String>> generateManchettes(Graph<String, String> railNetwork, Map<String, Point2D> positions) {
        outliers = outliersList(railNetwork); 
        lines = lineList(railNetwork);
        //System.out.println("outliers"+ outliers);

        sortOutliers();
        //System.out.println(lines);

        List<List<String>> allPaths=generateAllPathsFromOutliers(railNetwork);

        
        for (int i = 0; i < allPaths.size(); i++) {
            List<String> manchette = allPaths.get(i);
            double distance = calculateDistanceManchette(manchette, positions);
        }

        Map<String, List<String>> stationsInFlow = Flow.getStationsInFlow();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("stationsInFlow.txt"))) {
            for (Map.Entry<String, List<String>> entry : stationsInFlow.entrySet()) {
                writer.write("Station: " + entry.getKey() + "\n");
                writer.write("Connected Stations: " + entry.getValue() + "\n\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Récupérer toutes les stations du sous-graphe et calculer leur affluence
        for (String station : railNetwork.getVertices()) {
            int affluence = Flow.affluenceTotaleStation(station);
            System.out.println("Station: " + station + ", Affluence: " + affluence);
        }


        // for (int i = 0; i < Math.min(20, allPaths.size()); i++) {
        //     System.out.println(allPaths.get(i));
        //     System.out.println();
        // }
        // System.out.println("Taille de allPaths: " + allPaths.size());

        //Map<String, List<List<String>>> manchettePossibles=generateManchettesByLines(railNetwork);
        //System.out.println(manchettePossibles);



        return manchettes;
    }

    // private static void oneNeighbor(Collection<String> neighbors,String
    // currentStation,String ligne_reference,Set<String> visited,Set<String>
    // allVisited,List<String> manchette){
    // String nextStation = neighbors.iterator().next();
    // List<String> code_ligne_nextStation = RailNetwork.getCodeLignes(nextStation);
    // List<String> code_ligne_Station = RailNetwork.getCodeLignes(currentStation);
    // System.out.println("Début de ligne");

    // if (code_ligne_Station.size() == 1){
    // ligne_reference=code_ligne_Station.get(0);
    // }
    // else if (code_ligne_nextStation.size() == 1){
    // ligne_reference=code_ligne_nextStation.get(0);
    // }

    // if (!visited.contains(nextStation) && ligne_reference != null &&
    // code_ligne_nextStation.contains(ligne_reference)) {
    // manchette.add(nextStation);
    // visited.add(nextStation);
    // currentStation = nextStation;
    // allVisited.add(nextStation);
    // }
    // else {
    // end = true; // Éviter une boucle infinie
    // }
    // }

    private static List<List<String>> generateAllPathsFromOutliers(Graph<String, String> railNetwork) {
        List<List<String>> allPaths = new ArrayList<>();
        for (int i = 0; i < outliers.size(); i++) {
            for (int j = i + 1; j < outliers.size(); j++) { // J commence à i+1 pour éviter les doublons
                String startOutlier = outliers.get(i);
                String endOutlier = outliers.get(j);
                if (!startOutlier.equals(endOutlier)) {
                    Set<String> visited = new HashSet<>();
                    List<String> currentPath = new ArrayList<>();
                    currentPath.add(startOutlier);
                    generatePaths(railNetwork, startOutlier, endOutlier, visited, currentPath, allPaths);
                }
            }
        }
        return allPaths;
    }

    private static void generatePaths(Graph<String, String> railNetwork, String currentStation, String endOutlier, Set<String> visited, List<String> currentPath, List<List<String>> allPaths) {
        visited.add(currentStation);
        if (currentStation.equals(endOutlier)) {
            allPaths.add(new ArrayList<>(currentPath));
            visited.remove(currentStation);
            return;
        }

        Collection<String> neighbors = railNetwork.getNeighbors(currentStation);
        for (String neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                currentPath.add(neighbor);
                generatePaths(railNetwork, neighbor, endOutlier, visited, currentPath, allPaths);
                currentPath.remove(currentPath.size() - 1);
            }
        }
        visited.remove(currentStation);
    }

    public static double scoreManchette(List<String> manchette, Map<String, Point2D> positions, Graph<String, String> railNetwork) {
        double scoring=0;

        double manchetteDistance = calculateDistanceManchette(manchette, positions);
        double totalGraphDistance = calculateGraphDistance(railNetwork, positions);
        double distanceScore = manchetteDistance / totalGraphDistance;

        double affluenceScore = 0.0;
        for (String station : manchette) {
            affluenceScore += Flow.affluenceTotaleStation(station);
        }

        scoring=distanceScore+affluenceScore;
        return scoring;
    }

    public static double calculateDistanceManchette(List<String> manchette, Map<String, Point2D> positions) {
        double totalDistance = 0.0;
    
        for (int i = 0; i < manchette.size() - 1; i++) {
            String vertex1 = manchette.get(i);
            String vertex2 = manchette.get(i + 1);
    
            // Vérifier si les positions existent
            if (!positions.containsKey(vertex1) || !positions.containsKey(vertex2)) {
                continue; // Ignorer les stations sans coordonnées
            }
    
            // Convertir en coordonnées WGS84
            Point2D point1 = RailNetwork.convertToWGS84(positions, vertex1);
            Point2D point2 = RailNetwork.convertToWGS84(positions, vertex2);
    
            // Ajouter la distance entre les deux stations
            totalDistance += RailNetwork.haversineDistance(point1, point2);
        }
    
        return totalDistance;
    }
        
    public static double calculateGraphDistance(Graph<String, String> railNetwork, Map<String, Point2D> positions) {
        double totalDistance = 0;
    
        for (String edge : railNetwork.getEdges()) {
            Pair<String> endpoints = railNetwork.getEndpoints(edge);
            String vertex1 = endpoints.getFirst();
            String vertex2 = endpoints.getSecond();
    
            Point2D point1 = RailNetwork.convertToWGS84(positions, vertex1);
            Point2D point2 = RailNetwork.convertToWGS84(positions, vertex2);
    
            totalDistance += RailNetwork.haversineDistance(point1, point2);
        }
    
        return totalDistance;
    }
    
    private static void sortOutliers() {
        outliers.sort((o1, o2) -> {
            int size1 = lines.values().stream().filter(line -> line.contains(o1)).mapToInt(List::size).max().orElse(0);
            int size2 = lines.values().stream().filter(line -> line.contains(o2)).mapToInt(List::size).max().orElse(0);
            return Integer.compare(size2, size1); // Trier par ordre décroissant de taille de ligne
        });
    }

    private static Map<String, List<List<String>>> generateManchettesByLines(Graph<String, String> railNetwork) {
        Map<String, List<List<String>>> outlierToManchettesMap = new HashMap<>();
        for (String outlier : outliers) {
            List<String> code_lignes = RailNetwork.getCodeLignes(outlier);
            List<List<String>> manchettesForOutlier = new ArrayList<>();

            for (String code_ligne : code_lignes) {
                List<String> manchette = new ArrayList<>();
                manchette.add(outlier);
                Collection<String> stations = railNetwork.getVertices();
                for (String station : stations) {
                    if (RailNetwork.getCodeLignes(station).contains(code_ligne) && !station.equals(outlier)) {
                    manchette.add(station);
                    }
                }
                if (manchette.size() > 1) {
                    manchettesForOutlier.add(manchette);
                }
                
            }

            if (!manchettesForOutlier.isEmpty()) {
                outlierToManchettesMap.put(outlier, manchettesForOutlier);
            }
        }

        return outlierToManchettesMap;
    }
    
  

    private static Deque<String> exploreStation(Graph<String, String> railNetwork, String option_code,
            String curentStation, Set<String> allVisited, Set<String> visited, boolean priority) {
        Deque<String> queue = new LinkedList<>();
        boolean end_line = false;

        if (priority) {
            while (!end_line) {
                boolean foundOne = false;
                Collection<String> neighbors = railNetwork.getNeighbors(curentStation);

                for (String neighbor : neighbors) {
                    List<String> code_ligne_neighbor = RailNetwork.getCodeLignes(neighbor);
                    if (!allVisited.contains(neighbor) && !visited.contains(neighbor)
                            && code_ligne_neighbor.contains(option_code)) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                        allVisited.add(neighbor);
                        curentStation = neighbor;
                        foundOne = true;
                        break;
                    }
                }
                if (!foundOne) {
                    queue.add("PRIORITE");
                    end_line = true;
                }
            }
        }

        else if (!priority) {
            while (!end_line) {
                boolean foundOne = false;
                Collection<String> neighbors = railNetwork.getNeighbors(curentStation);

                for (String neighbor : neighbors) {
                    List<String> code_ligne_neighbor = RailNetwork.getCodeLignes(neighbor);
                    if (!visited.contains(neighbor) && code_ligne_neighbor.contains(option_code)) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                        allVisited.add(neighbor);
                        curentStation = neighbor;
                        foundOne = true;
                        break;
                    }
                }

                // if (RailNetwork.getCodeLignes(curentStation).size() > 1) {
                // end_line = true;
                // }

                if (!foundOne) {
                    queue.add("NO PRIORITE");
                    end_line = true;
                }
            }
        }

        return queue;
    }

    private static List<String> outliersList(Graph<String, String> railNetwork) {
        List<String> outliers = new ArrayList<>();

        for (String station : railNetwork.getVertices()) {
            System.out.print(station);
            System.out.println(railNetwork.getNeighborCount(station));
            if (railNetwork.getNeighborCount(station) <= 1) {
                outliers.add(station);
            }
        }
        return outliers;
    }

    private static Map<String, List<String>> lineList(Graph<String, String> railNetwork) {
        Map<String, List<String>> lines = new HashMap<>();
        for (String station : railNetwork.getVertices()) {
            List<String> code_lignes = RailNetwork.getCodeLignes(station);
            for (String code_ligne : code_lignes) {
                if (!lines.containsKey(code_ligne)) {
                    lines.put(code_ligne, new ArrayList<>());
                }
                if (!lines.get(code_ligne).contains(station)) {
                    lines.get(code_ligne).add(station);
                }
            }
        }
        return lines;
    }

    // Affichage des manchettes
    public static void printManchettes(List<List<String>> manchettes) {
        System.out.println("Number of manchettes: " + manchettes.size());
        System.out.println("List :");
        for (List<String> manchette : manchettes) {
            System.out.println(manchette);
            System.out.println();
        }
    }
}