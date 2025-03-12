package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.jung.algorithms.filters.Filter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter.EdgeType;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class RailNetwork {

    private static List<Gare_json> gares;
    private static List<Ligne_json> lignes;

    public static double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
    public static double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

    static {
        try {
            gares = loadGares("gares.json");
            lignes = loadLignes("lignes.json");

        } catch (IOException e) {
            System.err.println("Error while loading data: " + e.getMessage());
        }
    }

    private static List<Gare_json> loadGares(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<List<Gare_json>>() {
        });
    }

    private static List<Ligne_json> loadLignes(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<List<Ligne_json>>() {
        });
    }

    // Création du graphe ferroviaire
    public static Graph<String, String> createRailNetwork(Map<String, Point2D> positions) {
        Graph<String, String> railNetwork = new SparseMultigraph<>();
        Map<String, List<Gare_json>> garesParLigne = new HashMap<>();
        List<Ligne_json> lines;
        for (Gare_json Gare_json : gares) {
            garesParLigne.computeIfAbsent(Gare_json.code_ligne, _ -> new ArrayList<>()).add(Gare_json);
            railNetwork.addVertex(Gare_json.libelle);
        }

        for (Gare_json Gare_json : gares) {
            if (Gare_json.x_wgs84 < minX) {
                minX = Gare_json.x_wgs84;
            }
            if (Gare_json.x_wgs84 > maxX) {
                maxX = Gare_json.x_wgs84;
            }
            if (Gare_json.y_wgs84 < minY) {
                minY = Gare_json.y_wgs84;
            }
            if (Gare_json.y_wgs84 > maxY) {
                maxY = Gare_json.y_wgs84;
            }
        }

        // Ajout d'un padding en pourcentage des valeurs min et max
        double paddingX = (maxX - minX) * Window.margin;
        double paddingY = (maxY - minY) * Window.margin;
        minX -= paddingX;
        maxX += paddingX;
        minY -= paddingY;
        maxY += paddingY;

        // Normalisation des positions pour affichage
        for (Gare_json Gare_json : gares) {
            double normX = (Gare_json.x_wgs84 - minX) * Window.height / (maxX - minX);
            double normY = (Gare_json.y_wgs84 - minY) * Window.height / (maxY - minY);
            double symY = Window.height - normY;
            positions.put(Gare_json.libelle, new Point2D.Double(normX, symY));
        }

        // Merge lines with the same code_ligne but different rg_troncon
        lines = mergeLines(lignes);

        // Ajout des connexions entre gares d'une même Ligne_json
        computeEdgesToRailNetwork(lines, garesParLigne, railNetwork);
        return railNetwork;
    }

    private static List<Ligne_json> mergeLines(List<Ligne_json> lignes) {
        // Map pour stocker les lignes par code_ligne
        Map<String, List<Ligne_json>> lignesParCode = new HashMap<>();

        // Regrouper les lignes par code_ligne
        for (Ligne_json Ligne_json : lignes) {
            lignesParCode.computeIfAbsent(Ligne_json.code_ligne, _ -> new ArrayList<>()).add(Ligne_json);
        }

        // Liste des lignes fusionnées
        List<Ligne_json> lignesFusionnees = new ArrayList<>();

        // Fusionner les lignes ayant le même code_ligne
        for (List<Ligne_json> troncons : lignesParCode.values()) {
            if (troncons.size() == 1) {
                // Si une seule Ligne_json, pas besoin de fusion
                lignesFusionnees.add(troncons.get(0));
                continue;
            }

            // Trier les tronçons par rg_troncon (ordre des segments)
            troncons.sort(Comparator.comparingInt(l -> l.rg_troncon));

            // Prendre le premier tronçon comme base
            Ligne_json ligneFusionnee = troncons.get(0);
            List<List<Double>> coordinatesFusionnees = new ArrayList<>(ligneFusionnee.geo_shape.geometry.coordinates);

            // Fusionner les tronçons suivants
            for (int i = 1; i < troncons.size(); i++) {
                Ligne_json troncon = troncons.get(i);

                // Vérifier si la fin du dernier tronçon correspond au début du suivant
                List<Double> lastPoint = coordinatesFusionnees.get(coordinatesFusionnees.size() - 1);
                List<Double> firstPointNext = troncon.geo_shape.geometry.coordinates.get(0);

                if (lastPoint.equals(firstPointNext)) {
                    // Ajout direct si les tronçons sont déjà liés
                    coordinatesFusionnees.addAll(troncon.geo_shape.geometry.coordinates.subList(1,
                            troncon.geo_shape.geometry.coordinates.size()));
                } else {
                    // Ajout du reste des points du tronçon suivant
                    coordinatesFusionnees.addAll(troncon.geo_shape.geometry.coordinates);
                }
            }

            // Mise à jour des coordonnées de la Ligne_json fusionnée
            ligneFusionnee.geo_shape.geometry.coordinates = coordinatesFusionnees;
            lignesFusionnees.add(ligneFusionnee);
        }

        return lignesFusionnees;
    }

    private static void computeEdgesToRailNetwork(List<Ligne_json> linesList,
            Map<String, List<Gare_json>> garesParLigne,
            Graph<String, String> railNetwork) {
        // For each line referenced in the lignes.json file, add edges between stations
        for (Ligne_json line : linesList) {
            // Get the train stations for the current line
            List<Gare_json> stations = garesParLigne.get(line.code_ligne);

            // If no train stations are found for the current line, skip it
            if (stations == null) {
                // System.out
                // .println("No train stations found for line " + line.code_ligne + " (" +
                // line.type_ligne + ")");
                continue;
            }
            sortTrainStations(stations, line);
            // Add edges between stations
            for (int i = 0; i < stations.size() - 1; i++) {
                railNetwork.addEdge(stations.get(i).libelle + " -> " + stations.get(i + 1).libelle,
                        stations.get(i).libelle,
                        stations.get(i + 1).libelle);
            }
        }
    }

    // Sort the train stations by their position on the line
    private static void sortTrainStations(List<Gare_json> gares, Ligne_json line) {
        // Coordinates of the line
        List<List<Double>> coordinates = line.geo_shape.geometry.coordinates;
        // Indexes of the closest points on the line for each train station
        Map<String, Number> indexes = new HashMap<>();
        // Gaps between the train stations and the closest point on the line
        Map<String, Number> gaps = new HashMap<>();
        // Iterate over the train stations to find the closest point on the line
        for (Gare_json Gare_json : gares) {
            double x = Gare_json.x_wgs84;
            double y = Gare_json.y_wgs84;
            double minDistance = Double.MAX_VALUE;
            int index = 0;
            for (int i = 0; i < coordinates.size(); i++) {
                List<Double> point = coordinates.get(i);
                double distance = Math.pow(x - point.get(0), 2) + Math.pow(y - point.get(1), 2);
                if (distance < minDistance) {
                    minDistance = distance;
                    index = i;
                }
            }
            indexes.put(Gare_json.libelle, index);
            gaps.put(Gare_json.libelle, Math.abs((x - coordinates.get(index).get(0)) * 100 / x));
        }
        // Sort the train stations by their position on the line
        gares.sort(Comparator.comparing(Gare_json -> indexes.get(Gare_json.libelle).intValue()));
        // String maxGapGare = Collections
        // .max(gaps.entrySet(), Comparator.comparingDouble(entry ->
        // entry.getValue().doubleValue())).getKey();
        // System.out.println(
        // "Max gap : " + gaps.get(maxGapGare) + " for station " + maxGapGare + " on
        // line " + line.code_ligne);
    }

    public static void printSubgraph(Graph<String, String> subgraph) {
        System.out.println("Subgraph edges: " + subgraph.getEdges());
    }
}
