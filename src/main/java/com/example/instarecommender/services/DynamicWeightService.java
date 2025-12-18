package com.example.instarecommender.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.instarecommender.models.InteractionType;

@Service
public class DynamicWeightService {
    private final GraphService graphService;
    public DynamicWeightService(GraphService graphService) {
        this.graphService = graphService;
    }

    public void updateWeightBasedOnInteraction(String from, String to, 
                                                InteractionType type) {
        double currentWeight = graphService.getConnectionWeight(from, to);
        double increment = switch (type) {
            case COMMENT -> 0.5;
            case LIKE -> 0.1;
            case SHARE -> 1.0;
            case DIRECT_MESSAGE -> 2.0;
            case VIDEO_CALL -> 5.0;
        };
        
        // Apply decay factor to prevent weights from growing indefinitely
        double decayedWeight = currentWeight * 0.95;
        double newWeight = Math.min(decayedWeight + increment, 10.0);
        
        graphService.updateConnectionWeight(from, to, newWeight);
    }

    @Scheduled(cron = "0 */10 * * * *") // Run daily (ten minutes for testing)
    public void applyGlobalDecay() {
        // Reduce all weights slightly to reflect fading relationships
        graphService.getAllConnections().forEach((from, toSet) -> {
            toSet.forEach(to -> {
                double currentWeight = graphService.getConnectionWeight(from, to);
                double newWeight = currentWeight * 0.99; // 1% daily decay
                graphService.updateConnectionWeight(from, to, newWeight);
            });
        });
    }
}
