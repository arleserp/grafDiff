/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphdiff;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import graphutil.GraphSerialization;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationModel;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.Group;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineBuilder;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import unalcol.agents.NetworkSim.GraphElements;

/**
 *
 * @author arlese.rodriguezp
 */
public class FXMLDocumentController implements Initializable {

    FileChooser fileChooser = new FileChooser();
    File fileA;
    File fileB;
    Group visA;
    Graph<GraphElements.MyVertex, String> A;
    Graph<GraphElements.MyVertex, String> B;

    Layout<GraphElements.MyVertex, String> layout = null;
    private static final int CIRCLE_SIZE = 10; // default circle size

    @FXML
    VBox vbMenu;

    @FXML
    AnchorPane pane;

    @FXML
    AnchorPane cbpane;

    @FXML
    private Label label;

    @FXML
    AnchorPane networksPane;

    @FXML
    ComboBox<String> comboBoxLayout;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");

        label.setText("Hello World!");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fileChooser.setInitialDirectory(new File("C:\\temp"));
        fileA = null;
        visA = new Group();
        ObservableList<String> options
                = FXCollections.observableArrayList(
                        "ISOMLayout",
                        "CircleLayout"
                );
        comboBoxLayout = new ComboBox<>(options);
        comboBoxLayout.setValue("ISOMLayout");
        cbpane.getChildren().add(comboBoxLayout);

        // label.setText("");
        comboBoxLayout.valueProperty().addListener((obs, oldItem, newItem) -> {
//            //label.textProperty().unbind();
            if (newItem == null) {
                //label.setText("");
            } else {
                // label.textProperty().bind(newItem.detailsProperty());
                //System.out.println("juju");
                networksPane.getChildren().clear();

                initialize(url, rb);
                if (A != null && B != null) {
                    draw();
                }
            }
        });
    }

    @FXML
    private void handleMenuLoadNetworkA(ActionEvent event) {
        //System.out.println("helloooooo!");
        Window stage = vbMenu.getScene().getWindow();
        fileChooser.setTitle("Choose a graph file");
        //chooser.getExtensionFilters().add(extFilter);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("graph files (*.graph)", "*.graph");
        fileChooser.getExtensionFilters().add(extFilter);

        try {
            fileA = fileChooser.showOpenDialog(stage);
            System.out.println("opeeeen");
            fileChooser.setInitialDirectory(fileA.getParentFile());
            //TODO load file
            System.out.println("file: " + fileA);
        } catch (Exception ex) {
            System.out.println("error!" + ex.getLocalizedMessage());
        }

        A = GraphSerialization.loadDeserializeGraph(fileA.toString());
        System.out.println("A" + A);
        //layout = new CircleLayout<>(A);
        //layout = new ISOMLayout<>(A);
        switch (comboBoxLayout.getValue()) {
            case "ISOMLayout":
                layout = new ISOMLayout<>(A);
                break;
            case "CircleLayout":
                layout = new CircleLayout<>(A);
                break;
            default:
                layout = new ISOMLayout<>(A);
        }
        VisualizationModel<GraphElements.MyVertex, String> vm1 = new DefaultVisualizationModel<>(layout, new Dimension(800, 800));
        renderGraph(A, layout, visA);
        networksPane.getChildren().add(visA);
    }

    @FXML
    private void handleMenuLoadNetworkB(ActionEvent event) {
        //System.out.println("helloooooo!");        
        Window stage = vbMenu.getScene().getWindow();
        fileChooser.setTitle("Choose a graph file");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("graph file", "*.graph"));
        visA = new Group();
        try {
            fileB = fileChooser.showOpenDialog(stage);
            System.out.println("opeeeen");
            fileChooser.setInitialDirectory(fileB.getParentFile());
            //TODO load file
            System.out.println("file: " + fileB);
            B = GraphSerialization.loadDeserializeGraph(fileB.toString());
            System.out.println("B" + B);
            draw();
        } catch (Exception ex) {

        }
    }

    @FXML
    private void handleMenuExportNetworkDiff(ActionEvent event) {
        if (A == null) {
            System.out.println("You must load network A");
        }
        if (B == null) {
            System.out.println("You must load network B");
        }
        try {
            //node file            
            System.out.println("Filename: " + fileB.getName());
            String nodeCSV = fileB.getName().replace("graph", "diff.node.csv");
            String edgeCSV = fileB.getName().replace("graph", "diff.edge.csv");

            PrintWriter nodeCSVFile;
            nodeCSVFile = new PrintWriter(new BufferedWriter(new FileWriter(nodeCSV, true)));
            int nodeId = 0;
            HashMap<String, Integer> dictIds = new HashMap<>();
            nodeCSVFile.println("Id,Label,State");

            // draw the vertices in the graph
            for (GraphElements.MyVertex v : A.getVertices()) {
                // Get the position of the vertex                
                if (containsVertex(B, v.getName())) {
                    nodeCSVFile.println(nodeId + "," + v.getName() + ",Recovered");
                } else {
                    nodeCSVFile.println(nodeId + "," + v.getName() + ",Failed");
                }
                dictIds.put(v.getName(), nodeId++);
            }

            PrintWriter edgeCSVFile = new PrintWriter(new BufferedWriter(new FileWriter(edgeCSV, true)));
            edgeCSVFile.println("Source,Target,Type,State");

            // draw the edges
            //problem of implementation????? when i repair network structure I rename edges!
            for (String ed : A.getEdges()) {
                //System.out.println("edge"+ ed);
                // get the end points of the edge
                Pair<GraphElements.MyVertex> endpoints = A.getEndpoints(ed);
                                                
                //due to I generate edges with a different name! :(
                String newname = "e" + endpoints.getFirst().getName() + endpoints.getSecond().getName();
                String newnameB = "e" + endpoints.getSecond().getName() + endpoints.getFirst().getName();
                String newnameC = "eb" + endpoints.getFirst().getName() + endpoints.getSecond().getName();
                String newnameD = "eb" + endpoints.getSecond().getName() + endpoints.getFirst().getName();
                if (!B.containsEdge(ed) && !B.containsEdge(newname) && !B.containsEdge(newnameB) && !B.containsEdge(newnameC) && !B.containsEdge(newnameD)) {
                   edgeCSVFile.println(dictIds.get(endpoints.getFirst().getName()) + "," + dictIds.get(endpoints.getSecond().getName()) + ",Directed,EdgeFailed");                    
                }else{
                    edgeCSVFile.println(dictIds.get(endpoints.getFirst().getName()) + "," + dictIds.get(endpoints.getSecond().getName()) + ",Directed,EdgeRecovered");
                }                        
            }
            nodeCSVFile.close();
            edgeCSVFile.close();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void draw() {
        networksPane.getChildren().clear();
        //layout = new CircleLayout<>(A);
        switch (comboBoxLayout.getValue()) {
            case "ISOMLayout":
                layout = new ISOMLayout<>(A);
                break;
            case "CircleLayout":
                layout = new CircleLayout<>(A);
                break;
            default:
                layout = new ISOMLayout<>(A);
        }

        VisualizationModel<GraphElements.MyVertex, String> vm1 = new DefaultVisualizationModel<>(layout, new Dimension(800, 800));
        renderGraph(A, B, layout, visA);
        networksPane.getChildren().add(visA);

    }

    /**
     * Render a graph to a particular <code>Group</code>
     *
     * @param graph
     * @param layout
     * @param g
     */
    private void renderGraph(Graph<GraphElements.MyVertex, String> graph, Layout<GraphElements.MyVertex, String> layout, Group g) {
        // draw the vertices in the graph
        for (GraphElements.MyVertex v : graph.getVertices()) {
            // Get the position of the vertex
            Point2D p = layout.transform(v);

            // draw the vertex as a circle
            Circle circle = CircleBuilder.create()
                    .centerX(p.getX())
                    .centerY(p.getY())
                    .radius(CIRCLE_SIZE)
                    .build();
            circle.setFill(Color.BLUE);
            //System.out.println("circle" + circle);
            // add it to the group, so it is shown on screen
            g.getChildren().add(circle);
        }

        // draw the edges
        for (String n : graph.getEdges()) {
            //System.out.println("n" + n);
            // get the end points of the edge
            Pair<GraphElements.MyVertex> endpoints = graph.getEndpoints(n);

            // Get the end points as Point2D objects so we can use them in the 
            // builder
            Point2D pStart = layout.transform(endpoints.getFirst());
            Point2D pEnd = layout.transform(endpoints.getSecond());

            // Draw the line
            Line line = LineBuilder.create()
                    .startX(pStart.getX())
                    .startY(pStart.getY())
                    .endX(pEnd.getX())
                    .endY(pEnd.getY())
                    .build();
            // add the edges to the screen
            g.getChildren().add(line);
        }
    }

    private void renderGraph(Graph<GraphElements.MyVertex, String> A, Graph<GraphElements.MyVertex, String> B, Layout<GraphElements.MyVertex, String> layout, Group g) {
        // draw the vertices in the graph
        for (GraphElements.MyVertex v : A.getVertices()) {
            // Get the position of the vertex
            Point2D p = layout.transform(v);

            // draw the vertex as a circle
            Circle circle = CircleBuilder.create()
                    .centerX(p.getX())
                    .centerY(p.getY())
                    .radius(CIRCLE_SIZE)
                    .build();

            if (containsVertex(B, v.getName())) {
                circle.setFill(Color.BLUE);
            } else {
                circle.setFill(Color.YELLOW);
            }
            //System.out.println("circle" + circle);
            // add it to the group, so it is shown on screen
            g.getChildren().add(circle);
        }

        // draw the edges
        //problem of implementation????? when i repair network structure I rename edges!
        for (String ed : A.getEdges()) {
            //System.out.println("edge"+ ed);
            // get the end points of the edge
            Pair<GraphElements.MyVertex> endpoints = A.getEndpoints(ed);

            // Get the end points as Point2D objects so we can use them in the 
            // builder
            Point2D pStart = layout.transform(endpoints.getFirst());
            Point2D pEnd = layout.transform(endpoints.getSecond());

            // Draw the line
            Line line = LineBuilder.create()
                    .startX(pStart.getX())
                    .startY(pStart.getY())
                    .endX(pEnd.getX())
                    .endY(pEnd.getY())
                    .build();

            //due to I generate edges with a different name! :(
            String newname = "e" + endpoints.getFirst().getName() + endpoints.getSecond().getName();
            String newnameB = "e" + endpoints.getSecond().getName() + endpoints.getFirst().getName();
            String newnameC = "eb" + endpoints.getFirst().getName() + endpoints.getSecond().getName();
            String newnameD = "eb" + endpoints.getSecond().getName() + endpoints.getFirst().getName();
            if (!B.containsEdge(ed) && !B.containsEdge(newname) && !B.containsEdge(newnameB) && !B.containsEdge(newnameC) && !B.containsEdge(newnameD)) {
                line.setStroke(Color.RED);
            }
            // add the edges to the screen
            g.getChildren().add(line);
        }
    }

    public boolean containsVertex(Graph<GraphElements.MyVertex, String> g, String name) {
        for (GraphElements.MyVertex v : g.getVertices()) {
            if (v.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
