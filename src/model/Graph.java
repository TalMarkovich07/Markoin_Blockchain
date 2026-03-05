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



}
