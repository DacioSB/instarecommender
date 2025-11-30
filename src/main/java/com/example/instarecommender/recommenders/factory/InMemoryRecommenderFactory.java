package com.example.instarecommender.recommenders.factory;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.instarecommender.models.AlgorithmTypes;
import com.example.instarecommender.recommenders.AdamicAdarRecommender;
import com.example.instarecommender.recommenders.CommonNeighborsRecommender;
import com.example.instarecommender.recommenders.JaccardRecommender;
import com.example.instarecommender.recommenders.PageRankRecommender;
import com.example.instarecommender.repositories.GraphRepository;
import com.example.instarecommender.repositories.InMemoryGraphRepository;
import com.example.instarecommender.services.GraphService;

@Component
@Profile("in-memory")
public class InMemoryRecommenderFactory implements RecommenderFactory {

    private final Map<AlgorithmTypes, RecommenderStrategy> strategies;

    public InMemoryRecommenderFactory(GraphService graphService) {
        GraphRepository repo = graphService.getGraphRepository();
        InMemoryGraphRepository mem = (InMemoryGraphRepository) repo;

        strategies = new EnumMap<>(AlgorithmTypes.class);
        strategies.put(AlgorithmTypes.JACCARD, new JaccardRecommender(graphService));
        strategies.put(AlgorithmTypes.COMMON_NEIGHBORS, new CommonNeighborsRecommender(graphService));
        strategies.put(AlgorithmTypes.PAGERANK, new PageRankRecommender(mem.getGraph()));
        strategies.put(AlgorithmTypes.ADAMIC_ADAR, new AdamicAdarRecommender(graphService));
    }

    @Override
    public RecommenderStrategy getRecommender(AlgorithmTypes algorithm) {
        return strategies.get(algorithm);
    }
}

