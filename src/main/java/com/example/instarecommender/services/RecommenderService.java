package com.example.instarecommender.services;

import org.springframework.stereotype.Service;

import com.example.instarecommender.models.AlgorithmTypes;
import com.example.instarecommender.models.RecommendationResponse;
import com.example.instarecommender.recommenders.RecommenderFactory;
import com.example.instarecommender.recommenders.RecommenderStrategy;

@Service
public class RecommenderService {
    private final RecommenderFactory recommenderFactory;

    public RecommenderService(RecommenderFactory recommenderFactory) {
        this.recommenderFactory = recommenderFactory;
    }

    public RecommendationResponse recommend(String user, AlgorithmTypes algorithm, int limit) {
        RecommenderStrategy strategy = recommenderFactory.getRecommender(algorithm);
        return strategy.recommend(user, limit);
    }
}

