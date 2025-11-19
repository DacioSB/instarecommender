package com.example.instarecommender.repositories;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
}
