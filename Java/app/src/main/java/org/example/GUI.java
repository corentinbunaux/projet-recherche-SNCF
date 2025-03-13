package org.example;

import java.awt.BorderLayout;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;

public class GUI {

    // Declare static variables for the visualization viewer, toolbar, buttons, and selected button
    private static List<JToggleButton> toggleButtons;
    private static VisualizationViewer<String, String> vv;
    private static JScrollPane manchettePanel;
    private static JSplitPane splitPane;
    private static JFrame frame;
    private static Graph<String, String> railNetwork;
    private static Map<String, Point2D> positions;

    // Method to display the GUI with the given graph and positions
    public static void display(Graph<String, String> railNetwork_arg, Map<String, Point2D> positions_arg) {
        railNetwork = railNetwork_arg;
        positions = positions_arg;
        vv = GraphVisualizer.Graph(railNetwork, positions, null); // Initialize the visualization viewer
        frame = new JFrame("Réseau Ferroviaire - Visualisation"); // Create the main frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setJMenuBar(menuBar()); // Add the menu bar

        manchettePanel = createScrollPane();
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, vv, manchettePanel);
        frame.add(splitPane, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true); // Display the frame
    }

    // FIXME : Pourquoi on génère des manchettes sur le réseau complet ???
    private static JScrollPane createScrollPane() {
        return createScrollPaneWithManchettes(ManchetteGenerator.generateManchettes(railNetwork));
    }

    // Method to create a JScrollPane with the given manchettes
    private static JScrollPane createScrollPaneWithManchettes(List<List<String>> manchettes) {
        JScrollPane scrollPane = new JScrollPane();
        JScrollBar verticalScrollBar = new JScrollBar(JScrollBar.VERTICAL);
        scrollPane.setVerticalScrollBar(verticalScrollBar);

        JTree tree = new JTree();
        javax.swing.tree.DefaultMutableTreeNode manchettes_node = new javax.swing.tree.DefaultMutableTreeNode("Manchettes");

        for (int i = 0; i < manchettes.size(); i++) {
            javax.swing.tree.DefaultMutableTreeNode manchette = new javax.swing.tree.DefaultMutableTreeNode("Manchette " + (i + 1));
            for (int j = 0; j < manchettes.get(i).size(); j++) {
                javax.swing.tree.DefaultMutableTreeNode gare = new javax.swing.tree.DefaultMutableTreeNode(manchettes.get(i).get(j));
                manchette.add(gare);
            }
            manchettes_node.add(manchette);
        }

        tree.setModel(new javax.swing.tree.DefaultTreeModel(manchettes_node));
        tree.collapseRow(0); // Collapse the root node to hide all leaves by default
        scrollPane.setViewportView(tree);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    // Method to create the menu bar
    private static JMenuBar menuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu("Menu")); // Add a menu to the menu bar

        toggleButtons = new ArrayList<>();
        toggleButtons.add(toggleButton("Movement"));
        toggleButtons.add(toggleButton("Selection"));
        // Default mode : Movement
        toggleButtons.get(0).setSelected(true);

        List<JButton> buttons = new ArrayList<>();
        buttons.add(button("Manchette"));
        buttons.add(button("Reset"));

        for (JToggleButton toggleButton : toggleButtons) {
            menuBar.add(toggleButton); // Add a menu item to the menu bar
        }
        for (JButton button : buttons) {
            menuBar.add(button); // Add a menu item to the menu bar
        }

        return menuBar;
    }

    // Method to create a toggle button with the given title
    private static JToggleButton toggleButton(String title) {
        JToggleButton toggleButton = new JToggleButton(new javax.swing.ImageIcon("img/" + title + ".png"));
        toggleButton.setPreferredSize(new java.awt.Dimension(30, 30));
        toggleButton.setToolTipText(title);
        toggleButton.addActionListener(_ -> {
            resetToggleButtons();
            toggleButton.setSelected(true);
            handleToggleButton(title);
        });

        return toggleButton;
    }

    // Method to create a button with the given title
    private static JButton button(String title) {
        JButton button = new JButton(new javax.swing.ImageIcon("img/" + title + ".png"));
        button.setPreferredSize(new java.awt.Dimension(30, 30));
        button.setToolTipText(title);
        button.addActionListener(_ -> {
            resetToggleButtons();
            // Movement mode activated by default
            toggleButtons.get(0).setSelected(true);
            handleButtons(title);
        });
        return button;
    }

    // Method to handle toggle button actions
    private static void handleToggleButton(String title) {
        switch (title) {
            case "Movement" ->
                ((ModalGraphMouse) vv.getGraphMouse()).setMode(ModalGraphMouse.Mode.TRANSFORMING);
            case "Selection" ->
                ((ModalGraphMouse) vv.getGraphMouse()).setMode(ModalGraphMouse.Mode.PICKING);
            default -> {
            }
        }
    }

    // Method to handle button actions
    private static void handleButtons(String title) {
        switch (title) {
            case "Manchette" ->
                updateManchetteUI();
            case "Reset" ->
                reset();
            default -> {
            }
        }
    }

    // Method to reset the UI
    private static void reset() {
        frame.getContentPane().remove(splitPane);
        GraphVisualizer.resetUI();
        vv = GraphVisualizer.Graph(railNetwork, positions, null);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, vv, manchettePanel);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    // Method to reset toggle buttons
    private static void resetToggleButtons() {
        for (JToggleButton toggleButton : toggleButtons) {
            toggleButton.setSelected(false);
        }
    }

    // Method to create a menu with the given text
    private static JMenu menu(String text) {
        JMenu menu = new JMenu(text);
        menu.add(menuItem("Filtrer")); // Add a menu item to the menu
        return menu;
    }

    // Method to create a menu item with the given text
    private static JMenuItem menuItem(String text) {
        return new JMenuItem(text);
    }

    // Method to update the Manchette UI
    private static void updateManchetteUI() {
        frame.getContentPane().remove(splitPane);
        Graph<String, String> subgraph = RailNetwork.subGraphListVerteces(GraphVisualizer.getStackedVertices(), railNetwork);
        List<List<String>> manchettes=ManchettesOptimized.generateManchettes(subgraph);
        manchettePanel = createScrollPaneWithManchettes(manchettes);
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, GraphVisualizer.Graph(subgraph, positions, manchettes), manchettePanel);
        GraphVisualizer.resetUI();
        frame.add(splitPane, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    
}