package org.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;

public class TopologyBasedManchetteGenerator {
    //static boolean end = false;

    public static List<List<String>> generateManchettes(Graph<String, String> railNetwork) {
        List<List<String>> manchettes = new ArrayList<>();
        List<String> outliers = outliersList(railNetwork); // extrémités
        Set<String> visitedOutliers = new HashSet<>();
        Set<String> allVisited = new HashSet<>();

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
                String ligne_reference=null;

                while (!end) {
                    Collection<String> neighbors = railNetwork.getNeighbors(currentStation);

                    // Vérifier s'il n'y a qu'un seul voisin (début de ligne & fin de ligne)
                    if (neighbors.size() == 1) {
                        String nextStation = neighbors.iterator().next();
                        List<String> code_ligne_nextStation = RailNetworkXML.getCodeLignes(nextStation);
                        List<String> code_ligne_Station = RailNetworkXML.getCodeLignes(currentStation);
                        System.out.println("\nDebut de ligne");
                        

                        if (code_ligne_Station.size() == 1){
                            ligne_reference=code_ligne_Station.get(0);
                        }
                        else if (code_ligne_nextStation.size() == 1){
                            ligne_reference=code_ligne_nextStation.get(0);
                        }

                        if (!visited.contains(nextStation) && ligne_reference != null && code_ligne_nextStation.contains(ligne_reference)) {
                            manchette.add(nextStation);
                            visited.add(nextStation);
                            currentStation = nextStation;
                            allVisited.add(nextStation);
                        }
                        else {
                            end = true; // Éviter une boucle infinie
                        }
                        //oneNeighbor(neighbors,currentStation,ligne_reference, visited, allVisited,manchette);

                    } else {
                        boolean foundNewStation = false;
                        boolean notyet=false;
                        System.out.println("visité: "+visited);
                        for (String neighbor : neighbors) {
                            List<String> code_ligne_nextStation = RailNetworkXML.getCodeLignes(neighbor);
                            //List<String> code_ligne_Station = RailNetworkXML.getCodeLignes(currentStation);
                            
                            // System.out.println("\n code_ligne_reference: "+ligne_reference);
                            // System.out.println("currrent station: "+currentStation+" codeligne"+code_ligne_Station);
                            // System.out.println("next station: "+neighbor+" codeligne Next"+code_ligne_nextStation);

                            // Si on trouve une station non visitée qui est sur la même ligne, on continue
                            if (!allVisited.contains(neighbor) && !visited.contains(neighbor) && ligne_reference != null && code_ligne_nextStation.contains(ligne_reference) ) {
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

                        // Si on se trouve à une fin de ligne, on cherche si une nouvelle ligne en prolongement est disponible (prendre la plus grande)
                        if (!foundNewStation && !notyet) {
                            System.out.println("Recherche de queue");
                            List<String> code_ligne_Station = RailNetworkXML.getCodeLignes(currentStation);
                            int max=0;
                            String new_ligne_reference=null;
                            Deque<String> queue_max = new LinkedList<>();
                            for (String option_code : code_ligne_Station) {  
                                int count = 0;
                                for (String neighbor : neighbors) {
                                    List<String> code_ligne_neighbor = RailNetworkXML.getCodeLignes(neighbor);
                                    if (!allVisited.contains(neighbor) && !visited.contains(neighbor) && code_ligne_neighbor.contains(option_code)) {
                                        count++;
                                    }
                                }
                                if (count < 2) {
                                    if (ligne_reference != null && !option_code.equals(ligne_reference)) {
                                        System.out.println("Option code: "+option_code);
                                        Deque<String> queue=exploreStation(railNetwork, option_code, currentStation, allVisited, visited);
                                        System.out.println("Queue: "+queue);   
                                        if (queue.size() > max) {
                                            max = queue.size();
                                            queue_max = queue;
                                            new_ligne_reference=option_code;
                                        }
                                    }
                                }
                            }

                            if (max > 0) {
                                manchette.addAll(queue_max);
                                for (String station : queue_max) {
                                    allVisited.add(station);
                                    if (outliers.contains(station)) {
                                        visitedOutliers.add(station);
                                        end=true;
                                    }
                                    visited.add(station);
                                }
                                currentStation = queue_max.peekLast();
                                ligne_reference = new_ligne_reference;
                                foundNewStation = true;
                                System.out.println("Manchette: "+manchette);
                                System.out.println("Current station: "+currentStation);
                                System.out.println("Ligne reference: "+ligne_reference);
                                System.out.println("Queue max: "+queue_max);
                                
                            }
                            
                        }
    
                        // Si on ne trouve que des outliers, on termine la manchette
                        if (!foundNewStation && notyet) {
                            for (String neighbor : neighbors) {
                                if (!visitedOutliers.contains(neighbor)) {
                                    manchette.add(neighbor);
                                    visitedOutliers.add(neighbor);
                                    end = true;
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


    // private static void oneNeighbor(Collection<String> neighbors,String currentStation,String ligne_reference,Set<String> visited,Set<String> allVisited,List<String> manchette){
    //     String nextStation = neighbors.iterator().next();
    //     List<String> code_ligne_nextStation = RailNetworkXML.getCodeLignes(nextStation);
    //     List<String> code_ligne_Station = RailNetworkXML.getCodeLignes(currentStation);
    //     System.out.println("Début de ligne");

    //     if (code_ligne_Station.size() == 1){
    //         ligne_reference=code_ligne_Station.get(0);
    //     }
    //     else if (code_ligne_nextStation.size() == 1){
    //         ligne_reference=code_ligne_nextStation.get(0);
    //     }

    //     if (!visited.contains(nextStation) && ligne_reference != null && code_ligne_nextStation.contains(ligne_reference)) {
    //         manchette.add(nextStation);
    //         visited.add(nextStation);
    //         currentStation = nextStation;
    //         allVisited.add(nextStation);
    //     }
    //     else {
    //         end = true; // Éviter une boucle infinie
    //     }
    // }

    private static Deque<String> exploreStation(Graph<String, String> railNetwork, String option_code, String curentStation,Set<String> allVisited, Set<String> visited) {
        Deque<String> queue = new LinkedList<>();
        boolean end_line = false;
        
        
        while (!end_line) {
            boolean foundOne=false;
            boolean forced_to_stop=false;
            Collection<String> neighbors = railNetwork.getNeighbors(curentStation);
            for (String neighbor : neighbors) {
                List<String> code_ligne_neighbor = RailNetworkXML.getCodeLignes(neighbor);
                if (!allVisited.contains(neighbor) && !visited.contains(neighbor)&&code_ligne_neighbor.contains(option_code)) {
                    System.out.println("Neighbor: "+neighbor);
                    queue.add(neighbor);
                    visited.add(neighbor);
                    allVisited.add(neighbor);  
                    curentStation = neighbor;
                    foundOne=true;
                    break;
                } 
                else if (allVisited.contains(neighbor) && !visited.contains(neighbor)&&code_ligne_neighbor.contains(option_code)){
                    System.out.println("Neighbor: "+neighbor);
                    queue.add(neighbor);
                    visited.add(neighbor);
                    allVisited.add(neighbor);
                    curentStation = neighbor;
                    foundOne=true;
                    break;
                }
                    
            }
            if (!foundOne) {
                end_line = true;
            }
        }
        
        return queue;        
    }




    private static List<String> outliersList(Graph<String, String> railNetwork) {
        List<String> outliers = new ArrayList<>();
        for (String station : railNetwork.getVertices()) {
            if (railNetwork.getOutEdges(station).size() <= 1) {
                outliers.add(station);
            }
        }
        return outliers;
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