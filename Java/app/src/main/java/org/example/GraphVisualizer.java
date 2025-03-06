package org.example;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Map;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

public class GraphVisualizer {

    public static VisualizationViewer<String, String> Graph(Graph<String, String> graph, Map<String, Point2D> positions) {
        StaticLayout<String, String> layout = new StaticLayout<>(graph, vertex -> positions.get(vertex), new Dimension(Window.width, Window.height));
        VisualizationViewer<String, String> vv = new VisualizationViewer<>(layout);
        vv.setPreferredSize(new Dimension(Window.width, Window.height));

        // Set initial camera rendering position
        MutableTransformer layoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
        layoutTransformer.setTranslate(Window.width / 2, 0);

        // Labels des sommets
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);

        // Set vertex color
        vv.getRenderContext().setVertexFillPaintTransformer(_ -> ColorPalette.SNCF_RED);

        // Souris interactive
        DefaultModalGraphMouse<String, String> graphMouse = new DefaultModalGraphMouse<>();
        vv.setGraphMouse(graphMouse);

        return vv;
    }

    @SuppressWarnings("unchecked")
    public static DefaultModalGraphMouse<String, String> getGraphMouse(VisualizationViewer<String, String> vv) {
        return (DefaultModalGraphMouse<String, String>) vv.getGraphMouse();
    }
}
