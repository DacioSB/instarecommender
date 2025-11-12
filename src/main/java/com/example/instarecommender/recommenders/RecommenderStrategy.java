package com.example.instarecommender.recommenders;

import com.example.instarecommender.models.RecommendationResponse;

public interface RecommenderStrategy {
    RecommendationResponse recommend(String user, int limit);
    
}
