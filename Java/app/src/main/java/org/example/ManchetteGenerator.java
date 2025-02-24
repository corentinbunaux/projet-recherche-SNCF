package org.example;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.*;
import java.util.*;

public class ManchetteGenerator {
    // Création du graphe ferroviaire
    public static Graph<String, String> createRailNetwork() {
        Graph<String, String> railNetwork = new SparseMultigraph<>();

        // Liste des gares
        String[] stations = {
            "Paris_Gare_de_Lyon", "Lyon_Part_Dieu", "Dijon_Ville", "Marseille_St_Charles",
            "Grenoble", "Annecy", "Genève", "Lausanne", "Strasbourg", "Metz", "Luxembourg",
            "Bruxelles", "Paris_Nord", "Lille", "Londres", "Nice_Ville"
        };

        // Ajout des gares (sommets)
        for (String station : stations) {
            railNetwork.addVertex(station);
        }

        // Ajout des connexions (arêtes)
        String[][] connections = {
            {"Paris_Gare_de_Lyon", "Lyon_Part_Dieu"}, {"Paris_Gare_de_Lyon", "Dijon_Ville"},
            {"Lyon_Part_Dieu", "Marseille_St_Charles"}, {"Lyon_Part_Dieu", "Dijon_Ville"},
            {"Lyon_Part_Dieu", "Grenoble"}, {"Dijon_Ville", "Strasbourg"}, {"Dijon_Ville", "Lyon_Part_Dieu"},
            {"Marseille_St_Charles", "Nice_Ville"}, {"Marseille_St_Charles", "Lyon_Part_Dieu"},
            {"Grenoble", "Annecy"}, {"Annecy", "Genève"}, {"Genève", "Lausanne"},
            {"Strasbourg", "Metz"}, {"Metz", "Luxembourg"}, {"Luxembourg", "Bruxelles"},
            {"Bruxelles", "Paris_Nord"}, {"Paris_Nord", "Lille"}, {"Lille", "Londres"}
        };

        int edgeCounter = 0;
        for (String[] connection : connections) {
            railNetwork.addEdge("Edge_" + edgeCounter++, connection[0], connection[1], EdgeType.UNDIRECTED);
        }

        return railNetwork;
    }

    // Génération naïve des manchettes (parcours de trains)
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

    // Exploration DFS pour générer une manchette
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

    // Affichage des manchettes
    public static void printManchettes(List<List<String>> manchettes) {
        System.out.println("Manchettes generees :");
        for (List<String> manchette : manchettes) {
            System.out.println(manchette);
        }
    }
}