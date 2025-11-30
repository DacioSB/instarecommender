package com.example.instarecommender.recommenders.factory;

import java.util.EnumMap;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.instarecommender.models.AlgorithmTypes;
import com.example.instarecommender.recommenders.neo4j.Neo4jAdamicAdarRecommender;
import com.example.instarecommender.recommenders.neo4j.Neo4jCommonNeighborsRecommender;
import com.example.instarecommender.recommenders.neo4j.Neo4jJaccardRecommender;
import com.example.instarecommender.recommenders.neo4j.Neo4jPageRankRecommender;

@Component
@Profile("neo4j")
public class Neo4jRecommenderFactory implements RecommenderFactory {

    private final Map<AlgorithmTypes, RecommenderStrategy> strategies;

    public Neo4jRecommenderFactory(Driver driver) {
        strategies = new EnumMap<>(AlgorithmTypes.class);
        strategies.put(AlgorithmTypes.JACCARD, new Neo4jJaccardRecommender(driver));
        strategies.put(AlgorithmTypes.COMMON_NEIGHBORS, new Neo4jCommonNeighborsRecommender(driver));
        strategies.put(AlgorithmTypes.PAGERANK, new Neo4jPageRankRecommender(driver));
        strategies.put(AlgorithmTypes.ADAMIC_ADAR, new Neo4jAdamicAdarRecommender(driver));
    }

    @Override
    public RecommenderStrategy getRecommender(AlgorithmTypes algorithm) {
        return strategies.get(algorithm);
    }
}

