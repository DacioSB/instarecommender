package com.example.instarecommender.recommenders.neo4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import com.example.instarecommender.models.Recommendation;
import com.example.instarecommender.models.RecommendationResponse;
import com.example.instarecommender.recommenders.RecommenderStrategy;

public class Neo4jJaccardRecommender implements RecommenderStrategy {
    private final Driver driver;

    public Neo4jJaccardRecommender(Driver driver) {
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
        
        String query = "MATCH (u:User {id: $userId}) " +
                       "CALL gds.nodeSimilarity.stream('social-graph') " +
                       "YIELD node1, node2, similarity " +
                       "WHERE node1 = u " +
                       "WITH u, gds.util.asNode(node2) AS user2, similarity " +
                       "WHERE NOT (u)-[:FOLLOWS]->(user2) " +
                       "RETURN user2.id AS user, similarity AS score " +
                       "ORDER BY score DESC, user ASC " +
                       "LIMIT $limit";

        try (Session session = driver.session()) {
            Result result = session.run(query, Map.of("userId", userId, "limit", limit));
            List<Recommendation> recommendations = result.stream()
                .map(r -> new Recommendation(
                    r.get("user").asString(),
                    r.get("score").asDouble(),
                    "jaccard_neo4j"
                ))
                .collect(Collectors.toList());
            return new RecommendationResponse(recommendations, query);
        }
    }
}
