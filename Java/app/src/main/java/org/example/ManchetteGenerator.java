package org.example;

import edu.uci.ics.jung.graph.Graph;
import java.util.*;

public class ManchetteGenerator {
    // Génération naïve des manchettes (parcours de trains)
    public static List<List<String>> generateManchettes(Graph<String, String> railNetwork) {
        List<List<String>> manchettes = new ArrayList<>();
        List<String> outliers = outliersList(railNetwork);
        Set<String> visitedOutliers = new HashSet<>();
    
        for (String outlier : outliers) {
            if (!visitedOutliers.contains(outlier)) {
                // Créer une nouvelle manchette et ajouter l'outlier
                List<String> manchette = new ArrayList<>();
                manchette.add(outlier);
                visitedOutliers.add(outlier);
    
                // Liste des stations visitées pour éviter les doublons
                Set<String> visited = new HashSet<>();
                visited.add(outlier);
    
                String currentStation = outlier;
                boolean end = false;
    
                // Tant qu'on n'a pas atteint un autre outlier, on complète la manchette
                while (!end) {
                    Collection<String> neighbors = railNetwork.getNeighbors(currentStation);
    
                    // Vérifier s'il n'y a qu'un seul voisin (ligne terminée)
                    if (neighbors.size() == 1) {
                        String nextStation = neighbors.iterator().next();
                        if (!visited.contains(nextStation)) {
                            manchette.add(nextStation);
                            visited.add(nextStation);
                            currentStation = nextStation;
                        } else {
                            end = true; // Éviter une boucle infinie
                        }
                    } else {
                        boolean foundNewStation = false;
                        
                        // Parcourir les voisins pour trouver une station non visitée qui n'est pas un outlier
                        for (String neighbor : neighbors) {
                            if (!outliers.contains(neighbor) && !visited.contains(neighbor)) {
                                manchette.add(neighbor);
                                visited.add(neighbor);
                                currentStation = neighbor;
                                foundNewStation = true;
                                break;
                            }
                        }
    
                        // Si on ne trouve que des outliers, on termine la manchette
                        if (!foundNewStation) {
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