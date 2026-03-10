package model;

import java.io.Serializable;
import java.util.*;

public class Graph implements Serializable{
    private int numVertices;
    private Map<Integer,List<Integer>> adjList;

    private Graph(int numVertices){
        this.numVertices = numVertices;
        this.adjList = new HashMap<>();
        for(int i = 0; i < numVertices; i++)
            adjList.put(i, new ArrayList<>());

    }
    private void addEdge(int source, int destination){
        if (!adjList.get(source).contains(destination)) {
            adjList.get(source).add(destination);
            adjList.get(destination).add(source);
        }
    }
    public static Graph firstBlockGraph(int numVertices){
        Graph graph = new Graph(numVertices);
        int i = 0;
        for(; i < numVertices-1; i++)
            graph.addEdge(i, i+1);
        graph.addEdge(0, i);
        return graph;
    }
    public List<Integer> getNeighbors(int vertex){
        return adjList.get(vertex);
    }
    public int getNumVertices(){
        return numVertices;
    }
    public boolean isEdge(int source, int destination){
        return adjList.get(source).contains(destination);
    }
    public static Graph generateGraph(int numVertices){
        // generate hamiltonian graph using Dirac's theorem
        Graph graph = new Graph(numVertices);
        Random random = new Random();
        int minDegree = (int) Math.ceil(numVertices / 2.0);

        for (int i = 0; i < numVertices; i++) {
            while (graph.getNeighbors(i).size() < minDegree) {
                int target = random.nextInt(numVertices);
                if (i != target)
                    graph.addEdge(i, target);
            }
        }
        return graph;
    }
    public void printGraph() {
        System.out.println("Graph Structure (Adjacency List):");
        for (int i = 0; i < numVertices; i++) {
            List<Integer> neighbors = adjList.get(i);
            System.out.print("Vertex " + i + " is connected to: ");
            if (neighbors.isEmpty()) {
                System.out.print("No neighbors");
            } else {
                for (int j = 0; j < neighbors.size(); j++) {
                    System.out.print(neighbors.get(j));
                    if (j < neighbors.size() - 1) {
                        System.out.print(", ");
                    }
                }
            }
            System.out.println();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph (").append(numVertices).append(" vertices):\n");

        for (int i = 0; i < numVertices; i++) {
            sb.append("  [").append(i).append("] -> {");
            List<Integer> neighbors = adjList.get(i);

            for (int j = 0; j < neighbors.size(); j++) {
                sb.append(neighbors.get(j));
                if (j < neighbors.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}\n");
        }
        return sb.toString();
    }



}
