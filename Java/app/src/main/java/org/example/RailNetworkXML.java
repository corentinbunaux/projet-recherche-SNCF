package org.example;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import java.awt.geom.Point2D;

public class RailNetworkXML {

    public static final List<String> stationsAttributesToKeep = Arrays.asList("CodeReseau", "CodeImmuable", "Libelle", "CodeLocalisation", "CodeTypePr", "Longitude", "Latitude");
    public static final Map<String, String> attributesConditions = Map.of("CodeLocalisation", "FR", "CodeTypePr", "GARE");

    public static void filterData(String filePath) {
        Document doc = readXMLFile(filePath);
        Document filteredXML = createXMLFile();
        try {
            // Filter the data from the original XML and add to the new XML
            NodeList nodeList = doc.getElementsByTagName("PR");
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
                if (matchesConditions) {
                    Node importedNode = filteredXML.importNode(station, true);
                    filteredXML.getDocumentElement().appendChild(importedNode);
                }
            }
        } catch (DOMException e) {
            System.err.println("Error: " + e.getMessage());
        }
        writeDataToXMLFile(filteredXML, "data/rail_network.xml");
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

    public static Document createXMLFile(){
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

    public static boolean isDataFiltered(String filePath){
        try {
            File xmlFile = new File(filePath);
            return xmlFile.exists();
        } catch (Exception e) {
            return false;
        }
    }

        public static Graph<String, String> createRailNetwork(Map<String, Point2D> positions) {
        if (!isDataFiltered("data/rail_network.xml")) {
            System.out.println("Filtering data...");
            filterData("data/xml_REF_PROD_S01.xml");
        }
        Graph<String, String> railNetwork = new SparseMultigraph<>();
        try {
            Document doc = readXMLFile("data/rail_network.xml");
            NodeList nodeList = doc.getElementsByTagName("PR");
            System.out.println(nodeList.getLength() + " stations found in the rail network.");
    
            // Calculate min and max values for x and y coordinates
            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
    
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element station = (Element) nodeList.item(i);
                String xStr = station.getAttribute("Longitude");
                String yStr = station.getAttribute("Latitude");
    
                if (!xStr.isEmpty() && !yStr.isEmpty()) {
                    double x = Double.parseDouble(xStr);
                    double y = Double.parseDouble(yStr);
    
                    if (x < minX) {
                        minX = x;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
    
                    railNetwork.addVertex(station.getAttribute("Libelle"));
                }
            }
    
            // Add padding to min and max values
            double paddingX = (maxX - minX) * Window.margin;
            double paddingY = (maxY - minY) * Window.margin;
            minX -= paddingX;
            maxX += paddingX;
            minY -= paddingY;
            maxY += paddingY;
    
            // Normalize positions for display
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element station = (Element) nodeList.item(i);
                String xStr = station.getAttribute("Longitude");
                String yStr = station.getAttribute("Latitude");
    
                if (!xStr.isEmpty() && !yStr.isEmpty()) {
                    double x = Double.parseDouble(xStr);
                    double y = Double.parseDouble(yStr);
                    double normX = (x - minX) * Window.height / (maxX - minX);
                    double normY = (y - minY) * Window.height / (maxY - minY);
                    double symY = Window.height - normY;
                    positions.put(station.getAttribute("Libelle"), new Point2D.Double(normX, symY));
                }
            }
    
            System.out.println(railNetwork.getVertexCount() + " stations added to the rail network.");
    
        } catch (DOMException e) {
            System.err.println("Error: " + e.getMessage());
        }
        System.out.println("Rail network created.");
        return railNetwork;
    }
}
