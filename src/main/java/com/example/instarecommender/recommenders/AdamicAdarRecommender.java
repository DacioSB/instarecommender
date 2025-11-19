package com.example.instarecommender.recommenders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.example.instarecommender.models.Recommendation;
import com.example.instarecommender.models.RecommendationResponse;
import com.example.instarecommender.services.GraphService;

public class AdamicAdarRecommender implements RecommenderStrategy {
    private final GraphService graphService;

    public AdamicAdarRecommender(GraphService graphService) {
        this.graphService = graphService;
    }
    @Override
    public RecommendationResponse recommend(String user, int limit) {
        Set<String> userFollowing = graphService.getFollowing(user);
        Map<String, Double> scores = new HashMap<>();
        
        for (String friend : userFollowing) {
            Set<String> friendFollowing = graphService.getFollowing(friend);
            
            for (String candidate : friendFollowing) {
                if (!candidate.equals(user) && !userFollowing.contains(candidate)) {
                    Set<String> candidateFollowing = graphService.getFollowing(candidate);
                    
                    // Find common neighbors between user and candidate
                    Set<String> commonNeighbors = new HashSet<>(userFollowing);
                    commonNeighbors.retainAll(candidateFollowing);
                    
                    double aaScore = commonNeighbors.stream()
                        .mapToDouble(cn -> {
                            int cnFollowingSize = graphService.getFollowing(cn).size();
                            return cnFollowingSize > 1 ? 1.0 / Math.log(cnFollowingSize) : 0;
                        })
                        .sum();
                    
                    scores.merge(candidate, aaScore, Double::sum);
                }
            }
        }
        
        return new RecommendationResponse(scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(limit)
            .map(e -> new Recommendation(e.getKey(), e.getValue(), "adamic-adar"))
            .toList(), "In-memory Adamic-Adar calculation");
    }

}
