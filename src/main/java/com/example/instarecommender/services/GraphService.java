package com.example.instarecommender.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class GraphService {
    private final Graph<String, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    public Graph<String, DefaultWeightedEdge> getGraph() {
        return graph;
    }

    public void addOrUpdateEdge(String from, String to, double weight) {
        graph.addVertex(from);
        graph.addVertex(to);
        graph.setEdgeWeight(graph.addEdge(from, to), weight);
    }

    public Set<String> getFollowing(String user) {
        return graph.outgoingEdgesOf(user).stream()
            .map(graph::getEdgeTarget)
            .collect(Collectors.toSet());
    }

    public Set<String> getFollowers(String user) {
        return graph.incomingEdgesOf(user).stream()
            .map(graph::getEdgeSource)
            .collect(Collectors.toSet());
    }

    public void addUser(String user) {
        if (!graph.containsVertex(user)) {
            graph.addVertex(user);
        }
    }

    public double getConnectionWeight(String from, String to) {
        DefaultWeightedEdge edge = graph.getEdge(from, to);
        return edge != null ? graph.getEdgeWeight(edge) : 0.0;
    }

    @PostConstruct
    public void loadGraphFromFile() {
        try(BufferedReader reader = new BufferedReader(new FileReader("graph.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
               String[] parts = line.split(",");
               String from = parts[0];
               String to = parts[1];
               double weight = Double.parseDouble(parts[2]);
               addOrUpdateEdge(from, to, weight); 
            }
        } catch (Exception e) {
            System.out.println("[INFO] No graph.csv found. Starting with empty graph.");
        }
    }
}
