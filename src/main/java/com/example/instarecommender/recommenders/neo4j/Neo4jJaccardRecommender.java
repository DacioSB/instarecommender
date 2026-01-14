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
        String query =
            // Encontrar candidatos
            "MATCH (u:User {id: $userId})-[r1:FOLLOWS]->()-[r2:FOLLOWS]->(candidate) " +
            "WHERE u.id <> candidate.id " +
            
            // Calcular interseção PONDERADA pelos pesos
            "MATCH (u)-[r3:FOLLOWS]->(common)-[r4:FOLLOWS]->(candidate) " +
            "WITH candidate, " +
            "     sum(r3.weight * r4.weight) AS weightedIntersection, " +  // ← PESO!
            "     collect(DISTINCT common) AS commonUsers " +
            
            // Somar todos os pesos do usuário
            "MATCH (u:User {id: $userId})-[r5:FOLLOWS]->(userFollows) " +
            "WITH candidate, weightedIntersection, commonUsers, " +
            "     sum(r5.weight) AS userTotalWeight " +
            
            // Somar todos os pesos dos seguidores do candidato
            "MATCH (candidate)<-[r6:FOLLOWS]-(follower) " +
            "WITH candidate, weightedIntersection, userTotalWeight, " +
            "     sum(r6.weight) AS candidateTotalWeight " +
            
            // Calcular Jaccard ponderado
            "WITH candidate, " +
            "     weightedIntersection / (userTotalWeight + candidateTotalWeight - weightedIntersection) AS score " +
            
            "WHERE score > 0 " +
            "RETURN candidate.id AS user, score " +
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
