package org.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import java.awt.geom.Point2D;

class Station {
    public String name;
    public double x;
    public double y;
    public String codeImmu;
}

class Ligne_json {

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
            public List<List<Double>> coordinates; // Stockera les coordonnées après parsing

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

public class RailNetworkXML {

    private static List<Ligne_json> lignes;
    static {
        try {
            lignes = loadLignes("lignes.json");

        } catch (IOException e) {
            System.err.println("Error while loading data: " + e.getMessage());
        }
    }
    public static final Map<String, String> attributesConditions = Map.of("CodeLocalisation", "FR", "CodeTypePr",
            "GARE");
    public static final List<String> excludedStations = Arrays.asList("Avignon Voie 3 Garage",
            "Lieu Théorique Bâtiment Voyageurs");

    private static List<Ligne_json> loadLignes(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<List<Ligne_json>>() {
        });
    }

    public static void filterData(String filePath) {
        Document doc = readXMLFile(filePath);
        try {
            NodeList nodeList = doc.getElementsByTagName("LOCALISATION_PR_PK");
            Document filteredLinks = createXMLFile();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element line = (Element) nodeList.item(i);
                String codeLigne = line.getAttribute("CodeLigne");
                boolean lineExists = lignes.stream().anyMatch(l -> l.code_ligne.equals(codeLigne));
                if (lineExists) {
                    Node importedNode = filteredLinks.importNode(line, true);
                    filteredLinks.getDocumentElement().appendChild(importedNode);
                }
            }
            writeDataToXMLFile(filteredLinks, "data/links.xml");

            Document filteredNodes = createXMLFile();
            nodeList = doc.getElementsByTagName("PR");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element station = (Element) nodeList.item(i);
                boolean matchesConditions = true;
                for (Map.Entry<String, String> condition : attributesConditions.entrySet()) {
                    String attributeValue = station.getAttribute(condition.getKey());
                    if (!attributeValue.equals(condition.getValue())) {
                        matchesConditions = false;
                        break;
                    }
                }
                // Check if Latitude and Longitude are described
                String latitude = station.getAttribute("Latitude");
                String longitude = station.getAttribute("Longitude");
                if (matchesConditions && !excludedStations.contains(station.getAttribute("Libelle")) &&
                        !latitude.isEmpty() && !longitude.isEmpty()) {
                    Node importedNode = filteredNodes.importNode(station, true);
                    filteredNodes.getDocumentElement().appendChild(importedNode);
                }
            }
            writeDataToXMLFile(filteredNodes, "data/nodes.xml");
        } catch (DOMException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static Document readXMLFile(String filePath) {
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (IOException | ParserConfigurationException | DOMException | SAXException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }

    public static Document createXMLFile() {
        try {
            DocumentBuilderFactory dbFactory2 = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder2 = dbFactory2.newDocumentBuilder();
            Document filteredXML = dBuilder2.newDocument();
            Element rootElement = filteredXML.createElement("rail_network");
            filteredXML.appendChild(rootElement);
            return filteredXML;
        } catch (ParserConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }

    public static void writeDataToXMLFile(Document doc, String filePath) {
        try {
            // Write the new XML content to a file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);
        } catch (TransformerException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static boolean isDataFiltered() {
        try {
            List<File> files = Arrays.asList(new File("data/nodes.xml"), new File("data/links.xml"));
            for (File file : files) {
                if (!file.exists()) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Graph<String, String> createRailNetwork(Map<String, Point2D> positions) {
        if (!isDataFiltered()) {
            System.out.println("Filtering data...");
            filterData("data/xml_REF_PROD_S01.xml");
        }
        Graph<String, String> railNetwork = new SparseMultigraph<>();
        try {
            NodeList stationNodeList = readXMLFile("data/nodes.xml").getElementsByTagName("PR");
            NodeList linksNodeList = readXMLFile("data/links.xml").getElementsByTagName("LOCALISATION_PR_PK");
            Map<String, String> linkImmuLine = new HashMap<>();
            Map<String, Station> stations = new HashMap<>();

            for (int i = 0; i < linksNodeList.getLength(); i++) {
                Element link = (Element) linksNodeList.item(i);
                String codeLigne = link.getAttribute("CodeLigne");
                String codeImmu = link.getAttribute("CodeImmuable");
                if (!codeLigne.isEmpty() && !codeImmu.isEmpty()) {
                    linkImmuLine.put(codeImmu, codeLigne);
                }
            }

            for (int i = 0; i < stationNodeList.getLength(); i++) {
                Element station = (Element) stationNodeList.item(i);
                String libelle = station.getAttribute("Libelle");
                String xStr = station.getAttribute("Longitude");
                String yStr = station.getAttribute("Latitude");
                String codeImmu = station.getAttribute("CodeImmuable");

                if (!xStr.isEmpty() && !yStr.isEmpty() && !libelle.isEmpty() && !codeImmu.isEmpty()) {
                    double x = Double.parseDouble(xStr);
                    double y = Double.parseDouble(yStr);
                    Station s = new Station();
                    s.name = libelle;
                    s.x = x;
                    s.y = y;
                    s.codeImmu = codeImmu;
                    stations.put(codeImmu, s);
                }
            }

            // Calculate min and max values for x and y coordinates
            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

            for (Station s : stations.values()) {
                if (s.x < minX) {
                    minX = s.x;
                }
                if (s.x > maxX) {
                    maxX = s.x;
                }
                if (s.y < minY) {
                    minY = s.y;
                }
                if (s.y > maxY) {
                    maxY = s.y;
                }

                railNetwork.addVertex(s.name);
            }

            // Add padding to min and max values
            double paddingX = (maxX - minX) * Window.margin;
            double paddingY = (maxY - minY) * Window.margin;
            minX -= paddingX;
            maxX += paddingX;
            minY -= paddingY;
            maxY += paddingY;

            for (Station s : stations.values()) {
                double normX = (s.x - minX) * Window.width / (maxX - minX);
                double normY = (s.y - minY) * Window.height / (maxY - minY);
                double symY = Window.height - normY;
                positions.put(s.name, new Point2D.Double(normX, symY));
            }

            List<Ligne_json> lines = mergeLines(lignes);
            Map<String, List<Station>> garesParLigne = retrieveStationsForEachLine(lines, stations, linkImmuLine);
            computeEdgesToRailNetwork(lines, garesParLigne, railNetwork);

        } catch (DOMException e) {
            System.err.println("Error: " + e.getMessage());
        }
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

    private static Map<String, List<Station>> retrieveStationsForEachLine(List<Ligne_json> lignes,
            Map<String, Station> stations, Map<String, String> linkImmuLine) {
        Map<String, List<Station>> stationsForLines = new HashMap<>();

        for (Ligne_json ligne : lignes) {
            String codeLigne = ligne.code_ligne;
            List<Station> stationNames = stationsForLines.computeIfAbsent(codeLigne, k -> new ArrayList<>());

            for (Station station : stations.values()) {
                if (linkImmuLine.containsKey(station.codeImmu)
                        && linkImmuLine.get(station.codeImmu).equals(codeLigne)) {
                    stationNames.add(station);
                }
            }
        }

        return stationsForLines;
    }

    private static void computeEdgesToRailNetwork(List<Ligne_json> linesList,
            Map<String, List<Station>> garesParLigne,
            Graph<String, String> railNetwork) {
        // For each line referenced in the lignes.json file, add edges between stations
        for (Ligne_json line : linesList) {
            // Get the train stations for the current line
            List<Station> stations = garesParLigne.get(line.code_ligne);
            System.out.println(stations);

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
                railNetwork.addEdge(stations.get(i).name + " -> " + stations.get(i + 1).name, stations.get(i).name,
                        stations.get(i + 1).name);
            }
        }
    }

    private static void sortTrainStations(List<Station> stations, Ligne_json line) {
        // Coordinates of the line
        List<List<Double>> coordinates = line.geo_shape.geometry.coordinates;
        // Indexes of the closest points on the line for each train station
        Map<String, Number> indexes = new HashMap<>();
        // Gaps between the train stations and the closest point on the line
        Map<String, Number> gaps = new HashMap<>();
        // Iterate over the train stations to find the closest point on the line
        for (Station station : stations) {
            double x = station.x;
            double y = station.y;
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
            indexes.put(station.name, index);
            gaps.put(station.name, Math.abs((x - coordinates.get(index).get(0)) * 100 / x));
        }
        // Sort the train stations by their position on the line
        stations.sort(Comparator.comparing(Station -> indexes.get(Station.name).intValue()));
        // String maxGapGare = Collections
        // .max(gaps.entrySet(), Comparator.comparingDouble(entry ->
        // entry.getValue().doubleValue())).getKey();
        // System.out.println(
        // "Max gap : " + gaps.get(maxGapGare) + " for station " + maxGapGare + " on
        // line " + line.code_ligne);
    }
}
