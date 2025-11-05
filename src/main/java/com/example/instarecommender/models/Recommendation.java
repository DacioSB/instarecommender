package com.example.instarecommender.models;

public class Recommendation {
    private final String targetUser;   // The user being recommended (the vertex)
    private final double score;        // The algorithm score (strength of recommendation)
    private final String algorithm;    // e.g. "jaccard", "adamic-adar"

    public Recommendation(String targetUser, double score, String algorithm) {
        this.targetUser = targetUser;
        this.score = score;
        this.algorithm = algorithm;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public double getScore() {
        return score;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public String toString() {
        return String.format("Recommendation[target=%s, score=%.2f, algorithm=%s]",
                targetUser, score, algorithm);
    }
}

