package org.example;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
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

    private static int zoomIn = 0;
    private static float sizeVerteces = 7.0f;
    private static float sizeEdges = 1.0f;
    private static VisualizationViewer<String, String> vv;
    private static final List<String> stackedVertices = new ArrayList<>();

    @SuppressWarnings("Convert2Lambda")
    public static VisualizationViewer<String, String> Graph(Graph<String, String> graph, Map<String, Point2D> positions) {
        StaticLayout<String, String> layout = new StaticLayout<>(graph, vertex -> positions.get(vertex), new Dimension(Window.width, Window.height));
        vv = new VisualizationViewer<>(layout);
        vv.setPreferredSize(new Dimension(Window.width, Window.height));

        // Set vertex color and size
        vv.getRenderContext().setVertexFillPaintTransformer(_ -> ColorPalette.SNCF_RED);
        updateVertecesSize();

        // Set edge color, size and shape
        vv.getRenderContext().setEdgeDrawPaintTransformer(_ -> ColorPalette.SNCF_BLACK);
        updateEdgesSize();
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));

        // Souris interactive
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

        //Add a curstom mouse scroll listener
        vv.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoomIn += e.getWheelRotation();
                sizeVerteces += e.getWheelRotation() * 0.03f * (int) sizeVerteces;
                sizeEdges += e.getWheelRotation() * 0.05f * (int) sizeEdges;
                updateVertecesSize();
                updateEdgesSize();
                if (zoomIn > Window.zoomThresold) {
                    // Labels des sommets
                    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
                    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);
                } else {
                    vv.getRenderContext().setVertexLabelTransformer(_ -> null);
                }
            }
        });

        return vv;
    }

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

    private static void updateVertecesSize() {
        // Set vertex size
        vv.getRenderContext().setVertexShapeTransformer(_ -> {
            return new java.awt.geom.Ellipse2D.Double(-sizeVerteces / 2.0, -sizeVerteces / 2.0, sizeVerteces, sizeVerteces);
        });
    }

    private static void updateEdgesSize() {
        vv.getRenderContext().setEdgeStrokeTransformer(_ -> new BasicStroke(sizeEdges));
    }

    public static void resetUI() {
        zoomIn = 0;
        sizeVerteces = 7.0f;
        sizeEdges = 1.0f;
        updateVertecesSize();
        updateEdgesSize();
        stackedVertices.clear();
    }

    public static List<String> getStackedVertices() {
        return stackedVertices;
    }
}
