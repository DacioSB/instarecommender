package com.example.instarecommender.recommenders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import com.example.instarecommender.models.Recommendation;
import com.example.instarecommender.models.RecommendationResponse;
import com.example.instarecommender.recommenders.factory.RecommenderStrategy;
import com.example.instarecommender.services.GraphService;

public class GNNRecommender implements RecommenderStrategy{

    private final GraphService graphService;
    private static final int EMBEDDING_SIZE = 64;
    private static final int LAYERS = 2;

    public GNNRecommender(GraphService graphService) {
        this.graphService = graphService;
    }

    @Override
    public RecommendationResponse recommend(String targetUser, int limit) {
        // 1. Snapshot the graph
        Map<String, Set<String>> adjacency = graphService.getAllConnections();

        // Create a mapping from User String ID -> Int Index (0 to N)
        Set<String> uniqueUsers = new HashSet<>(adjacency.keySet());
        adjacency.values().forEach(uniqueUsers::addAll);
        List<String> nodeList = new ArrayList<>(uniqueUsers);
        Collections.sort(nodeList);

        int numNodes = nodeList.size();
        Map<String, Integer> userToIndex = new HashMap<>();
        for (int i = 0; i < numNodes; i++) {
            userToIndex.put(nodeList.get(i), i);
        }

        if (!userToIndex.containsKey(targetUser)) {
            return new RecommendationResponse(List.of(), "User not found in graph");
        }

        // 2. Create Adjacency Matrix (A) using ND4J

        INDArray adjMatrix = Nd4j.zeros(numNodes, numNodes);
        for (String from : adjacency.keySet()) {
            int fromIdx = userToIndex.get(from);
            for (String to : adjacency.get(from)) {
                if (userToIndex.containsKey(to)) {
                    int toIdx = userToIndex.get(to);
                    // Add self-loops (A + I) is common in GCN to retain own features
                    adjMatrix.putScalar(fromIdx, toIdx, 1);
                }
            }
        }

        //Add Self-Loops (Important so you don't "forget" your own identity)
        for (int i = 0; i < numNodes; i++) {
            adjMatrix.putScalar(i, i, 1);
        }

        // 3. Normalize Adjacency Matrix (Row Normalize: D^-1 * A)
        // This prevents features from exploding in magnitude
        INDArray degrees = adjMatrix.sum(1);
        for (int i = 0; i < numNodes; i++) {
            double degree = degrees.getDouble(i);
            if (degree > 0) {
                adjMatrix.getRow(i).divi(Nd4j.scalar(degree));
            }
        }

        // 4. Initialize Feature Matrix (X)
        // In a real app, this could be Age, Location, etc.
        // Here, we use Random Gaussian noise. This works surprisingly well for structure learning.
        // Or we could use an Identity matrix (One-Hot) for pure ID-based learning.
        INDArray features = Nd4j.randn(numNodes, EMBEDDING_SIZE);

        // 5. Message Passing (The "Neural Network" part)
        INDArray embeddings = features;
        for (int i = 0; i < LAYERS; i++) {
            embeddings = adjMatrix.mmul(embeddings);
        }

        // 6. Generate Recommendations based on Cosine Similarity
        // A. Normalize all embeddings to Unit Vectors (Length = 1)
        // This makes Cosine Similarity equivalent to a simple Dot Product
        // norm2(1) calculates the L2 norm for each row (axis 1)
        INDArray norms = embeddings.norm2(1); 
        // Divide in-place (add small epsilon 1e-8 to avoid division by zero)
        embeddings.diviColumnVector(norms.add(1e-8)); 

        // B. Get the normalized target vector
        // Since we normalized the whole matrix 'embeddings', this row is already normalized.
        int targetIdx = userToIndex.get(targetUser);
        INDArray targetVector = embeddings.getRow(targetIdx);

        // C. Calculate Scores via Matrix Multiplication
        // [NumNodes, Features] x [Features, 1] = [NumNodes, 1]
        // The result is a column vector where row 'i' is the similarity score for user 'i'
        INDArray similarities = embeddings.mmul(targetVector.transpose());

        List<Recommendation> recs = new ArrayList<>();
        Set<String> alreadyFollowing = graphService.getFollowing(targetUser);

        // D. Extract results
        for (int i = 0; i < numNodes; i++) {
            String candidate = nodeList.get(i);
            
            if (!candidate.equals(targetUser) && !alreadyFollowing.contains(candidate)) {
                // Get the scalar score from the results vector
                double score = similarities.getDouble(i);
                recs.add(new Recommendation(candidate, score, "gnn_lightgcn_embedding"));
            }
        }

        List<Recommendation> topRecs = recs.stream()
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(limit)
            .collect(Collectors.toList());
        return new RecommendationResponse(topRecs, "GNN (Matrix Factorization/Propagation) via ND4J");
    }
    
}
