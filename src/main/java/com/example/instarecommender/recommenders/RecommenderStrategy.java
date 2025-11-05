package com.example.instarecommender.recommenders;

import java.util.List;

import com.example.instarecommender.models.Recommendation;

public interface RecommenderStrategy {
    List<Recommendation> recommend(String user, int limit);
    
}
