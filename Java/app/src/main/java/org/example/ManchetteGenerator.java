package org.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;

public class ManchetteGenerator {
    // Génération des manchettes
    public static List<List<String>> generateManchettes(Graph<String, String> railNetwork) {
        if(railNetwork == null) {
            return null;
        }
        List<List<String>> manchettes = new ArrayList<>();
        List<String> outliers = outliersList(railNetwork); // extrémités
        Set<String> visitedOutliers = new HashSet<>();
        Set<String> allVisited = new HashSet<>();
    
        for (String outlier : outliers) {
            if (!visitedOutliers.contains(outlier)) {
                // Créer une nouvelle manchette et ajouter l'outlier
                List<String> manchette = new ArrayList<>();
                manchette.add(outlier);
                visitedOutliers.add(outlier);

                allVisited.add(outlier);
                Set<String> visited = new HashSet<>();
                visited.add(outlier);
                String currentStation = outlier;
                boolean end = false;

                // Tant qu'on n'a pas atteint un autre outlier, on complète la manchette
                while (!end) {
                    Collection<String> neighbors = railNetwork.getNeighbors(currentStation);
                    // System.out.println("Current station: " + currentStation + ", Neighbors: " + neighbors);

                    

                    // Vérifier s'il n'y a qu'un seul voisin (ligne terminée)
                    if (neighbors.size() == 1) {
                        String nextStation = neighbors.iterator().next();
                        List<String> code_ligne_nextStation = RailNetwork.getCodeLignes(nextStation);
                        List<String> code_ligne_Station = RailNetwork.getCodeLignes(currentStation);
                        // System.out.println("Code lignes current station: " + code_ligne_Station+ "curent station: "+currentStation);

                        
                        if (!visited.contains(nextStation) && !Collections.disjoint(code_ligne_nextStation, code_ligne_Station)) {
                            manchette.add(nextStation);
                            visited.add(nextStation);
                            currentStation = nextStation;
                            allVisited.add(nextStation);
                        }
                        else {
                            end = true; // Éviter une boucle infinie
                        }
                    } else {
                        boolean foundNewStation = false;
                        boolean notyet=false;
                        
                        // Parcourir les voisins pour trouver une station non visitée qui n'est pas un outlier
                        for (String neighbor : neighbors) {
                            List<String> code_ligne_nextStation = RailNetwork.getCodeLignes(neighbor);
                            List<String> code_ligne_Station = RailNetwork.getCodeLignes(currentStation);
                            //System.out.println("Code lignes current station: " + code_ligne_Station+ "curent station: "+currentStation);
                            //System.out.println("Code lignes next station: " + code_ligne_nextStation + "next station: "+neighbor); //!outliers.contains(neighbor) &&
                            if (!allVisited.contains(neighbor) && !visited.contains(neighbor) && !Collections.disjoint(code_ligne_nextStation, code_ligne_Station) ) {
                                // System.out.println("visited: " + visited);
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

                        // Si on ne trouve que des outliers, on termine la manchette
                        if (!foundNewStation &&!notyet) {
                            for (String neighbor : neighbors) {
                                List<String> code_ligne_nextStation = RailNetwork.getCodeLignes(neighbor);
                                List<String> code_ligne_Station = RailNetwork.getCodeLignes(currentStation);
                                if (!visited.contains(neighbor) && !Collections.disjoint(code_ligne_nextStation, code_ligne_Station) ) {
                                    manchette.add(neighbor);
                                    visited.add(neighbor);
                                    allVisited.add(neighbor);
                                    if (!visitedOutliers.contains(neighbor)) {
                                        visitedOutliers.add(neighbor);
                                    }
                                    currentStation = neighbor;
                                    foundNewStation = true;
                                    notyet=true;
                                    
                                    break;
                                }
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

    // Test implémentation Manchette Algo profondeur (fonctionne)
    public static List<List<String>> generateManchettesDfs(Graph<String, String> railNetwork) {
        List<List<String>> manchettes = new ArrayList<>();
        List<String> outliers = outliersList(railNetwork);
        Set<String> allVisitedOutliers = new HashSet<>();

        for (String outlier : outliers) {
            if(!allVisitedOutliers.contains(outlier)) {
                // Créer une nouvelle liste pour cette manchette
                System.out.println("Step 1 : Choose outlier (" + outlier + ")");
                Set<String> visited = new HashSet<>();
                List<List<String>> manchettesOutlier = new ArrayList<>();
                Set<String> allVisited = new HashSet<>();
                dfs(railNetwork, outlier, new ArrayList<>(), visited, allVisited, manchettesOutlier);
                manchettes.add(getLongestList(manchettesOutlier));
                allVisitedOutliers.add(getLongestList(manchettesOutlier).getLast());
            }
        }
        return manchettes;
    }

    // Recherche en profondeur
    private static void dfs(Graph<String, String> railNetwork, String currentStation, 
                            List<String> manchette, Set<String> visited, 
                            Set<String> allVisited, List<List<String>> manchettes) {
        visited.add(currentStation);
        allVisited.add(currentStation);
        manchette.add(currentStation);

        List<String> neighbors = new ArrayList<>(railNetwork.getNeighbors(currentStation));
        neighbors.removeAll(visited); // Éviter les boucles

        if (!neighbors.isEmpty()) {
            for (String neighbor : neighbors) {
                if (!allVisited.contains(neighbor) && areConnectedByLine(currentStation, neighbor)) {
                    // Nouvelle manchette pour chaque bifurcation
                    List<String> newManchette = new ArrayList<>(manchette);
                    System.out.println("Step 2 : Choose branch " + manchette);
                    dfs(railNetwork, neighbor, newManchette, visited, allVisited, manchettes);
                }
            }
        } else {
            // On est à une extrémité ou une impasse, donc on sauvegarde la manchette
            manchettes.add(new ArrayList<>(manchette));
            System.out.println("Step 3 : Add manchette " + manchette);
        }  
    }

    // Récupère la plus longue branche parmis les branches récupérées
    public static List<String> getLongestList(List<List<String>> lists) {
        if (lists == null || lists.isEmpty()) {
            return null; // Retourne null si la liste est vide ou nulle
        }

        List<String> longestList = lists.get(0);

        for (List<String> list : lists) {
            if (list.size() > longestList.size()) {
                longestList = list;
            }
        }

        return longestList;
    }

    private static boolean areConnectedByLine(String station1, String station2) {
        List<String> code_ligne1 = RailNetwork.getCodeLignes(station1);
        List<String> code_ligne2 = RailNetwork.getCodeLignes(station2);
        return !Collections.disjoint(code_ligne1, code_ligne2);
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