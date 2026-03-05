import model.Graph;

import java.util.ArrayList;
import java.util.List;

public class HamiltonSolver {

    public static List<Integer> findCycle(Graph graph){
       int n = graph.getNumVertices();
       int[] path = new int[n];
       for(int i = 0; i < n; i++) path[i] = -1;

       path[0] = 0; // always starting from vertex 0

       if(solve(graph, path, 1)){
           List<Integer> result = new ArrayList<>();
           for(int node : path) result.add(node);
           result.add(path[0]);
           return result;
       }
       return null;
    }

    private static boolean solve(Graph graph, int[] path, int pos){
        int n  = graph.getNumVertices();
        if(pos == n) //if vertex is the last one, make sure it has an edge to the first one
            return graph.isEdge(path[pos-1], path[0]);

        for(int v = 1; v < n; v++){
            if(isSafe(v, graph, path, pos)){
                path[pos] = v;
                if(solve(graph, path, pos+1)) return true;
                path[pos] = -1;
            }
        }
        return false;
    }
    private static boolean isSafe(int v, Graph graph, int[] path, int pos){
        // checks if given vertex can be the next vertex in the solution
        if(!graph.isEdge(path[pos-1], v)) return false; //checks if there is an edge between last vertex and given vertex
        for(int i = 0; i < pos; i++)
            if(path[i] == v) return false; //checks if vertex already exists in the solution
        return true;
    }

    public static boolean verifySolution(Graph graph, List<Integer> path) {
        if (path == null || path.size() != graph.getNumVertices() + 1) return false;
        //path size must equal all vertices + 1

        boolean[] visited = new boolean[graph.getNumVertices()];

        for (int i = 0; i < path.size() - 1; i++) {

            //checks that there is a vertex between any 2 adjacent vertices
            int u = path.get(i);
            int v = path.get(i + 1);
            if (!graph.isEdge(u, v)) return false;

            //checks that no vertex appears twice
            if (u < 0 || u >= graph.getNumVertices()) return false;
            if (visited[u]) return false;
            visited[u] = true;
        }
        //checks that the first vertex is the same as the last vertex
        return path.get(0).equals(path.get(path.size() - 1));
    }
}
