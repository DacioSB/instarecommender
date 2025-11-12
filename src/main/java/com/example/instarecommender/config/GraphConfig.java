package com.example.instarecommender.config;

import com.example.instarecommender.repositories.GraphRepository;
import com.example.instarecommender.repositories.InMemoryGraphRepository;
import com.example.instarecommender.repositories.Neo4jGraphRepository;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphConfig {

    @Bean
    public GraphRepository graphRepository(
        Driver neo4jDriver,
        @Value("${app.graph.storage-type}") String storageType
    ) {
        if ("neo4j".equalsIgnoreCase(storageType)) {
            System.out.println("[INFO] Using Neo4j for graph storage.");
            return new Neo4jGraphRepository(neo4jDriver);
        }
        System.out.println("[INFO] Using in-memory JGraphT for graph storage.");
        return new InMemoryGraphRepository();
    }
}
