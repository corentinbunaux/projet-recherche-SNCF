package org.example;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.*;

import java.awt.Dimension;

import javax.swing.*;

public class GraphVisualizer {
    public static void displayGraph(Graph<String, String> graph) {
        Layout<String, String> layout = new CircleLayout<>(graph);
        layout.setSize(new Dimension(300, 300));
        BasicVisualizationServer<String, String> vv = new BasicVisualizationServer<>(layout);
        vv.setPreferredSize(new Dimension(350, 350));

        JFrame frame = new JFrame("JUNG Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }

    public static void GraphVisualisation() {
        Graph<String, String> graph = new SparseMultigraph<>();
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addEdge("Edge-A-B", "A", "B");

        displayGraph(graph);
    }
}