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

    @Column(length = 12)
    private String status = "COMPLETED";

    @Column(length = 600)
    private String evidence;

    @Column(nullable = false, updatable = false)
    private Instant completedAt = Instant.now();

    protected JourneyStageProgress() {
    }

    public JourneyStageProgress(Long userId, String stageId, String status) {
        this.userId = userId;
        this.stageId = stageId;
        this.status = status;
    }

    public String getStageId() { return stageId; }
    public boolean isCompleted() { return status == null || status.equals("COMPLETED"); }
    public boolean isSkipped() { return status != null && status.equals("SKIPPED"); }
    public void markCompleted() { status = "COMPLETED"; }
    public void markSkipped() { status = "SKIPPED"; }
    public void invalidate() { status = "IN_PROGRESS"; evidence = null; }
    public void saveEvidence(String evidence) { this.evidence = evidence; }
    public String getEvidence() { return evidence; }
    public Instant getCompletedAt() { return completedAt; }
}
