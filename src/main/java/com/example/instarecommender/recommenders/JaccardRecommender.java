package com.example.instarecommender.recommenders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.instarecommender.models.Recommendation;
import com.example.instarecommender.models.RecommendationResponse;
import com.example.instarecommender.services.GraphService;

public class JaccardRecommender implements RecommenderStrategy {

    private final GraphService graphService;

    public JaccardRecommender(GraphService graphService) {
        this.graphService = graphService;
    }

    @Override
    public RecommendationResponse recommend(String user, int limit) {
        Set<String> userFollowing = graphService.getFollowing(user);
        Map<String, Double> scores = new HashMap<>();

        Set<String> candidates = userFollowing.stream()
            .flatMap(f -> graphService.getFollowing(f).stream())
            .filter(c -> !c.equals(user) && !userFollowing.contains(c))
            .collect(Collectors.toSet());

        for (String candidate : candidates) {
            Set<String> candidateFollowers = graphService.getFollowers(candidate);

            Set<String> intersection = new HashSet<>(userFollowing);
            intersection.retainAll(candidateFollowers);

            Set<String> union = new HashSet<>(userFollowing);
            union.addAll(candidateFollowers);

            double jaccard = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
            scores.put(candidate, jaccard);
        }

        List<Recommendation> recommendations = scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(limit)
            .map(e -> new Recommendation(e.getKey(), e.getValue(), "jaccard_in_memory"))
            .toList();
        
        return new RecommendationResponse(recommendations, "In-memory Jaccard calculation");
    }
}
