package org.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;

public class ManchetteGenerator {
    // Génération des manchettes
    public static List<List<String>> generateManchettes(Graph<String, String> railNetwork) {
        List<List<String>> manchettes = new ArrayList<>();
        List<String> outliers = outliersList(railNetwork);  //extrémités
        Set<String> visitedOutliers = new HashSet<>();
        Set<String> allVisited = new HashSet<>();
        Set<String> allVisited = new HashSet<>();
    
        for (String outlier : outliers) {
            if (!visitedOutliers.contains(outlier)) {
                // Créer une nouvelle manchette et ajouter l'outlier
                List<String> manchette = new ArrayList<>();
                manchette.add(outlier);
                visitedOutliers.add(outlier);
                allVisited.add(outlier);
                allVisited.add(outlier);
    
                // Liste des stations visitées pour éviter les doublons
                Set<String> visited = new HashSet<>();
                visited.add(outlier);
    
                String currentStation = outlier;
                boolean end = false;
    
                // Tant qu'on n'a pas atteint un autre outlier, on complète la manchette
                while (!end) {
                    Collection<String> neighbors = railNetwork.getNeighbors(currentStation);
                    System.out.println("Current station: " + currentStation + ", Neighbors: " + neighbors);
    
                    // Vérifier s'il n'y a qu'un seul voisin (ligne terminée)
                    if (neighbors.size() == 1) {
                        String nextStation = neighbors.iterator().next();
                        List<String> code_ligne_nextStation = RailNetwork.getCodeLignes(nextStation);
                        List<String> code_ligne_Station = RailNetwork.getCodeLignes(currentStation);
                        if (!visited.contains(nextStation) && !Collections.disjoint(code_ligne_nextStation, code_ligne_Station)) {
                        List<String> code_ligne_nextStation = RailNetwork.getCodeLignes(nextStation);
                        List<String> code_ligne_Station = RailNetwork.getCodeLignes(currentStation);
                        if (!visited.contains(nextStation) && !Collections.disjoint(code_ligne_nextStation, code_ligne_Station)) {
                            manchette.add(nextStation);
                            visited.add(nextStation);
                            currentStation = nextStation;
                            allVisited.add(nextStation);
                            allVisited.add(nextStation);
                        }
                        else {
                            end = true; // Éviter une boucle infinie
                        }
                    } else {
                        boolean foundNewStation = false;
                        boolean notyet=false;
                        boolean notyet=false;
                        
                        // Parcourir les voisins pour trouver une station non visitée qui n'est pas un outlier
                        for (String neighbor : neighbors) {
                            List<String> code_ligne_nextStation = RailNetwork.getCodeLignes(neighbor);
                            List<String> code_ligne_Station = RailNetwork.getCodeLignes(currentStation);
                            System.out.println("Code lignes current station: " + code_ligne_Station+ "curent station: "+currentStation);
                            System.out.println("Code lignes next station: " + code_ligne_nextStation + "next station: "+neighbor); //!outliers.contains(neighbor) &&
                            if (!allVisited.contains(neighbor) && !visited.contains(neighbor) && !Collections.disjoint(code_ligne_nextStation, code_ligne_Station) ) {
                                System.out.println("visited: " + visited);
                            List<String> code_ligne_nextStation = RailNetwork.getCodeLignes(neighbor);
                            List<String> code_ligne_Station = RailNetwork.getCodeLignes(currentStation);
                            System.out.println("Code lignes current station: " + code_ligne_Station+ "curent station: "+currentStation);
                            System.out.println("Code lignes next station: " + code_ligne_nextStation + "next station: "+neighbor); //!outliers.contains(neighbor) &&
                            if (!allVisited.contains(neighbor) && !visited.contains(neighbor) && !Collections.disjoint(code_ligne_nextStation, code_ligne_Station) ) {
                                System.out.println("visited: " + visited);
                                manchette.add(neighbor);
                                visited.add(neighbor);
                                allVisited.add(neighbor);
                                if (!visitedOutliers.contains(neighbor)) {
                                    visitedOutliers.add(neighbor);
                                }
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