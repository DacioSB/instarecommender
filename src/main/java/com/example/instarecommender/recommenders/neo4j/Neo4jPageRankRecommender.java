package com.example.instarecommender.recommenders.neo4j;

import com.example.instarecommender.models.Recommendation;
import com.example.instarecommender.models.RecommendationResponse;
import com.example.instarecommender.recommenders.RecommenderStrategy;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Neo4jPageRankRecommender implements RecommenderStrategy {

    private final Driver driver;

    public Neo4jPageRankRecommender(Driver driver) {
        this.driver = driver;
    }

    private void ensureGraphProjection() {
        try (Session session = driver.session()) {
            Result result = session.run("RETURN gds.graph.exists('social-graph') AS exists");
            if (result.hasNext() && !result.next().get("exists").asBoolean()) {
                session.run("CALL gds.graph.project('social-graph', 'User', 'FOLLOWS', {relationshipProperties: 'weight'})");
            }
        }
    }

    @Override
    public RecommendationResponse recommend(String userId, int limit) {
        ensureGraphProjection();

        String query = "CALL gds.pageRank.stream('social-graph', {relationshipWeightProperty: 'weight'}) " +
                       "YIELD nodeId, score " +
                       "WITH gds.util.asNode(nodeId) AS node, score " +
                       "WHERE NOT ((:User {id: $userId})-[:FOLLOWS]->(node)) " +
                       "AND node.id <> $userId " +
                       "RETURN node.id AS user, score " +
                       "ORDER BY score DESC " +
                       "LIMIT $limit";

        try (Session session = driver.session()) {
            Result result = session.run(query, Map.of("userId", userId, "limit", limit));
            
            List<Recommendation> recommendations = result.stream()
                .map(record -> new Recommendation(
                    record.get("user").asString(),
                    record.get("score").asDouble(),
                    "pagerank_neo4j"
                ))
                .collect(Collectors.toList());
            
            return new RecommendationResponse(recommendations, query);
        }
    }
}
