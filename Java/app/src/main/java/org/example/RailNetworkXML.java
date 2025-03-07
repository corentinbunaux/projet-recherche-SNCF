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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;


public class RailNetworkXML {

    public static final List<String> stationsAttributesToKeep = Arrays.asList("CodeReseau", "CodeImmuable", "Libelle", "CodeLocalisation", "CodeTypePr", "Longitude", "Latitude");
    public static final Map<String, String> attributesConditions = Map.of("CodeLocalisation", "FR", "CodeTypePr", "GARE");

    public static void filterData(String filePath) {
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Create a new XML document for the filtered data
            DocumentBuilderFactory dbFactory2 = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder2 = dbFactory2.newDocumentBuilder();
            Document filteredXML = dBuilder2.newDocument();
            Element rootElement = filteredXML.createElement("rail_network");
            filteredXML.appendChild(rootElement);

            // Filter the data from the original XML and add to the new XML
            NodeList nodeList = doc.getElementsByTagName("PR");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element station = (Element) nodeList.item(i);
                for (String attribute : stationsAttributesToKeep) {
                    String attributeValue = station.getAttribute(attribute);
                    if (attributesConditions.containsKey(attribute) && !attributeValue.equals(attributesConditions.get(attribute))) {
                        continue;
                    }
                    Element newStation = filteredXML.createElement(attribute);
                    newStation.appendChild(filteredXML.createTextNode(attributeValue));
                    rootElement.appendChild(newStation);
                }
            }

            // Write the new XML content to a file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(filteredXML);
            StreamResult result = new StreamResult(new File("data/rail_network.xml"));
            transformer.transform(source, result);
        } catch (IOException | ParserConfigurationException | TransformerException | DOMException | SAXException e) {
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

    public static Graph<String, String> createRailNetwork(){
        if (!isDataFiltered("data/rail_network.xml")) {
            System.out.println("Filtering data...");
            filterData("data/xml_REF_PROD_S01.xml");
        }
        Graph<String, String> railNetwork = new SparseMultigraph<>();
        try {
            System.out.println("Creating rail network...");
            File xmlFile = new File("data/rail_network.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("CodeReseau");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element station = (Element) nodeList.item(i);
                String stationName = station.getTextContent();
                railNetwork.addVertex(stationName);
            }
        } catch (IOException | ParserConfigurationException | DOMException | SAXException e) {
            System.err.println("Error: " + e.getMessage());
        }
        System.out.println("Rail network created.");
        return railNetwork;
    }
}
