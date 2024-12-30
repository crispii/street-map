package hw6;

import exceptions.InsertionException;
import exceptions.PositionException;
import exceptions.RemovalException;

import java.lang.reflect.Array;
import java.util.*;

/**
 * An implementation of Graph ADT using incidence lists
 * for sparse graphs where most nodes aren't connected.
 *
 * @param <V> Vertex element type.
 * @param <E> Edge element type.
 */
public class SparseGraph<V, E> implements Graph<V, E> {
  Set<EdgeNode<E>> edges;
  Set<VertexNode<V>> vertices;
  Map<VertexNode<V>, List<EdgeNode<E>>> outgoingEdges;
  Map<VertexNode<V>, List<EdgeNode<E>>> incomingEdges;

  /**
   * Public constructor for SparseGraph.
   */
  public SparseGraph() {
    edges = new HashSet<>();
    vertices = new HashSet<>();
    outgoingEdges = new HashMap<>();
    incomingEdges = new HashMap<>();
  }

  private ArrayList<Vertex<V>> dfs(Vertex<V> start) {
    VertexNode<V> startNode = convert(start);
    ArrayList<VertexNode<V>> path = new ArrayList<>();
    Set<VertexNode<V>> visited = new HashSet<>();
    Stack<VertexNode<V>> stack = new Stack<>();
    stack.push(startNode);
    dfsRecursive(stack, visited, path);
    return new ArrayList<>(path); // filler
  }

  private void dfsRecursive(Stack<VertexNode<V>> stack, Set<VertexNode<V>> visited, ArrayList<VertexNode<V>> path) {
    if (stack.empty()) {
      return;
    }
    VertexNode<V> currentNode = stack.pop();
    if (!visited.contains(currentNode)) {
      visited.add(currentNode);
      path.add(currentNode);
      for (EdgeNode<E> neighbor : outgoingEdges.get(currentNode)) {
        VertexNode<V> nextVertex = convert(to(neighbor));
        if (!visited.contains(nextVertex)) {
          stack.push(nextVertex);
        }
      }
    }

    dfsRecursive(stack, visited, path);
  }

  private ArrayList<VertexNode<V>> bfs(VertexNode<V> start) {
    ArrayList<VertexNode<V>> path = new ArrayList<>();
    Queue<VertexNode<V>> queue = new LinkedList<>();
    Set<VertexNode<V>> visited = new HashSet<>();

    queue.add(start);
    visited.add(start);

    while (!queue.isEmpty()) {
      VertexNode<V> currentNode = queue.poll();
      path.add(currentNode);
      for (EdgeNode<E> neighbor : outgoingEdges.get(currentNode)) {
        VertexNode<V> nextVertex = convert(to(neighbor));
        if (!visited.contains(nextVertex)) {
          visited.add(nextVertex);
          queue.add(nextVertex);
        }
      }
    }
    return path;
  }


  // Converts the vertex back to a VertexNode to use internally
  private VertexNode<V> convert(Vertex<V> v) throws PositionException {
    try {
      VertexNode<V> gv = (VertexNode<V>) v;
      if (gv.owner != this) {
        throw new PositionException();
      }
      return gv;
    } catch (NullPointerException | ClassCastException ex) {
      throw new PositionException();
    }
  }

  // Converts and edge back to a EdgeNode to use internally
  private EdgeNode<E> convert(Edge<E> e) throws PositionException {
    try {
      EdgeNode<E> ge = (EdgeNode<E>) e;
      if (ge.owner != this) {
        throw new PositionException();
      }
      return ge;
    } catch (NullPointerException | ClassCastException ex) {
      throw new PositionException();
    }
  }

  @Override
  public Vertex<V> insert(V v) throws InsertionException {
    if (v == null) {
      throw new InsertionException();
    }
    for (VertexNode<V> vv : vertices) {
      if (vv.data.equals(v)) {
        throw new InsertionException();
      }
    }
    VertexNode<V> vertex = new VertexNode<>(v, this);
    vertices.add(vertex);
    incomingEdges.put(vertex, new ArrayList<>());
    outgoingEdges.put(vertex, new ArrayList<>());
    return vertex;
  }

  @Override
  public Edge<E> insert(Vertex<V> from, Vertex<V> to, E e)
      throws PositionException, InsertionException {
    VertexNode<V> fromNode = convert(from);
    VertexNode<V> toNode = convert(to);
    if (from == null || to == null || !vertices.contains(fromNode) || !vertices.contains(toNode)) {
      throw new PositionException();
    }
    EdgeNode<E> edge = new EdgeNode<>(fromNode, toNode, e, this);
    for (EdgeNode<E> edgeNode : outgoingEdges.get(fromNode)) {
      if (edgeNode.to.equals(toNode)) {
        throw new InsertionException();
      }
    }
    if (fromNode.equals(toNode)) { // loop
      throw new InsertionException();
    }
    edges.add(edge);
    outgoingEdges.get(fromNode).add(edge);
    incomingEdges.get(toNode).add(edge);
    return edge;
  }

  @Override
  public V remove(Vertex<V> v) throws PositionException, RemovalException {
    if (v == null) {
      throw new PositionException();
    }
    VertexNode<V> vertexNode = convert(v);
    if (!vertices.contains(vertexNode)) {
      throw new PositionException();
    }
    // removal exception if vertex still has incident edges
    if (!outgoingEdges.get(vertexNode).isEmpty() || !incomingEdges.get(vertexNode).isEmpty()) {
      throw new RemovalException();
    }

    vertices.remove(vertexNode);
    outgoingEdges.remove(vertexNode);
    incomingEdges.remove(vertexNode);
    return vertexNode.get();
  }

  @Override
  public E remove(Edge<E> e) throws PositionException {
    if (e == null) {
      throw new PositionException();
    }
    EdgeNode<E> edgeNode = convert(e);
    if (!edges.contains(edgeNode)) {
      throw new PositionException();
    }
    VertexNode<V> fromNode = edgeNode.from;
    VertexNode<V> toNode = edgeNode.to;
    if (!vertices.contains(fromNode) || !vertices.contains(toNode)) {
      throw new PositionException();
    }
    outgoingEdges.get(fromNode).remove(edgeNode);
    incomingEdges.get(fromNode).remove(edgeNode);
    edges.remove(edgeNode);
    return edgeNode.get();
  }

  @Override
  public Iterable<Vertex<V>> vertices() {
    return Collections.unmodifiableList(new ArrayList<>(vertices));
  }

  @Override
  public Iterable<Edge<E>> edges() {
    return Collections.unmodifiableList(new ArrayList<>(edges));
  }

  @Override
  public Iterable<Edge<E>> outgoing(Vertex<V> v) throws PositionException {
    if (v == null) {
      throw new PositionException();
    }
    VertexNode<V> vertexNode = convert(v);
    if (!outgoingEdges.containsKey(vertexNode)) {
      throw new PositionException();
    }
    return Collections.unmodifiableList(outgoingEdges.get(vertexNode));
  }

  @Override
  public Iterable<Edge<E>> incoming(Vertex<V> v) throws PositionException {
    if (v == null) {
      throw new PositionException();
    }
    VertexNode<V> vertexNode = convert(v);
    if (!incomingEdges.containsKey(vertexNode)) {
      throw new PositionException();
    }
    return Collections.unmodifiableList(incomingEdges.get(vertexNode));
  }

  @Override
  public Vertex<V> from(Edge<E> e) throws PositionException {
    if (e == null) {
      throw new PositionException();
    }
    EdgeNode<E> edge = convert(e);
    if (!edges.contains(edge)) {
      throw new PositionException();
    }
    return edge.from;
  }

  @Override
  public Vertex<V> to(Edge<E> e) throws PositionException {
    if (e == null) {
      throw new PositionException();
    }
    EdgeNode<E> edge = convert(e);
    if (!edges.contains(edge)) {
      throw new PositionException();
    }
    return edge.to;
  }

  @Override
  public void label(Vertex<V> v, Object l) throws PositionException {
    if (v == null) {
      throw new PositionException();
    }
    VertexNode<V> vertexNode = convert(v);
    if (vertexNode.owner != this || !vertices.contains(vertexNode)) {
      throw new PositionException();
    }
    vertexNode.label = l;
  }

  @Override
  public void label(Edge<E> e, Object l) throws PositionException {
    if (e == null) {
      throw new PositionException();
    }
    EdgeNode<E> edgeNode = convert(e);
    if (edgeNode.owner != this || !edges.contains(edgeNode)) {
      throw new PositionException();
    }
    edgeNode.label = l;
  }

  @Override
  public Object label(Vertex<V> v) throws PositionException {
    if (v == null) {
      throw new PositionException();
    }
    VertexNode<V> vertexNode = convert(v);
    if (vertexNode.owner != this || !vertices.contains(vertexNode)) {
      throw new PositionException();
    }
    return vertexNode.label;
  }

  @Override
  public Object label(Edge<E> e) throws PositionException {
    if (e == null) {
      throw new PositionException();
    }
    EdgeNode<E> edgeNode = convert(e);
    if (edgeNode.owner != this || !edges.contains(edgeNode)) {
      throw new PositionException();
    }
    return edgeNode.label;
  }

  @Override
  public void clearLabels() {
    for (Edge<E> edge : edges) {
      EdgeNode<E> edgeNode = convert(edge);
      edgeNode.label = null;
    }
    for (Vertex<V> vertex : vertices) {
      VertexNode<V> vertexNode = convert(vertex);
      vertexNode.label = null;
    }
  }

  @Override
  public String toString() {
    GraphPrinter<V, E> gp = new GraphPrinter<>(this);
    return gp.toString();
  }

  // Class for a vertex of type V
  private final class VertexNode<V> implements Vertex<V> {
    V data;
    Graph<V, E> owner;
    Object label;
    // TODO You may need to add fields/methods here!

    VertexNode(V v) {
      this.data = v;
      this.label = null;
    }

    VertexNode(V v, Graph<V, E> g) {
      this.data = v;
      this.label = null;
      this.owner = g;
    }

    @Override
    public V get() {
      return this.data;
    }
  }

  //Class for an edge of type E
  private final class EdgeNode<E> implements Edge<E> {
    E data;
    Graph<V, E> owner;
    VertexNode<V> from;
    VertexNode<V> to;
    Object label;
    // TODO You may need to add fields/methods here!

    // Constructor for a new edge
    EdgeNode(VertexNode<V> f, VertexNode<V> t, E e) {
      this.from = f;
      this.to = t;
      this.data = e;
      this.label = null;
    }

    EdgeNode(VertexNode<V> f, VertexNode<V> t, E e, Graph<V, E> g) {
      this.from = f;
      this.to = t;
      this.data = e;
      this.label = null;
      this.owner = g;
    }

    @Override
    public E get() {
      return this.data;
    }
  }
}
