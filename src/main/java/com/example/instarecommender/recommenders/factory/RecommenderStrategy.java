package com.example.instarecommender.recommenders.factory;

import com.example.instarecommender.models.RecommendationResponse;

public interface RecommenderStrategy {
    RecommendationResponse recommend(String user, int limit);
    
}
