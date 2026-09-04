package com.learnpath.dashboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "study_session")
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate studyDate;

    @Column(nullable = false)
    private int minutes;

    @Column(nullable = false, length = 50)
    private String source;

    protected StudySession() {
    }

    public StudySession(Long userId, LocalDate studyDate, int minutes, String source) {
        this.userId = userId;
        this.studyDate = studyDate;
        this.minutes = minutes;
        this.source = source;
    }

    public LocalDate getStudyDate() { return studyDate; }
    public int getMinutes() { return minutes; }
}
