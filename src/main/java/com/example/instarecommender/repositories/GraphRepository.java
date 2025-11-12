package com.example.instarecommender.repositories;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GraphRepository {
    void addOrUpdateEdge(String from, String to, double weight);
    Set<String> getFollowing(String user);
    Set<String> getFollowers(String user);
    void addUser(String user);
    List<Map<String, Object>> getGraphData();
    void clear();
}
