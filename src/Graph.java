import java.io.Serializable;
import java.util.*;

public class Graph implements Serializable{
    private int numVertices;
    private Map<Integer,List<Integer>> adjList;

    public Graph(int numVertices){
        this.numVertices = numVertices;
        this.adjList = new HashMap<>();
        for(int i = 0; i < numVertices; i++)
            adjList.put(i, new ArrayList<>());
    }

    public boolean isValidSolution(List<Integer> path){
        if(path.size() != numVertices + 1)
            return false;
        Set<Integer> visited = new HashSet<>();
        for(int i = 0; i < numVertices; i++){
            int u = path.get(i);
            int v = path.get(i+1);
            if(!isEdge(u,v)) return false;
            //if there is no edge between two vertices there is no cycle

            visited.add(u);
        }
        if(visited.size() == numVertices) // goes through all vertices
            if(path.get(0).equals(path.get(path.size()-1))) //starts and ends in the same vertice
                return true;
        return false;
    }

    public void addEdge(int source, int destination){
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
}
