package com.example.instarecommender;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public void addUser(String user) {
        graph.addVertex(user);
    }

    public List<String> recommend(String user) {
        if (!graph.containsVertex(user)) return List.of();

        var userFollowings = graph.outgoingEdgesOf(user).stream()
            .map(graph::getEdgeTarget)
            .collect(Collectors.toSet());

        Map<String, Double> scores = new HashMap<>();

        for (String friend : userFollowings) {
            for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(friend)) {
                String suggestion = graph.getEdgeTarget(edge);
                if (!suggestion.equals(user) && !userFollowings.contains(suggestion)) {
                    scores.put(suggestion,
                    scores.getOrDefault(suggestion, 0.0) + graph.getEdgeWeight(edge));
                }
            }
        }


        return scores.entrySet().stream()
        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
        .map(Map.Entry::getKey)
        .toList();
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
