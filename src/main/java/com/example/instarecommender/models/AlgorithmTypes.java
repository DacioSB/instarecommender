package com.example.instarecommender.models;

public enum AlgorithmTypes {
    COMMON_NEIGHBORS ("common-neighbors"),
    JACCARD ("jaccard");

    private final String value;
    AlgorithmTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
