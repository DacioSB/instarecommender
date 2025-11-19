package com.example.instarecommender.services;

import com.example.instarecommender.repositories.GraphRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public void loadGraphFromFile() {
        graphRepository.clear();
        try(BufferedReader reader = new BufferedReader(new FileReader("graph.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
               String[] parts = line.split(",");
               if (parts.length >= 3) {
                   String from = parts[0];
                   String to = parts[1];
                   double weight = Double.parseDouble(parts[2]);
                   addOrUpdateEdge(from, to, weight);
               }
            }
            System.out.println("[INFO] Graph data loaded from graph.csv.");
        } catch (Exception e) {
            System.out.println("[INFO] No graph.csv found or error reading file. Starting with empty graph.");
        }
    }
}
