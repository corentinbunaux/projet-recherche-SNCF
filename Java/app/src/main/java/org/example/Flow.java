package org.example;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import java.awt.geom.Point2D;
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
        if (x instanceof Boolean) {
            return (Boolean) x ? 1.0 : 0.0;
        } else if (x instanceof Number) {
            return ((Number) x).doubleValue();
        } else {
            return null;
        }
    }

    public Double getYAsDouble() {
        if (y instanceof Boolean) {
            return (Boolean) y ? 1.0 : 0.0;
        } else if (y instanceof Number) {
            return ((Number) y).doubleValue();
        } else {
            return null;
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

class flow_json {
    public VarInfo varInfo;
    public List<Point> points;
}

public class Flow {

    public static List<flow_json> flows;

    static {
        try {
            flows = loadFlows("flows.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void flow() {
        Graph<String, String> flowNetwork = new SparseMultigraph<>();
        Map<String, Point2D> positions = new HashMap<>();
        flow_json testFlow = flows.get(0);
        for (int i = 0; i < testFlow.points.size(); i++) {
            Point point = testFlow.points.get(i);
            flowNetwork.addVertex(point.libelle);
            positions.put(point.libelle, new Point2D.Double(point.getXAsDouble(), point.getYAsDouble()));
            
            if (i > 0) {
            Point previousPoint = testFlow.points.get(i - 1);
            flowNetwork.addEdge(previousPoint.libelle + "-" + point.libelle, previousPoint.libelle, point.libelle);
            }
        }
        GUI.display(flowNetwork,positions);
    }

    private static List<flow_json> loadFlows(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<List<flow_json>>() {
        });
    }
}