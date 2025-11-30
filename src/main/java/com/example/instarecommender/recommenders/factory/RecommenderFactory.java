package com.example.instarecommender.recommenders.factory;


import com.example.instarecommender.models.AlgorithmTypes;

public interface RecommenderFactory {
    RecommenderStrategy getRecommender(AlgorithmTypes algorithm);
}

