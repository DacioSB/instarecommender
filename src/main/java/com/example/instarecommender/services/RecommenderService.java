package com.example.instarecommender.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.instarecommender.models.AlgorithmTypes;
import com.example.instarecommender.models.Recommendation;
import com.example.instarecommender.recommenders.RecommenderFactory;

@Service
public class RecommenderService {
    private final RecommenderFactory factory;

    public RecommenderService(RecommenderFactory factory) {
        this.factory = factory;
    }

    public List<Recommendation> recommend (String user, AlgorithmTypes algorithm, int limit) {
        
        var recommender = factory.create(algorithm.getValue());
        return recommender.recommend(user, limit);
    }
}
