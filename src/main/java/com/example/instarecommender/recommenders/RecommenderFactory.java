package com.example.instarecommender.recommenders;

import java.util.EnumMap;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.instarecommender.models.AlgorithmTypes;
import com.example.instarecommender.recommenders.neo4j.Neo4jAdamicAdarRecommender;
import com.example.instarecommender.recommenders.neo4j.Neo4jCommonNeighborsRecommender;
import com.example.instarecommender.recommenders.neo4j.Neo4jJaccardRecommender;
import com.example.instarecommender.recommenders.neo4j.Neo4jPageRankRecommender;
import com.example.instarecommender.repositories.GraphRepository;
import com.example.instarecommender.repositories.InMemoryGraphRepository;
import com.example.instarecommender.services.GraphService;

@Component
public class RecommenderFactory {
    private final Map<AlgorithmTypes, RecommenderStrategy> inMemoryStrategies;
    private final Map<AlgorithmTypes, RecommenderStrategy> neo4jStrategies;
    private final String storageType;

    public RecommenderFactory(GraphService graphService, Driver neo4jDriver, @Value("${app.graph.storage-type}") String storageType) {
        this.storageType = storageType;

        this.inMemoryStrategies = new EnumMap<>(AlgorithmTypes.class);
        GraphRepository graphRepository = graphService.getGraphRepository();
        if (graphRepository instanceof InMemoryGraphRepository repo) {
            inMemoryStrategies.put(AlgorithmTypes.JACCARD, new JaccardRecommender(graphService));
            inMemoryStrategies.put(AlgorithmTypes.COMMON_NEIGHBORS, new CommonNeighborsRecommender(graphService));
            inMemoryStrategies.put(AlgorithmTypes.PAGERANK, new PageRankRecommender(repo.getGraph()));
            inMemoryStrategies.put(AlgorithmTypes.ADAMIC_ADAR, new AdamicAdarRecommender(graphService));
        }

        this.neo4jStrategies = new EnumMap<>(AlgorithmTypes.class);
        neo4jStrategies.put(AlgorithmTypes.JACCARD, new Neo4jJaccardRecommender(neo4jDriver));
        neo4jStrategies.put(AlgorithmTypes.COMMON_NEIGHBORS, new Neo4jCommonNeighborsRecommender(neo4jDriver));
        neo4jStrategies.put(AlgorithmTypes.PAGERANK, new Neo4jPageRankRecommender(neo4jDriver));
        neo4jStrategies.put(AlgorithmTypes.ADAMIC_ADAR, new Neo4jAdamicAdarRecommender(neo4jDriver));
    }

    public RecommenderStrategy getRecommender(AlgorithmTypes algorithm) {
        Map<AlgorithmTypes, RecommenderStrategy> strategies = "neo4j".equalsIgnoreCase(storageType) ? neo4jStrategies : inMemoryStrategies;
        RecommenderStrategy strategy = strategies.get(algorithm);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported algorithm for storage type " + storageType + ": " + algorithm);
        }
        return strategy;
    }
}
