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

class Gare {
    public String code_uic;
    public String libelle;
    public String code_ligne;
    public String fret;
    public String voyageurs;
    public int rg_troncon;
    public String pk;
    public String commune;
    public String departemen;
    public int idreseau;
    public String idgaia;
    public double x_l93;
    public double y_l93;
    public double x_wgs84;
    public double y_wgs84;
    public GeoCoordinate c_geo;
    public GeoCoordinate geo_point_2d;
    public GeoShape geo_shape;

    static class GeoCoordinate {
        public double lon;
        public double lat;
    }

    static class GeoShape {
        public String type;
        public Geometry geometry;
        public Map<String, Object> properties;

        static class Geometry {
            public String type;
            public List<Double> coordinates;
        }
    }
}

public class RailNetwork {
    private static List<Gare> loadGares(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<List<Gare>>() {});
    }

    // Création du graphe ferroviaire
    public static Graph<String, String> createRailNetwork(Map<String, Point2D> positions) {
        Graph<String, String> railNetwork = new SparseMultigraph<>();

        try {
            List<Gare> gares = loadGares("gares.json");

            Map<String, List<Gare>> garesParLigne = new HashMap<>();
            for (Gare gare : gares) {
                garesParLigne.computeIfAbsent(gare.code_ligne, k -> new ArrayList<>()).add(gare);
                railNetwork.addVertex(gare.libelle);
            }

            // Normalisation des positions pour affichage
            for (Gare gare : gares) {
                double normX = (gare.x_wgs84) * Window.width / 100;
                double normY = (gare.y_wgs84) * Window.height / 100;
                double symY = Window.height - normY; // Horizontal symmetry
                positions.put(gare.libelle, new Point2D.Double(normX, symY));
            }

            // Ajout des connexions entre gares d'une même ligne
            for (List<Gare> garesLigne : garesParLigne.values()) {
                garesLigne.sort(Comparator.comparing(g -> g.code_uic));
                for (int i = 0; i < garesLigne.size() - 1; i++) {
                    railNetwork.addEdge(garesLigne.get(i).libelle + " -> " + garesLigne.get(i + 1).libelle,
                            garesLigne.get(i).libelle, garesLigne.get(i + 1).libelle);
                }
            }

            // System.out.println("Nombre de gares: " + railNetwork.getVertexCount());
            // System.out.println("Nombre de liaisons: " + railNetwork.getEdgeCount());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return railNetwork;
    }

    public static void printSubgraph(Graph<String, String> subgraph) {
        System.out.println("Subgraph edges: " + subgraph.getEdges());
    }

    public static Graph<String, String> neighborhood(Graph<String, String> railNetwork, String startVertex, int k) {
        Filter<String, String> filter = new KNeighborhoodFilter<>(startVertex, k, EdgeType.IN_OUT);
        Graph<String, String> neighborhood = filter.apply(railNetwork);
        return neighborhood;
    }
}

