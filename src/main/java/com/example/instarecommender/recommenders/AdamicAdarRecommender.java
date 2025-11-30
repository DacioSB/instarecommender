package com.example.instarecommender.recommenders;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.example.instarecommender.models.Recommendation;
import com.example.instarecommender.models.RecommendationResponse;
import com.example.instarecommender.recommenders.factory.RecommenderStrategy;
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
            Set<String> friendsOfFriend = graphService.getFollowing(friend);
            
            int degree = friendsOfFriend.size();
            if (degree <= 1) continue;
            
            double weight = 1.0 / Math.log(degree);

            for (String candidate : friendsOfFriend) {
                if (!candidate.equals(user) && !userFollowing.contains(candidate)) {
                    scores.merge(candidate, weight, Double::sum);
                }
            }
        }
        
        return new RecommendationResponse(scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(limit)
            .map(e -> new Recommendation(e.getKey(), e.getValue(), "adamic-adar-memory"))
            .toList(), "In-memory Adamic-Adar");
    }
}