package hw6;

import java.util.*;


public class DijkstraStreetSearcher extends StreetSearcher {

  /**
   * Creates a StreetSearcher object.
   *
   * @param graph an implementation of Graph ADT.
   */
  public DijkstraStreetSearcher(Graph<String, String> graph) {
    super(graph);
  }

  private void setUp(Vertex<String> start, Set<Vertex<String>> explored, Map<Vertex<String>, Double> distances,
                     PriorityQueue<DistanceNode> queue) {
    for (Vertex<String> vertex : graph.vertices()) {
      distances.put(vertex, MAX_DISTANCE);
    }
    distances.put(start, 0.0);
    explored.add(start);
    queue.add(new DistanceNode(start, 0.0));
  }

  private boolean checkValid(String start, String end) {
    try {
      checkValidEndpoint(start);
      checkValidEndpoint(end);
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid Endpoint: " + end);
      return false;
    }
    return true;
  }

  @Override
  public void findShortestPath(String startName, String endName) {
    Vertex<String> start = vertices.get(startName);
    Vertex<String> end = vertices.get(endName);

    if (!checkValid(startName, endName)) {
      return;
    }

    Set<Vertex<String>> explored = new HashSet<>();
    Map<Vertex<String>, Double> distances = new HashMap<>();
    PriorityQueue<DistanceNode> pq = new PriorityQueue<>();

    double totalDist = -1;
    if (!start.equals(end)) {
      setUp(start, explored, distances, pq);
      findShortestHelper(explored, distances, pq);
      totalDist = distances.get(end);
    }

    // These method calls will create and print the path for you
    List<Edge<String>> path = getPath(end, start);
    if (VERBOSE) {
      printPath(path, totalDist);
    }
  }

  private void findShortestHelper(Set<Vertex<String>> explored, Map<Vertex<String>, Double> distances,
                                  PriorityQueue<DistanceNode> pq) {
    while (!pq.isEmpty()) {
      DistanceNode minDist = pq.poll();
      Vertex<String> curr = minDist.vertex;

      for (Edge<String> outgoingEdge : graph.outgoing(curr)) {
        Vertex<String> neighbor = graph.to(outgoingEdge);
        if (!explored.contains(neighbor)) {
          double tempDistance = distances.get(curr) + (double) graph.label(outgoingEdge);
          if (tempDistance < distances.get(neighbor)) {
            distances.put(neighbor, tempDistance);
            graph.label(neighbor, outgoingEdge);
            pq.add(new DistanceNode(neighbor, tempDistance));
          }
        }
      }
    }
  }

  // PRIM IS UNRELATED TO HW
  public Graph<String, String> prim(Graph<String, String> graph) {
    Graph<String, String> mst = new SparseGraph<>();
    Vertex<String> start = graph.vertices().iterator().next();
    PriorityQueue<Edge<String>> pq = new PriorityQueue<>();
    HashSet<Vertex<String>> explored = new HashSet<>();

    explored.add(start);
    mst.insert(start.get());
    for (Edge<String> edge : graph.outgoing(start)) {
      pq.add(edge);
    }
    while (!pq.isEmpty()) {
      Edge<String> minEdge = pq.poll();
      if (!explored.contains(graph.to(minEdge))) {
        mst.insert(graph.to(minEdge).get());
        mst.insert(graph.from(minEdge), graph.to(minEdge), minEdge.get());
        explored.add(graph.to(minEdge));

        for (Edge<String> outgoingEdge : graph.outgoing(graph.to(minEdge))) {
          pq.add(outgoingEdge);
        }
      }
    }
    return mst;
  }

  // KRUSKAL IS UNRELATED TO HW
  public Graph<String, String> kruskal(Graph<String, String> graph) {
    PriorityQueue<Edge<String>> edges = new PriorityQueue<>();

    // create a min-heap of edges
    for (Edge<String> edge : graph.edges()) {
      edges.add(edge);
    }

    int vertexCount = 0;
    for (Vertex<String> vertex : graph.vertices()) {
      vertexCount++;
    }

    // create union find with space for all vertices
    StringUnionFind uf = new StringUnionFind();

    for (Vertex<String> vertex : graph.vertices()) {
      uf.add(vertex.get()); // Ensure all vertices are initialized
    }

    Graph<String, String> mst = new SparseGraph<>();
    int edgesAdded = 0;

    // white the min heap contains edges to look at
    while (edgesAdded < vertexCount - 1) {
      // pick to minimum edge
      Edge<String> min = edges.poll();

      while (Objects.equals(uf.find(graph.from(min).get()), uf.find(graph.to(min).get()))) {
        min = edges.poll();
      }
      // if the edge does not create a cycle, insert it into the mst
      if (uf.union((String) graph.from(min).get(), (String) graph.to(min).get())) {
        mst.insert(graph.from(min), graph.to(min), (String) graph.label(min));
      }
      edgesAdded++;
    }

    return mst;
  }

  private static class DistanceNode implements Comparable<DistanceNode> {
    Vertex<String> vertex;
    double distance;

    DistanceNode(Vertex<String> vertex, double distance) {
      this.vertex = vertex;
      this.distance = distance;
    }

    @Override
    public int compareTo(DistanceNode o) {
      return Double.compare(distance, o.distance);
    }
  }

  // Union-Find (Disjoint Set) class
  static class StringUnionFind {
    private Map<String, String> parent;
    private Map<String, Integer> rank;

    // Constructor to initialize parent and rank maps
    StringUnionFind() {
      parent = new HashMap<>();
      rank = new HashMap<>();
    }

    // Add a new node
    public void add(String node) {
      if (!parent.containsKey(node)) {
        parent.put(node, node); // Node is its own parent initially
        rank.put(node, 0);      // Rank starts at 0
      }
    }

    // Find with path compression
    public String find(String node) {
      if (!parent.containsKey(node)) {
        throw new IllegalArgumentException("Node not found: " + node);
      }
      if (!node.equals(parent.get(node))) {
        parent.put(node, find(parent.get(node))); // Path compression
      }
      return parent.get(node);
    }

    // Union by rank
    public boolean union(String node1, String node2) {
      add(node1);
      add(node2);

      String root1 = find(node1);
      String root2 = find(node2);

      if (root1.equals(root2)) {
        return false; // Already in the same set
      }

      // Union by rank
      int rank1 = rank.get(root1);
      int rank2 = rank.get(root2);
      if (rank1 < rank2) {
        parent.put(root1, root2);
      } else if (rank1 > rank2) {
        parent.put(root2, root1);
      } else {
        parent.put(root2, root1);
        rank.put(root1, rank1 + 1);
      }
      return true;
    }
  }
}
