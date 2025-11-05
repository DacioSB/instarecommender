package com.example.instarecommender.recommenders;

import org.springframework.stereotype.Component;

import com.example.instarecommender.services.GraphService;

@Component
public class RecommenderFactory {
    private final GraphService graphService;

    public RecommenderFactory(GraphService graphService) {
        this.graphService = graphService;
    }

    public RecommenderStrategy create(String algorithm) {
        return switch (algorithm.toLowerCase()) {
            case "common-neighbors" -> new CommonNeighborsRecommender(graphService);
            case "jaccard" -> new JaccardRecommender(graphService);
            default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        };
    }
}
