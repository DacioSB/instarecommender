package com.example.instarecommender.recommenders;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.instarecommender.models.RecommendationResponse;
import com.example.instarecommender.recommenders.factory.RecommenderStrategy;
import com.example.instarecommender.services.GraphService;

public class GNNRecommender implements RecommenderStrategy{

    private final GraphService graphService;
    private static final int EMBEDDING_SIZE = 64;
    private static final int LAYERS = 2;

    public GNNRecommender(GraphService graphService) {
        this.graphService = graphService;
    }

    @Override
    public RecommendationResponse recommend(String user, int limit) {
        // 1. Snapshot the graph
        Map<String, Set<String>> adjacency = graphService.getAllConnections();

        // Create a mapping from User String ID -> Int Index (0 to N)
        List<String> users = adjacency.keySet().stream().toList();
       return null;
    }
    
}
