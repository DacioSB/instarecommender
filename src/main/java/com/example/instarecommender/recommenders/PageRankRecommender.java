package com.example.instarecommender.recommenders;

import com.example.instarecommender.models.Recommendation;
import com.example.instarecommender.models.RecommendationResponse;
import com.example.instarecommender.recommenders.factory.RecommenderStrategy;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PageRankRecommender implements RecommenderStrategy {

    private final Graph<String, DefaultWeightedEdge> graph;
    private final Set<String> allUsers;

    public PageRankRecommender(Graph<String, DefaultWeightedEdge> graph) {
        this.graph = graph;
        this.allUsers = graph.vertexSet();
    }

    @Override
    public RecommendationResponse recommend(String user, int limit) {
        PageRank<String, DefaultWeightedEdge> pageRank = new PageRank<>(graph);
        Set<String> userFollowing = graph.outgoingEdgesOf(user).stream()
                                        .map(graph::getEdgeTarget)
                                        .collect(Collectors.toSet());

        List<Recommendation> recommendations = allUsers.stream()
            .filter(u -> !u.equals(user) && !userFollowing.contains(u))
            .map(u -> new Recommendation(u, pageRank.getVertexScore(u), "pagerank_in_memory"))
            .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
            .limit(limit)
            .collect(Collectors.toList());

        return new RecommendationResponse(recommendations, "In-memory PageRank calculation using JGraphT");
    }
}
