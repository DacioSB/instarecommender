package com.example.instarecommender.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class InMemoryGraphRepository implements GraphRepository {
    private final Graph<String, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    public Graph<String, DefaultWeightedEdge> getGraph() {
        return graph;
    }

    @Override
    public void addOrUpdateEdge(String from, String to, double weight) {
        graph.addVertex(from);
        graph.addVertex(to);
        DefaultWeightedEdge e = graph.addEdge(from, to);
        if (e == null) { 
            e = graph.getEdge(from, to);
        }
        graph.setEdgeWeight(e, weight);
    }

    @Override
    public Set<String> getFollowing(String user) {
        if (!graph.containsVertex(user)) return Set.of();
        return graph.outgoingEdgesOf(user).stream()
            .map(graph::getEdgeTarget)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getFollowers(String user) {
        if (!graph.containsVertex(user)) return Set.of();
        return graph.incomingEdgesOf(user).stream()
            .map(graph::getEdgeSource)
            .collect(Collectors.toSet());
    }

    @Override
    public void addUser(String user) {
        graph.addVertex(user);
    }

    @Override
    public List<Map<String, Object>> getGraphData() {
        return graph.edgeSet().stream().map(edge -> {
            Map<String, Object> map = new HashMap<>();
            map.put("from", graph.getEdgeSource(edge));
            map.put("to", graph.getEdgeTarget(edge));
            map.put("weight", graph.getEdgeWeight(edge));
            return map;
        }).collect(Collectors.toList());
    }


    @Override
    public void clear() {
        Set<String> vertices = Set.copyOf(graph.vertexSet());
        vertices.forEach(graph::removeVertex);
    }

    @Override
    public boolean isGraphEmpty() {
        return true;
    }

    
}
