package com.example.instarecommender.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.instarecommender.repositories.GraphRepository;

import jakarta.annotation.PostConstruct;

@Service
public class GraphService {
    private final GraphRepository graphRepository;

    public GraphService(GraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    public List<Map<String, Object>> getGraph() {
        return graphRepository.getGraphData();
    }

    public void addOrUpdateEdge(String from, String to, double weight) {
        graphRepository.addOrUpdateEdge(from, to, weight);
    }

    public Set<String> getFollowing(String user) {
        return graphRepository.getFollowing(user);
    }

    public Set<String> getFollowers(String user) {
        return graphRepository.getFollowers(user);
    }

    public void addUser(String user) {
        graphRepository.addUser(user);
    }

    public GraphRepository getGraphRepository() {
        return graphRepository;
    }

    @PostConstruct
    public void initializeGraph() {
        if (graphRepository.isGraphEmpty()) {
            System.out.println("[INFO] Graph is empty. Initializing from file...");
            loadGraphFromFile();
        } else {
            System.out.println("[INFO] Graph data found in storage. Skipping file load.");
        }

        if (graphRepository.supportsGds()) {
            System.out.println("[INFO] Creating GDS projection...");
            graphRepository.createGdsProjection();
        } else {
            System.out.println("[INFO] Skipping GDS (in-memory mode).");
        }
    }


    private void loadGraphFromFile() {
        try(BufferedReader reader = new BufferedReader(new FileReader("graph.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
               String[] parts = line.split(",");
               if (parts.length >= 3) {
                   String from = parts[0].trim();
                   String to = parts[1].trim();
                   double weight = Double.parseDouble(parts[2].trim());
                   addOrUpdateEdge(from, to, weight);
               }
            }
            System.out.println("[INFO] Graph data loaded from graph.csv.");
        } catch (Exception e) {
            System.out.println("[WARN] No graph.csv found or error reading file: " + e.getMessage());
        }
    }

    public double getConnectionWeight(String from, String to) {
        return graphRepository.getConnectionWeight(from, to);
    }

    public void updateConnectionWeight(String from, String to, double newWeight) {
        graphRepository.updateConnectionWeight(from, to, newWeight);
    }

    //getAllConnections
    public Map<String, Set<String>> getAllConnections() {
        return graphRepository.getAllConnections();
    }

}
