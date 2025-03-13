package org.example;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public class GraphVisualizer {

    private static int zoomIn = Window.initial_zoomIn;
    private static float sizeVerteces = Window.initial_sizeVerteces;
    private static float sizeEdges = Window.initial_sizeEdges;
    private static VisualizationViewer<String, String> vv;
    private static final List<String> stackedVertices = new ArrayList<>();

    @SuppressWarnings("Convert2Lambda")
    public static VisualizationViewer<String, String> Graph(Graph<String, String> graph, Map<String, Point2D> positions, List<List<String>> manchette) {
        StaticLayout<String, String> layout = new StaticLayout<>(graph, vertex -> positions.get(vertex), new Dimension(Window.width, Window.height));
        vv = new VisualizationViewer<>(layout);
        vv.setPreferredSize(new Dimension(Window.width, Window.height));

        // Set vertex color and size
        vv.getRenderContext().setVertexFillPaintTransformer(_ -> ColorPalette.SNCF_RED);
        updateVertecesSize();
        // Set edge color, size and shape
        if (manchette == null) {
            vv.getRenderContext().setEdgeDrawPaintTransformer(_ -> ColorPalette.SNCF_BLACK);
        } else {
            Map<String, List<java.awt.Color>> edgeColorMap = new HashMap<>();

            for (int i = 0; i < manchette.size(); i++) {
                List<String> man = manchette.get(i);
                List<String> edges = new ArrayList<>();
                for (int j = 0; j < man.size() - 1; j++) {
                    String gare1 = man.get(j);
                    String gare2 = man.get(j + 1);
                    String edge = graph.findEdge(gare1, gare2);
                    if (edge != null) {
                        edge=normalizeEdge(gare1, gare2);
                        edges.add(edge);
                    }
                }
                //System.out.println("edges"+edges);
                java.awt.Color color = ColorPalette.getColor(i);
                for (String edge : edges) {
                    edgeColorMap.computeIfAbsent(edge, k -> new ArrayList<>()).add(color);
                }
            }
            
            //Vérification dans la transformation
            vv.getRenderContext().setEdgeDrawPaintTransformer(e -> {
                String gare1 = e.toString().split("->")[0].trim();
                String gare2 = e.toString().split("->")[1].trim();
                String normalizedEdge = normalizeEdge(gare1, gare2);
               
                List<java.awt.Color> colors = edgeColorMap.getOrDefault(normalizedEdge, List.of(ColorPalette.SNCF_BLACK));
                System.out.println(e);
                System.out.println(normalizedEdge);
                System.out.println("colors"+colors);
                if (colors.size() > 1) {
                    // Combine colors if there are multiple
                    return new java.awt.GradientPaint(0, 0, colors.get(0), 10, 0, colors.get(1), true);
                } else {
                    return colors.get(0);
                }
            });
        }
    
            
        
        updateEdgesSize();
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));

        // Add interactive mouse controls
        addMouseControls();

        // Add custom mouse scroll listener
        addMouseWheelListener();

        return vv;
    }

    private static String normalizeEdge(String gare1, String gare2) {
        return (gare1.compareTo(gare2) < 0) ? gare1 + " -> " + gare2 : gare2 + " -> " + gare1;
    }
    

    // Add interactive mouse controls
    private static void addMouseControls() {
        DefaultModalGraphMouse<String, String> graphMouse = new DefaultModalGraphMouse<String, String>() {
            @Override
            public void setMode(Mode mode) {
                if (mode == Mode.PICKING) {
                    // Override the functionality for PICKING mode
                    vv.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            if (e.getButton() == MouseEvent.BUTTON1) { // Only handle left click
                                PickedState<String> pickedState = vv.getPickedVertexState();
                                Set<String> pickedVertices = pickedState.getPicked();
                                if (!pickedVertices.isEmpty()) {
                                    handleVerticesSelected(pickedVertices);
                                    pickedState.clear();
                                }
                            } else if (e.getButton() == MouseEvent.BUTTON3) { // Only handle right click
                                System.out.println("Right click");
                            }
                        }
                    });
                }
                super.setMode(mode);
            }
        };
        vv.setGraphMouse(graphMouse);
    }

    // Add custom mouse scroll listener
    private static void addMouseWheelListener() {
        vv.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoomIn += e.getWheelRotation();
                sizeVerteces += e.getWheelRotation() * Window.vertecesZoomFactor * (int) sizeVerteces;
                sizeEdges += e.getWheelRotation() * Window.edgesZoomFactor * (int) sizeEdges;
                updateVertecesSize();
                updateEdgesSize();
                if (zoomIn > Window.zoomThresold) {
                    // Show vertex labels
                    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
                    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);
                } else {
                    vv.getRenderContext().setVertexLabelTransformer(_ -> null);
                }
            }
        });
    }

    // Handle selected vertices
    private static void handleVerticesSelected(Set<String> pickedVertices) {
        for (String vertex : pickedVertices) {
            if (!stackedVertices.contains(vertex)) {
                stackedVertices.add(vertex);
            } else {
                stackedVertices.remove(vertex);
            }
        }
        vv.getRenderContext().setVertexFillPaintTransformer(v -> {
            if (stackedVertices.contains(v)) {
                return ColorPalette.SNCF_PURPLE;
            } else {
                return ColorPalette.SNCF_RED;
            }
        });
    }

    // Update vertex size
    private static void updateVertecesSize() {
        vv.getRenderContext().setVertexShapeTransformer(_ -> {
            return new java.awt.geom.Ellipse2D.Double(-sizeVerteces / 2.0, -sizeVerteces / 2.0, sizeVerteces, sizeVerteces);
        });
    }

    // Update edge size
    private static void updateEdgesSize() {
        vv.getRenderContext().setEdgeStrokeTransformer(_ -> new BasicStroke(sizeEdges));
    }

    // Reset UI to default state
    public static void resetUI() {
        zoomIn = Window.initial_zoomIn;
        sizeVerteces = Window.initial_sizeVerteces;
        sizeEdges = Window.initial_sizeEdges;
        updateVertecesSize();
        updateEdgesSize();
        stackedVertices.clear();
    }

    // Get the list of stacked vertices
    public static List<String> getStackedVertices() {
        return stackedVertices;
    }
}