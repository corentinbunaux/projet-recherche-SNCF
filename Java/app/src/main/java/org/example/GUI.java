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
        vv = GraphVisualizer.Graph(railNetwork, positions); // Initialize the visualization viewer
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

    private static JScrollPane createScrollPane() {
        JScrollPane scrollPane = new JScrollPane();

        JScrollBar verticalScrollBar = new JScrollBar(JScrollBar.VERTICAL);
        scrollPane.setVerticalScrollBar(verticalScrollBar);

        JTree tree = new JTree();
        javax.swing.tree.DefaultMutableTreeNode manchette1 = new javax.swing.tree.DefaultMutableTreeNode("Manchette 1");
        javax.swing.tree.DefaultMutableTreeNode gare1 = new javax.swing.tree.DefaultMutableTreeNode("Gare 1");
        javax.swing.tree.DefaultMutableTreeNode gare2 = new javax.swing.tree.DefaultMutableTreeNode("Gare 2");
        manchette1.add(gare1);
        manchette1.add(gare2);
        tree.setModel(new javax.swing.tree.DefaultTreeModel(manchette1));
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
        //Default mode : Movement
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

    private static void reset() {
        frame.getContentPane().remove(splitPane);
        GraphVisualizer.resetUI();
        vv = GraphVisualizer.Graph(railNetwork, positions);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, vv, manchettePanel);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

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
        JMenuItem menuItem = new JMenuItem(text);
        return menuItem;
    }

    private static void updateManchetteUI() {
        frame.getContentPane().remove(splitPane);
        Graph<String, String> subgraph = RailNetwork.subGraphListVerteces(GraphVisualizer.getStackedVertices(), railNetwork);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, GraphVisualizer.Graph(subgraph, positions), manchettePanel);
        GraphVisualizer.resetUI();
        frame.add(splitPane, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
}
