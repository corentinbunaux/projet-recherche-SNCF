package org.example;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.JFrame;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class GraphVisualizer {
    public static void displayGraph(Graph<String, String> graph, Map<String, Point2D> positions) {
        StaticLayout<String, String> layout = new StaticLayout<>(graph, vertex -> positions.get(vertex), new Dimension(Window.width, Window.height));
        VisualizationViewer<String, String> vv = new VisualizationViewer<>(layout);
        vv.setPreferredSize(new Dimension(Window.width, Window.height));

        // Set initial camera rendering position
        MutableTransformer layoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
        layoutTransformer.setTranslate(Window.width/2, 0);
        
        // Labels des sommets
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

        // 🎛 Souris interactive
        DefaultModalGraphMouse<String, String> graphMouse = new DefaultModalGraphMouse<>();
        vv.setGraphMouse(graphMouse);

        // Ajout d'un menu pour changer de mode
        JComboBox<ModalGraphMouse.Mode> modeBox = new JComboBox<>(ModalGraphMouse.Mode.values());
        modeBox.addActionListener(e -> graphMouse.setMode((ModalGraphMouse.Mode) modeBox.getSelectedItem()));

        // Interface graphique
        JPanel panel = new JPanel();
        panel.add(modeBox);

        // Création de la fenêtre
        JFrame frame = new JFrame("Réseau Ferroviaire - Visualisation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(vv, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }
}