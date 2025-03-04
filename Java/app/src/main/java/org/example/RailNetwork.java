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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

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

class Ligne {
    public String type_ligne;
    public String idgaia;
    public String code_ligne;
    public String lib_ligne;
    public int rg_troncon;
    public String pkd;
    public String pkf;
    public double x_d_l93;
    public double y_d_l93;
    public double x_f_l93;
    public double y_f_l93;
    public double x_d_wgs84;
    public double y_d_wgs84;
    public double x_f_wgs84;
    public double y_f_wgs84;
    public String c_geo_d;
    public GeoCoordinate c_geo_f;
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
            public List<List<Double>> coordinates;  // Stockera les coordonnées après parsing

            @JsonCreator
            public Geometry(@JsonProperty("type") String type, @JsonProperty("coordinates") JsonNode coordinatesNode) {
                this.type = type;
                this.coordinates = new ArrayList<>();

                if (coordinatesNode.isArray()) {
                    if (type.equals("LineString")) {
                        // Cas d'un `LineString` (List<List<Double>>)
                        for (JsonNode point : coordinatesNode) {
                            List<Double> coord = new ArrayList<>();
                            coord.add(point.get(0).asDouble());
                            coord.add(point.get(1).asDouble());
                            this.coordinates.add(coord);
                        }
                    } else if (type.equals("MultiLineString")) {
                        // Cas d'un `MultiLineString` (List<List<List<Double>>>)
                        for (JsonNode line : coordinatesNode) {
                            for (JsonNode point : line) {
                                List<Double> coord = new ArrayList<>();
                                coord.add(point.get(0).asDouble());
                                coord.add(point.get(1).asDouble());
                                this.coordinates.add(coord);
                            }
                        }
                    }
                }
            }
        }
    }
}

public class RailNetwork {

    

    private static List<Gare> loadGares(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<List<Gare>>() {});
    }
    private static List<Gare> gares;

    static {
        try {
            gares = loadGares("gares.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Ligne> loadLignes(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<List<Ligne>>() {});
    }

    // Création du graphe ferroviaire
    public static Graph<String, String> createRailNetwork(Map<String, Point2D> positions) {
        Graph<String, String> railNetwork = new SparseMultigraph<>();

    
            List<Ligne> lignes = loadLignes("lignes.json");
            
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
            computeEdgesToRailNetwork(lignes, garesParLigne, railNetwork);

        

        return railNetwork;
    }

    private static void computeEdgesToRailNetwork(List<Ligne> linesList, Map<String, List<Gare>> garesParLigne, Graph<String, String> railNetwork) {
        //For each line referenced in the lignes.json file, add edges between stations
        for(Ligne line : linesList) {
            // Get the train stations for the current line
            List<Gare> gares = garesParLigne.get(line.code_ligne);
            if(gares == null) {
                continue;
            }
            sortTrainStations(gares, line);
            // Add edges between stations
            for (int i = 0; i < gares.size() - 1; i++) {
                railNetwork.addEdge(gares.get(i).libelle + " -> " + gares.get(i + 1).libelle, gares.get(i).libelle, gares.get(i + 1).libelle);
            }
        }
    }

    // Sort the train stations by their position on the line
    private static void sortTrainStations(List<Gare> gares, Ligne line) {
        //Coordinates of the line
        List<List<Double>> coordinates = line.geo_shape.geometry.coordinates;
        // Indexes of the closest points on the line for each train station
        Map<String, Number> indexes = new HashMap<>();
        // Iterate over the train stations to find the closest point on the line
        for(Gare gare : gares) {
            double x = gare.x_wgs84;
            double y = gare.y_wgs84;
            double minDistance = Double.MAX_VALUE;
            int index = 0;
            for(int i = 0; i < coordinates.size(); i++) {
                List<Double> point = coordinates.get(i);
                double distance = Math.pow(x - point.get(0), 2) + Math.pow(y - point.get(1), 2);
                if(distance < minDistance) {
                    minDistance = distance;
                    index = i;
                }
            }
            indexes.put(gare.libelle, index);
        }
        // Sort the train stations by their position on the line
        gares.sort(Comparator.comparing(gare -> indexes.get(gare.libelle).intValue()));  
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

    public static String getCodeLignes(String station)  {
        for (Gare gare : gares) {
            if (gare.libelle.equals(station)) {
                return gare.code_ligne;
            }
        }
        return "error";
    }
        
        
}

