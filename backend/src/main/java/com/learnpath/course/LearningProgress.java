package com.learnpath.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "learning_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
public class LearningProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private int completedLessons;

    @Column(nullable = false)
    private Instant lastStudiedAt = Instant.now();

    protected LearningProgress() {
    }

    public LearningProgress(Long userId, Long courseId, int completedLessons) {
        this.userId = userId;
        this.courseId = courseId;
        this.completedLessons = completedLessons;
    }

    public void update(int completedLessons) {
        this.completedLessons = completedLessons;
        this.lastStudiedAt = Instant.now();
    }

    public Long getCourseId() { return courseId; }
    public int getCompletedLessons() { return completedLessons; }
    public Instant getLastStudiedAt() { return lastStudiedAt; }
}
