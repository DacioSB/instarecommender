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

public class Neo4jPageRankRecommender implements RecommenderStrategy {

    private final Driver driver;

    public Neo4jPageRankRecommender(Driver driver) {
        this.driver = driver;
    }

    private void ensureGraphProjection() {
        try (Session session = driver.session()) {
            Result result = session.run("RETURN gds.graph.exists('social-graph') AS exists");
            if (result.hasNext() && !result.next().get("exists").asBoolean()) {
                session.run("CALL gds.graph.project('social-graph', 'User', 'FOLLOWS')");
            }
        }
    }

    @Override
    public RecommendationResponse recommend(String userId, int limit) {
        ensureGraphProjection();
        String query = 
            "MATCH (u:User {id: $userId}) " +
            "CALL gds.pageRank.stream('social-graph', {" +
            "   sourceNodes: [u], " + 
            "   relationshipWeightProperty: null" +
            "}) " +
            "YIELD nodeId, score " +
            "WITH gds.util.asNode(nodeId) AS candidate, score " +
            "WHERE candidate.id <> $userId " +
            // Only exclude if there is an explicit 'isFollowing=true' relationship
            "AND NOT EXISTS { " +
            "   MATCH (:User {id: $userId})-[r:FOLLOWS]->(candidate) " +
            "   WHERE r.isFollowing = true " +
            "} " +
            "RETURN candidate.id AS user, score " +
            "ORDER BY score DESC " +
            "LIMIT $limit";

        try (Session session = driver.session()) {
            Result result = session.run(query, Map.of("userId", userId, "limit", limit));
            
            List<Recommendation> recommendations = result.stream()
                .map(r -> new Recommendation(
                    r.get("user").asString(),
                    r.get("score").asDouble(),
                    "personalized_pagerank_neo4j"
                ))
                .collect(Collectors.toList());
            
            return new RecommendationResponse(recommendations, query);
        }
    }
}