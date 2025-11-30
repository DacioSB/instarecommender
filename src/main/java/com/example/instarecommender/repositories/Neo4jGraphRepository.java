package com.example.instarecommender.repositories;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

public class Neo4jGraphRepository implements GraphRepository {

    private final Driver driver;

    public Neo4jGraphRepository(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void addOrUpdateEdge(String from, String to, double weight) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MERGE (a:User {id: $from}) MERGE (b:User {id: $to}) " +
                       "MERGE (a)-[r:FOLLOWS]->(b) SET r.weight = $weight",
                       Map.of("from", from, "to", to, "weight", weight));
                return null;
            });
        }
    }

    @Override
    public Set<String> getFollowing(String user) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run("MATCH (:User {id: $userId})-[:FOLLOWS]->(following) RETURN following.id AS id",
                                       Map.of("userId", user));
                return result.stream().map(r -> r.get("id").asString()).collect(Collectors.toSet());
            });
        }
    }

    @Override
    public Set<String> getFollowers(String user) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run("MATCH (follower)-[:FOLLOWS]->(:User {id: $userId}) RETURN follower.id AS id",
                                       Map.of("userId", user));
                return result.stream().map(r -> r.get("id").asString()).collect(Collectors.toSet());
            });
        }
    }

    @Override
    public void addUser(String user) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MERGE (:User {id: $userId})", Map.of("userId", user));
                return null;
            });
        }
    }

    @Override
    public List<Map<String, Object>> getGraphData() {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run("MATCH (from)-[r:FOLLOWS]->(to) RETURN from.id AS from, to.id AS to, r.weight AS weight");
                return result.list(r -> Map.of(
                    "from", r.get("from").asString(),
                    "to", r.get("to").asString(),
                    "weight", r.get("weight").asDouble()
                ));
            });
        }
    }

    @Override
    public void clear() {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
        }
    }

    @Override
    public boolean isGraphEmpty() {
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (n) RETURN count(n) AS count LIMIT 1");
            if (result.hasNext()) {
                return result.next().get("count").asLong() == 0;
            }
            return true;
        }
    }

    @Override
    public boolean supportsGds() {
        return true;
    }

    @Override
    public void createGdsProjection() {
        try (Session session = driver.session()) {
            System.out.println("[GDS] Dropping old projection (if exists)...");
            session.run("CALL gds.graph.drop('social-graph', false)");

            System.out.println("[GDS] Creating projection social-graph...");
            session.run("""
                CALL gds.graph.project(
                    'social-graph',
                    'User',
                    {
                        FOLLOWS: {
                            type: 'FOLLOWS',
                            orientation: 'NATURAL'
                        }
                    }
                )
            """);

            System.out.println("[GDS] Projection ready!");
        }
    }
}
