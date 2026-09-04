package com.learnpath.dashboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "study_task")
public class StudyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 50)
    private String subject;

    @Column(nullable = false)
    private int estimatedMinutes;

    @Column(nullable = false)
    private int xpReward;

    @Column(nullable = false)
    private LocalDate taskDate;

    @Column(nullable = false)
    private boolean completed;

    private Instant completedAt;

    protected StudyTask() {
    }

    public StudyTask(Long userId, String title, String subject, int estimatedMinutes,
                     int xpReward, LocalDate taskDate, boolean completed) {
        this.userId = userId;
        this.title = title;
        this.subject = subject;
        this.estimatedMinutes = estimatedMinutes;
        this.xpReward = xpReward;
        this.taskDate = taskDate;
        this.completed = completed;
        this.completedAt = completed ? Instant.now() : null;
    }

    public void toggle() {
        completed = !completed;
        completedAt = completed ? Instant.now() : null;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public int getXpReward() { return xpReward; }
    public boolean isCompleted() { return completed; }
}
