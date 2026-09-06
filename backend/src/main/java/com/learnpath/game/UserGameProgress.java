package com.learnpath.game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "user_game_progress", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class UserGameProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int totalScore;

    @Column(nullable = false, length = 500)
    private String completedChallenges = "";

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected UserGameProgress() {
    }

    public UserGameProgress(Long userId) {
        this.userId = userId;
    }

    public void complete(String challengeId, int points) {
        Set<String> completed = completedSet();
        if (completed.add(challengeId)) {
            completedChallenges = String.join(",", completed);
            totalScore += points;
            updatedAt = Instant.now();
        }
    }

    public Set<String> completedSet() {
        if (completedChallenges.isBlank()) return new LinkedHashSet<>();
        return new LinkedHashSet<>(Arrays.asList(completedChallenges.split(",")));
    }

    public int getTotalScore() { return totalScore; }
    public Instant getUpdatedAt() { return updatedAt; }
}
