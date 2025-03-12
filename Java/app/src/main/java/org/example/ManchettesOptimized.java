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

public class ManchettesOptimized {
    // static boolean end = false;

    public static List<List<String>> generateManchettes(Graph<String, String> railNetwork) {
        List<List<String>> manchettes = new ArrayList<>();
        List<String> outliers = outliersList(railNetwork); // extrémités
        Set<String> visitedOutliers = new HashSet<>();
        Set<String> allVisited = new HashSet<>();
        Map<String, List<String>> lines = lineList(railNetwork);

        System.out.println("Lines: " + lines);

        // Trier les outliers par leur apparition dans les lignes
        outliers.sort((o1, o2) -> {
            int size1 = lines.values().stream().filter(line -> line.contains(o1)).mapToInt(List::size).max().orElse(0);
            int size2 = lines.values().stream().filter(line -> line.contains(o2)).mapToInt(List::size).max().orElse(0);
            return Integer.compare(size2, size1); // Trier par ordre décroissant de taille de ligne
        });
        System.out.println("Outliers: " + outliers);

        for (String outlier : outliers) {
            if (!visitedOutliers.contains(outlier)) {
                // Créer une nouvelle manchette tant qu'il y a des outliers non visités
                List<String> manchette = new ArrayList<>();
                Set<String> visited = new HashSet<>();

                manchette.add(outlier);
                visitedOutliers.add(outlier);
                allVisited.add(outlier);
                visited.add(outlier);

                String currentStation = outlier;
                boolean end = false; // Fin de la manchette
                String ligne_reference = null;

                while (!end) {
                    Collection<String> neighbors = railNetwork.getNeighbors(currentStation);

                    // Vérifier s'il n'y a qu'un seul voisin (début de ligne & fin de ligne)
                    if (neighbors.size() == 1) {
                        String nextStation = neighbors.iterator().next();
                        List<String> code_ligne_nextStation = RailNetworkXML.getCodeLignes(nextStation);
                        List<String> code_ligne_Station = RailNetworkXML.getCodeLignes(currentStation);
                        System.out.println("\nDebut de ligne");

                        if (code_ligne_Station.size() == 1) {
                            ligne_reference = code_ligne_Station.get(0);
                        } else if (code_ligne_nextStation.size() == 1) {
                            ligne_reference = code_ligne_nextStation.get(0);
                        }
                        System.out.println(
                                "Première station: " + currentStation + " avec le code ligne: " + code_ligne_Station);
                        if (!visited.contains(nextStation) && ligne_reference != null
                                && code_ligne_nextStation.contains(ligne_reference)) {
                            manchette.add(nextStation);
                            visited.add(nextStation);
                            currentStation = nextStation;
                            allVisited.add(nextStation);
                        } else {
                            end = true; // Éviter une boucle infinie
                        }
                        // oneNeighbor(neighbors,currentStation,ligne_reference, visited,
                        // allVisited,manchette);

                    } else {
                        boolean foundNewStation = false;
                        boolean notyet = false;

                        for (String neighbor : neighbors) {
                            List<String> code_ligne_nextStation = RailNetworkXML.getCodeLignes(neighbor);
                            // System.out.println("\n code_ligne_reference: "+ligne_reference);
                            // System.out.println("currrent station: "+currentStation+"
                            // codeligne"+code_ligne_Station);
                            // System.out.println("next station: "+neighbor+" codeligne
                            // Next"+code_ligne_nextStation);

                            // Si on trouve une station non visitée qui est sur la même ligne, on continue
                            if (!allVisited.contains(neighbor) && !visited.contains(neighbor) && ligne_reference != null
                                    && code_ligne_nextStation.contains(ligne_reference)) {
                                manchette.add(neighbor);
                                visited.add(neighbor);
                                allVisited.add(neighbor);
                                if (!visitedOutliers.contains(neighbor)) {
                                    visitedOutliers.add(neighbor);
                                }
                                currentStation = neighbor;
                                foundNewStation = true;
                                break;
                            }
                        }

                        // if (!foundNewStation && !notyet) {
                        // for (String neighbor : neighbors) {
                        // List<String> code_ligne_nextStation = RailNetworkXML.getCodeLignes(neighbor);

                        // // Si on trouve une station visitée qui est sur la même ligne, on continue
                        // if (!visited.contains(neighbor) && ligne_reference != null &&
                        // code_ligne_nextStation.contains(ligne_reference) ) {
                        // manchette.add(neighbor);
                        // visited.add(neighbor);
                        // allVisited.add(neighbor);
                        // if (!visitedOutliers.contains(neighbor)) {
                        // visitedOutliers.add(neighbor);
                        // }
                        // currentStation = neighbor;
                        // foundNewStation = true;
                        // System.out.println();System.out.println("Case 2 voisins déjà visité sur la
                        // même ligne"+ neighbor+ " "+ligne_reference);
                        // break;
                        // }
                        // }
                        // }

                        // Si on se trouve à une fin de ligne, on cherche si une nouvelle ligne en
                        // prolongement est disponible (prendre la plus grande)
                        if (!foundNewStation && !notyet) {
                            List<String> code_ligne_Station = RailNetworkXML.getCodeLignes(currentStation);
                            System.out.println("Recherche de queue avec Code ligne station: " + code_ligne_Station
                                    + " current station:" + currentStation);
                            int max = 0;
                            String new_ligne_reference = null;
                            Deque<String> queue_max = null;
                            for (String option_code : code_ligne_Station) {

                                if (ligne_reference != null) { // && !option_code.equals(ligne_reference)
                                    Deque<String> queue = null;
                                    if (option_code.equals(ligne_reference)) {
                                        queue = exploreStation(railNetwork, option_code, currentStation, allVisited,
                                                visited, false);
                                        System.out.println("Queue pour le même code: " + queue);
                                    } else if (!option_code.equals(ligne_reference)) {
                                        queue = exploreStation(railNetwork, option_code, currentStation, allVisited,
                                                visited, true);
                                        System.out.println("Queue: " + queue);
                                    }

                                    String lastElement = queue.pollLast();
                                    if (!queue.isEmpty()) {
                                        // Récupère le dernier élément

                                        boolean isPriorityQueue = lastElement.equals("PRIORITE");

                                        if (isPriorityQueue) { // Si la queue actuelle a priorité
                                            if (queue_max == null || !queue_max.peekLast().equals("PRIORITE")
                                                    || queue.size() > max) {
                                                max = queue.size();
                                                queue_max = new LinkedList<>(queue);
                                                new_ligne_reference = option_code;
                                            }
                                        } else { // Si la queue actuelle est une "NO PRIORITE"
                                            if (queue_max == null
                                                    || !queue_max.peekLast().equals("PRIORITE") && queue.size() > max) {
                                                max = queue.size();
                                                queue_max = new LinkedList<>(queue);
                                                new_ligne_reference = option_code;
                                            }
                                        }
                                    }
                                }

                            }

                            if (max > 0) {
                                System.out.println("in max>0");
                                manchette.addAll(queue_max);
                                for (String station : queue_max) {
                                    allVisited.add(station);
                                    if (outliers.contains(station)) {
                                        visitedOutliers.add(station);
                                        end = true;
                                    }
                                    visited.add(station);
                                }
                                currentStation = queue_max.peekLast();
                                ligne_reference = new_ligne_reference;
                                foundNewStation = true;
                                // System.out.println("Manchette: "+manchette);
                                // System.out.println("Current station: "+currentStation);
                                // System.out.println("Ligne reference: "+ligne_reference);
                                System.out.println("Queue max: " + queue_max);

                            } else {
                                notyet = true;
                            }

                        }

                        // Si on ne trouve que des outliers, on termine la manchette
                        if (!foundNewStation && notyet) {

                            for (String neighbor : neighbors) {
                                if (!visited.contains(neighbor)) {
                                    manchette.add(neighbor);
                                    currentStation = neighbor;
                                    foundNewStation = true;
                                    visited.add(neighbor);
                                    if (!visitedOutliers.contains(neighbor)) {
                                        visitedOutliers.add(neighbor);
                                    }

                                    break;
                                }
                            }
                        }

                        // Si aucun voisin utilisable, arrêter la manchette
                        if (!end && !foundNewStation) {
                            end = true;
                        }
                    }
                }

                // Ajouter la manchette complète
                manchettes.add(manchette);
            }
        }
        return manchettes;
    }

    // private static void oneNeighbor(Collection<String> neighbors,String
    // currentStation,String ligne_reference,Set<String> visited,Set<String>
    // allVisited,List<String> manchette){
    // String nextStation = neighbors.iterator().next();
    // List<String> code_ligne_nextStation = RailNetworkXML.getCodeLignes(nextStation);
    // List<String> code_ligne_Station = RailNetworkXML.getCodeLignes(currentStation);
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

    private static Deque<String> exploreStation(Graph<String, String> railNetwork, String option_code,
            String curentStation, Set<String> allVisited, Set<String> visited, boolean priority) {
        Deque<String> queue = new LinkedList<>();
        boolean end_line = false;

        if (priority) {
            while (!end_line) {
                boolean foundOne = false;
                Collection<String> neighbors = railNetwork.getNeighbors(curentStation);

                for (String neighbor : neighbors) {
                    List<String> code_ligne_neighbor = RailNetworkXML.getCodeLignes(neighbor);
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
                    List<String> code_ligne_neighbor = RailNetworkXML.getCodeLignes(neighbor);
                    if (!visited.contains(neighbor) && code_ligne_neighbor.contains(option_code)) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                        allVisited.add(neighbor);
                        curentStation = neighbor;
                        foundOne = true;
                        break;
                    }
                }

                // if (RailNetworkXML.getCodeLignes(curentStation).size() > 1) {
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
            List<String> code_lignes = RailNetworkXML.getCodeLignes(station);
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