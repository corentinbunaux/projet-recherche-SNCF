package org.example;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;

public class GUI {

    // Declare static variables for the visualization viewer, toolbar, buttons, and selected button
    private static VisualizationViewer<String, String> vv;
    private static JPanel toolbar;
    private static List<JButton> buttons;
    private static JButton selectedButton;

    // Method to display the GUI with the given graph and positions
    public static void display(Graph<String, String> railNetwork, Map<String, Point2D> positions) {
        toolbar = toolBar(); // Initialize the toolbar
        vv = GraphVisualizer.Graph(railNetwork, positions); // Initialize the visualization viewer
        JFrame frame = new JFrame("Réseau Ferroviaire - Visualisation"); // Create the main frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.setJMenuBar(menuBar()); // Add the menu bar
        frame.add(toolbar, BorderLayout.NORTH); // Add the toolbar to the frame
        frame.add(vv, BorderLayout.CENTER); // Add the visualization viewer to the frame
        frame.pack();
        frame.setVisible(true); // Display the frame
    }

    // Method to create the toolbar with buttons
    private static JPanel toolBar() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttons = new java.util.ArrayList<>();
        selectedButton = button("Movement"); // Initialize the first button as selected
        buttons.add(selectedButton);
        buttons.add(button("Selection"));
        buttons.add(button("Manchette"));
        buttons.add(button("Reset"));
        for (JButton button : buttons) {
            panel.add(button); // Add buttons to the panel
        }
        selectedButton.setBackground(ColorPalette.SNCF_COOL_GRAY_7); // Set the background color of the selected button
        return panel;
    }

    // Method to create a button with the given image path
    private static JButton button(String imagePath) {
        JButton button = new JButton(new javax.swing.ImageIcon("img/" + imagePath + ".png"));
        button.setPreferredSize(new java.awt.Dimension(30, 30));
        button.setBackground(button != selectedButton ? java.awt.Color.WHITE : ColorPalette.SNCF_COOL_GRAY_7);
        button.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY));
        button.setFocusPainted(false);

        // Add mouse listeners to change the button's background color on hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button != selectedButton) {
                    button.setBackground(ColorPalette.SNCF_COOL_GRAY_3);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button != selectedButton) {
                    button.setBackground(java.awt.Color.WHITE);
                }
            }
        });

        // Add action listener to handle button clicks
        button.addActionListener(_ -> btnHandler(button, imagePath));
        return button;
    }

    // Method to handle button clicks
    private static void btnHandler(JButton button, String btnName) {
        // Reset the previously selected button's background color
        if (selectedButton != null) {
            selectedButton.setBackground(java.awt.Color.WHITE);
        }

        // Handle different button actions
        switch (btnName) {
            case "Movement" ->
                movementHandler(button, vv);
            case "Selection" ->
                selectionHandler(button, vv);
            case "Manchette" ->
                System.out.println("Mode manchette");
            case "Reset" ->
                System.out.println("Réinitialisation");
            default ->
                System.out.println("Mode inconnu");
        }

        // Revalidate and repaint the toolbar to reflect changes
        toolbar.revalidate();
        toolbar.repaint();
    }

    // Method to handle the "Movement" button action
    private static void movementHandler(JButton button, VisualizationViewer<String, String> vv) {
        colorButton(button); // Change the button color
        GraphVisualizer.getGraphMouse(vv).setMode(ModalGraphMouse.Mode.TRANSFORMING); // Set the graph mouse mode to transforming
    }

    // Method to handle the "Selection" button action
    private static void selectionHandler(JButton button, VisualizationViewer<String, String> vv) {
        colorButton(button); // Change the button color
        GraphVisualizer.getGraphMouse(vv).setMode(ModalGraphMouse.Mode.PICKING); // Set the graph mouse mode to picking
    }

    // Method to change the color of the selected button
    private static void colorButton(JButton button) {
        selectedButton = button; // Set the new selected button
        button.setBackground(ColorPalette.SNCF_COOL_GRAY_7); // Change the background color
        button.repaint();
        button.revalidate();
    }

    // Method to create the menu bar
    private static JMenuBar menuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu("Menu")); // Add a menu to the menu bar
        return menuBar;
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
}
