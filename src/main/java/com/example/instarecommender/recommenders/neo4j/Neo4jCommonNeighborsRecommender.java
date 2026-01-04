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

public class Neo4jCommonNeighborsRecommender implements RecommenderStrategy {
    private final Driver driver;

    public Neo4jCommonNeighborsRecommender(Driver driver) {
        this.driver = driver;
    }

    @Override
    public RecommendationResponse recommend(String userId, int limit) {
        String query = 
            "MATCH (u:User {id: $userId})-[:FOLLOWS]->(common)-[:FOLLOWS]->(candidate) " +
            "WHERE u <> candidate " +
            // Only exclude if there is an explicit 'isFollowing=true' relationship
            "AND NOT EXISTS { MATCH (u)-[r:FOLLOWS]->(candidate) WHERE r.isFollowing = true } " +
            "RETURN candidate.id AS user, count(common) AS score " +
            "ORDER BY score DESC, user ASC " +
            "LIMIT $limit";
        
        try (Session session = driver.session()) {
            Result result = session.run(query, Map.of("userId", userId, "limit", limit));
            List<Recommendation> recommendations = result.stream()
                .map(r -> new Recommendation(
                    r.get("user").asString(),
                    r.get("score").asDouble(),
                    "common_neighbors_neo4j"
                ))
                .collect(Collectors.toList());
            return new RecommendationResponse(recommendations, query);
        }
    }
}