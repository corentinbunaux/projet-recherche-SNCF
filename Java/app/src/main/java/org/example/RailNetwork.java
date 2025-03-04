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
                double symY = Window.height - normY;
                positions.put(gare.libelle, new Point2D.Double(normX, symY));
            }

            // Ajout des connexions entre gares d'une même ligne
            for (List<Gare> garesLigne : garesParLigne.values()) {
                // FIXME : trouver les liens entre gares d'une même ligne
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

    public static Graph<String, String> graphNeighborhood(Graph<String, String> railNetwork, String startVertex, int nbOfNeighbors) {
        Filter<String, String> filter = new KNeighborhoodFilter<>(startVertex, nbOfNeighbors, EdgeType.IN_OUT);
        Graph<String, String> neighborhood = filter.apply(railNetwork);
        return neighborhood;
    }

    public static Graph<String, String> graphBorder(Graph<String, String> railNetwork, Map<String, Point2D> positions, String startVertex, double maxDistance) {
        Graph<String, String> border = new SparseMultigraph<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(startVertex);
        visited.add(startVertex);
        border.addVertex(startVertex);

        Point2D startPoint = convertToWGS84(positions, startVertex);

        while (!queue.isEmpty()) {
            String currentVertex = queue.poll();

            for (String neighbor : railNetwork.getNeighbors(currentVertex)) {
                if (!visited.contains(neighbor)) {
                    Point2D neighborPoint = convertToWGS84(positions, neighbor);
                    double distance = haversineDistance(startPoint, neighborPoint);
                    if (distance <= maxDistance) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                        border.addVertex(neighbor);
                        border.addEdge(currentVertex + " -> " + neighbor, currentVertex, neighbor);
                    }
                }
            }
        }
        return border;
    }

    private static Point2D convertToWGS84( Map<String, Point2D> positions, String startVertex) {
        double x_wgs84 = positions.get(startVertex).getX()*100/Window.width;
        double y_wgs84 = (Window.height - positions.get(startVertex).getY())*100/Window.height;
        return new Point2D.Double(x_wgs84, y_wgs84);
    }

    public static double haversineDistance(Point2D point1, Point2D point2) {
        final double R = 6371; // Rayon de la Terre en kilomètres

        double lat1 = Math.toRadians(point1.getY());
        double lon1 = Math.toRadians(point1.getX());
        double lat2 = Math.toRadians(point2.getY());
        double lon2 = Math.toRadians(point2.getX());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.pow(Math.sin(dlat / 2), 2) +
                   Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance en kilomètres
    }
}

