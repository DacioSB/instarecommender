package com.example.instarecommender.recommenders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.instarecommender.models.Recommendation;
import com.example.instarecommender.services.GraphService;

public class CommonNeighborsRecommender implements RecommenderStrategy {

    private final GraphService graphService;

    public CommonNeighborsRecommender(GraphService graphService) {
        this.graphService = graphService;
    }

    @Override
    public List<Recommendation> recommend(String user, int limit) {
        Set<String> userFollowing = graphService.getFollowing(user);
        Map<String, Double> scores = new HashMap<>();
        for (String friend : userFollowing) {
            Set<String> friendFollowing = graphService.getFollowing(friend);
            double friendWeight = graphService.getConnectionWeight(user, friend);

            for (String candidate : friendFollowing) {
                if (!candidate.equals(user) && !userFollowing.contains(candidate)) {
                    double candidateWeight = graphService.getConnectionWeight(friend, candidate);
                    double score = friendWeight * candidateWeight;
                    scores.merge(candidate, score, Double::sum);
                }
            }
        }

        return scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(limit)
            .map(e -> new Recommendation(e.getKey(), e.getValue(), "common-neighbors"))
            .toList();
    }
    
}
