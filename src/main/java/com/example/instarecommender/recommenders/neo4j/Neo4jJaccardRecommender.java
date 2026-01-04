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
            // Find candidates through friend-of-friend
            "MATCH (u:User {id: $userId})-[:FOLLOWS]->()-[:FOLLOWS]->(candidate) " +
            "WHERE u.id <> candidate.id " +
            "AND NOT EXISTS { MATCH (u)-[r:FOLLOWS]->(candidate) WHERE r.isFollowing = true } " +
            // Intersection: people user follows who ALSO follow the candidate
            "MATCH (u)-[:FOLLOWS]->(common)-[:FOLLOWS]->(candidate) " +
            "WITH candidate, collect(DISTINCT common) AS commonUsers " +
            
            // Get all users that user follows (for union calculation)
            "MATCH (u:User {id: $userId})-[:FOLLOWS]->(userFollows) " +
            "WITH candidate, commonUsers, collect(DISTINCT userFollows) AS allUserFollows " +
            
            // Get all users that follow the candidate (for union calculation)
            "MATCH (candidate)<-[:FOLLOWS]-(follower) " +
            "WITH candidate, commonUsers, allUserFollows, collect(DISTINCT follower) AS candidateFollowers " +
            
            // Calculate Jaccard
            "WITH candidate, " +
            "     size(commonUsers) AS intersection, " +
            "     size(allUserFollows) AS userFollowsCount, " +
            "     size(candidateFollowers) AS candidateFollowersCount " +
            
            "WITH candidate, intersection, " +
            "     userFollowsCount + candidateFollowersCount - intersection AS unionSize " +
            
            "WITH candidate, " +
            "     CASE WHEN unionSize > 0 THEN toFloat(intersection) / unionSize ELSE 0.0 END AS score " +
            
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
