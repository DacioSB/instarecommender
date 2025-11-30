package com.example.instarecommender.recommenders.neo4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import com.example.instarecommender.models.Recommendation;
import com.example.instarecommender.models.RecommendationResponse;
import com.example.instarecommender.recommenders.factory.RecommenderStrategy;

public class Neo4jAdamicAdarRecommender implements RecommenderStrategy {
    private final Driver driver;

    public Neo4jAdamicAdarRecommender(Driver driver) {
        this.driver = driver;
    }

    @Override
    public RecommendationResponse recommend(String userId, int limit) {
        String query = 
            "MATCH (u:User {id: $userId})-[:FOLLOWS]->(common)-[:FOLLOWS]->(candidate) " +
            "WHERE NOT (u)-[:FOLLOWS]->(candidate) AND u <> candidate " +
            "WITH candidate, common, size((common)-[:FOLLOWS]->()) AS degree " +
            "WHERE degree > 1 " +
            "WITH candidate, sum(1.0 / log(degree)) AS score " +
            "RETURN candidate.id AS user, score " +
            "ORDER BY score DESC, user ASC " +
            "LIMIT $limit";
        
        try (Session session = driver.session()) {
            Result result = session.run(query, Map.of("userId", userId, "limit", limit));
            List<Recommendation> recommendations = result.stream()
                .map(r -> new Recommendation(
                    r.get("user").asString(),
                    r.get("score").asDouble(),
                    "adamic_adar_neo4j"
                ))
                .collect(Collectors.toList());
            return new RecommendationResponse(recommendations, query);
        }
    }
}