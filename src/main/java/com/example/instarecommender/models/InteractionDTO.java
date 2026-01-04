package com.example.instarecommender.models;

public class InteractionDTO {
    private String from;
    private String to;
    private InteractionType type;

    public InteractionDTO(String from, String to, InteractionType type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public InteractionType getType() {
        return type;
    }
}
