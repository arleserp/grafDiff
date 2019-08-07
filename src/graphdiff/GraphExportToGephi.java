/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphdiff;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import graphutil.GraphSerialization;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import unalcol.agents.NetworkSim.GraphElements;

/**
 *
 * @author arlese.rodriguezp
 */
public class GraphExportToGephi {

    static Graph<GraphElements.MyVertex, String> A;

    public static void main(String[] args) {
        File srcDir = new File("./");
        File[] filesSrcDir = srcDir.listFiles();

        for (File fileSrc : filesSrcDir) {
            String fileA = fileSrc.getName();
            int i = fileA.lastIndexOf('.');
            String extension = "";
            int p = Math.max(fileA.lastIndexOf('/'), fileA.lastIndexOf('\\'));
            if (i > p) {
                extension = fileA.substring(i + 1);
            }
            if (fileSrc.isFile() && extension.equals("graph")) {
                String outputDir = "csvgephi";
                System.out.println("loading.... " + fileA);
                A = GraphSerialization.loadDeserializeGraph(fileA);
                System.out.println("A" + A);
                createDir("csvgephi");
                System.out.println("Loaded converting to gephi... ");

                //fileA = fileA.replace(".graph", "");
                //spoke condition added for spokecommunitycircle+v+100+beta+0.5+degree+4+clusters+4+sp+10+length+3
                // communitycircle+v+100+beta+0.5+degree+4+clusters+4 is substring
                try {
                    //node file                                
                    String nodeCSV = "./" + outputDir + "/" + fileA.replace("graph", "node.csv");
                    String edgeCSV = "./" + outputDir + "/" + fileA.replace("graph", "edge.csv");

                    PrintWriter nodeCSVFile;
                    nodeCSVFile = new PrintWriter(new BufferedWriter(new FileWriter(nodeCSV, true)));
                    int nodeId = 0;
                    HashMap<String, Integer> dictIds = new HashMap<>();
                    nodeCSVFile.println("Id,Label");

                    // draw the vertices in the graph
                    for (GraphElements.MyVertex v : A.getVertices()) {
                        // Get the position of the vertex                
                        nodeCSVFile.println(nodeId + "," + v.getName());
                        dictIds.put(v.getName(), nodeId++);
                    }

                    try (PrintWriter edgeCSVFile = new PrintWriter(new BufferedWriter(new FileWriter(edgeCSV, true)))) {
                        edgeCSVFile.println("Source,Target");

                        // draw the edges
                        //problem of implementation????? when i repair network structure I rename edges!
                        A.getEdges().stream().forEach((ed) -> {
                            //System.out.println("edge"+ ed);
                            // get the end points of the edge
                            Pair<GraphElements.MyVertex> endpoints = A.getEndpoints(ed);

                            //due to I generate edges with a different name! :(
                            String newname = "e" + endpoints.getFirst().getName() + endpoints.getSecond().getName();
                            String newnameB = "e" + endpoints.getSecond().getName() + endpoints.getFirst().getName();
                            String newnameC = "eb" + endpoints.getFirst().getName() + endpoints.getSecond().getName();
                            String newnameD = "eb" + endpoints.getSecond().getName() + endpoints.getFirst().getName();
                            edgeCSVFile.println(dictIds.get(endpoints.getFirst().getName()) + "," + dictIds.get(endpoints.getSecond().getName()));
                        });
                        nodeCSVFile.close();
                        edgeCSVFile.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(GraphExportToGephi.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static void createDir(String filename) {
        File theDir = new File(filename);

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("creating directory: " + filename);
            boolean result = false;

            try {
                theDir.mkdir();
                result = true;
            } catch (SecurityException se) {
                System.out.println("Security Exception!");
            }
            if (result) {
                System.out.println("DIR created");
            }
        }

    }

    public static boolean containsVertex(Graph<GraphElements.MyVertex, String> g, String name) {
        return g.getVertices().stream().anyMatch((v) -> (v.getName().equals(name)));
    }
}
