package com.example.instarecommender.models;

import java.util.List;

public class RecommendationResponse {
    private List<Recommendation> recommendations;
    private String query;

    public RecommendationResponse(List<Recommendation> recommendations, String query) {
        this.recommendations = recommendations;
        this.query = query;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
