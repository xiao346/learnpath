package com.learnpath.game;

import java.time.Instant;
import java.util.List;

public final class GameDtos {
    private GameDtos() {
    }

    public record GameProgressView(int totalScore, int completedCount, int totalChallenges,
                                   List<String> completedChallenges, Instant updatedAt) {
    }
}
