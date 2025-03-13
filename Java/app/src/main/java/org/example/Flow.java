package org.example;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Point {
    public String typeHoraire;
    public String libelle;
    public int horaireHouat;
    public int horaireSecondes;
    public String CI;
    public String CH;
    public Object x;
    public Object y;
    public int aKm;
    public String voieEntree;
    public String voieVia;
    public String voieSortie;

    public Double getXAsDouble() {
        if (x instanceof Number) {
            return ((Number) x).doubleValue();
        } else {
            return -1000.0;
        }
    }

    public Double getYAsDouble() {
        if(y instanceof Number) {
            return ((Number) y).doubleValue();
        } 
        else{
            return -1000.0;
        }
    }
}

class VarInfo {
    public String type_article;
    public String numero_tribu;
    public String marqueur_incremental;
    public String marqueur_type;
    public String numero_marche_depart;
    public String code_RGUN;
    public String heure_theorique_depart;
    public String nature;
    public String numero_marche_complementaire;
    public String mnemo_indice_compo;
    public String regime;
    public String CI_origine;
    public String CH_origine;
    public String CI_destination;
    public String CH_destination;
    public String code_UI_origine;
    public String code_TCT_origine;
    public String code_famille;
    public String code_mission;
    public String type_ligne;
    public String indexation;
    public String hlpDu;
    public String hlpPour;
}

class flows_json {
    public VarInfo varInfo;
    public List<Point> points;
}

public class Flow {

    public static List<flows_json> flows;

    static {
        try {
            flows = loadFlows("flows.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void flow(){
        Map<String, List<String>> stationsInFlow = getStationsInFlow();
        Graph<String, String> railNetwork = new SparseMultigraph<>();
        Map<String, Point2D> positions = new HashMap<>();
        System.out.println(stationsInFlow.size() + " flows loaded");
        for(flows_json flow : flows){
            String NTribu = flow.varInfo.numero_tribu;
            for(int i = 0;i<stationsInFlow.get(NTribu).size();i++){
                String stationCI = stationsInFlow.get(NTribu).get(i);
                String station = getStationName(stationCI, flow.points);
                Double xStation = getStationX(stationCI, flow.points);
                Double yStation = getStationY(stationCI, flow.points);
                Boolean condition1 = xStation.compareTo(-1000.0) == 0;
                Boolean condition2 = yStation.compareTo(-1000.0) == 0;
                if(!station.isEmpty() && !condition1 && !condition2){
                    railNetwork.addVertex(station);
                    positions.put(station, new Point2D.Double(xStation, yStation));
                }
            }
        }
        GUI.display(railNetwork, positions);
    }

    private static Double getStationY(String stationCI, List<Point> points){
        for(Point point : points){
            if(point.CI.equals(stationCI) && !point.getYAsDouble().equals(null)){
                return point.getYAsDouble();
            }
        }
        return null;
    }

    private static Double getStationX(String stationCI, List<Point> points) {
        for(Point point : points){
            if(point.CI.equals(stationCI) && !point.getXAsDouble().equals(null)){
                    return point.getXAsDouble();
            }
        }
        return null;
    }

    private static String getStationName(String stationCI, List<Point> points) {
        for(Point point : points){
            if(point.CI.equals(stationCI)){
                return point.libelle;
            }
        }
        return "";
    }

    public static  Map<String, List<String>> getStationsInFlow() {
        Map<String, List<String>> stationsInFlow = new HashMap<>();
        for(flows_json flow : flows) {
            List<String> stations = new ArrayList<>();
            for(int i=0;i<flow.points.size();i++) {
                if(i>0 && !flow.points.get(i).CI.equals(flow.points.get(i-1).CI)){
                    stations.add(flow.points.get(i).CI);
                }   
            }
            stationsInFlow.put(flow.varInfo.numero_tribu, stations);
        }
        return stationsInFlow;
    }

    private static List<flows_json> loadFlows(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<List<flows_json>>() {
        });
    }
}