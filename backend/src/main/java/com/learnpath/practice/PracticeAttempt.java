package com.learnpath.practice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "practice_attempt")
public class PracticeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(nullable = false, length = 1)
    private String selectedOption;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private int pointsEarned;

    @Column(nullable = false, updatable = false)
    private Instant answeredAt = Instant.now();

    protected PracticeAttempt() {
    }

    public PracticeAttempt(Long userId, Long questionId, String selectedOption, boolean correct, int pointsEarned) {
        this.userId = userId;
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.correct = correct;
        this.pointsEarned = pointsEarned;
    }

    public Long getQuestionId() { return questionId; }
    public boolean isCorrect() { return correct; }
    public int getPointsEarned() { return pointsEarned; }
    public Instant getAnsweredAt() { return answeredAt; }
}
