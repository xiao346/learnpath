package com.learnpath.journey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "journey_stage_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "stage_id"}))
public class JourneyStageProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stage_id", nullable = false, length = 24)
    private String stageId;

    @Column(nullable = false, updatable = false)
    private Instant completedAt = Instant.now();

    protected JourneyStageProgress() {
    }

    public JourneyStageProgress(Long userId, String stageId) {
        this.userId = userId;
        this.stageId = stageId;
    }

    public String getStageId() { return stageId; }
    public Instant getCompletedAt() { return completedAt; }
}
